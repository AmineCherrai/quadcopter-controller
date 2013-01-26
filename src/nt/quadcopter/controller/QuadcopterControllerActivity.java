package nt.quadcopter.controller;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

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
    private SeekBar mAngleXSlider;
    private SeekBar mAngleYSlider;
    
    private boolean mPowerOn;//TODO: delete
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        
        BluetoothDevice device = null;
        
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        for(BluetoothDevice pairedDevice : pairedDevices)
        {
            if(pairedDevice.getName().equals(BLUETOOTH_DEVICE_NAME))
            {
                device = pairedDevice;
                break;
            }
        }
        
        if(device == null)
        {
            new AlertDialog.Builder(this)
                    .setMessage("Device Not Found")
                    .setNeutralButton("OK", new OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            QuadcopterControllerActivity.this.finish();
                        }
                    }).show();
        }
        else
        {
            final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";
            UUID uuid = UUID.fromString(SPP_UUID);
            
            try
            {
                mSocket = device.createRfcommSocketToServiceRecord(uuid);
                mSocket.connect();
            }
            catch (IOException e)
            {
                new AlertDialog.Builder(this)
                        .setTitle("Error connecting to device")
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
        }
        
        mSpeedSlider = (SeekBar) findViewById(R.id.speed);
        mSpeedSlider.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
        {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar)
			{
				sendUpdate();
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}
		});
        
        mAngleXSlider = (SeekBar) findViewById(R.id.angle_x);
        mAngleXSlider.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
        {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar)
			{
				sendUpdate();
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}
		});
        
        mAngleYSlider = (SeekBar) findViewById(R.id.angle_y);
        mAngleYSlider.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
        {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar)
			{
				sendUpdate();
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}
		});
    }
    
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        
        if(mSocket != null)
        {
        	try
        	{
        		mSocket.close();
        	}
        	catch (IOException e)
        	{
        		//wtf is supposed to happen here?
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
    
    public void sendUpdate()
    {
    	try
    	{
    		mSocket.getOutputStream().write(mPowerOn ? '1' : '0');
    		mSocket.getOutputStream().write(mSpeedSlider.getProgress());
    		//TODO: send angles in less hacked way
    		mSocket.getOutputStream().write(mAngleXSlider.getProgress() - 35);
    		mSocket.getOutputStream().write(mAngleYSlider.getProgress() - 35);
    	}
    	catch (IOException e)
    	{
    		e.printStackTrace();
    	}
    }
    
}
