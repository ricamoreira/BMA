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

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Intances of BT Manager and BT Adapter needed to work with BT in Android.
        BluetoothManager bluetoothManager = getSystemService(BluetoothManager.class);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        //Intances of the Android UI elements that will will use during the execution of the APP
        //TextView btReadings = findViewById(R.id.btReadings);
        TextView output = findViewById(R.id.outputLB);
        Button Connect = (Button) findViewById(R.id.ConnectBTN);
        EditText angleInput = findViewById(R.id.angleInputTXT);
        //Button seachDevices = (Button) findViewById(R.id.seachDevices);
        Log.d(TAG, "Begin Execution");

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN}, 2);
                return;
            }
        } else {
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } else {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    //output.setText("Erro :(");
                } else {
                    Connect.setEnabled(true);
                    Connect.setBackgroundColor(Color.parseColor("#009688"));
                }
            }
        }
    }

    public void ConnectToHC05(View view) {
        Button setAngle = (Button) findViewById(R.id.setAngleBTN);
        Button Connect = (Button) findViewById(R.id.ConnectBTN);

        BluetoothManager bluetoothManager = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            bluetoothManager = getSystemService(BluetoothManager.class);
        }
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

        String btDevicesString = "";
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
                        Connect.setEnabled(false);
                        Connect.setBackgroundColor(Color.parseColor("#0082FC"));
                        Connect.setTextColor(Color.parseColor("#FFFFFF"));
                        Connect.setText("Connected");
                    } catch (IOException e) {
                        Toast.makeText(MainActivity.this, "Error: " + e, Toast.LENGTH_SHORT).show();
                        //output.setText("Erro: " + e);
                    }
                }
                //output.setText(btDevicesString);
            }
        }
    }

    public void SetAllAngles_click(View view) {
        try {
            EditText angleInput = findViewById(R.id.angleInputTXT);
            String a = angleInput.getText().toString();

            if (bluetoothSocket != null && bluetoothSocket.isConnected()) {

                OutputStream outputStream = bluetoothSocket.getOutputStream();
                if (outputStream != null) {
                    String data = a + "," + a + "," + a + "," + a + "," + a;
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
    }

}