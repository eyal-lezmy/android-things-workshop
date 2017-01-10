package fr.eyal.androidcatapult;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;

import com.google.android.things.contrib.driver.button.Button;
import com.google.android.things.contrib.driver.button.ButtonInputDriver;
import com.google.android.things.contrib.driver.pwmservo.Servo;
import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManagerService;
import com.google.android.things.pio.Pwm;
import com.google.android.things.userdriver.UserSensorDriver;
import com.google.android.things.userdriver.UserSensorReading;

import java.io.IOException;
import java.util.List;

import static android.content.ContentValues.TAG;

public class MainActivity extends Activity {

    private ButtonInputDriver mButtonInputDriver;
    private Servo mServo;
    private double mCurrentAngle;


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
            mServo.setEnabled(true);

            mButtonInputDriver = new ButtonInputDriver(BoardDefaults.getButtonPort(),
                    Button.LogicState.PRESSED_WHEN_LOW, KeyEvent.KEYCODE_SPACE);
            mButtonInputDriver.register();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "Catapult ready");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_SPACE) {
            toogleTrigger();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    private void toogleTrigger() {
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
        Log.i(TAG, "Button pressed change angle to " + mCurrentAngle);
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
            } finally{
                mButtonInputDriver = null;
            }
        }
    }
}
