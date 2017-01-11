package fr.eyal.androidcatapult;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

import com.google.android.things.contrib.driver.button.Button;
import com.google.android.things.contrib.driver.button.ButtonInputDriver;
import com.google.android.things.contrib.driver.pwmservo.Servo;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;
import java.util.List;

import static android.content.ContentValues.TAG;

public class MainActivity extends Activity {

    private ButtonInputDriver mButtonInputDriver;
    private ButtonInputDriver mButtonInputDriverTrigger;
    private Servo mServo;
    private Servo mServoTrigger;
    private double mCurrentAngle;
    private double mCurrentAngleTrigger;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PeripheralManagerService service = new PeripheralManagerService();

        List<String> list = service.getPwmList();
        Log.d(TAG, "PWM: " + list);
        list = service.getGpioList();
        Log.d(TAG, "GPIO: " + list);

        try {
            mServo = new Servo(BoardDefaults.getServoPort());
            mServo.setAngleRange(0f, 180f);
            mServo.setPulseDurationRange(0.6, 2.4);
            mServo.setEnabled(true);

            mServoTrigger = new Servo(BoardDefaults.getServoTriggerPort());
            mServoTrigger.setAngleRange(0f, 90f);
            mServoTrigger.setPulseDurationRange(1, 2);
            mServoTrigger.setEnabled(true);

            mButtonInputDriver = new ButtonInputDriver(BoardDefaults.getButtonPort(),
                    Button.LogicState.PRESSED_WHEN_LOW, KeyEvent.KEYCODE_SPACE);
            mButtonInputDriver.register();

            mButtonInputDriverTrigger = new ButtonInputDriver(BoardDefaults.getButtonTriggerPort(),
                    Button.LogicState.PRESSED_WHEN_LOW, KeyEvent.KEYCODE_0);
            mButtonInputDriverTrigger.register();

        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "Catapult ready");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_SPACE) {
            toogleServo();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_0) {
            toogleTrigger();
        }

        return super.onKeyDown(keyCode, event);
    }

    private void toogleServo() {
        if (mCurrentAngle < mServo.getMaximumAngle()) {
            mCurrentAngle = mServo.getMaximumAngle();
        } else {
            mCurrentAngle = mServo.getMinimumAngle();
        }

        try {
            mServo.setAngle(mCurrentAngle);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "Button pressed fire change angle to " + mCurrentAngle);
    }

    private void toogleTrigger() {
        if (mCurrentAngleTrigger < mServoTrigger.getMaximumAngle()) {
            mCurrentAngleTrigger = mServoTrigger.getMaximumAngle();
        } else {
            mCurrentAngleTrigger = mServoTrigger.getMinimumAngle();
        }

        try {
            mServoTrigger.setAngle(mCurrentAngleTrigger);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "Button pressed trigger change angle to " + mCurrentAngleTrigger);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mServo != null) {
            try {
                mServo.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing Servo");
            } finally {
                mServo = null;
            }
        }

        if (mButtonInputDriver != null) {
            mButtonInputDriver.unregister();
            try {
                mButtonInputDriver.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing Button driver", e);
            } finally {
                mButtonInputDriver = null;
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

        if (mButtonInputDriverTrigger != null) {
            mButtonInputDriverTrigger.unregister();
            try {
                mButtonInputDriverTrigger.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing Button trigger driver", e);
            } finally {
                mButtonInputDriverTrigger = null;
            }
        }
    }
}
