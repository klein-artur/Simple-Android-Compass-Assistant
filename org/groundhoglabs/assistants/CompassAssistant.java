package org.groundhoglabs.assistants;

import android.content.Context;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;

import org.groundhoglabs.assistants.MovingAverageList;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Artur Hellmann on 10.05.16.
 *
 * This class is a compass helper. It provides data to rotate a view to point it to north in your UI.
 */
public class CompassAssistant implements SensorEventListener {

    /**
     * This is a listener interface. Every object that wants to get data from the CompassAssistant
     * needs to implement this interface and register itself as a listener.
     */
    public interface CompassAssistantListener {

        /**
        * is getting called when the assistant evaluates a new heading.
        * @param degrees the new degrees
        */
        void onNewDegreesToNorth(float degrees);

        /**
        * is getting called when the assistant evaluates a new heading. This degrees are smoothed
        * by the moving average algorythm.
        * @param degrees the new smoothed degrees.
        */
        void onNewSmoothedDegreesToNorth(float degrees);

        /**
        * is getting called when the compass was stopped.
        */
        void onCompassStopped();

        /*
        * is getting called when the compass was started.
        */
        void onCompassStarted();
        
    }

    private Context context;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;
    private float[] lastAccelerometer = new float[3];
    private float[] lastMagnetometer = new float[3];
    private boolean lastAccelerometerSet = false;
    private boolean lastMagnetometerSet = false;
    private float[] rotationMatrix = new float[9];
    private float[] orientation = new float[3];
    private float currentDegree = 0f;
    private float currentSmoothedDegree = 0f;
    private int currentAccuracy = 0;

    private float declination = 0.0f;

    private MovingAverageList moovingAverageList = new MovingAverageList(10);

    private List<CompassAssistantListener> listeners = new ArrayList<>();

    /**
     * initializes a CompassAssistant with a context. A CompassAssistant initialized with this
     * constructor will not point to the geographic, but to the magnetic north.
     * @param context the context
     */
    public CompassAssistant(final Context context) {
        this(context, null);
    }

    /**
     * initializes a CompassAssistant with a context and a Location. If you use this constructor the
     * resulting degrees will be calculated with the declination for the given location. The compass
     * will point to the geographic north.
     * @param context the context
     * @param l the location to which the CompassAssistant should refer its pointing.
     */
    public CompassAssistant(final Context context, Location l) {
        this.context = context;

        if (l != null) {
            GeomagneticField geomagneticField = new GeomagneticField((float)l.getLatitude(),
                    (float)l.getLongitude(), (float)l.getAltitude(), new Date().getTime());
            declination = geomagneticField.getDeclination();
        }

        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

    }

    /**
     * Adds a listener to the listeners.
     * @param listener the listener to add.
     */
    public void addListener(CompassAssistantListener listener) {
        this.listeners.add(listener);
    }

    /**
     * Removes a listener from the listeners.
     * @param listener the listener to remove.
     */
    public void removeListener(CompassAssistantListener listener) {
        this.listeners.remove(listener);
    }

