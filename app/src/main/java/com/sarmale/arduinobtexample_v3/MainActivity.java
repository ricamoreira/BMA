package com.sarmale.arduinobtexample_v3;


import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    // Global variables we will use in the
    private static final String TAG = "FrugalLogs";
    private static final int REQUEST_ENABLE_BT = 1;
    //We will use a Handler to get the BT Connection status
    public static Handler handler;
    public BluetoothSocket bluetoothSocket = null;
    private final static int ERROR_READ = 0; // used in bluetooth handler to identify message update
    BluetoothDevice arduinoBTModule = null;
    UUID arduinoUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //We declare a default UUID to create the global variable

    public int a[] = new int[5];

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Intances of the Android UI elements that will will use during the execution of the APP
        //TextView btReadings = findViewById(R.id.btReadings);
        TextView output = findViewById(R.id.outputLB);
        Button Connect = (Button) findViewById(R.id.ConnectBTN);
        EditText angleInput = findViewById(R.id.angleInputTXT);
        //Button seachDevices = (Button) findViewById(R.id.seachDevices);
        Log.d(TAG, "Begin Execution");

        for (int i = 0; i < a.length; i++) {
            a[i] = 0;
        }
    }

    public void ConnectToHC05(View view) {
        //Intances of BT Manager and BT Adapter needed to work with BT in Android.
        BluetoothManager bluetoothManager = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            bluetoothManager = getSystemService(BluetoothManager.class);
        }
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN}, 2);
                return;
        }else{
            if (!bluetoothAdapter.isEnabled()) {
                Log.d(TAG, "Bluetooth is disabled");
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    Log.d(TAG, "We don't BT Permissions");
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                    Log.d(TAG, "Bluetooth is enabled now");
                } else {
                    Log.d(TAG, "We have BT Permissions");
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                    Log.d(TAG, "Bluetooth is enabled now");
                }

            }else{
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED && bluetoothAdapter.isEnabled()) {
                        DoConnection();
                    }
                } else {
                    if (bluetoothAdapter.isEnabled()) {
                        DoConnection();
                    }
                }
            }
        }
    }

    public void DoConnection() {
        TextView output = findViewById(R.id.outputLB);
        Button setAngle = (Button) findViewById(R.id.setAngleBTN);
        Button Connect = (Button) findViewById(R.id.ConnectBTN);

        boolean deviceFound = false;

        if (bluetoothSocket != null && bluetoothSocket.isConnected()) {
            try {
                OutputStream outputStream = bluetoothSocket.getOutputStream();
                if (outputStream != null) {
                    String data = 0 + "," + 0 + "," + 0 + "," + 0 + "," + 0;
                    //Toast.makeText(MainActivity.this, data, Toast.LENGTH_SHORT).show();
                    outputStream.write((data + "\r\n").getBytes());
                    Toast.makeText(MainActivity.this, "Disconnected", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "OutputStream is null", Toast.LENGTH_SHORT).show();
                }

                bluetoothSocket.close();

                setAngle.setEnabled(false);
                setAngle.setBackgroundColor(Color.parseColor("#AFAFAF"));
                Connect.setBackgroundColor(Color.parseColor("#009688"));
                Connect.setText("Connect");
            } catch (IOException e) {
                // Error occurred while closing the socket
                // Handle the exception
            }
        } else {
            Connect.setEnabled(false);
            Connect.setBackgroundColor(Color.parseColor("#ffd700"));
            Connect.setText("Connecting...");

            BluetoothManager bluetoothManager = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                bluetoothManager = getSystemService(BluetoothManager.class);
            }
            BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

            String btDevicesString = "";

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
            }
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

            if (pairedDevices.size() > 0) {
                // There are paired devices. Get the name and address of each paired device.
                for (BluetoothDevice device : pairedDevices) {
                    String deviceName = device.getName();
                    String deviceHardwareAddress = device.getAddress(); // MAC address
                    // We append all devices to a String that we will display in the UI
                    btDevicesString = btDevicesString + deviceName + " || " + deviceHardwareAddress + "\n";
                    // If we find the HC 05 device (the Arduino BT module)
                    // We assign the device value to the Global variable BluetoothDevice
                    // We enable the button "Connect to HC 05 device"
                    if ("HC-05".equals(deviceName)) {
                        Log.d(TAG, "HC-05 found");
                        deviceFound = true;
                        arduinoUUID = device.getUuids()[0].getUuid();
                        arduinoBTModule = device;
                        // HC -05 Found, enabling the button to read results

                        try {
                            BluetoothDevice hc05Device = bluetoothAdapter.getRemoteDevice(deviceHardwareAddress); // Replace with the MAC address of your HC-05 module
                            bluetoothSocket = hc05Device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")); // Use the UUID for SPP (Serial Port Profile)
                            bluetoothSocket.connect();

                            Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_SHORT).show();
                            setAngle.setEnabled(true);
                            setAngle.setBackgroundColor(Color.parseColor("#009688"));
                            Connect.setEnabled(true);
                            Connect.setBackgroundColor(Color.parseColor("#0082FC"));
                            Connect.setText("Disconnect");
                        } catch (IOException e) {
                            Connect.setEnabled(true);
                            Connect.setBackgroundColor(Color.parseColor("#009688"));
                            Connect.setText("Connect");
                            Toast.makeText(MainActivity.this, "Make sure the device is turned on", Toast.LENGTH_SHORT).show();
                            //output.setText("Erro: " + e);
                        }
                    }
                    //output.setText(btDevicesString);
                }

                if(deviceFound == false){
                    Toast.makeText(MainActivity.this, "You must pair your phone with the HC-05 first", Toast.LENGTH_SHORT).show();
                    Connect.setEnabled(true);
                    Connect.setBackgroundColor(Color.parseColor("#009688"));
                    Connect.setText("Connect");
                }
            }
        }
    }

    public void SetAllAngles_click(View view) {
        EditText angleInput = findViewById(R.id.angleInputTXT);
        Switch switch1 = findViewById(R.id.switch1);
        Switch switch2 = findViewById(R.id.switch2);
        Switch switch3 = findViewById(R.id.switch3);
        Switch switch4 = findViewById(R.id.switch4);
        Switch switch5 = findViewById(R.id.switch5);

        if(angleInput.getText() != null){
            if(Integer.parseInt(angleInput.getText().toString()) <= 180){
                a[0] = (switch1.isChecked()) ? Integer.parseInt(angleInput.getText().toString()) : a[0];
                a[1] = (switch2.isChecked()) ? Integer.parseInt(angleInput.getText().toString()) : a[1];
                a[2] = (switch3.isChecked()) ? Integer.parseInt(angleInput.getText().toString()) : a[2];
                a[3] = (switch4.isChecked()) ? Integer.parseInt(angleInput.getText().toString()) : a[3];
                a[4] = (switch5.isChecked()) ? Integer.parseInt(angleInput.getText().toString()) : a[4];

                try {
                    if (bluetoothSocket != null && bluetoothSocket.isConnected()) {

                        OutputStream outputStream = bluetoothSocket.getOutputStream();
                        if (outputStream != null) {
                            String data = a[0] + "," + a[1] + "," + a[2] + "," + a[3] + "," + a[4];
                            //Toast.makeText(MainActivity.this, data, Toast.LENGTH_SHORT).show();
                            outputStream.write((data + "\r\n").getBytes());
                            Toast.makeText(MainActivity.this, "Data sent successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "OutputStream is null", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Bluetooth connection is not established", Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(MainActivity.this, "The maximum value is 180", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void onCheckedChanged(View view) {
        Switch allFingers = (Switch) view;

        Switch switch1 = findViewById(R.id.switch1);
        Switch switch2 = findViewById(R.id.switch2);
        Switch switch3 = findViewById(R.id.switch3);
        Switch switch4 = findViewById(R.id.switch4);
        Switch switch5 = findViewById(R.id.switch5);

        if (!allFingers.isChecked()) {
            switch1.setChecked(false);
            switch2.setChecked(false);
            switch3.setChecked(false);
            switch4.setChecked(false);
            switch5.setChecked(false);
        } else {
            switch1.setChecked(true);
            switch2.setChecked(true);
            switch3.setChecked(true);
            switch4.setChecked(true);
            switch5.setChecked(true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Perform cleanup or saving operations here
        Button setAngle = (Button) findViewById(R.id.setAngleBTN);
        Button Connect = (Button) findViewById(R.id.ConnectBTN);

        if (bluetoothSocket != null && bluetoothSocket.isConnected()) {

            try {
                OutputStream outputStream = bluetoothSocket.getOutputStream();
                if (outputStream != null) {
                    String data = 0 + "," + 0 + "," + 0 + "," + 0 + "," + 0;
                    //Toast.makeText(MainActivity.this, data, Toast.LENGTH_SHORT).show();
                    outputStream.write((data + "\r\n").getBytes());
                    Toast.makeText(MainActivity.this, "Disconnected", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "OutputStream is null", Toast.LENGTH_SHORT).show();
                }

                bluetoothSocket.close();

                setAngle.setEnabled(false);
                setAngle.setBackgroundColor(Color.parseColor("#AFAFAF"));
                Connect.setBackgroundColor(Color.parseColor("#009688"));
                Connect.setText("Connect");
            } catch (IOException e) {
                // Error occurred while closing the socket
                // Handle the exception
            }
        }
    }
}