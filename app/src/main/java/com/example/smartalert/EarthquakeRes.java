package com.example.smartalert;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.TextView;

public class EarthquakeRes extends AppCompatActivity {

    dbHelper db;
    TextView t1,t2,t3,t4,t5, tv1,tv2,tv3,tv4,tv5,tv6;
    SharedPreferences sharedpreferences;
    public static final String MyPREFERENCES = "MyPrefs" ;
    String lang;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_earthquake_res);

        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        lang=sharedpreferences.getString("Language", "");
        if (lang.equals("null") || lang.isEmpty())
        {
            lang = "English";
        }

        db = new dbHelper(this);
        t1=(TextView)findViewById(R.id.userres3);
        t2=(TextView)findViewById(R.id.eventres3);
        t3=(TextView)findViewById(R.id.lonres3);
        t4=(TextView)findViewById(R.id.latres3);
        t5=(TextView)findViewById(R.id.timeres3);

        tv1=(TextView)findViewById(R.id.earthres1);
        tv2=(TextView)findViewById(R.id.earthres2);
        tv3=(TextView)findViewById(R.id.earthres3);
        tv4=(TextView)findViewById(R.id.earthres4);
        tv5=(TextView)findViewById(R.id.earthres5);
        tv6=(TextView)findViewById(R.id.earthres6);

        languageCheck();

        String timestamp = getIntent().getStringExtra("transfer"); //getting the timestamp from the previous activity

        Cursor c = db.getUserEarthquakesByTime(Login.unamewel, timestamp);
        c.moveToNext();
        t1.setText(c.getString(c.getColumnIndex("uname")));               //Setting the result details of a specific timestamp earthquake
        if(lang.equals("Ελληνικά"))
        {
            if (c.getString(c.getColumnIndex("event_type")).equals("Earthquake Sense Event"))
            {
                t2.setText("Συμβάν Εντοπισμού Σεισμού");
            }
            else if (c.getString(c.getColumnIndex("event_type")).equals("Earthquake Confirmed Event"))
            {
                t2.setText("Συμβάν Επιβεβαίωσης Σεισμού");
            }
        }
        else if(lang.equals("English"))
        {
            t2.setText(c.getString(c.getColumnIndex("event_type")));
        }
        else if(lang.equals("Nederlands"))
        {
            if (c.getString(c.getColumnIndex("event_type")).equals("Earthquake Sense Event"))
            {
                t2.setText("Aardbeving Detectie-gebeurtenis");
            }
            else if (c.getString(c.getColumnIndex("event_type")).equals("Earthquake Confirmed Event"))
            {
                t2.setText("Event van Bevestigde Aardbeving");
            }
        }
        if(c.getString(c.getColumnIndex("longitude")).equals("0") && c.getString(c.getColumnIndex("latitude")).equals("0"))
        {
            t3.setText("- - . - -");
            t4.setText("- - . - -");
        }
        else{
            t3.setText(c.getString(c.getColumnIndex("longitude")));
            t4.setText(c.getString(c.getColumnIndex("latitude")));
        }

        t5.setText(c.getString(c.getColumnIndex("timestamp")));
    }

    @Override
    protected void onResume() {  //Checking the language
        super.onResume();
        languageCheck();
    }


    private void languageCheck()   //UI Language Management
    {
        if(lang.equals("Ελληνικά"))
        {
            tv1.setText ("Λεπτομέρειες εντοπισμού σεισμού:");
            tv2.setText ("Χρήστης:");
            tv3.setText ("Τύπος συμβάντος:");
            tv4.setText ("Γεωγραφικό μήκος:");
            tv5.setText ("Γεωγραφικό πλάτος:");
            tv6.setText ("Χρονική σήμανση συμβάντος:");
        }
        else if(lang.equals("English"))
        {
            tv1.setText("Earthquake Sense Details:");
            tv2.setText("User:");
            tv3.setText("Event Type:");
            tv4.setText("Geographical Longitude:");
            tv5.setText("Geographical Latitude:");
            tv6.setText("Event's Timestamp:");
        }
        else if(lang.equals("Nederlands"))
        {
            tv1.setText ("Details van aardbevings detectie:");
            tv2.setText ("Gebruiker:");
            tv3.setText ("Gebeurtenistype:");
            tv4.setText ("Geografische lengtegraad:");
            tv5.setText ("Geografische breedte:");
            tv6.setText ("Tijdstempel van evenement:");
        }
    }
}
