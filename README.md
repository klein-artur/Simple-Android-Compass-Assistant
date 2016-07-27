# CompassAssistant
## Description
The CompassAssistant is a helper class that provides the heading of the device to
the geographic or the magnetic north by degrees. The information can be used to rotate a UI element,
for example a compass. 

## Init an CompassAssistant
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

The listener will now evaluate the heading of the phone to north and then it will pass the degrees
to its listeners.

Don´t forget to stop your assistant if you don´t need it anymore. You can start it at every time.

## Usage Example:

```java
public class CompassActivity extends Activity implements CompassAssistant.CompassAssistantListener {

    public static final String MESSAGE_ID_KEY = "mid";

    private CompassAssistant compassAssistant;

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
        compassAssistant = new CompassAssistant(MessageDetailActivity.this);
        compassAssistant.addListener(MessageDetailActivity.this);
        compassAssistant.start();
            
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.compassAssistant.stop();
    }

    @Override
    public void onNewDegreesToNorth(float degrees) {
        // this is not used here because we want to have a smooth moving compass.
    }

    @Override
    public void onNewSmoothedDegreesToNorth(float degrees) {

        final RotateAnimation ra = new RotateAnimation(
                currentDegree,
                -degrees,
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