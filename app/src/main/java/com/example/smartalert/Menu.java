package com.example.smartalert;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class Menu extends AppCompatActivity implements SensorEventListener {

    dbHelper db;
    Button b1,b2,b3,b4,b5,b6;
    TextView t1, t2, t3;

    Sensor sensor,sensor_main;
    SensorManager sm;
    LocationManager locationManager;
    double sum,check_sum,value;
    boolean min,max,flag;
    int i,count;
    Calendar c;
    CountDownTimer timer;
    private Button mButton;
    private PopupWindow mPopupWindow;
    private ScrollView mRelativeLayout;
    boolean gpspermgranted;
    private static final int MY_PERMISSIONS_REQUEST_CODE = 123;
    public static final String PREFS_NAME = "MyPrefsFile";
    SharedPreferences sharedpreferences;
    public static final String MyPREFERENCES = "MyPrefs" ;
    String lang, ringtone_value;
    private AudioManager myAudioManager;
    TextToSpeech tt1;
    Ringtone r;
    private double latitude = 0.0;
    private double longitude = 0.0;
    Timer timer2;
    boolean gps_enabled = false;
    boolean network_enabled = false;
    public static boolean earthquakeBefore;  // flag to check if an earthquake got detected before ours
    public static int detectCounter;  //counter to avoid unwanted multiple entries in the earthquake alarms
    AlertDialog.Builder builderEarth;

    DatabaseReference reff;
    Earthquakes_Firebase earthquakesFirebase;
    long maxid=0;

    @SuppressLint("InvalidWakeLockTag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);  //keeping screen on

        builderEarth = new AlertDialog.Builder(this);

        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        lang=sharedpreferences.getString("Language", "");
        ringtone_value=sharedpreferences.getString("Ringtone", "");
        if (lang.equals("null") || lang.isEmpty())
        {
            lang = "English";
        }

        db = new dbHelper(this);
        b1=(Button)findViewById(R.id.viewmyFalls);
        b2 =(Button)findViewById(R.id.settings);
        b3 =(Button)findViewById(R.id.logout);
        b4=(Button)findViewById(R.id.fireMode);
        b5=(Button)findViewById(R.id.viewmyFires);
        b6=(Button)findViewById(R.id.viewmyEarthquakes);
        t1=(TextView)findViewById(R.id.weltext);
        t2=(TextView)findViewById(R.id.statusMsg);
        t3=(TextView)findViewById(R.id.menu1);

        languageCheck();
        t1.append(", "+Login.unamewel);  //adding user's name to the welcome text

        if(!isNetworkAvailable()){  //Checking for internet connection and warning
            if (lang.equals("Ελληνικά")) {
                Toast.makeText(getBaseContext(), "Δεν υπάρχει σύνδεση στο Διαδίκτυο. Συνδεθείτε σε ένα δίκτυο για να την λειτουργία της online καταγραφής", Toast.LENGTH_SHORT).show();
            } else if (lang.equals("English")) {
                Toast.makeText(getBaseContext(), "No internet connection available. Please connect to a network for online logging to function", Toast.LENGTH_SHORT).show();
            } else if (lang.equals("Nederlands")) {
                Toast.makeText(getBaseContext(), "Geen internetverbinding beschikbaar. Maak verbinding met een netwerk om online loggen te laten functioneren", Toast.LENGTH_SHORT).show();
            }
        }

        gpspermgranted = false;

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            checkPermission();                 //Checking-Asking the required permissions
        }


        sm=(SensorManager)getSystemService(SENSOR_SERVICE);
        sensor=sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensor_main=sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        count = 0;
        earthquakeBefore=false;
        detectCounter=0;

        earthquakesFirebase = new Earthquakes_Firebase();
        reff = FirebaseDatabase.getInstance().getReference().child("Earthquakes");
        reff.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    maxid=(snapshot.getChildrenCount());  //Auto-increment for the id of earthquake firebase logs
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Menu.this,FallEventList.class); //going to the FallEventList activity
                startActivity(i);
            }
        });

        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Menu.this,SettingsMenu.class); //going to the SettingsMenu activity
                startActivity(i);
            }
        });

        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {             //Logout
                if(lang.equals("Ελληνικά"))
                {
                    new AlertDialog.Builder(Menu.this)
                            .setMessage("Είστε σίγουρος ότι θέλετε να Φύγετε?")
                            .setCancelable(false)
                            .setPositiveButton("Ναι", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    Intent i = new Intent(Menu.this,MainActivity.class); //going to the main activity
                                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(i);
                                }
                            })
                            .setNegativeButton("Οχι", null)
                            .show();
                }
                else if(lang.equals("English")) {
                    new AlertDialog.Builder(Menu.this)
                            .setMessage("Are you sure you want to Exit?")
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    Intent i = new Intent(Menu.this, MainActivity.class); //going to the main activity
                                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(i);
                                }
                            })
                            .setNegativeButton("No", null)
                            .show();
                }
                else if(lang.equals("Nederlands"))
                {
                    new AlertDialog.Builder(Menu.this)
                            .setMessage("Weet u zeker dat u wilt afsluiten?")
                            .setCancelable(false)
                            .setPositiveButton("Ja", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    Intent i = new Intent(Menu.this, MainActivity.class); //going to the main activity
                                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(i);
                                }
                            })
                            .setNegativeButton("Nee", null)
                            .show();
                }
            }
        });

        b4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Menu.this,FireReport.class); //going to the FireReport activity
                startActivity(i);
            }
        });

        b5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Menu.this,FireReportList.class); //going to the FireReportList activity
                startActivity(i);
            }
        });

        b6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Menu.this,EarthquakeList.class); //going to the EarthquakeList activity
                startActivity(i);
            }
        });
    }

    LocationListener locationListenerGps = new LocationListener() {  //GPS Location Listener
        public void onLocationChanged(Location location) {
            timer2.cancel();
            latitude =location.getLatitude();
            longitude = location.getLongitude();
            //locationManager.removeUpdates(this);
            locationManager.removeUpdates(locationListenerNetwork);
        }

        public void onProviderDisabled(String provider) {
            enableGPSPrompt(Menu.this);
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    LocationListener locationListenerNetwork = new LocationListener() {     //Network Location Listener
        public void onLocationChanged(Location location) {
            timer2.cancel();
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            locationManager.removeUpdates(this);
            //locationManager.removeUpdates(locationListenerGps);
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    class GetLastLocation extends TimerTask {      //Getting last location if after 20 seconds both GPS and Network location fail to get a fix
        @SuppressLint("MissingPermission")
        @Override
        public void run() {
            //locationManager.removeUpdates(locationListenerGps);
            locationManager.removeUpdates(locationListenerNetwork);

            Location net_loc=null, gps_loc=null;
            if(gps_enabled)
                gps_loc=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(network_enabled)
                net_loc=locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            //if there are both values use the latest one
            if(gps_loc!=null && net_loc!=null){
                if(gps_loc.getTime()>net_loc.getTime())
                {latitude = gps_loc.getLatitude();
                    longitude = gps_loc.getLongitude();
                }
                else
                {latitude = net_loc.getLatitude();
                    longitude = net_loc.getLongitude();
                }
            }

            if(gps_loc!=null){
                {latitude = gps_loc.getLatitude();
                    longitude = gps_loc.getLongitude();
                }
            }
            if(net_loc!=null){
                {latitude = net_loc.getLatitude();
                    longitude = net_loc.getLongitude();
                }
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {   //Accelerometer activity

        if (!isPhonePluggedIn(this)){      //If phone isn't charging, check for fall detection.

            if(lang.equals("Ελληνικά"))
            {
                t2.setText ("Ανίχνευση πτώσης σε αναμονή...");
            }
            else if(lang.equals("English"))
            {
                t2.setText("Fall Detection on Stand By...");
            }
            else if(lang.equals("Nederlands"))
            {
                t2.setText ("Valdetectie op stand-by...");
            }
            t2.setTextColor(Color.parseColor("#00FF0A"));
            sum = Math.round(Math.sqrt(Math.pow(event.values[0], 2)     // Calculation if a fall occurred
                    + Math.pow(event.values[1], 2)
                    + Math.pow(event.values[2], 2)));

        if (sum <= 5.0) {
            min = true;
        }

        if (min == true) {
            i++;
            if (sum >= 16.5) {
                max = true;
            }
        }

        if (min == true && max == true) {            // If a possible fall gets detected then we go to the PossibleFall activity for further checks
            sm.unregisterListener(this);
            if(lang.equals("Ελληνικά"))
            {
                Toast.makeText(getApplicationContext(), "Εντοπίστηκε πιθανή πτώση", Toast.LENGTH_SHORT).show();
            }
            else if(lang.equals("English"))
            {
                Toast.makeText(getApplicationContext(), "Possible Fall Occurred", Toast.LENGTH_SHORT).show();
            }
            else if(lang.equals("Nederlands"))
            {
                Toast.makeText(getApplicationContext(), "Mogelijke val deed zich voor", Toast.LENGTH_SHORT).show();
            }
            Intent test = new Intent(Menu.this, PossibleFall.class);
            startActivityForResult(test, 2);

            min = false;
            max = false;

            if (count > 45) {
                if(lang.equals("Ελληνικά"))
                {
                    Toast.makeText(getApplicationContext(), "Επιβεβαιώθηκε Πτώση", Toast.LENGTH_LONG).show();
                }
                else if(lang.equals("English"))
                {
                    Toast.makeText(getApplicationContext(), "Fall Confirmed", Toast.LENGTH_LONG).show();
                }
                else if(lang.equals("Nederlands"))
                {
                    Toast.makeText(getApplicationContext(), "Val Bevestigd", Toast.LENGTH_LONG).show();
                }
                i = 0;
                count = 0;
                min = false;
                max = false;
            }
        }
    }else{                                   //If the phone is plugged in --> Earthquake Detection Mode
            if(lang.equals("Ελληνικά"))
            {
                t2.setText("Ανίχνευση σεισμού σε αναμονή...");
            }
            else if(lang.equals("English"))
            {
                t2.setText("Earthquake Detection on Stand By...");
            }
            else if(lang.equals("Nederlands"))
            {
                t2.setText("Aardbevingsdetectie op stand-by...");
            }
            t2.setTextColor(Color.parseColor("#FF0000"));


            if (event.values[0]>13 || event.values[1]>13 || event.values[2]>13) {   //possible earthquake sense from our device

               // sm.unregisterListener(this);                     //After a detection setting a cooldown time of 5 seconds. EDIT: Maybe not that reliable cause a sensor listener could get initialized after we stop it (if an earthquake gets confirmed)
              /*  new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        sm.registerListener(Menu.this,
                                sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                                SensorManager.SENSOR_DELAY_NORMAL);
                    }
                }, 5000); */


                if(lang.equals("Ελληνικά"))  //informing the user about a possible earthquake detection (not confirmed yet)
                {
                    Toast.makeText(getApplicationContext(),"Η συσκευή εντόπισε πιθανή σεισμική δόνηση",Toast.LENGTH_SHORT).show();
                }
                else if(lang.equals("English"))
                {
                    Toast.makeText(getApplicationContext(),"Device Detected Possible Earthquake Vibration",Toast.LENGTH_SHORT).show();
                }
                else if(lang.equals("Nederlands"))
                {
                    Toast.makeText(getApplicationContext(),"Apparaat gedetecteerd Mogelijke trillingen door aardbevingen",Toast.LENGTH_SHORT).show();
                }

                boolean insert = db.insertEarthquakeEvent(Login.unamewel, "Earthquake Sense Event", longitude, latitude);  //logging in sqlite
                if(!insert){
                    if(lang.equals("Ελληνικά"))
                    {
                        Toast.makeText(getApplicationContext(),"Η καταγραφή του συμβάντος απέτυχε",Toast.LENGTH_SHORT).show();
                    }
                    else if(lang.equals("English"))
                    {
                        Toast.makeText(getApplicationContext(),"Event Logging Failed",Toast.LENGTH_SHORT).show();
                    }
                    else if(lang.equals("Nederlands"))
                    {
                        Toast.makeText(getApplicationContext(),"Logboekregistratie is mislukt",Toast.LENGTH_SHORT).show();
                    }
                }

                if (isNetworkAvailable()) {  //check if there is an internet connection
                    earthquakesFirebase.setUser(Login.unamewel);                          //Logging the possible earthquake detection in Firebase
                    earthquakesFirebase.setLatitude(latitude);
                    earthquakesFirebase.setLongitude(longitude);
                    earthquakesFirebase.setTimestamp(getDateTime());
                    earthquakesFirebase.setEventType("Earthquake Sense Event");
                    reff.child(String.valueOf(maxid + 1)).setValue(earthquakesFirebase);

                    Date currentTime= null;
                    String currentDate = getDateTime();  //current time that the earthquake got sensed
                    final Double currLongitude = longitude;  //saving current detection coordinates
                    final Double currLatitude = latitude;
                    try {
                        currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(currentDate);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    Calendar c = Calendar.getInstance();
                    c.setTime(currentTime);
                    c.add(Calendar.SECOND, -30);
                    Date newDate = c.getTime();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String newDateMinus30 = dateFormat.format(newDate);  //current time minus 30 seconds

                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Earthquakes");
                    Query query = ref.orderByChild("timestamp").startAt(newDateMinus30).endAt(currentDate);
                        query.addListenerForSingleValueEvent(new ValueEventListener() {       //checking for other earthquake reports that are recorded before 30 sec, are not from the same user and they are in a 500km distance radius from our report
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.getChildrenCount() > 0) {  //if there are such detections
                                    int i=0;
                                    for (DataSnapshot quakeSnapshot: dataSnapshot.getChildren()) {
                                        Earthquakes_Firebase earthquakes_firebase = quakeSnapshot.getValue(Earthquakes_Firebase.class);
                                        if(!earthquakes_firebase.getUser().equals(Login.unamewel) && earthquakes_firebase.getEventType().equals("Earthquake Sense Event"))  // checking if they ain't our self records and if it's an earthquake sense event (and not confirmed)
                                        {
                                            if(currLatitude!=0.0 && currLongitude!=0.0 && earthquakes_firebase.getLatitude()!=0.0 && earthquakes_firebase.getLongitude()!=0.0) {  //checking if coordinates are zero
                                                double distance = getDistance(currLatitude, earthquakes_firebase.getLatitude(), currLongitude, earthquakes_firebase.getLongitude(), 0.0, 0.0); //calculating the distance of our sense with other senses reported close to ours (in terms of time too)
                                                if (distance >= 0.0 && distance <= 500000.0) {
                                                    i++;
                                                }
                                            }
                                        }
                                    }
                                    if(i>0 && detectCounter<1)   //if there are such records found --> real earthquake confirmed
                                    {
                                        earthquakeBefore=true;
                                        detectCounter++;
                                        sm.unregisterListener(Menu.this);
                                        if(r!=null) {
                                            r.stop();
                                        }
                                        if(tt1!=null){
                                            tt1.stop();
                                            tt1.shutdown();
                                        }

                                        earthquakesFirebase.setUser(Login.unamewel);           //Logging the confirmation in Firebase
                                        earthquakesFirebase.setLatitude(latitude);
                                        earthquakesFirebase.setLongitude(longitude);
                                        earthquakesFirebase.setTimestamp(getDateTime());
                                        earthquakesFirebase.setEventType("Earthquake Confirmed Event");
                                        reff.child(String.valueOf(maxid + 1)).setValue(earthquakesFirebase);

                                        new Handler().postDelayed(new Runnable() {   //delay in order no to mess 2 entries together
                                            @Override
                                            public void run() {     //Logging the confirmation in SQLite after a second. To avoid conflicts with the sense records
                                                boolean insertcon1 = db.insertEarthquakeEvent(Login.unamewel, "Earthquake Confirmed Event", longitude, latitude);  //insert in sqlite
                                                if (!insertcon1) {
                                                    if (lang.equals("Ελληνικά")) {
                                                        Toast.makeText(getApplicationContext(), "Η καταγραφή του συμβάντος απέτυχε", Toast.LENGTH_SHORT).show();
                                                    } else if (lang.equals("English")) {
                                                        Toast.makeText(getApplicationContext(), "Event Logging Failed", Toast.LENGTH_SHORT).show();
                                                    } else if (lang.equals("Nederlands")) {
                                                        Toast.makeText(getApplicationContext(), "Logboekregistratie is mislukt", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            }
                                           }, 1000);

                                        earthquakeAlert();  //Triggering the alert
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                throw databaseError.toException();
                            }
                        });

                    if(!earthquakeBefore) {   //executing the post check only if there wasn't an earthquake detected before ours
                        new Handler().postDelayed(new Runnable() {     //30 seconds after
                            @Override
                            public void run() {       //doing the same as before but with a 30 sec delay so we can also check 30 sec ahead of our report for possible other reports from other users nearby
                                Date currentTime = null;
                                String currentDate = getDateTime();  //current time that the earthquake got sensed
                                try {
                                    currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(currentDate);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                Calendar c = Calendar.getInstance();
                                c.setTime(currentTime);
                                c.add(Calendar.SECOND, -30); //checking 30 seconds earlier (from the delayed time) if someone else sensed an earthquake
                                Date newDate = c.getTime();
                                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                String newDateMinus30 = dateFormat.format(newDate);  //new time minus 30 seconds

                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Earthquakes");
                                Query query = ref.orderByChild("timestamp").startAt(newDateMinus30).endAt(currentDate);
                                query.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.getChildrenCount() > 0) {
                                            int i = 0;
                                            for (DataSnapshot quakeSnapshot : dataSnapshot.getChildren()) {
                                                Earthquakes_Firebase earthquakes_firebase = quakeSnapshot.getValue(Earthquakes_Firebase.class);
                                                if (!earthquakes_firebase.getUser().equals(Login.unamewel) && earthquakes_firebase.getEventType().equals("Earthquake Sense Event"))  //na mhn einai dikes mas eggrafes
                                                {
                                                    if (currLatitude != 0.0 && currLongitude != 0.0 && earthquakes_firebase.getLatitude() != 0.0 && earthquakes_firebase.getLongitude() != 0.0) {
                                                        double distance = getDistance(currLatitude, earthquakes_firebase.getLatitude(), currLongitude, earthquakes_firebase.getLongitude(), 0.0, 0.0); //getting distance
                                                        if (distance >= 0.0 && distance <= 500000.0) {
                                                            i++;
                                                        }
                                                    }
                                                }
                                            }
                                            if (i > 0 && detectCounter<1) {
                                                detectCounter++;
                                                sm.unregisterListener(Menu.this);

                                                if (r != null) {
                                                    r.stop();
                                                }
                                                if (tt1 != null) {
                                                    tt1.stop();
                                                    tt1.shutdown();
                                                }

                                                earthquakesFirebase.setUser(Login.unamewel);
                                                earthquakesFirebase.setLatitude(latitude);
                                                earthquakesFirebase.setLongitude(longitude);
                                                earthquakesFirebase.setTimestamp(getDateTime());
                                                earthquakesFirebase.setEventType("Earthquake Confirmed Event");
                                                reff.child(String.valueOf(maxid + 1)).setValue(earthquakesFirebase);

                                                boolean insertcon2 = db.insertEarthquakeEvent(Login.unamewel, "Earthquake Confirmed Event", longitude, latitude);  //insert in sqlite
                                                if (!insertcon2) {
                                                    if (lang.equals("Ελληνικά")) {
                                                        Toast.makeText(getApplicationContext(), "Η καταγραφή του συμβάντος απέτυχε", Toast.LENGTH_SHORT).show();
                                                    } else if (lang.equals("English")) {
                                                        Toast.makeText(getApplicationContext(), "Event Logging Failed", Toast.LENGTH_SHORT).show();
                                                    } else if (lang.equals("Nederlands")) {
                                                        Toast.makeText(getApplicationContext(), "Logboekregistratie is mislukt", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                                earthquakeAlert();
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        throw databaseError.toException();
                                    }
                                });
                            }
                        }, 30000);  //checking again after 30 seconds in case someone else sensed the earthquake later
                    }

                } else { //If there is no internet connection, logging locally and warning the user
                    if (lang.equals("Ελληνικά")) {
                        Toast.makeText(getBaseContext(), "Δεν υπάρχει σύνδεση στο Διαδίκτυο. Το συμβάν καταγράφηκε τοπικά και δεν μπορούν να αντληθούν δεδομένα από άλλες συσκευές!", Toast.LENGTH_LONG).show();
                    } else if (lang.equals("English")) {
                        Toast.makeText(getBaseContext(), "No internet connection available. Event logged locally and data sourcing from other devices is not possible!", Toast.LENGTH_LONG).show();
                    } else if (lang.equals("Nederlands")) {
                        Toast.makeText(getBaseContext(), "Geen internetverbinding beschikbaar. Gebeurtenis lokaal gelogd en data sourcing van andere apparaten is niet mogelijk!", Toast.LENGTH_LONG).show();
                    }
                }

                if (longitude == 0.0 && latitude == 0.0)  //In case there is no GPS signal, warning user
                {
                    if (lang.equals("Ελληνικά")) {
                        Toast.makeText(getBaseContext(), "Δεν ήταν δυνατή η καταγραφή της τοποθεσίας GPS καθώς δεν ήταν διαθέσιμη", Toast.LENGTH_LONG).show();
                    } else if (lang.equals("English")) {
                        Toast.makeText(getBaseContext(), "Unable to log GPS Location as it was not available", Toast.LENGTH_LONG).show();
                    } else if (lang.equals("Nederlands")) {
                        Toast.makeText(getBaseContext(), "Kan gps-locatie niet vastleggen omdat deze niet beschikbaar was", Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onResume() {  //language checking, internet connection checking, restarting sensor listener and location listeners
        super.onResume();
        languageCheck();
        if(!isNetworkAvailable()){
            if (lang.equals("Ελληνικά")) {
                Toast.makeText(getBaseContext(), "Δεν υπάρχει σύνδεση στο Διαδίκτυο. Συνδεθείτε σε ένα δίκτυο για να την λειτουργία της online καταγραφής", Toast.LENGTH_SHORT).show();
            } else if (lang.equals("English")) {
                Toast.makeText(getBaseContext(), "No internet connection available. Please connect to a network for online logging to function", Toast.LENGTH_SHORT).show();
            } else if (lang.equals("Nederlands")) {
                Toast.makeText(getBaseContext(), "Geen internetverbinding beschikbaar. Maak verbinding met een netwerk om online loggen te laten functioneren", Toast.LENGTH_SHORT).show();
            }
        }
        sm.registerListener(this,
                sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
        if(gpspermgranted) {
            doStuff();   //very important to have it also here. for example if GPS is turned off and we get prompted to enable it, then as we go back (resume) to our app the gps will start again
        }
    }

    @Override
    protected void onPause() {   //stopping sensor and gps listeners
        super.onPause();
        sm.unregisterListener(this);
        if(gpspermgranted) {                             //otherwise the app would crash when it didn't have the permission
            //locationManager.removeUpdates(this);  //to stop the location manager when this activity isn't on foreground
            locationManager.removeUpdates(locationListenerGps);
        }
    }

    public void speakout()   //Text to Speech
    {
        Log.e("Speech", "In speech module");

        tt1.speak("Warning, an earthquake vibration got sensed from your and other devices! Please move to a safe location and be cautious.", TextToSpeech.QUEUE_FLUSH, null);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

        } else {
            tt1.speak("Warning, an earthquake vibration got sensed from your and other devices! Please move to a safe location and be cautious.", TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    @Override
    public void onBackPressed() {       //If user tries to go back from the menu activity, asking him if he really wants to exit and if so, we log him out
        if(lang.equals("Ελληνικά"))
        {
            new AlertDialog.Builder(this)
                    .setMessage("Είστε σίγουρος ότι θέλετε να Φύγετε?")
                    .setCancelable(false)
                    .setPositiveButton("Ναι", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent i = new Intent(Menu.this,MainActivity.class); //going to the main activity
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(i);
                        }
                    })
                    .setNegativeButton("Οχι", null)
                    .show();
        }
        else if(lang.equals("English")) {
            new AlertDialog.Builder(this)
                    .setMessage("Are you sure you want to Exit?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent i = new Intent(Menu.this, MainActivity.class); //going to the main activity
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(i);
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
        }
        else if(lang.equals("Nederlands"))
        {
            new AlertDialog.Builder(this)
                    .setMessage("Weet u zeker dat u wilt afsluiten?")
                    .setCancelable(false)
                    .setPositiveButton("Ja", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent i = new Intent(Menu.this, MainActivity.class); //going to the main activity
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(i);
                        }
                    })
                    .setNegativeButton("Nee", null)
                    .show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==2)
        {
            int message=data.getIntExtra("count_value", 0);
            String msg=String.valueOf(message);
            Log.e("Result is",msg);
            if(message>25)
            {
                Intent alert_intent=new Intent(Menu.this, FallDetected.class);  //for severe detections we bypass the fall confirmation process
                startActivity(alert_intent);
            }
        }
    }

    protected void checkPermission(){    //Checking and requesting all required permissions and also informing the user why they are needed
        if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                + checkSelfPermission(Manifest.permission.READ_CONTACTS)
                + checkSelfPermission(Manifest.permission.SEND_SMS)
                + checkSelfPermission(Manifest.permission.READ_PHONE_STATE)
                + checkSelfPermission(Manifest.permission.CAMERA)
                + checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){

            // Do something, when permissions not granted
            if(shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)
                    || shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS)
                    || shouldShowRequestPermissionRationale(Manifest.permission.SEND_SMS)
                    || shouldShowRequestPermissionRationale(Manifest.permission.READ_PHONE_STATE)
                    || shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)
                    || shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)){

                // Show an alert dialog here with request explanation
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                if(lang.equals("Ελληνικά"))
                {
                    builder.setMessage ("Τοποθεσία: Απαιτείται για την αποστολή της τοποθεσίας σας σε περίπτωση έκτακτης ανάγκης. \n" +
                            "Πρόσβαση σε επαφές: Απαιτείται για να ορίσετε τον παραλήπτη των μηνυμάτων έκτακτης ανάγκης. \n" +
                            "Πρόσβαση σε SMS: Απαιτείται για την αποστολή των μηνυμάτων ειδοποίησης SMS. \n" +
                            "Πρόσβαση στο τηλέφωνο: Απαιτείται για τη ρύθμιση των ληφθέντων κλήσεων από αξιόπιστες επαφές σε λειτουργία ανοικτής ακρόασης, σε περίπτωση έκτακτης ανάγκης. \n" +
                            "Πρόσβαση σε κάμερα & αποθηκευτικό χώρο: Απαιτείται για την υποβολή φωτογραφιών στις αναφορές πυρκαγιάς.");
                    builder.setTitle ("Παρακαλώ παραχωρήστε τα ακόλουθα δικαιώματα");
                }
                else if(lang.equals("English"))
                {
                    builder.setMessage("Location: Required to send your location in case of an emergency.\n" +
                            "Contact Access: Required to set your emergency alerts recipient.\n" +
                            "SMS Access: Required to send SMS alert messages.\n" +
                            "Phone Access: Required to set received calls from trusted contacts in speaker mode, in case of an emergency.\n" +
                            "Camera & Storage Access: Required to provide photos for the fire reports.");
                    builder.setTitle("Please grant the following permissions");
                }
                else if(lang.equals("Nederlands"))
                {
                    builder.setMessage ("Locatie: vereist om uw locatie te verzenden in geval van nood. \n" +
                            "Contacttoegang: vereist om uw ontvanger voor noodwaarschuwingen in te stellen. \n" +
                            "Sms-toegang: vereist om sms-waarschuwingsberichten te verzenden. \n" +
                            "Telefoontoegang: vereist om ontvangen oproepen van vertrouwde contacten in de luidsprekermodus in te stellen in geval van nood. \n" +
                            "Toegang tot camera en opslag: vereist om foto's te leveren voor de brandrapporten.");
                    builder.setTitle ("Verleen de volgende rechten");
                }

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        requestPermissions(
                                new String[]{
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.READ_CONTACTS,
                                        Manifest.permission.SEND_SMS,
                                        Manifest.permission.READ_PHONE_STATE,
                                        Manifest.permission.CAMERA,
                                        Manifest.permission.READ_EXTERNAL_STORAGE
                                },
                                MY_PERMISSIONS_REQUEST_CODE
                        );
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }else{
                // Directly request for required permissions, without explanation
                requestPermissions(
                        new String[]{
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.READ_CONTACTS,
                                Manifest.permission.SEND_SMS,
                                Manifest.permission.READ_PHONE_STATE,
                                Manifest.permission.CAMERA,
                                Manifest.permission.READ_EXTERNAL_STORAGE
                        },
                        MY_PERMISSIONS_REQUEST_CODE
                );
            }
        } else {
            firstRunStuff();
            gpspermgranted = true;
            doStuff();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        switch (requestCode){
            case MY_PERMISSIONS_REQUEST_CODE:{
                // When request is cancelled, the results array are empty
                if(
                        (grantResults.length >0) &&
                                (grantResults[0]
                                        + grantResults[1]
                                        + grantResults[2]
                                        + grantResults[3]
                                        + grantResults[4]
                                        + grantResults[5]
                                        == PackageManager.PERMISSION_GRANTED
                                )
                ){
                    // Permissions are granted
                    if(lang.equals("Ελληνικά"))
                    {
                        Toast.makeText(getApplicationContext(),"Όλα τα δικαιώματα έχουν εκχωρηθεί με επιτυχία.",Toast.LENGTH_SHORT).show();
                    }
                    else if(lang.equals("English"))
                    {
                        Toast.makeText(getApplicationContext(),"All permissions have been granted successfully.",Toast.LENGTH_SHORT).show();
                    }
                    else if(lang.equals("Nederlands"))
                    {
                        Toast.makeText(getApplicationContext(),"Alle machtigingen zijn met succes verleend.",Toast.LENGTH_SHORT).show();
                    }
                    firstRunStuff();
                    gpspermgranted = true;
                    doStuff();

                }else {
                    // Permissions are denied
                    if(lang.equals("Ελληνικά"))
                    {
                        Toast.makeText(getApplicationContext(),"Το SmartAlert απαιτεί τα ζητηθέντα δικαιώματα για να λειτουργήσει σωστά.",Toast.LENGTH_LONG).show();
                    }
                    else if(lang.equals("English"))
                    {
                        Toast.makeText(getApplicationContext(),"SmartAlert requires the asked permissions to function properly.",Toast.LENGTH_LONG).show();
                    }
                    else if(lang.equals("Nederlands"))
                    {
                        Toast.makeText(getApplicationContext(),"SmartAlert vereist de gevraagde machtigingen om correct te functioneren.",Toast.LENGTH_LONG).show();
                    }
                    Intent i = new Intent(Menu.this,MainActivity.class); //going to the main activity
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                }
                return;
            }
        }
    }


    @SuppressLint("MissingPermission")
    private void doStuff(){  //Location Managers Initialization
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);


        //if (gps_enabled)
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0,
                    locationListenerGps);
        if (network_enabled)
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 0,
                    locationListenerNetwork);
        timer2=new Timer();
        timer2.schedule(new GetLastLocation(), 20000);
    }

    //Getting current timestamp
    private String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

    private void firstRunStuff()  //Different behavior if the app is running in our device for the 1st time. Prompting the user to set all the required settings for reliable use from the start.
    {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        boolean firstRun = settings.getBoolean("firstRun", true);
        if (firstRun) {                                          //Sending user to settings in 1st time run.
            Log.w("activity", "first time");
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("firstRun", false);
            editor.commit();

            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            if(lang.equals("Ελληνικά"))
            {
                alertDialog.setTitle ("Σημαντική ειδοποίηση");
                alertDialog.setMessage ("Δεδομένου ότι χρησιμοποιείτε την εφαρμογή SmartAlert για πρώτη φορά, ορίστε όλες τις ρυθμίσεις ώστε να λειτουργεί σωστά.");
            }
            else if(lang.equals("English"))
            {
                alertDialog.setTitle("Important Notice");
                alertDialog.setMessage("Since you are using the SmartAlert app for the first time, please configure all the settings for it to function properly.");
            }
            else if(lang.equals("Nederlands"))
            {
                alertDialog.setTitle ("Belangrijke mededeling");
                alertDialog.setMessage ("Aangezien u de SmartAlert-app voor de eerste keer gebruikt, moet u alle instellingen configureren zodat deze correct werkt.");
            }

            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent i = new Intent(Menu.this,SettingsMenu.class); //going to the SettingsMenu activity
                            startActivity(i);
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
        }
        else {
            Log.w("activity", "second time");
        }
    }

    //Prompt for the user to enable GPS if not enabled.
    public void enableGPSPrompt(final Activity activity)
    {

        final AlertDialog.Builder builder =  new AlertDialog.Builder(activity);
        final String action = Settings.ACTION_LOCATION_SOURCE_SETTINGS;

        if(lang.equals("Ελληνικά"))
        {
            builder.setMessage("Παρακαλώ ενεργοποιήστε τις υπηρεσίες τοποθεσίας για να λειτουργεί σωστά η εφαρμογή SmartAlert.")
                    .setPositiveButton("Ρυθμισεις",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int id) {
                                    activity.startActivity(new Intent(action));
                                    d.dismiss();
                                }
                            });
        }
        else if(lang.equals("English"))
        {
            builder.setMessage("Please enable Location Services in order for the SmartAlert app to function properly.")
                    .setPositiveButton("Settings",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int id) {
                                    activity.startActivity(new Intent(action));
                                    d.dismiss();
                                }
                            });
        }
        else if(lang.equals("Nederlands"))
        {
            builder.setMessage("Schakel locatieservices in om ervoor te zorgen dat de SmartAlert app correct werkt.")
                    .setPositiveButton("Instellingen",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int id) {
                                    activity.startActivity(new Intent(action));
                                    d.dismiss();
                                }
                            });
        }
        builder.create().show();
    }

    public static boolean isPhonePluggedIn(Context context){   // To check if phone is plugged in
        boolean charging = false;

        final Intent batteryIntent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int status = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean batteryCharge = status==BatteryManager.BATTERY_STATUS_CHARGING;

        int chargePlug = batteryIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
        boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;

        if (batteryCharge) charging=true;
        if (usbCharge) charging=true;
        if (acCharge) charging=true;

        return charging;
    }

    private void languageCheck()  //UI Language Management
    {
        if(lang.equals("Ελληνικά"))
        {
            t1.setText ("Καλώς ορίσατε στο κύριο μενού της εφαρμογής SmartAlert");
            t1.append(", "+Login.unamewel);
            t2.setText ("Ανίχνευση πτώσης σε αναμονή ...");
            t3.setText ("Επιλέξτε τη λειτουργία που επιθυμείτε:");
            b1.setText ("Προβολη των συμβαντων πτωσης μου");
            b2.setText ("Ρυθμισεις");
            b3.setText ("Αποσυνδεση");
            b4.setText ("Αναφορα πυρκαγιας");
            b5.setText ("Προβολη των αναφορων πυρκαγιας μου");
            b6.setText ("Προβολη των συμβαντων σεισμων μου");
        }
        else if(lang.equals("English"))
        {
            t1.setText("Welcome to the SmartAlert App's Main Menu");
            t1.append(", "+Login.unamewel);
            t2.setText("Fall Detection on Stand By...");
            t3.setText("Select the function you prefer:");
            b1.setText("View My Fall Events");
            b2.setText("Settings");
            b3.setText("Logout");
            b4.setText("Report Fire");
            b5.setText("View My Fire Reports");
            b6.setText("View My Earthquake Events");
        }
        else if(lang.equals("Nederlands"))
        {
            t1.setText ("Welkom bij het hoofdmenu van de SmartAlert-app");
            t1.append(", "+Login.unamewel);
            t2.setText ("Valdetectie op stand-by ...");
            t3.setText ("Selecteer de functie die u verkiest:");
            b1.setText ("Bekijk mijn herfst gebeurtenissen");
            b2.setText ("Instellingen");
            b3.setText ("Uitloggen");
            b4.setText ("Brand melden");
            b5.setText ("Bekijk mijn brandrapporten");
            b6.setText ("Bekijk mijn aardbeving gebeurtenissen");
        }
    }

    private boolean isNetworkAvailable() {       //Checking if device is connected to the internet
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static double getDistance(double lat1, double lat2, double lon1,  // getting distance from 2 coordinates
                                  double lon2, double el1, double el2) {

        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        double height = el1 - el2;

        distance = Math.pow(distance, 2) + Math.pow(height, 2);

        return Math.sqrt(distance);
    }

    private void earthquakeAlert(){           //Ringtone, TTS and Alert Dialog for the Earthquake Alarm
        Uri uri = Uri.parse(ringtone_value);
        if(r!=null) {
            r.stop();
        }
        r = RingtoneManager.getRingtone(getApplicationContext(), uri);
        r.play();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                r.stop();

                tt1 = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if (status != TextToSpeech.ERROR) {
                            tt1.setLanguage(Locale.UK);
                            speakout();
                        }
                    }
                });


            }
        }, 10000);

        builderEarth = new AlertDialog.Builder(this);

        if(lang.equals("Ελληνικά"))
        {
            builderEarth.setTitle ("ΕΠΙΒΕΒΑΙΩΘΗΚΕ ΣΕΙΣΜΟΣ!");
            builderEarth.setMessage ("Προσοχή, έγινε αντιληπτή μια σεισμική δόνηση από την δική σας και άλλες συσκευές! Μεταβείτε σε μια ασφαλή τοποθεσία και να είστε προσεκτικοί.");
        }
        else if(lang.equals("English"))
        {
            builderEarth.setTitle("EARTHQUAKE GOT CONFIRMED!");
            builderEarth.setMessage("Warning, an earthquake vibration got sensed from your and other devices! Please move to a safe location and be cautious.");
        }
        else if(lang.equals("Nederlands"))
        {
            builderEarth.setTitle ("AARDBEVING IS BEVESTIGD!");
            builderEarth.setMessage ("Waarschuwing, een aardbeving trilling werd waargenomen door uw en andere apparaten! Ga naar een veilige locatie en wees voorzichtig.");
        }
        builderEarth.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent i = new Intent(Menu.this,Menu.class);
                startActivity(i);
                finish();
                dialog.dismiss();
            }
        });
        AlertDialog alertEarth = builderEarth.create();
        if(!((Menu) this ).isFinishing())  //preventing a crash
        {
            alertEarth.show();
        }else {
            if (lang.equals("Ελληνικά")) {
                Toast.makeText(getApplicationContext(), "ΕΠΙΒΕΒΑΙΩΘΗΚΕ ΣΕΙΣΜΟΣ!", Toast.LENGTH_LONG).show();
            } else if (lang.equals("English")) {
                Toast.makeText(getApplicationContext(), "EARTHQUAKE GOT CONFIRMED!", Toast.LENGTH_LONG).show();
            } else if (lang.equals("Nederlands")) {
                Toast.makeText(getApplicationContext(), "AARDBEVING IS BEVESTIGD!", Toast.LENGTH_LONG).show();
            }
            if (r != null) {
                r.stop();
            }
            if(tt1 != null) {
                tt1.stop();
                tt1.shutdown();
            }
            if(sm!=null) {
                sm.unregisterListener(this);
            }
            finish();
        }
    }
}

