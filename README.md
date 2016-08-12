# CompassAssistant
## Description
The CompassAssistant is a helper class that provides the heading of the device to
the geographic or the magnetic north by degrees. The information can be used to rotate a UI element,
for example a compass. 

## Init a CompassAssistant
There are two constructors:

### Assistant without declination
```java
CompassAssistant assistant = new CompassAssistant(context);
```

### Assistant with declination
```java
CompassAssistant assistant = new CompassAssistant(context, location);
```

The difference between them is that the first one will point to the magnetic northpole while the
second one will point to the geographic north. For the location you have to use the Location class from
the android.location package.

## Use the CompassAssistant
After you created your CompassAssistant you can use it. Add a listener to the assistant and start
it:

```java
assistant.addListener(listener);
assistant.start();
```

The assistant will now evaluate the heading of the phone to north and then it will pass the degrees
to its listeners.

Don´t forget to stop your assistant if you don´t need it anymore. You can restart it at every time:

```java
assistant.stop();
```

## Use the bearing functionality
The assistant can give you the bearing from one location to the other. The locations are given as latitude and
longitude doubles. As long as the CompassAssistant isn´t started and haven´t got any heading of the device it will
give the direct bearing between the two locations. If the assistant has a heading of the phone it will add the
heading of the phone to the bearing. If you pass your current location as the first location and a destination
location as the second location you can get the bearing to this point and rotate a view to show the user the bearing.

The functions to get the bearing are:

```java
// the parameters are:
// latStart, lngStart, latDestination, lngDestination
// the result is smoothed
assistant.getBearingBetweenLocations(47.845366, 12.543425, 48.135125, 11.581981);

// the same as the first function but with the last boolean you can choose whether the
// result should be smoothed (true) or not (false).
assistant.getBearingBetweenLocations(47.845366, 12.543425, 48.135125, 11.581981, true);
``` 

## Usage Example:

```java
public class CompassActivity extends Activity implements CompassAssistant.CompassAssistantListener {

    private CompassAssistant CompassAssistant;
    private float currentDegree;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.compass = (ImageView) findViewById(R.id.compass_view);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // this assistant will point to the magnetic north. If you want to have a compass that points
        // to the geographic north, you have to put a location into the constructor.
        CompassAssistant = new CompassAssistant(CompassActivity.this);
        CompassAssistant.addListener(CompassActivity.this);
        CompassAssistant.start();
            
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.CompassAssistant.stop();
    }

    @Override
    public void onNewDegreesToNorth(float degrees) {
        // this is not used here because we want to have a smooth moving compass.
    }

    @Override
    public void onNewSmoothedDegreesToNorth(float degrees) {

        final RotateAnimation ra = new RotateAnimation(
                currentDegree,
                degrees,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f);
        ra.setDuration(210);
        ra.setFillAfter(true);

        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                compass.startAnimation(ra);
            }
        });

        currentDegree = degrees;

        /*
        If you want you can get the bearing between two locations right here. 
        If you do this in this function you can be sure that the compassassistant is started and
        you will get continously new degrees:
        
        float bearing = assistant.getBearingBetweenLocations(currLocLat, currLocLng, destLocLat, destLocLng);

        */

    }

    @Override
    public void onCompassStopped() {
        // the compass has stopped. Do maybe 
    }

    @Override
    public void onCompassStarted() {
        // you can do things here for example if you want to hide a loading indicator.
    }
}
```