    /**
     * Starts the CompassAssistant. After you call this function the CompassAssistant will
     * provide degrees to its listeners.
     */
    public void start() {
        sensorManager.registerListener(CompassAssistant.this, accelerometer,
                SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(CompassAssistant.this, magnetometer,
                SensorManager.SENSOR_DELAY_UI);
        for (CompassAssistantListener listener : listeners) {
            listener.onCompassStarted();
        }
    }

    /**
     * Stops the CompassAssistant.
     */
    public void stop() {
        this.sensorManager.unregisterListener(this, this.accelerometer);
        this.sensorManager.unregisterListener(this, this.magnetometer);
        this.lastAccelerometer = new float[3];
        this.lastMagnetometer = new float[3];
        this.lastAccelerometerSet = false;
        this.lastMagnetometerSet = false;
        this.currentAccuracy = 0;
        this.currentDegree = 0f;
        this.currentSmoothedDegree = 0f;
        this.moovingAverageList = new MovingAverageList(10);
        for (CompassAssistantListener l : this.listeners) {
            l.onCompassStopped();
        }
    }
    
    

    /**
     * Returns if the magneticFieldSensor is uncalibrated and needs to be calibrated by the
     * user.
     * @return true, if the sensor needs to be calibrated.
     */
    public boolean calibrationRequired() {
        return this.calibrationRequired(2);
    }

    /**
     * Returns if the magneticFieldSensor is uncalibrated and needs to be calibrated by the
     * user. You can give a level at which the compass needs to be calibrated. For levels see
     * {@link android.hardware.SensorManager}
     * @param level the level at which the compass needs to be calibrated. {@link android.hardware.SensorManager}
     * @return true, if the sensor needs to be calibrated.
     */
    public boolean calibrationRequired(int level) {
        return this.currentAccuracy <= level;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor == this.accelerometer) {
            System.arraycopy(event.values, 0, this.lastAccelerometer, 0, event.values.length);
            this.lastAccelerometerSet = true;
        } else if (event.sensor == this.magnetometer) {
            System.arraycopy(event.values, 0, this.lastMagnetometer, 0, event.values.length);
            this.lastMagnetometerSet = true;

        }

        if (this.lastAccelerometerSet && this.lastAccelerometerSet) {
            SensorManager.getRotationMatrix(this.rotationMatrix,null, this.lastAccelerometer, this.lastMagnetometer);
            SensorManager.getOrientation(this.rotationMatrix, this.orientation);


            float azimuthInRadiands = this.orientation[0];
            float azimuthInDegrees = (float) Math.toDegrees(azimuthInRadiands);

            this.currentDegree = cleanDegrees(azimuthInDegrees+declination);

            informListenersAboutNewDegree(this.currentDegree);

            this.currentSmoothedDegree = this.moovingAverageList.addAndGetAverage(this.currentDegree);
            informListenersAboutNewSmoothedDegree(this.currentSmoothedDegree);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (sensor == magnetometer) {
            this.currentAccuracy = accuracy;
        }
    }

    /**
     * This function is private and cleans the given degrees in reference to the old value. That
     * is important!. Otherwise the compass would rotate to the wrong direction.
     * @param degree the degree to clean
     * @return the cleaned degrees
     */
    private float cleanDegrees(float degree) {
        float difference = Math.abs(currentDegree - degree);
        if (difference > 180) {
            return degree + (currentDegree >= 0 ? 360 : -360);
        }
        else {
            return degree;
        }
    }

    /**
     * This function is private and informs the listeners about a new degree.
     * @param degree the degree
     */
    private void informListenersAboutNewDegree(float degree) {
        for (CompassAssistantListener l : this.listeners) {
            l.onNewDegreesToNorth(-degree);
        }
    }

    /**
     * This function is private and informs the listeners about a new smoothed degree.
     * @param degree
     */
    private void informListenersAboutNewSmoothedDegree(float degree) {
        for (CompassAssistantListener l : this.listeners) {
            l.onNewSmoothedDegreesToNorth(-degree);
        }
    }


    /**
     * Calculates the bearing between two locations with the current heading of the phone.
     * With this function you can make a view that points on the destination location if you give
     * the current location as the first parameter.
     * @param lat1 the latitude of the first location. This could be the current phones location
     * @param lng1 the longitude of the first location. This could be the current phones location
     * @param lat2 the latitude of the destination location at which to point at.
     * @param lng2 the longitude of the destination location at which to point at.
     * @return the smoothed degrees including the current heading of the phone.
     */
    public float getBearingBetweenLocations(double lat1,
            double lng1, 
            double lat2,
            double lng2) {
        return this.getBearingBetweenLocations(lat1, lng1, lat2, lng2, true);
    }

    /**
     * Calculates the bearing between two locations with the current heading of the phone.
     * With this function you can make a view that points on the destination location if you give
     * the current location as the first parameter. You can choose whether the result should be
     * smoothed or not.
     * @param lat1 the latitude of the first location. This could be the current phones location
     * @param lng1 the longitude of the first location. This could be the current phones location
     * @param lat2 the latitude of the destination location at which to point at.
     * @param lng2 the longitude of the destination location at which to point at.
     * @param smoothed whether the result should be smoothed or not.
     * @return the smoothed or not smoothed degrees including the current heading of the phone.
     */
    public float getBearingBetweenLocations(double lat1,
            double lng1, 
            double lat2,
            double lng2,
            boolean smoothed) {

        double x = Math.cos(lat2) * Math.sin(lng1-lng2);
        double y = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(lng1-lng2);
        double bearing = Math.atan2(x, y);
        return (-(smoothed ? this.currentSmoothedDegree : this.currentDegree) + (float)Math.toDegrees(bearing));
    }
}
