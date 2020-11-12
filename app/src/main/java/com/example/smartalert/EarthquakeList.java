package com.example.smartalert;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class EarthquakeList extends AppCompatActivity {

    dbHelper db;
    ListView l1;
    TextView t1,t2;
    SharedPreferences sharedpreferences;
    public static final String MyPREFERENCES = "MyPrefs" ;
    String lang;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_earthquake_list);

        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        lang=sharedpreferences.getString("Language", "");
        if (lang.equals("null") || lang.isEmpty())
        {
            lang = "English";
        }

        db = new dbHelper(this);
        l1=(ListView) findViewById(R.id.listviewEarthquakes);
        t1=(TextView)findViewById(R.id.earthlist1);
        t2=(TextView)findViewById(R.id.earthlist2);
        languageCheck();
        ArrayList<String> alist = new ArrayList<>();
        Cursor c = db.getUserEarthquakes(Login.unamewel);

        if(c.getCount() == 0){                   //Getting the earthquake logs for the user's account
            if(lang.equals("Ελληνικά"))
            {
                Toast.makeText (this, "Δεν βρέθηκαν εντοπισμοί σεισμού για τον λογαριασμό σας.", Toast.LENGTH_LONG).show();
            }
            else if(lang.equals("English"))
            {
                Toast.makeText(this, "No Earthquake Senses found for your account.",Toast.LENGTH_LONG).show();
            }
            else if(lang.equals("Nederlands"))
            {
                Toast.makeText(this, "Geen aardbevings detecties gevonden voor uw account.",Toast.LENGTH_LONG).show();
            }
            Intent i = new Intent(EarthquakeList.this,Menu.class); //going to the menu activity
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            finish();
        }
        else
        {
            while(c.moveToNext()){
                alist.add(c.getString(c.getColumnIndex("timestamp")));
                ListAdapter listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, alist);
                l1.setAdapter(listAdapter);
            }
            l1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String clickinput = ((TextView) view).getText().toString();
                    Intent i = new Intent(EarthquakeList.this,EarthquakeRes.class); //going to the EarthquakeRes activity
                    i.putExtra("transfer",clickinput); //sending the timestamp to the next activity so it can find the corresponding event details
                    startActivity(i);
                }
            });

        }
    }

    @Override
    protected void onResume() {   //Checking the language
        super.onResume();
        languageCheck();
    }

    private void languageCheck()   //UI Language Management
    {
        if(lang.equals("Ελληνικά"))
        {
            t1.setText ("Τα logs των Εντοπισμών Σεισμών μου:");
            t2.setText ("Γρήγορη συμβουλή: Μπορείτε να πατήσετε πάνω σε κάθε χρονική σήμανση για να δείτε περισσότερες λεπτομέρειες σχετικά με κάθε συμβάν σεισμού.");
        }
        else if(lang.equals("English"))
        {
            t1.setText("My Earthquake Senses Logs:");
            t2.setText("Quick Tip: You can tap on each timestamp to see further details about each earthquake event.");
        }
        else if(lang.equals("Nederlands"))
        {
            t1.setText ("Mijn Aardbeving Detectie logboeken:");
            t2.setText ("Snelle tip: je kunt op elk tijdstempel tikken om meer details over elke aardbeving te zien.");
        }
    }
}
