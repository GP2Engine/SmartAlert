package com.example.smartalert;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    dbHelper db;
    EditText e1,e2,e3;
    Button b1,b2;
    TextView t1,t2,t3,t4,t5;

    SharedPreferences sharedpreferences;
    public static final String MyPREFERENCES = "MyPrefs" ;
    String lang;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        lang=sharedpreferences.getString("Language", "");
        if (lang.equals("null") || lang.isEmpty())
        {
            lang = "English";
        }

        db = new dbHelper(this);
        e1=(EditText)findViewById(R.id.unameL);
        e2=(EditText)findViewById(R.id.passL);
        e3=(EditText)findViewById(R.id.cpass);
        b1=(Button)findViewById(R.id.register);
        b2=(Button)findViewById(R.id.gologin);
        t1=(TextView)findViewById(R.id.main1);
        t2=(TextView)findViewById(R.id.main2);
        t3=(TextView)findViewById(R.id.main3);
        t4=(TextView)findViewById(R.id.main4);
        t5=(TextView)findViewById(R.id.main5);

        languageCheck();

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {                       //Registration Process
                String s1 = e1.getText().toString();
                String s2 = e2.getText().toString();
                String s3 = e3.getText().toString();

                if (s1.equals("")||s2.equals("")||s3.equals("")){
                    if(lang.equals("Ελληνικά"))
                    {
                        Toast.makeText(getApplicationContext(),"Συμπληρώστε τα κενά πεδία.",Toast.LENGTH_SHORT).show();
                    }
                    else if(lang.equals("English"))
                    {
                        Toast.makeText(getApplicationContext(),"Please fill the empty fields.",Toast.LENGTH_SHORT).show();
                    }
                    else if(lang.equals("Nederlands"))
                    {
                        Toast.makeText(getApplicationContext(),"Gelieve de lege velden in te vullen.",Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    if(s2.equals(s3)){
                        Boolean checkuname = db.checkuname(s1);
                        if(checkuname==true){
                            Boolean insert = db.insertUser(s1,s2);
                            if(insert==true){
                                if(lang.equals("Ελληνικά"))
                                {
                                    Toast.makeText(getApplicationContext(),"Επιτυχής Εγγραφή.",Toast.LENGTH_SHORT).show();
                                }
                                else if(lang.equals("English"))
                                {
                                    Toast.makeText(getApplicationContext(),"Registration Successful.",Toast.LENGTH_SHORT).show();
                                }
                                else if(lang.equals("Nederlands"))
                                {
                                    Toast.makeText(getApplicationContext(),"Registratie gelukt.",Toast.LENGTH_SHORT).show();
                                }
                                Intent i = new Intent(MainActivity.this,Login.class); //going to the login activity
                                startActivity(i);
                            }
                            else
                            {
                                if(lang.equals("Ελληνικά"))
                                {
                                    Toast.makeText(getApplicationContext(),"Η εγγραφή απέτυχε.",Toast.LENGTH_SHORT).show();
                                }
                                else if(lang.equals("English"))
                                {
                                    Toast.makeText(getApplicationContext(),"Registration Failed.",Toast.LENGTH_SHORT).show();
                                }
                                else if(lang.equals("Nederlands"))
                                {
                                    Toast.makeText(getApplicationContext(),"Registratie mislukt.",Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                        else{
                            if(lang.equals("Ελληνικά"))
                            {
                                Toast.makeText(getApplicationContext(),"Επιλέξτε άλλο όνομα χρήστη. Το συγκεκριμένο υπάρχει ήδη.",Toast.LENGTH_SHORT).show();
                            }
                            else if(lang.equals("English"))
                            {
                                Toast.makeText(getApplicationContext(),"Please select another username. Current one already exists.",Toast.LENGTH_SHORT).show();
                            }
                            else if(lang.equals("Nederlands"))
                            {
                                Toast.makeText(getApplicationContext(),"Selecteer een andere gebruikersnaam. Deze bestaat al.",Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                    else{
                        if(lang.equals("Ελληνικά"))
                        {
                            Toast.makeText(getApplicationContext(),"Οι κωδικοί πρόσβασης δεν ταιριάζουν.",Toast.LENGTH_SHORT).show();
                        }
                        else if(lang.equals("English"))
                        {
                            Toast.makeText(getApplicationContext(),"Passwords do not match.",Toast.LENGTH_SHORT).show();
                        }
                        else if(lang.equals("Nederlands"))
                        {
                            Toast.makeText(getApplicationContext(),"Wachtwoorden komen niet overeen.",Toast.LENGTH_SHORT).show();
                        }
                    }
                }

            }
        });

        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this,Login.class); //going to the login activity
                startActivity(i);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        languageCheck();    //Checking the Language
    }

    private void languageCheck()   //UI Language Management
    {
        if(lang.equals("Ελληνικά"))
        {
            t1.setText("Καλώς ορίσατε στην εφαρμογή SmartAlert!");
            t2.setText("Για να συνεχίσετε, παρακαλώ δημιουργήστε ένα νέο λογαριασμό:");
            t3.setText("Όνομα Χρήστη:");
            t4.setText("Κωδικός Πρόσβασης:");
            t5.setText("Επιβεβαίωση Κωδικού Πρόσβασης:");
            b1.setText("Εγγραφη");
            b2.setText("Εχω Ηδη Λογαριασμο");
            e1.setHint("πληκτρολογήστε εδώ το όνομα χρήστη");
            e2.setHint("πληκτρολογήστε εδώ τον κωδικό πρόσβασης");
            e3.setHint("επαναπληκτρολογήστε εδώ τον κωδικό πρόσβασης");
        }
        else if(lang.equals("English"))
        {
            t1.setText("Welcome to the SmartAlert App!");
            t2.setText("To continue, please create a new account:");
            t3.setText("Username:");
            t4.setText("Password:");
            t5.setText("Confirm Password:");
            b1.setText("Register");
            b2.setText("I Already Have An Account");
            e1.setHint("type your username here");
            e2.setHint("type your password here");
            e3.setHint("retype your password here");
        }
        else if(lang.equals("Nederlands"))
        {
            t1.setText ("Welkom bij de SmartAlert-app!");
            t2.setText ("Om door te gaan, maak een nieuw account aan:");
            t3.setText ("Gebruikersnaam:");
            t4.setText ("Wachtwoord:");
            t5.setText ("Wachtwoord bevestigen:");
            b1.setText ("Registreren");
            b2.setText ("Ik heb al een account");
            e1.setHint ("typ hier uw gebruikersnaam");
            e2.setHint ("typ hier uw wachtwoord");
            e3.setHint ("typ hier uw wachtwoord opnieuw");
        }
    }
}
