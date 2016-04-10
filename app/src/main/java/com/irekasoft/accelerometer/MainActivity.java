package com.irekasoft.accelerometer;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.irekasoft.accelerometer.model.AccelerometerData;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class MainActivity extends ActionBarActivity implements SensorEventListener {

  Sensor accelerometer;
  SensorManager sm;
  TextView acceleration;
  SensorEvent sensorEvent;
  Realm realm;
  ArrayList<String> dataArray;

  ArrayAdapter<String> myAdapter;
  ListView listView;

  private int mInterval = 100; // 1000 = 1 seconds by default, can be changed later
  private Handler mHandler;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    sm = (SensorManager) getSystemService(SENSOR_SERVICE);
    accelerometer = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    sm.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

    acceleration = (TextView) findViewById(R.id.accelerometer);

    RealmConfiguration config = new RealmConfiguration.Builder(this).build();
    Realm.setDefaultConfiguration(config);
    realm = Realm.getDefaultInstance();
    Log.d("", "path: " + realm.getPath());
    ///

    // CREATE ARRAY OF STRINGS
    dataArray = new ArrayList<String>();


    myAdapter = new ArrayAdapter<String>(
        this,
        android.R.layout.simple_list_item_1,
        dataArray
    );

// GET LIST VIEW AND SET ADAPTER
    listView = (ListView)findViewById(R.id.listView);
    listView.setAdapter(myAdapter);


    // start scheduler
    mHandler = new Handler();
    startRepeatingTask();

  }

  Runnable mStatusChecker = new Runnable() {
    @Override
    public void run() {
      try {
        updateStatus(); //this function can change value of mInterval.
      } finally {
        // 100% guarantee that this always happens, even if
        // your update method throws an exception
        mHandler.postDelayed(mStatusChecker, mInterval);
      }
    }
  };

  void updateStatus(){

    Calendar c = Calendar.getInstance();
//    System.out.println("Current time => " + c.getTime());


    String accelerometer = "";

    if (sensorEvent != null){
      accelerometer = "X:" + sensorEvent.values[0] + " Y:" + sensorEvent.values[1] + " Z:" + sensorEvent.values[2];
    }else{
      return;
    }

    SimpleDateFormat df = new SimpleDateFormat("dd-M-yyyy hh:mm:ss aa");
    String formattedDate = df.format(c.getTime());
    acceleration.setText(formattedDate+"\n"+accelerometer);
    Date date = new Date();



    realm.beginTransaction();

    AccelerometerData ad = realm.createObject(AccelerometerData.class);
    ad.setX((double)sensorEvent.values[0]);
    ad.setY((double)sensorEvent.values[1]);
    ad.setZ((double)sensorEvent.values[2]);
    ad.setMillisecond(date.getTime());

    realm.commitTransaction();


    dataArray.add(accelerometer + " - " + date.getTime());
    myAdapter.notifyDataSetChanged();
    listView.post(new Runnable() {
      @Override
      public void run() {
        // Select the last row so it will scroll into view...
        listView.setSelection(myAdapter.getCount() - 1);
      }
    });

  }

  void startRepeatingTask() {
    mStatusChecker.run();
  }

  void stopRepeatingTask() {
    mHandler.removeCallbacks(mStatusChecker);
  }

  // SensorEventListener
  @Override
  public void onSensorChanged(SensorEvent event) {

    //acceleration.setText("X:" + event.values[0] + "\nY:" + event.values[1] + "\nZ:" + event.values[2]);
    sensorEvent = event;

  }

  @Override
  public void onAccuracyChanged(Sensor sensor, int accuracy) {

  }

  public void export(View v) {

    // init realm
    Realm realm = Realm.getInstance(this);

    File exportRealmFile = null;
    try {
      // get or create an "export.realm" file
      exportRealmFile = new File(this.getExternalCacheDir(), "export.realm");

      // if "export.realm" already exists, delete
      exportRealmFile.delete();

      // copy current realm to "export.realm"
      realm.writeCopyTo(exportRealmFile);

    } catch (IOException e) {
      e.printStackTrace();
    }
    realm.close();

    // init email intent and add export.realm as attachment
    Intent intent = new Intent(Intent.ACTION_SEND);
    intent.setType("plain/text");
    intent.putExtra(Intent.EXTRA_EMAIL, "YOUR MAIL");
    intent.putExtra(Intent.EXTRA_SUBJECT, "YOUR SUBJECT");
    intent.putExtra(Intent.EXTRA_TEXT, "YOUR TEXT");
    Uri u = Uri.fromFile(exportRealmFile);
    intent.putExtra(Intent.EXTRA_STREAM, u);

    // start email intent
    startActivity(Intent.createChooser(intent, "YOUR CHOOSER TITLE"));
  }
}
