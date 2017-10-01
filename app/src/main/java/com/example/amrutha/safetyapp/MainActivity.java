package com.example.amrutha.safetyapp;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;


import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;

import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import com.squareup.seismic.ShakeDetector;

import butterknife.ButterKnife;
import butterknife.InjectView;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements ShakeDetector.Listener {
    private static final long MINIMUM_DISTANCE_CHANGE_FOR_UPDATES = 1; // in Meters
    private static final long MINIMUM_TIME_BETWEEN_UPDATES = 1000; // in Milliseconds
    protected LocationManager locationManager;
    private final static String dummy = "AHA LOG";
    private final static int MY_PERMISSION_ACCESS_COARSE_LOCATION = 11;
    private final static int MY_PERMISSION_ACCESS_FINE_LOCATION = 11;
    private final static int MY_PERMISSION_SEND_SMS= 11;
    private final static int MY_PERMISSION_INTERNET =11;
    @InjectView(R.id.input_name)    EditText _name;
    @InjectView(R.id.phone_num_1) EditText _phonenum1;
    @InjectView(R.id.phone_num_2) EditText _phonenum2;
    @InjectView(R.id.phone_num_3) EditText _phonenum3;
    @InjectView(R.id.btn_save)
    Button _saveButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        //populate name and phone number with existing data
        populate();
        //sendMsg("nothing veer");
        _saveButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                save();
            }
        });
        SharedPreferences settings = getSharedPreferences("BeSafe", 0);
        SharedPreferences.Editor editor = settings.edit();


        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)) {
                Log.e("show permission request",dummy);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSION_ACCESS_FINE_LOCATION);
                Log.e("request permission","location");
            }

        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_COARSE_LOCATION)) {
                Log.e("show permission request",dummy);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSION_ACCESS_COARSE_LOCATION);
                Log.e("request permission",dummy);
            }
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)!= PackageManager.PERMISSION_GRANTED) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.SEND_SMS)) {
                Log.e("show permission request",dummy);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.SEND_SMS},
                        MY_PERMISSION_SEND_SMS);
                Log.e("request permission","send sms");
            }
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)!= PackageManager.PERMISSION_GRANTED) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.INTERNET)) {
                Log.e("show permission request",dummy);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.INTERNET},
                        MY_PERMISSION_INTERNET);
                Log.e("request permission","location");
            }

        }
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                MINIMUM_TIME_BETWEEN_UPDATES,
                MINIMUM_DISTANCE_CHANGE_FOR_UPDATES,
                new MyLocationListener()
        );
        // create sensor manager to listen for acc/cyro
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        ShakeDetector sd = new ShakeDetector(this);
        sd.start(sensorManager);
    }
    //It will save name and phone numeber in the database
    public void save(){
        Log.e("save function", "entered");
        SharedPreferences settings = getSharedPreferences("BeSafe", 0);
        SharedPreferences.Editor editor = settings.edit();
        String name =  _name.getText().toString();
        String ph1 = _phonenum1.getText().toString();
        String ph2 = _phonenum2.getText().toString();
        String ph3 = _phonenum3.getText().toString();

        editor.putString("name",name);
        editor.putString("phonenum1", ph1);
        editor.putString("phonenum2", ph2);
        editor.putString("phonenum3", ph3);
        editor.commit();

    }
    // This funciton will populate name and phone number field from the database
    public void populate(){
        SharedPreferences settings = getSharedPreferences("BeSafe", 0);
        //SharedPreferences.Editor editor = settings.edit();
        String name = settings.getString("name", "");
        String ph1 = settings.getString("phonenum1", "");
        String ph2 = settings.getString("phonenum2", "");
        String ph3 = settings.getString("phonenum3", "");
        _name.setText(name);
        _phonenum1.setText(ph1);
       _phonenum2.setText(ph2);
        _phonenum3.setText(ph3);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    protected String showCurrentLocation() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_COARSE_LOCATION)) {
                Log.e("show permission request",dummy);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSION_ACCESS_COARSE_LOCATION);
                Log.e("request permission",dummy);
            }

        }
        Log.e("request location update", dummy);

        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        String message = "No location obtained";
        if (location != null) {
            message = String.format(
                    "Current Location \n Longitude: %1$s \n Latitude: %2$s",
                    location.getLongitude(), location.getLatitude()
            );
            Toast.makeText(MainActivity.this, message,
                    Toast.LENGTH_LONG).show();
        }
        return message;

    }

    @Override public void hearShake() {
        Toast.makeText(this, "Shake Alert!!!!!", Toast.LENGTH_SHORT).show();
        String sms = showCurrentLocation();
        try
        {
            showDialog( sms);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }
    public void sendMsg(String location){
        SharedPreferences settings = getSharedPreferences("BeSafe", 0);
        //SharedPreferences.Editor editor = settings.edit();
        String name = settings.getString("name", "");
        String ph1 = settings.getString("phonenum1", "");
        String ph2 = settings.getString("phonenum2", "");
        String ph3 = settings.getString("phonenum3", "");
        SmsManager smsManager = SmsManager.getDefault();

        String msg = name + location;
        try {
            if (ph1 != "") {
                Log.i("sending to phone ", " 1");
                smsManager.sendTextMessage(ph1, null, msg, null, null);

            }
            if (ph2 != "") {
                Log.i("sending to phone ", " 2");
                smsManager.sendTextMessage(ph2, null, msg, null, null);
            }
            if (ph2 != "") {
                Log.i("sending to phone ", " 3");
                smsManager.sendTextMessage(ph3, null, msg, null, null);
            }
        } catch (Exception e) {

            e.printStackTrace();
        }
        Log.i("sending data to", "Http Server");

    try {
        URL url = new URL("http://posttestserver.com/post.php");
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();


        urlConnection.setDoInput(true);
        urlConnection.setDoOutput(true);
        urlConnection.setRequestProperty("Content-Type", "application/json");

        urlConnection.setRequestMethod("POST");
        urlConnection.connect();
        JSONObject jsonParam = new JSONObject();
        jsonParam.put("sms", msg);
        DataOutputStream printout = new DataOutputStream(urlConnection.getOutputStream ());
        printout.writeBytes(URLEncoder.encode(jsonParam.toString(), "UTF-8"));
        printout.flush ();
        printout.close ();

    }catch (MalformedURLException e){

    }
        catch (Exception e){

        }
    }

    private class MyLocationListener implements LocationListener {

        public void onLocationChanged(Location location) {
            String message = String.format(
                    "New Location \n Longitude: %1$s \n Latitude: %2$s",
                    location.getLongitude(), location.getLatitude()
            );
            Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
        }

        public void onStatusChanged(String s, int i, Bundle b) {
            Toast.makeText(MainActivity.this, "Provider status changed",
                    Toast.LENGTH_LONG).show();
        }

        public void onProviderDisabled(String s) {
            Toast.makeText(MainActivity.this,
                    "Provider disabled by the user. GPS turned off",
                    Toast.LENGTH_LONG).show();
        }

        public void onProviderEnabled(String s) {
            Toast.makeText(MainActivity.this,
                    "Provider enabled by the user. GPS turned on",
                    Toast.LENGTH_LONG).show();
        }

    }

    public void showDialog(final String sms) throws Exception
    {
         final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        builder.setMessage("Click Abort to STOP");


        builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sendMsg(sms);
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("Abort", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });


        final AlertDialog  alert = builder.create();
        alert.show();

        final Handler handler  = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (alert.isShowing()) {
                    Log.i("timer expired","");
                    sendMsg(sms);
                    alert.dismiss();
                }
            }
        };

        alert.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                handler.removeCallbacks(runnable);
            }
        });
        handler.postDelayed(runnable, 10000);

    }
}
