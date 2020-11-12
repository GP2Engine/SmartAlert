package com.example.smartalert;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class SettingsMenu extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    Button b1,b2,b3;
    Spinner rs1, rs2;
    TextView t1,t2,t3,t4,t5;

    String contactNumber,chosenRingtone,response_mode,language,lang;
    long response_time;
    Ringtone r;
    int count;
    TextView sampletext,setting_heading;
    final long[] spinnervalues={60000,30000,15000};
    public static final String MyPREFERENCES = "MyPrefs" ;
    SharedPreferences sharedpreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_menu);

        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        lang=sharedpreferences.getString("Language", "");
        if (lang.equals("null") || lang.isEmpty())
        {
            lang = "English";
        }

        b1=(Button)findViewById(R.id.chosContact);
        b2 =(Button)findViewById(R.id.chosSound);
        b3 =(Button)findViewById(R.id.saveSettings);
        t1 =(TextView)findViewById(R.id.setmen1);
        t2 =(TextView)findViewById(R.id.setmen2);
        t3 =(TextView)findViewById(R.id.setmen3);
        t4 =(TextView)findViewById(R.id.setmen4);
        t5 =(TextView)findViewById(R.id.setmen5);
        rs1=(Spinner)findViewById(R.id.response_spinner);
        rs2=(Spinner)findViewById((R.id.response_spinner2));

        languageCheck();

        b1.setOnClickListener(new View.OnClickListener() {            //Choose Contact Button
            @Override
            public void onClick(View v) {       //Recipient Contact selection for the SMS Alerts
                Uri uri = Uri.parse("content://contacts");
                Intent intent = new Intent(Intent.ACTION_PICK, uri);
                intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
                startActivityForResult(intent, 4);
            }
        });

        b2.setOnClickListener(new View.OnClickListener() {          //Choose Sound Button
            @Override
            public void onClick(View v) {   //Ringtone for both the Fall and the Earthquake Alerts
                Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_RINGTONE);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Tone");
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, (Uri) null);
                startActivityForResult(intent, 5);
            }
        });

        b3.setOnClickListener(new View.OnClickListener() {        //Save Button
            @Override
            public void onClick(View v) {   //Saving the Settings if all required options are selected
                boolean flag;
                if ((chosenRingtone == null) ||(contactNumber == null) || (response_mode == null) || (language == null)) {
                    if(lang.equals("Ελληνικά"))
                    {
                        Toast.makeText(getApplicationContext(), "Παρακαλώ συμπληρώστε όλες τις ρυθμίσεις!", Toast.LENGTH_LONG).show();
                    }
                    else if(lang.equals("English"))
                    {
                        Toast.makeText(getApplicationContext(), "Please Fill All Settings!", Toast.LENGTH_LONG).show();
                    }
                    else if(lang.equals("Nederlands"))
                    {
                        Toast.makeText(getApplicationContext(), "Vul alle instellingen in!", Toast.LENGTH_LONG).show();
                    }

                } else {
                    SharedPreferences.Editor editor = sharedpreferences.edit();  //Saving the settings to SharedPreferences
                    editor.clear();
                    editor.putString("Contact_number",contactNumber);
                    editor.putString("Ringtone", chosenRingtone);
                    editor.putLong("Response",response_time);
                    editor.putString("Language",language);
                    flag=editor.commit();
                    if(flag==true)
                    {
                        if(lang.equals("Ελληνικά"))
                        {
                            Toast.makeText(getApplicationContext(),"Οι Ρυθμίσεις Αποθηκεύτηκαν",Toast.LENGTH_LONG).show();
                        }
                        else if(lang.equals("English"))
                        {
                            Toast.makeText(getApplicationContext(),"Settings Saved",Toast.LENGTH_LONG).show();
                        }
                        else if(lang.equals("Nederlands"))
                        {
                            Toast.makeText(getApplicationContext(),"Instellingen Opgeslagen",Toast.LENGTH_LONG).show();
                        }
                        Intent i = new Intent(SettingsMenu.this,Menu.class); //going to the Menu activity
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                        finish();
                    }
                }

            }
        });

        rs1.setOnItemSelectedListener(this);
        List<String> categories = new ArrayList<String>();   // Filling the response time dropdown
        if(lang.equals("Ελληνικά"))
        {
            categories.add("1 λεπτό");
            categories.add("30 δευτερόλεπτα");
            categories.add("15 δευτερόλεπτα");
        }
        else if(lang.equals("English"))
        {
            categories.add("1 minute");
            categories.add("30 seconds");
            categories.add("15 seconds");
        }
        else if(lang.equals("Nederlands"))
        {
            categories.add("1 minuut");
            categories.add("30 seconden");
            categories.add("15 seconden");
        }

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);

        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        rs1.setAdapter(dataAdapter);

        rs2.setOnItemSelectedListener(this);             //Filling the dropdown for the available languages
        List<String> categories2 = new ArrayList<String>();
        categories2.add("English");
        categories2.add("Ελληνικά");
        categories2.add("Nederlands");

        ArrayAdapter<String> dataAdapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories2);

        dataAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        rs2.setAdapter(dataAdapter2);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {   //Managing the results we get from the contact and ringtone select activities

        if (resultCode == Activity.RESULT_OK && requestCode == 5)
        {
            Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);

            if (uri != null)
            {
                this.chosenRingtone = uri.toString();
            }
            else
            {
                this.chosenRingtone = null;
            }
            Toast.makeText(this,chosenRingtone,Toast.LENGTH_LONG).show();
        }
        else if (resultCode == Activity.RESULT_OK && requestCode == 4)
        {
            Uri phone_uri = data.getData();                      // Entopismos stoixeiwn epafhs paralhpth SMS
            String[] projection = { ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME };

            Cursor cursor = getContentResolver().query(phone_uri, projection,
                    null, null, null);
            cursor.moveToFirst();

            int numberColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            contactNumber = cursor.getString(numberColumnIndex);

            int nameColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            String name = cursor.getString(nameColumnIndex);
        }
        else
        {
            if(lang.equals("Ελληνικά"))
            {
                Toast.makeText(this,"Κανένα Αποτέλεσμα",Toast.LENGTH_LONG).show();
            }
            else if(lang.equals("English"))
            {
                Toast.makeText(this,"No Results",Toast.LENGTH_LONG).show();
            }
            else if(lang.equals("Nederlands"))
            {
                Toast.makeText(this,"Geen Resultaten",Toast.LENGTH_LONG).show();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {  //For the dropdown spinners (response time and language)
        Spinner spinner = (Spinner) parent;
        if(spinner.getId() == R.id.response_spinner) {
            response_mode = rs1.getItemAtPosition(position).toString();
            response_time = spinnervalues[position];
        }
        else if(spinner.getId() == R.id.response_spinner2)
        {
            language = rs2.getSelectedItem().toString();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        languageCheck();  //Checking the language
    }


    private void languageCheck()  //UI Language Management
    {
        if(lang.equals("Ελληνικά"))
        {
            t1.setText ("Ρυθμίσεις");
            t2.setText ("Επιλέξτε την επαφή παραλήπτη των ειδοποιήσεων SMS:");
            t3.setText ("Επιλέξτε τον ήχο ειδοποίησης:");
            t4.setText ("Επιλέξτε τον διαθέσιμο χρόνο απόκρισης του χρήστη:");
            t5.setText ("Επιλέξτε τη γλώσσα της εφαρμογής:");
            b1.setText ("Επιλογη Επαφης");
            b2.setText ("Επιλογη Ηχου");
            b3.setText ("Αποθηκευση Ρυθμισεων");
        }
        else if(lang.equals("English"))
        {
            t1.setText("Settings");
            t2.setText("Select the SMS Alerts recipient contact:");
            t3.setText("Select the Alert Sound:");
            t4.setText("Select the user's available response time:");
            t5.setText("Select the app's language:");
            b1.setText("Choose Contact");
            b2.setText("Choose Sound");
            b3.setText("Save Settings");
        }
        else if(lang.equals("Nederlands"))
        {
            t1.setText ("Instellingen");
            t2.setText ("Selecteer het contact van de ontvanger van de SMS Alerts:");
            t3.setText ("Selecteer het waarschuwingsgeluid:");
            t4.setText ("Selecteer de beschikbare reactietijd van de gebruiker:");
            t5.setText ("Selecteer de taal van de app:");
            b1.setText ("Kies contactpersoon");
            b2.setText ("Kies geluid");
            b3.setText ("Instellingen opslaan");
        }
    }
}
