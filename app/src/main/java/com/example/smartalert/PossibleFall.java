package com.example.smartalert;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.TextView;

public class PossibleFall extends AppCompatActivity implements SensorEventListener {

    Sensor sensor;
    SensorManager sensormanager;
    CountDownTimer timer;
    TextView t1,t2;
    double sum;
    int count;
    int run_count,repeat_count;
    SharedPreferences sharedpreferences;
    public static final String MyPREFERENCES = "MyPrefs" ;
    String lang;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_possible_fall);

        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        lang=sharedpreferences.getString("Language", "");
        if (lang.equals("null") || lang.isEmpty())
        {
            lang = "English";
        }

        t1=(TextView)findViewById(R.id.possible1);
        t2=(TextView)findViewById(R.id.possible2);

        languageCheck();

        sensormanager=(SensorManager)getSystemService(SENSOR_SERVICE);
        sensor=sensormanager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        run_count=1;
        repeat_count=0;
    }

    @Override
    public void onSensorChanged(final SensorEvent event) {
        run_count++;                                          //Reading the sensors for further 30 seconds after a possible fall detection
        if(run_count==2) {                               //We will confirm whether it was a real fall or a false detection.
            timer = new CountDownTimer(30000, 1000) {        //For example is the device stays still after the fall it could indicate that the user is unconscious or needs help

                public void onTick(long millisUntilFinished) {

                    sum = Math.round(Math.sqrt(Math.pow(event.values[0], 2)
                            + Math.pow(event.values[1], 2)
                            + Math.pow(event.values[2], 2)));
                    repeat_count++;
                    Log.e("Check sum", String.valueOf(sum));
                    if ((sum >= 9.80) && (sum <= 11.0)) {
                        count++;
                    }
                }

                public void onFinish() {
                    String c = String.valueOf(count);
                    String rep=String.valueOf(repeat_count);
                    Log.e("Count is", c);
                    Log.e("Loop count",rep);
                    Intent intent=new Intent();
                    intent.putExtra("count_value",count);
                    setResult(2, intent);
                    finish();
                }
            }.start();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onResume() {  //Checking the language and restarting the accelerometer sensor listeners
        super.onResume();
        languageCheck();
        sensormanager.registerListener(this,
                sensormanager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {  //stopping the accelerometer sensor listeners
        super.onPause();
        sensormanager.unregisterListener(this);
    }

    private void languageCheck()   //UI Language Management
    {
        if(lang.equals("Ελληνικά"))
        {
            t1.setText("Εντοπίστηκε πιθανή πτώση!");
            t2.setText("Μετά από 30 δευτερόλεπτα θα καθοριστεί εάν έχει συμβεί πραγματικά πτώση. Επιβεβαίωση...");
        }
        else if(lang.equals("English"))
        {
            t1.setText("A possible fall has been detected!");
            t2.setText("After 30 seconds it will be determined whether a real fall has actually occurred. Confirming...");
        }
        else if(lang.equals("Nederlands"))
        {
            t1.setText("Er is een mogelijke val gedetecteerd!");
            t2.setText("Na 30 seconden wordt bepaald of er daadwerkelijk een val heeft plaatsgevonden. Bevestigen...");
        }
    }
}
