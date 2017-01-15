package fr.eyal.androidcatapult;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.things.contrib.driver.button.Button;
import com.google.android.things.contrib.driver.pwmservo.Servo;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;
import java.util.List;

public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private Button mButtonTrigger;
    private Servo mServoElastics;
    private Servo mServoTrigger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PeripheralManagerService service = new PeripheralManagerService();

        List<String> list = service.getPwmList();
        Log.d(TAG, "PWM: " + list);
        list = service.getGpioList();
        Log.d(TAG, "GPIO: " + list);

        try {
            mServoElastics = new Servo(BoardDefaults.getServoPort());
            mServoElastics.setAngleRange(0f, 180f);
            mServoElastics.setPulseDurationRange(0.6, 2.4);
            mServoElastics.setEnabled(true);

            mServoTrigger = new Servo(BoardDefaults.getServoTriggerPort());
            mServoTrigger.setAngleRange(0f, 90f);
            mServoTrigger.setPulseDurationRange(1, 2);
            mServoTrigger.setEnabled(true);

            mButtonTrigger = new Button(BoardDefaults.getButtonTriggerPort(),
                    Button.LogicState.PRESSED_WHEN_LOW);
            mButtonTrigger.setOnButtonEventListener(new Button.OnButtonEventListener() {
                @Override
                public void onButtonEvent(Button button, boolean pressed) {
                    Log.d(TAG, "onButtonEvent: TRIGGER");
                    if (pressed) {
                        fire();
                    }
                }
            });

            // Initializing the catapult
            openTrigger();
            Thread.sleep(1000);
            releaseElastics();
            Thread.sleep(2000);
            closeTrigger();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "Catapult ready");
    }

    private void stretchElastics() throws IOException {
        Log.d(TAG, "stretchElastics");
        mServoElastics.setAngle(mServoElastics.getMaximumAngle());
    }

    private void releaseElastics() throws IOException {
        Log.d(TAG, "releaseElastics");
        mServoElastics.setAngle(mServoElastics.getMinimumAngle());
    }

    private void openTrigger() throws IOException {
        Log.d(TAG, "openTrigger");
        mServoTrigger.setAngle(mServoTrigger.getMinimumAngle());
    }

    private void closeTrigger() throws IOException {
        Log.d(TAG, "closeTrigger");
        mServoTrigger.setAngle(mServoTrigger.getMaximumAngle());
    }

    private void fire() {
        try {

            closeTrigger(); // Blocking the shaft
            Thread.sleep(1000);
            stretchElastics(); // Rewinding on the elastics
            Thread.sleep(1000);
            openTrigger(); // Fire!
            Thread.sleep(1000);
            releaseElastics(); // Falling the shaft
            Thread.sleep(2000);
            closeTrigger(); // Blocking the shaft

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mServoElastics != null) {
            try {
                mServoElastics.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing Servo");
            } finally {
                mServoElastics = null;
            }
        }

        if (mServoTrigger != null) {
            try {
                mServoTrigger.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing Servo trigger");
            } finally {
                mServoTrigger = null;
            }
        }

        if (mButtonTrigger != null) {
            try {
                mButtonTrigger.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing Button trigger driver", e);
            } finally {
                mButtonTrigger = null;
            }
        }
    }
}
