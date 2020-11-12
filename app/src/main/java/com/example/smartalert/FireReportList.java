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

public class FireReportList extends AppCompatActivity {

    dbHelper db;
    ListView l1;
    TextView t1,t2;
    SharedPreferences sharedpreferences;
    public static final String MyPREFERENCES = "MyPrefs" ;
    String lang;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fire_report_list);

        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        lang=sharedpreferences.getString("Language", "");
        if (lang.equals("null") || lang.isEmpty())
        {
            lang = "English";
        }

        db = new dbHelper(this);
        l1=(ListView) findViewById(R.id.listviewFire);
        t1=(TextView)findViewById(R.id.firelist1);
        t2=(TextView)findViewById(R.id.firelist2);

        languageCheck();

        ArrayList<String> alist = new ArrayList<>();
        Cursor c = db.getUserFires(Login.unamewel);         //Getting the fire report logs for a specific user

        if(c.getCount() == 0){
            if(lang.equals("Ελληνικά"))
            {
                Toast.makeText(this, "Δεν βρέθηκαν αναφορές πυρκαγιάς για τον λογαριασμό σας.",Toast.LENGTH_LONG).show();
            }
            else if(lang.equals("English"))
            {
                Toast.makeText(this, "No Fire Reports found for your account.",Toast.LENGTH_LONG).show();
            }
            else if(lang.equals("Nederlands"))
            {
                Toast.makeText(this, "Geen brandrapporten gevonden voor uw account.",Toast.LENGTH_LONG).show();
            }
            Intent i = new Intent(FireReportList.this,Menu.class); //going to the menu activity
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
                    Intent i = new Intent(FireReportList.this,FireRes.class); //going to the FireRes activity
                    i.putExtra("transfer",clickinput); //sending the timestamp to the next activity so it can find the corresponding event details
                    startActivity(i);
                }
            });

        }
    }

    @Override
    protected void onResume() {  //Checking the language
        super.onResume();
        languageCheck();
    }


    private void languageCheck()  //UI Language Management
    {
        if(lang.equals("Ελληνικά"))
        {
            t1.setText("Τα logs Καταγραφών Πυρκαγιών μου:");
            t2.setText("Γρήγορη συμβουλή: Μπορείτε να πατήσετε πάνω σε κάθε χρονική σήμανση για να δείτε περισσότερες λεπτομέρειες σχετικά με κάθε αναφορά πυρκαγιάς.");
        }
        else if(lang.equals("English"))
        {
            t1.setText("My Fire Report Logs:");
            t2.setText("Quick Tip: You can tap on each timestamp to see further details about each fire report.");
        }
        else if(lang.equals("Nederlands"))
        {
            t1.setText("Mijn Brand Rapport logboeken:");
            t2.setText("Snelle tip: u kunt op elk tijdstempel tikken om meer informatie over elk brandrapport te zien.");
        }
    }
}
