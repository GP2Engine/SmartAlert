package com.example.smartalert;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.TextView;

public class FallRes extends AppCompatActivity {

    dbHelper db;
    TextView t1,t2,t3,t4,t5, tv1,tv2,tv3,tv4,tv5,tv6;
    SharedPreferences sharedpreferences;
    public static final String MyPREFERENCES = "MyPrefs" ;
    String lang;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fall_res);

        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        lang=sharedpreferences.getString("Language", "");
        if (lang.equals("null") || lang.isEmpty())
        {
            lang = "English";
        }

        db = new dbHelper(this);
        t1=(TextView)findViewById(R.id.userres);
        t2=(TextView)findViewById(R.id.eventres);
        t3=(TextView)findViewById(R.id.lonres);
        t4=(TextView)findViewById(R.id.latres);
        t5=(TextView)findViewById(R.id.timeres);

        tv1=(TextView)findViewById(R.id.fallres1);
        tv2=(TextView)findViewById(R.id.fallres2);
        tv3=(TextView)findViewById(R.id.fallres3);
        tv4=(TextView)findViewById(R.id.fallres4);
        tv5=(TextView)findViewById(R.id.fallres5);
        tv6=(TextView)findViewById(R.id.fallres6);

        languageCheck();
        String timestamp = getIntent().getStringExtra("transfer"); //getting the timestamp from the previous activity

        Cursor c = db.getUserFallsByTime(Login.unamewel, timestamp);     //Getting the fall logs of a specific user
        c.moveToNext();
        t1.setText(c.getString(c.getColumnIndex("uname")));
        if(lang.equals("Ελληνικά"))
        {
            if (c.getString(c.getColumnIndex("event_type")).equals("Fall Event Abort"))
            {
                t2.setText("Ακύρωση Συμβάντος Πτώσης");
            }
            else if (c.getString(c.getColumnIndex("event_type")).equals("Fall Event Abort + False Alarm SMS"))
            {
                t2.setText("Ακύρωση συμβάντος πτώσης + SMS Λάθος Συναγερμού");
            }
            else if (c.getString(c.getColumnIndex("event_type")).equals("Fall Event + SMS"))
            {
                t2.setText("Συμβάν Πτώσης + SMS");
            }
        }
        else if(lang.equals("English"))
        {
            t2.setText(c.getString(c.getColumnIndex("event_type")));
        }
        else if(lang.equals("Nederlands"))
        {
            if (c.getString(c.getColumnIndex("event_type")).equals("Fall Event Abort"))
            {
                t2.setText("Afbreken herfst gebeurtenis");
            }
            else if (c.getString(c.getColumnIndex("event_type")).equals("Fall Event Abort + False Alarm SMS"))
            {
                t2.setText("Afbreken herfst gebeurtenis + Vals alarm-sms");
            }
            else if (c.getString(c.getColumnIndex("event_type")).equals("Fall Event + SMS"))
            {
                t2.setText("Herfst evenement + SMS");
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
    protected void onResume() {  //Language Check
        super.onResume();
        languageCheck();
    }


    private void languageCheck()   //UI Language Management
    {
        if(lang.equals("Ελληνικά"))
        {
            tv1.setText ("Λεπτομέρειες συμβάντος πτώσης:");
            tv2.setText ("Χρήστης:");
            tv3.setText ("Τύπος συμβάντος:");
            tv4.setText ("Γεωγραφικό μήκος:");
            tv5.setText ("Γεωγραφικό πλάτος:");
            tv6.setText ("Χρονική σήμανση συμβάντος:");
        }
        else if(lang.equals("English"))
        {
            tv1.setText("Fall Event Details:");
            tv2.setText("User:");
            tv3.setText("Event Type:");
            tv4.setText("Geographical Longitude:");
            tv5.setText("Geographical Latitude:");
            tv6.setText("Event's Timestamp:");
        }
        else if(lang.equals("Nederlands"))
        {
            tv1.setText ("Details herfst gebeurtenis:");
            tv2.setText ("Gebruiker:");
            tv3.setText ("Gebeurtenistype:");
            tv4.setText ("Geografische lengtegraad:");
            tv5.setText ("Geografische breedte:");
            tv6.setText ("Tijdstempel van evenement:");
        }
    }
}
