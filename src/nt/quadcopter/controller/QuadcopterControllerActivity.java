package nt.quadcopter.controller;

import java.io.IOException;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import nt.quadcopter.controller.joystick.JoystickMovedListener;
import nt.quadcopter.controller.joystick.JoystickView;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class QuadcopterControllerActivity extends Activity
{
    private static final String BLUETOOTH_DEVICE_NAME = "Quadcopter-2C5B";

    private BluetoothSocket mSocket;
    private SeekBar mSpeedSlider;
    private JoystickView mJoystick;
    private boolean mPowerOn;
    private int mXRotation, mYRotation, mSpeed;
    private Timer mTimer;
    private boolean mUpdateNeeded;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);

        try
        {
            connect();
        }
        catch (IOException e)
        {
            new AlertDialog.Builder(this)
                    .setMessage(e.getMessage())
                    .setNeutralButton("OK", new OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    QuadcopterControllerActivity.this.finish();
                                }
                            })
                    .show();
        }

        mSpeedSlider = (SeekBar) findViewById(R.id.speed);
        mJoystick = (JoystickView) findViewById(R.id.joystick);
        mTimer = new Timer();
        mTimer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                if (mUpdateNeeded)
                {
                    QuadcopterControllerActivity.this.sendUpdate();
                    mUpdateNeeded = false;
                }
            }
        }, 0, 1000);

        initializeViewListeners();
    }

    private void connect() throws IOException
    {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice device = null;

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        for (BluetoothDevice pairedDevice : pairedDevices)
        {
            if (pairedDevice.getName().equals(BLUETOOTH_DEVICE_NAME))
            {
                device = pairedDevice;
                break;
            }
        }

        if (device == null)
        {
            throw new IOException("Device Not Found");
        }
        else
        {
            final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";
            UUID uuid = UUID.fromString(SPP_UUID);

            mSocket = device.createRfcommSocketToServiceRecord(uuid);
            mSocket.connect();
        }
    }

    private void initializeViewListeners()
    {
        mSpeedSlider.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
        {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                mSpeed = progress;

                mUpdateNeeded = true;
            }
        });

        mJoystick.setOnJostickMovedListener(new JoystickMovedListener()
        {
            @Override
            public void OnMoved(int pan, int tilt)
            {
                mXRotation = tilt * 4;
                mYRotation = pan * 4;

                mUpdateNeeded = true;
            }

            @Override
            public void OnReleased() {}

            @Override
            public void OnReturnedToCenter() {}
        });
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        mTimer.cancel();

        if (mSocket != null)
        {
            sendOff(null);

            try
            {
                mSocket.close();
            }
            catch (IOException e)
            {
                // wtf is supposed to happen here?
            }
        }
    }

    public void sendOn(View button)
    {
        mPowerOn = true;
        sendUpdate();
    }

    public void sendOff(View button)
    {
        mPowerOn = false;
        sendUpdate();
    }

    private void sendUpdate()
    {
        try
        {
            mSocket.getOutputStream().write(mPowerOn ? '1' : '0');
            mSocket.getOutputStream().write(mSpeed);
            mSocket.getOutputStream().write(mXRotation);
            mSocket.getOutputStream().write(mYRotation);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
