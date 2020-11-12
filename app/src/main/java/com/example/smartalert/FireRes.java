package com.example.smartalert;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class FireRes extends AppCompatActivity {

    dbHelper db;
    TextView t1,t2,t3,t4,t5, tv1,tv2,tv3,tv4,tv5,tv6,tv7;
    SharedPreferences sharedpreferences;
    public static final String MyPREFERENCES = "MyPrefs" ;
    String lang;
    private ImageView selectedImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fire_res);

        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        lang=sharedpreferences.getString("Language", "");
        if (lang.equals("null") || lang.isEmpty())
        {
            lang = "English";
        }

        db = new dbHelper(this);
        t1=(TextView)findViewById(R.id.userres2);
        t2=(TextView)findViewById(R.id.eventres2);
        t3=(TextView)findViewById(R.id.lonres2);
        t4=(TextView)findViewById(R.id.latres2);
        t5=(TextView)findViewById(R.id.timeres2);

        tv1=(TextView)findViewById(R.id.fireres1);
        tv2=(TextView)findViewById(R.id.fireres2);
        tv3=(TextView)findViewById(R.id.fireres3);
        tv4=(TextView)findViewById(R.id.fireres4);
        tv5=(TextView)findViewById(R.id.fireres5);
        tv6=(TextView)findViewById(R.id.fireres6);
        tv7=(TextView)findViewById(R.id.fireres7);

        languageCheck();
        selectedImageView = (ImageView) findViewById(R.id.selected_image2);
        String timestamp = getIntent().getStringExtra("transfer"); //getting the timestamp from the previous activity

        Cursor c = db.getUserFiresByTime(Login.unamewel, timestamp);
        c.moveToNext();
        t1.setText(c.getString(c.getColumnIndex("uname")));       //Setting the result details of a specific timestamp fire report
        if(lang.equals("Ελληνικά"))
        {
            if (c.getString(c.getColumnIndex("event_type")).equals("Fire Event + SMS"))
            {
                t2.setText("Συμβάν Φωτιάς + SMS");
            }
        }
        else if(lang.equals("English"))
        {
            t2.setText(c.getString(c.getColumnIndex("event_type")));
        }
        else if(lang.equals("Nederlands"))
        {
            if (c.getString(c.getColumnIndex("event_type")).equals("Fire Event + SMS"))
            {
                t2.setText("Brand Gebeurtenis + SMS");
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

        byte [] byteArr = c.getBlob(c.getColumnIndex("image"));
        Bitmap bitmap = BitmapFactory.decodeByteArray(byteArr, 0 ,byteArr.length);
        selectedImageView.setImageBitmap(bitmap);
    }

    @Override
    protected void onResume() {   //Language Check
        super.onResume();
        languageCheck();
    }


    private void languageCheck()     //UI Language Management
    {
        if(lang.equals("Ελληνικά"))
        {
            tv1.setText ("Λεπτομέρειες αναφοράς πυρκαγιάς:");
            tv2.setText ("Χρήστης:");
            tv3.setText ("Τύπος συμβάντος:");
            tv4.setText ("Γεωγραφικό μήκος:");
            tv5.setText ("Γεωγραφικό πλάτος:");
            tv6.setText ("Χρονική σήμανση συμβάντος:");
            tv7.setText ("Εικόνα αναφοράς:");
        }
        else if(lang.equals("English"))
        {
            tv1.setText("Fire Report Details:");
            tv2.setText("User:");
            tv3.setText("Event Type:");
            tv4.setText("Geographical Longitude:");
            tv5.setText("Geographical Latitude:");
            tv6.setText("Event's Timestamp:");
            tv7.setText("Reported Image:");
        }
        else if(lang.equals("Nederlands"))
        {
            tv1.setText ("Brandrapportdetails:");
            tv2.setText ("Gebruiker:");
            tv3.setText ("Gebeurtenistype:");
            tv4.setText ("Geografische lengtegraad:");
            tv5.setText ("Geografische breedte:");
            tv6.setText ("Tijdstempel van evenement:");
            tv7.setText ("Gerapporteerde afbeelding:");
        }
    }
}
