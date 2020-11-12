package com.example.smartalert;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.hardware.Sensor;
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
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class FallDetected extends AppCompatActivity {

    private LocationManager locationManager;
    private double latitude = 0.0;
    private double longitude = 0.0;
    Timer timer2;
    boolean gps_enabled = false;
    boolean network_enabled = false;
    private String msg;
    dbHelper db;
    Button alertbutton;
    TextView tv1,tv2,tv3;
    CountDownTimer timer;
    private AudioManager myAudioManager;
    TextToSpeech t1;
    Ringtone r;
    SharedPreferences sharedpreferences;
    public static final String MyPREFERENCES = "MyPrefs" ;
    String sms_number,ringtone_value,lang;
    long response_mode,resp_time;
    boolean isPlaying;
    int timer_enabled;
    private static boolean smsSENTflag;
    private static boolean flag;
    private static int counter;

    DatabaseReference reff;
    Falls_Firebase fallsFirebase;
    long maxid=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fall_detected);

        doStuff();  //Starting Location Listeners

        alertbutton=(Button)findViewById(R.id.btn_sms);
        db = new dbHelper(this);
        smsSENTflag=false;
        flag=false;
        counter=0;
        isPlaying=true;
        timer_enabled=0;

        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);   // Perasma twn rithmisewn apo to Settings Activity
        lang=sharedpreferences.getString("Language", "");
        if (lang.equals("null") || lang.isEmpty())
        {
            lang = "English";
        }
        sms_number=sharedpreferences.getString("Contact_number", "");    //Getting the settings
        ringtone_value=sharedpreferences.getString("Ringtone", "");
        response_mode=sharedpreferences.getLong("Response", 00);
        resp_time=response_mode/60000;

        tv1=(TextView)findViewById(R.id.falldet1);
        tv2=(TextView)findViewById(R.id.falldet2);
        tv3=(TextView)findViewById(R.id.falldet3);

        languageCheck();

        if(!isNetworkAvailable()){            //Internet connection check
            if (lang.equals("Ελληνικά")) {
                Toast.makeText(getBaseContext(), "Δεν υπάρχει σύνδεση στο Διαδίκτυο. Συνδεθείτε σε ένα δίκτυο για να την λειτουργία της online καταγραφής", Toast.LENGTH_SHORT).show();
            } else if (lang.equals("English")) {
                Toast.makeText(getBaseContext(), "No internet connection available. Please connect to a network for online logging to function", Toast.LENGTH_SHORT).show();
            } else if (lang.equals("Nederlands")) {
                Toast.makeText(getBaseContext(), "Geen internetverbinding beschikbaar. Maak verbinding met een netwerk om online loggen te laten functioneren", Toast.LENGTH_SHORT).show();
            }
        }

        fallsFirebase = new Falls_Firebase();
        reff = FirebaseDatabase.getInstance().getReference().child("Falls");
        reff.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    maxid=(snapshot.getChildrenCount());  //Auto-increment for the Falls firebase entries
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        alertbutton.setOnClickListener(new View.OnClickListener() {    //Abort/sms button
            @Override
            public void onClick(View v) {

                if(lang.equals("Ελληνικά"))
                {
                    String phone_num="1234567890";    // A random phone number initialization and whole sms function
                    String sms_msg = "";
                    if(latitude!=0.0 && longitude!=0.0) {               //Changing SMS content in case there is no GPS signal available
                        sms_msg="SOS! Epesa edw: "+"https://www.google.com/maps/place/"+latitude+","+longitude+"/"+" stis "+getDateTime();
                    }
                    else
                    {
                        sms_msg = "SOS! Epesa stis "+getDateTime()+" alla den exw shma GPS!";
                    }
                    try {
                        SmsManager smsManager = SmsManager.getDefault();
                        smsManager.sendTextMessage(phone_num, null, sms_msg, null, null);
                        Toast.makeText(getApplicationContext(), "Το μήνυμα εστάλη επιτυχώς",     //Toast gia epityxh apostolh SMS
                                Toast.LENGTH_LONG).show();
                    } catch (Exception ex) {
                        Toast.makeText(getApplicationContext(),ex.getMessage().toString(),
                                Toast.LENGTH_LONG).show();
                        ex.printStackTrace();
                    }
                }
                else if(lang.equals("English"))
                {
                    String phone_num="1234567890";    // Mia tyxaia arxikopoihsh arithmou
                    String sms_msg = "";
                    if(latitude!=0.0 && longitude!=0.0) {
                        sms_msg="SOS! I fell down here: "+"https://www.google.com/maps/place/"+latitude+","+longitude+"/"+" at "+getDateTime();
                    }
                    else
                    {
                        sms_msg = "SOS! I fell down at "+getDateTime()+" but I don't have GPS signal!";
                    }
                    try {
                        SmsManager smsManager = SmsManager.getDefault();
                        smsManager.sendTextMessage(phone_num, null, sms_msg, null, null);
                        Toast.makeText(getApplicationContext(), "Message Successfully Sent",     //Toast gia epityxh apostolh SMS
                                Toast.LENGTH_LONG).show();
                    } catch (Exception ex) {
                        Toast.makeText(getApplicationContext(),ex.getMessage().toString(),
                                Toast.LENGTH_LONG).show();
                        ex.printStackTrace();
                    }
                }
                else if(lang.equals("Nederlands"))
                {
                    String phone_num="1234567890";    // Mia tyxaia arxikopoihsh arithmou
                    String sms_msg = "";
                    if(latitude!=0.0 && longitude!=0.0) {
                        sms_msg="SOS! Ik viel hier neer: "+"https://www.google.com/maps/place/"+latitude+","+longitude+"/"+" om "+getDateTime();
                    }
                    else
                    {
                        sms_msg = "SOS! Ik viel neer om "+getDateTime()+" maar ik heb geen GPS-signaal!";
                    }
                    try {
                        SmsManager smsManager = SmsManager.getDefault();
                        smsManager.sendTextMessage(phone_num, null, sms_msg, null, null);
                        Toast.makeText(getApplicationContext(), "Bericht succesvol verzonden",     //Toast gia epityxh apostolh SMS
                                Toast.LENGTH_LONG).show();
                    } catch (Exception ex) {
                        Toast.makeText(getApplicationContext(),ex.getMessage().toString(),
                                Toast.LENGTH_LONG).show();
                        ex.printStackTrace();
                    }
                }
            }
        });

        if((sms_number==null)||(ringtone_value==null)||(response_mode==0.0f))           // Checking for settings passing failure
        {
            if(lang.equals("Ελληνικά"))
            {
                Toast.makeText(this,"Σφάλμα στην ανάγνωση των ρυθμίσεων",Toast.LENGTH_LONG).show();
            }
            else if(lang.equals("English"))
            {
                Toast.makeText(this,"Error In Settings Reading",Toast.LENGTH_LONG).show();
            }
            else if(lang.equals("Nederlands"))
            {
                Toast.makeText(this,"Fout bij het lezen van instellingen",Toast.LENGTH_LONG).show();
            }
        }

        Uri uri = Uri.parse(ringtone_value);
        r = RingtoneManager.getRingtone(this, uri);        // Ringtone audio alarm
        r.play();


        new Handler().postDelayed(new Runnable() {        // Gia na stamataei o epilegmenos hxos (meta apo 10s) kai sth synexeia na paizei to tts
            @Override
            public void run() {                     // Text to Speech audio
                r.stop();
                t1=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if(status != TextToSpeech.ERROR) {
                            t1.setLanguage(Locale.UK);
                            speakout();
                        }
                    }
                });

            }
        }, 10000);


        timer = new CountDownTimer(response_mode, 1000) {        // Countdown timer for the response time, as set by the user
            public void onTick(long millisUntilFinished) {
                int seconds = (int) (millisUntilFinished / 1000);
                int minutes = seconds / 60;
                seconds = seconds % 60;
                alertbutton.setText(String.format("%02d", minutes)
                        + ":" + String.format("%02d", seconds));

                alertbutton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {                    // Alert Abort
                        if(!smsSENTflag && !flag && counter<1) {
                            timer.cancel();
                            if(lang.equals("Ελληνικά"))
                            {
                                alertbutton.setText("Η αποστολη SMS Ματαιωθηκε!");
                            }
                            else if(lang.equals("English"))
                            {
                                alertbutton.setText("SMS Sending Aborted!");
                            }
                            else if(lang.equals("Nederlands"))
                            {
                                alertbutton.setText("SMS-verzending Afgebroken!");
                            }

                            if(isNetworkAvailable()) {                    //Logging into firebase
                                fallsFirebase.setUser(Login.unamewel);
                                fallsFirebase.setLatitude(latitude);
                                fallsFirebase.setLongitude(longitude);
                                fallsFirebase.setTimestamp(getDateTime());
                                fallsFirebase.setEventType("Fall Event Abort");
                                reff.child(String.valueOf(maxid + 1)).setValue(fallsFirebase);
                            }
                            else{
                                if (lang.equals("Ελληνικά")) {
                                    Toast.makeText(getBaseContext(), "Δεν υπάρχει σύνδεση στο Διαδίκτυο. Το συμβάν καταγράφηκε τοπικά", Toast.LENGTH_LONG).show();
                                } else if (lang.equals("English")) {
                                    Toast.makeText(getBaseContext(), "No internet connection available. Event logged locally", Toast.LENGTH_LONG).show();
                                } else if (lang.equals("Nederlands")) {
                                    Toast.makeText(getBaseContext(), "Geen internetverbinding beschikbaar. Gebeurtenis lokaal geregistreerd", Toast.LENGTH_LONG).show();
                                }
                            }

                            if(longitude==0.0 && latitude==0.0)    //Informing user if there was no GPS signal
                            {
                                if (lang.equals("Ελληνικά")) {
                                    Toast.makeText(getBaseContext(), "Δεν ήταν δυνατή η καταγραφή της τοποθεσίας GPS καθώς δεν ήταν διαθέσιμη", Toast.LENGTH_LONG).show();
                                } else if (lang.equals("English")) {
                                    Toast.makeText(getBaseContext(), "Unable to log GPS Location as it was not available", Toast.LENGTH_LONG).show();
                                } else if (lang.equals("Nederlands")) {
                                    Toast.makeText(getBaseContext(), "Kan gps-locatie niet vastleggen omdat deze niet beschikbaar was", Toast.LENGTH_LONG).show();
                                }
                            }

                            boolean insert = db.insertFallEvent(Login.unamewel, "Fall Event Abort", longitude, latitude);  //Logging into SQLite
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
                                    Toast.makeText(getApplicationContext(),"Logboekregistratie mislukt",Toast.LENGTH_SHORT).show();
                                }
                            }
                            timer_enabled = 1;
                            counter++;  // In order for not to execute those functions multiple times (like spam the database with abort insertions)
                        }
                        else if (smsSENTflag)     //False Alarm SMS
                        {
                            timer.cancel();
                            if(lang.equals("Ελληνικά"))
                            {
                                alertbutton.setText("Εσταλη SMS για Λαθος Συναγερμο!");
                            }
                            else if(lang.equals("English"))
                            {
                                alertbutton.setText("False Alarm SMS Sent!");
                            }
                            else if(lang.equals("Nederlands"))
                            {
                                alertbutton.setText("Vals alarm-sms verzonden!");
                            }

                            if(isNetworkAvailable()) {       //Logging into firebase
                                fallsFirebase.setUser(Login.unamewel);
                                fallsFirebase.setLatitude(latitude);
                                fallsFirebase.setLongitude(longitude);
                                fallsFirebase.setTimestamp(getDateTime());
                                fallsFirebase.setEventType("Fall Event Abort + False Alarm SMS");
                                reff.child(String.valueOf(maxid + 1)).setValue(fallsFirebase);
                            }
                            else{
                                if (lang.equals("Ελληνικά")) {
                                    Toast.makeText(getBaseContext(), "Δεν υπάρχει σύνδεση στο Διαδίκτυο. Το συμβάν καταγράφηκε τοπικά", Toast.LENGTH_SHORT).show();
                                } else if (lang.equals("English")) {
                                    Toast.makeText(getBaseContext(), "No internet connection available. Event logged locally", Toast.LENGTH_SHORT).show();
                                } else if (lang.equals("Nederlands")) {
                                    Toast.makeText(getBaseContext(), "Geen internetverbinding beschikbaar. Gebeurtenis lokaal geregistreerd", Toast.LENGTH_SHORT).show();
                                }
                            }

                            boolean insert = db.insertFallEvent(Login.unamewel, "Fall Event Abort + False Alarm SMS", longitude, latitude); //Logging into SQLite
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
                                    Toast.makeText(getApplicationContext(),"Logboekregistratie mislukt",Toast.LENGTH_SHORT).show();
                                }
                            }
                            timer_enabled = 1;
                            if(lang.equals("Ελληνικά"))          //False Alarm SMS
                            {
                                String sms_msg = "Λάθος συναγερμός! Συγγνώμη για την ταλαιπωρία.";
                                try {
                                    SmsManager smsManager = SmsManager.getDefault();
                                    smsManager.sendTextMessage(sms_number, null, sms_msg, null, null);
                                    if(longitude!=0 && latitude!=0){
                                    Toast.makeText(getApplicationContext(), "Το μήνυμα για λάθος συναγερμό εστάλη επιτυχώς",
                                            Toast.LENGTH_LONG).show();}
                                    else{
                                        Toast.makeText(getApplicationContext(), "Το μήνυμα για λάθος συναγερμό εστάλη, αλλά η τοποθεσία GPS δεν είναι διαθέσιμη",
                                                Toast.LENGTH_LONG).show();
                                    }
                                } catch (Exception ex) {
                                    Toast.makeText(getApplicationContext(), ex.getMessage().toString(),
                                            Toast.LENGTH_LONG).show();
                                    ex.printStackTrace();
                                }
                            }
                            else if(lang.equals("English"))
                            {
                                String sms_msg = "False Alarm! Sorry for the inconvenience.";
                                try {
                                    SmsManager smsManager = SmsManager.getDefault();
                                    smsManager.sendTextMessage(sms_number, null, sms_msg, null, null);
                                    if(longitude!=0 && latitude!=0) {
                                        Toast.makeText(getApplicationContext(), "Message for False Alarm Successfully Sent",
                                                Toast.LENGTH_LONG).show();
                                    }
                                    else{
                                        Toast.makeText(getApplicationContext(), "Message for False Alarm Sent, but GPS Location not available",
                                                Toast.LENGTH_LONG).show();
                                    }
                                } catch (Exception ex) {
                                    Toast.makeText(getApplicationContext(), ex.getMessage().toString(),
                                            Toast.LENGTH_LONG).show();
                                    ex.printStackTrace();
                                }
                            }
                            else if(lang.equals("Nederlands"))
                            {
                                String sms_msg = "Vals alarm! Excuses voor het ongemak.";
                                try {
                                    SmsManager smsManager = SmsManager.getDefault();
                                    smsManager.sendTextMessage(sms_number, null, sms_msg, null, null);
                                    if(longitude!=0 && latitude!=0) {
                                        Toast.makeText(getApplicationContext(), "Bericht voor vals alarm verzonden",
                                                Toast.LENGTH_LONG).show();
                                    }
                                    else{
                                        Toast.makeText(getApplicationContext(), "Bericht voor vals alarm verzonden, maar gps-locatie niet beschikbaar",
                                                Toast.LENGTH_LONG).show();
                                    }
                                } catch (Exception ex) {
                                    Toast.makeText(getApplicationContext(), ex.getMessage().toString(),
                                            Toast.LENGTH_LONG).show();
                                    ex.printStackTrace();
                                }
                            }

                            smsSENTflag = false;
                            flag = true; // In order not to change the button again (not very important)
                        }
                    }
                });
            }

            public void onFinish() {           //Successful SMS send
                if(lang.equals("Ελληνικά"))
                {
                    alertbutton.setText("Η ειδοποιηση SMS εσταλη"); // Toast message ean to SMS stalei
                }
                else if(lang.equals("English"))
                {
                    alertbutton.setText("SMS Alert Sent"); // Toast message ean to SMS stalei
                }
                else if(lang.equals("Nederlands"))
                {
                    alertbutton.setText("SMS-waarschuwing verzonden"); // Toast message ean to SMS stalei
                }
                smsSENTflag = true;

                if(isNetworkAvailable()) {        //Logging into firebase
                    fallsFirebase.setUser(Login.unamewel);
                    fallsFirebase.setLatitude(latitude);
                    fallsFirebase.setLongitude(longitude);
                    fallsFirebase.setTimestamp(getDateTime());
                    fallsFirebase.setEventType("Fall Event + SMS");
                    reff.child(String.valueOf(maxid + 1)).setValue(fallsFirebase);
                }
                else{
                    if (lang.equals("Ελληνικά")) {
                        Toast.makeText(getBaseContext(), "Δεν υπάρχει σύνδεση στο Διαδίκτυο. Το συμβάν καταγράφηκε τοπικά", Toast.LENGTH_SHORT).show();
                    } else if (lang.equals("English")) {
                        Toast.makeText(getBaseContext(), "No internet connection available. Event logged locally", Toast.LENGTH_SHORT).show();
                    } else if (lang.equals("Nederlands")) {
                        Toast.makeText(getBaseContext(), "Geen internetverbinding beschikbaar. Gebeurtenis lokaal geregistreerd", Toast.LENGTH_SHORT).show();
                    }
                }

                boolean insert = db.insertFallEvent(Login.unamewel, "Fall Event + SMS", longitude, latitude);  //Logging into SQLite
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
                        Toast.makeText(getApplicationContext(),"Logboekregistratie mislukt",Toast.LENGTH_SHORT).show();
                    }
                }

                if(lang.equals("Ελληνικά"))                   //Similar logic as earlier
                {
                    String sms_msg = "";
                    if(latitude!=0.0 && longitude!=0.0) {
                        sms_msg="SOS! Epesa edw: "+"https://www.google.com/maps/place/"+latitude+","+longitude+"/"+" stis "+getDateTime();
                    }
                    else
                    {
                        sms_msg = "SOS! Epesa stis "+getDateTime()+" alla den exw shma GPS!";
                    }
                    try {                                                                        // ^ Rithmisi tou mhnymatos tou SMS pou tha dei o paralhpths (mazi me thn topothesia)
                        SmsManager smsManager = SmsManager.getDefault();
                        smsManager.sendTextMessage(sms_number, null, sms_msg, null, null);
                        if(latitude!=0.0 && longitude!=0.0) {
                            Toast.makeText(getApplicationContext(), "Το μήνυμα εστάλη επιτυχώς",      // Toast message gia epityxh apostolh
                                    Toast.LENGTH_LONG).show();
                        }
                        else{
                            Toast.makeText(getApplicationContext(), "Το μήνυμα εστάλη, αλλά η τοποθεσία GPS δεν είναι διαθέσιμη",
                                    Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception ex) {
                        Toast.makeText(getApplicationContext(), ex.getMessage().toString(),
                                Toast.LENGTH_LONG).show();
                        ex.printStackTrace();
                    }
                }
                else if(lang.equals("English"))
                {
                    String sms_msg = "";
                    if(latitude!=0.0 && longitude!=0.0) {
                        sms_msg="SOS! I fell down here: "+"https://www.google.com/maps/place/"+latitude+","+longitude+"/"+" at "+getDateTime();
                    }
                    else
                    {
                        sms_msg = "SOS! I fell down at "+getDateTime()+" but I don't have GPS signal!";
                    }
                    try {                                                                        // ^ Rithmisi tou mhnymatos tou SMS pou tha dei o paralhpths (mazi me thn topothesia)
                        SmsManager smsManager = SmsManager.getDefault();
                        smsManager.sendTextMessage(sms_number, null, sms_msg, null, null);
                        if(latitude!=0.0 && longitude!=0.0) {
                            Toast.makeText(getApplicationContext(), "Message Successfully Sent",      // Toast message gia epityxh apostolh
                                    Toast.LENGTH_LONG).show();
                        }
                        else{
                            Toast.makeText(getApplicationContext(), "Message Sent, but GPS Location not available",
                                    Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception ex) {
                        Toast.makeText(getApplicationContext(), ex.getMessage().toString(),
                                Toast.LENGTH_LONG).show();
                        ex.printStackTrace();
                    }
                }
                else if(lang.equals("Nederlands"))
                {
                    String sms_msg = "";
                    if(latitude!=0.0 && longitude!=0.0) {
                        sms_msg="SOS! Ik viel hier neer: "+"https://www.google.com/maps/place/"+latitude+","+longitude+"/"+" om "+getDateTime();
                    }
                    else
                    {
                        sms_msg = "SOS! Ik viel neer om "+getDateTime()+" maar ik heb geen GPS-signaal!";
                    }
                    try {                                                                        // ^ Rithmisi tou mhnymatos tou SMS pou tha dei o paralhpths (mazi me thn topothesia)
                        SmsManager smsManager = SmsManager.getDefault();
                        smsManager.sendTextMessage(sms_number, null, sms_msg, null, null);
                        if(latitude!=0.0 && longitude!=0.0) {
                            Toast.makeText(getApplicationContext(), "Bericht succesvol verzonden",      // Toast message gia epityxh apostolh
                                    Toast.LENGTH_LONG).show();
                        }
                        else{
                            Toast.makeText(getApplicationContext(), "Bericht verzonden, maar gps-locatie niet beschikbaar",
                                    Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception ex) {
                        Toast.makeText(getApplicationContext(), ex.getMessage().toString(),
                                Toast.LENGTH_LONG).show();
                        ex.printStackTrace();
                    }
                }

                initialize_phone();
            }

        }.start();
    }

    LocationListener locationListenerGps = new LocationListener() {      //Location Listener for GPS
        public void onLocationChanged(Location location) {
            timer2.cancel();
            latitude =location.getLatitude();
            longitude = location.getLongitude();
            //locationManager.removeUpdates(this);
            locationManager.removeUpdates(locationListenerNetwork);
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    LocationListener locationListenerNetwork = new LocationListener() {    //Location listener for Network location
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

    class GetLastLocation extends TimerTask {         // Getting last known location after 20 seconds if both GPS and Network location failed
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

    public void speakout()    // Text to Speech
    {
        Log.e("Speech", "In speech module");
        t1.speak("A fall has been detected! Please cancel the SMS alert if you are OK.", TextToSpeech.QUEUE_FLUSH, null);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

        } else {
            t1.speak("A fall has been detected! Please cancel the SMS alert if you are OK.", TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    @Override
    protected void onResume() {   //Checking the language again, network and restarting location listeners
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
        doStuff();
    }

    @Override
    protected void onPause() {    //Stopping the GPS listener
        super.onPause();
        //locationManager.removeUpdates(this);  //to stop the location manager when this activity isn't on foreground
        locationManager.removeUpdates(locationListenerGps);
    }

    public void initialize_phone()        // Checking the state of the phone and if it's on alert mode and it's ringing, call will be accepeted automatically
    {                                     // If it's already in a call, it will be set to speaker mode automatically

        TelephonyManager telephonyManager =
                (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);

        PhoneStateListener callStateListener = new PhoneStateListener() {
            public void onCallStateChanged(int state, String incomingNumber)
            {
                if(state==TelephonyManager.CALL_STATE_RINGING){
                    if(lang.equals("Ελληνικά"))
                    {
                        Toast.makeText(getApplicationContext(),"Το τηλέφωνο χτυπά αυτήν τη στιγμή",
                                Toast.LENGTH_LONG).show();
                    }
                    else if(lang.equals("English"))
                    {
                        Toast.makeText(getApplicationContext(),"Phone Is Currently Ringing",
                                Toast.LENGTH_LONG).show();
                    }
                    else if(lang.equals("Nederlands"))
                    {
                        Toast.makeText(getApplicationContext(),"De telefoon gaat momenteel over",
                                Toast.LENGTH_LONG).show();
                    }

                }
                if(state==TelephonyManager.CALL_STATE_OFFHOOK){
                    if(lang.equals("Ελληνικά"))
                    {
                        Toast.makeText(getApplicationContext(),"Το τηλέφωνο βρίσκεται σε κλήση αυτήν τη στιγμή",
                                Toast.LENGTH_LONG).show();
                    }
                    else if(lang.equals("English"))
                    {
                        Toast.makeText(getApplicationContext(),"Phone Is Currently In a Call",
                        Toast.LENGTH_LONG).show();
                    }
                    else if(lang.equals("Nederlands"))
                    {
                        Toast.makeText(getApplicationContext(),"Telefoon is momenteel in gesprek",
                                Toast.LENGTH_LONG).show();
                    }


                    myAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                    myAudioManager.setMode(AudioManager.MODE_IN_CALL);
                    myAudioManager.setSpeakerphoneOn(true);


                }
            }
        };
        telephonyManager.listen(callStateListener,PhoneStateListener.LISTEN_CALL_STATE);
    }

    @Override
    public void onBackPressed() {         //Preventing the user from exiting if the alarm is still ON without getting Aborted
        if(timer_enabled==1)
        {
            super.onBackPressed();
        }
        else {
            if(lang.equals("Ελληνικά"))
            {
                Toast.makeText(getApplicationContext(),"Για να επιστρέψετε στο κύριο μενού, πρέπει να ακυρώσετε την αποστολή ειδοποίησης SMS!",Toast.LENGTH_LONG).show();
            }
            else if(lang.equals("English"))
            {
                Toast.makeText(getApplicationContext(),"To Get Back to Main Menu you Must Cancel the SMS Alert Sending!",Toast.LENGTH_LONG).show();
            }
            else if(lang.equals("Nederlands"))
            {
                Toast.makeText(getApplicationContext(),"Om terug te keren naar het hoofdmenu, moet u het verzenden van een sms-waarschuwing annuleren!",Toast.LENGTH_LONG).show();
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void doStuff(){                    //Location Listeners initialization
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

    private void languageCheck()        //UI Language Management
    {
        if(lang.equals("Ελληνικά"))
        {
            tv1.setText ("ΕΝΤΟΠΙΣΤΗΚΕ ένα συμβάν ΠΤΩΣΗΣ!");
            tv2.setText ("Μια σχετική ειδοποίηση SMS θα σταλεί στην προκαθορισμένη επαφή σας μετά τη λήξη της αντίστροφης μέτρησης (συμπεριλαμβανομένης της τοποθεσίας σας και της χρονικής σήμανσης του συμβάντος):");
            tv3.setText ("Πατήστε το παρακάτω κουμπί για ματαίωση:");
            alertbutton.setText ("Ματαιωση");
        }
        else if(lang.equals("English"))
        {
            tv1.setText("FALL event has been DETECTED!");
            tv2.setText("A related SMS alert will be sent to your preset contact  after the timer runs out (including your location and the event's timestamp):");
            tv3.setText("Tap the below button to abort:");
            alertbutton.setText("Abort");
        }
        else if(lang.equals("Nederlands"))
        {
            tv1.setText ("Er is een valgebeurtenis gedetecteerd!");
            tv2.setText ("Er wordt een gerelateerde sms-waarschuwing verzonden naar uw vooraf ingestelde contactpersoon nadat het aftellen is afgelopen (inclusief uw locatie en het tijdstempel van het evenement):");
            tv3.setText ("Tik op de onderstaande knop om af te breken:");
            alertbutton.setText ("Afbreken");
        }
    }

    private boolean isNetworkAvailable() {       //Checking if device is connected to the internet
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
