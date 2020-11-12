package com.example.smartalert;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class FireReport extends AppCompatActivity {

    private LocationManager locationManager;
    private static final int GALLERY_REQUEST_CODE = 100;
    private static final int CAMERA_REQUEST_CODE = 200;
    private static boolean imageSet;
    private ImageView selectedImageView;
    private double latitude = 0.0;
    private double longitude = 0.0;
    Timer timer;
    boolean gps_enabled = false;
    boolean network_enabled = false;
    public byte[] imgtoupload;
    public static final String MyPREFERENCES = "MyPrefs" ;
    SharedPreferences sharedpreferences;
    public static boolean fromGallery; // flag to check if coming from gallery

    String lang;
    String sms_number;
    Button b1,b2,b3;
    TextView t1;
    dbHelper db;

    DatabaseReference reff;
    Fires_Firebase firesFirebase;
    private FirebaseStorage storage;
    private StorageReference mStorageRef;

    long maxid=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fire_report);

        doStuff();   //Starting Location Listeners

        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        lang=sharedpreferences.getString("Language", "");
        if (lang.equals("null") || lang.isEmpty())
        {
            lang = "English";
        }

        selectedImageView = (ImageView) findViewById(R.id.selected_image);
        db = new dbHelper(this);
        b1 = (Button) findViewById(R.id.addGallery);
        b2 = (Button) findViewById(R.id.addCamera);
        b3 = (Button) findViewById(R.id.reportFire);
        t1 = (TextView)findViewById(R.id.report1);

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

        storage = FirebaseStorage.getInstance();
        mStorageRef = storage.getReference("FireReports");
        firesFirebase = new Fires_Firebase();
        reff = FirebaseDatabase.getInstance().getReference().child("Fires");
        reff.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    maxid=(snapshot.getChildrenCount());  //Auto-increment for firebase fire log entries
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        imageSet=false; //flag to check if an image is set
        fromGallery=false;  //flag to check if coming from gallery (and not camera capture)

        sms_number=sharedpreferences.getString("Contact_number", "");

        if(sms_number==null)           // Warning in case of error at settings read
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
                                              //Getting Picture from Gallery
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), GALLERY_REQUEST_CODE);
            }
        });
                                            //Getting Picture from Camera
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
                }
            }
        });
                                        //Reporting
        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageSet) {   //In case image is set (in order not to send reports with the default placeholder image)
                    AlertDialog.Builder builder = new AlertDialog.Builder(FireReport.this);
                    if(lang.equals("Ελληνικά"))
                    {
                        builder.setTitle("Επιβεβαίωση Αναφοράς");          //Confirm Report Dialog
                        builder.setMessage("Θέλετε πραγματικά να αναφέρετε αυτήν την πυρκαγιά;");
                        builder.setPositiveButton("Ναι, Αναφορα", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Bitmap image = ((BitmapDrawable)selectedImageView.getDrawable()).getBitmap();
                                byte[] imageArray = getBitmapAsByteArray(image);            //converting the bitmap image to byte array

                                fileUploader();  //uploading the report with the image to firebase realtime database and firebase storage (for the image)
                                boolean insert = db.insertFireEvent(Login.unamewel, "Fire Event + SMS", longitude, latitude, imageArray); //logging also to sqlite
                                if(!insert){
                                    Toast.makeText(getApplicationContext(),"Η καταγραφή του συμβάντος απέτυχε",Toast.LENGTH_SHORT).show();
                                }
                                String sms_msg = "";
                                if(latitude!=0.0 && longitude!=0.0) {  //SMS sending
                                    sms_msg = "Eimai edw: " + " " + "https://www.google.com/maps/place/" + latitude + "," + longitude + "/" + " stis " + getDateTime() + " kai blepw mia pyrkagia!";
                                }
                                else      //Changing SMS content if there is no GPS signal
                                {
                                    sms_msg = "Blepw mia pirkagia stis "+getDateTime()+" alla den exw shma GPS!";
                                }
                                try {
                                    SmsManager smsManager = SmsManager.getDefault();
                                    smsManager.sendTextMessage(sms_number, null, sms_msg, null, null);
                                    if(latitude!=0.0 && longitude!=0.0) {
                                        Toast.makeText(getApplicationContext(), "Η αναφορά πυρκαγιάς και η αποστολή SMS πραγματοποιήθηκαν με επιτυχία", Toast.LENGTH_LONG).show();
                                    }
                                    else{
                                        Toast.makeText(getApplicationContext(), "Η αναφορά πυρκαγιάς και η αποστολή SMS πραγματοποιήθηκαν, αλλά η τοποθεσία GPS δεν είναι διαθέσιμη", Toast.LENGTH_LONG).show();
                                    }
                                } catch (Exception ex) {
                                    Toast.makeText(getApplicationContext(), ex.getMessage().toString(),
                                            Toast.LENGTH_LONG).show();
                                    ex.printStackTrace();
                                }

                                dialog.dismiss();
                            }
                        });
                        builder.setNegativeButton("Ματαιωση", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                    else if(lang.equals("English"))       //same logic but for other languages
                    {
                        builder.setTitle("Report Confirmation");
                        builder.setMessage("Do you really want to report this fire?");
                        builder.setPositiveButton("Yes, Report", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Bitmap image = ((BitmapDrawable)selectedImageView.getDrawable()).getBitmap();
                                byte[] imageArray = getBitmapAsByteArray(image);            //converting the bitmap image to byte array

                                fileUploader();
                                boolean insert = db.insertFireEvent(Login.unamewel, "Fire Event + SMS", longitude, latitude, imageArray);
                                if(!insert){
                                    Toast.makeText(getApplicationContext(),"Event Logging Failed",Toast.LENGTH_SHORT).show();
                                }
                                String sms_msg = "";
                                if(latitude!=0.0 && longitude!=0.0) {
                                    sms_msg = "I'm in this location:"+" " + "https://www.google.com/maps/place/" + latitude + "," +longitude+"/"+" at "+getDateTime()+" and I'm observing a fire!";
                                }
                                else
                                {
                                    sms_msg = "I'm observing a fire at "+getDateTime()+" but I don't have GPS signal!";
                                }
                                try {                                                                        // ^ Rithmisi tou mhnymatos tou SMS pou tha dei o paralhpths (mazi me thn topothesia)
                                    SmsManager smsManager = SmsManager.getDefault();
                                    smsManager.sendTextMessage(sms_number, null, sms_msg, null, null);
                                    if(latitude!=0.0 && longitude!=0.0) {
                                    Toast.makeText(getApplicationContext(), "Fire Reported and SMS Successfully Sent", Toast.LENGTH_LONG).show();}
                                    else{
                                        Toast.makeText(getApplicationContext(), "Fire Reported and SMS Sent, but GPS Location not available", Toast.LENGTH_LONG).show();
                                    }
                                } catch (Exception ex) {
                                    Toast.makeText(getApplicationContext(), ex.getMessage().toString(),
                                            Toast.LENGTH_LONG).show();
                                    ex.printStackTrace();
                                }

                                dialog.dismiss();
                            }
                        });
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                    else if(lang.equals("Nederlands"))
                    {
                        builder.setTitle("Bevestiging Melden");
                        builder.setMessage("Wil je deze brand echt melden?");
                        builder.setPositiveButton("Ja, rapport", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Bitmap image = ((BitmapDrawable)selectedImageView.getDrawable()).getBitmap();
                                byte[] imageArray = getBitmapAsByteArray(image);            //converting the bitmap image to byte array

                                fileUploader();
                                boolean insert = db.insertFireEvent(Login.unamewel, "Fire Event + SMS", longitude, latitude, imageArray);
                                if(!insert){
                                    Toast.makeText(getApplicationContext(),"Logboekregistratie is mislukt",Toast.LENGTH_SHORT).show();
                                }
                                String sms_msg = "";
                                if(latitude!=0.0 && longitude!=0.0) {
                                    sms_msg = "Ik ben op deze locatie:"+" " + "https://www.google.com/maps/place/" + latitude + "," +longitude+"/"+" om "+getDateTime()+" en ik observeer een brand!";
                                }
                                else
                                {
                                    sms_msg = "Ik observeer een brand om "+getDateTime()+" maar ik heb geen GPS-signaal!";
                                }
                                try {                                                                        // ^ Rithmisi tou mhnymatos tou SMS pou tha dei o paralhpths (mazi me thn topothesia)
                                    SmsManager smsManager = SmsManager.getDefault();
                                    smsManager.sendTextMessage(sms_number, null, sms_msg, null, null);
                                    if(latitude!=0.0 && longitude!=0.0) {
                                        Toast.makeText(getApplicationContext(), "Brand gemeld en sms succesvol verzonden", Toast.LENGTH_LONG).show();
                                    }
                                    else{
                                        Toast.makeText(getApplicationContext(), "Brand gemeld en sms verzonden, maar gps-locatie niet beschikbaar", Toast.LENGTH_LONG).show();
                                    }
                                } catch (Exception ex) {
                                    Toast.makeText(getApplicationContext(), ex.getMessage().toString(),
                                            Toast.LENGTH_LONG).show();
                                    ex.printStackTrace();
                                }

                                dialog.dismiss();
                            }
                        });
                        builder.setNegativeButton("Annuleren", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                        AlertDialog alert = builder.create();
                        alert.show();
                    }

                }
                else   //No image set warning
                {
                    if(lang.equals("Ελληνικά"))
                    {
                        Toast.makeText(getApplicationContext(), "Παρακαλώ προσθέστε μια φωτογραφία για να αναφέρετε την πυρκαγιά.", Toast.LENGTH_SHORT).show();
                    }
                    else if(lang.equals("English"))
                    {
                        Toast.makeText(getApplicationContext(), "Please add a photo to report a fire.", Toast.LENGTH_SHORT).show();
                    }
                    else if(lang.equals("Nederlands"))
                    {
                        Toast.makeText(getApplicationContext (), "Voeg een foto toe om een brand te melden.", Toast.LENGTH_SHORT).show ();
                    }
                }



            }
        });
    }

    LocationListener locationListenerGps = new LocationListener() {      //GPS Location Listener
        public void onLocationChanged(Location location) {
            timer.cancel();
            latitude =location.getLatitude();
            longitude = location.getLongitude();
            //locationManager.removeUpdates(this);
            locationManager.removeUpdates(locationListenerNetwork);
        }

        public void onProviderDisabled(String provider) {
            final AlertDialog.Builder builder =  new AlertDialog.Builder(FireReport.this);
            final String action = Settings.ACTION_LOCATION_SOURCE_SETTINGS;

            if(lang.equals("Ελληνικά"))
            {
                builder.setMessage("Παρακαλώ ενεργοποιήστε τις υπηρεσίες τοποθεσίας για να λειτουργεί σωστά η εφαρμογή SmartAlert.")
                        .setPositiveButton("Ρυθμισεις",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface d, int id) {
                                        FireReport.this.startActivity(new Intent(action));
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
                                        FireReport.this.startActivity(new Intent(action));
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
                                        FireReport.this.startActivity(new Intent(action));
                                        d.dismiss();
                                    }
                                });
            }
            builder.create().show();
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    LocationListener locationListenerNetwork = new LocationListener() {   //Network Location Listener
        public void onLocationChanged(Location location) {
            timer.cancel();
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

    class GetLastLocation extends TimerTask {   //Getting last known location after 20 seconds if both GPS and Network locations failed
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {    //retrieving the images (either from gallery or from camera)
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == GALLERY_REQUEST_CODE) {
            try {
                fromGallery=true;
                Uri selectedImage = data.getData();
                InputStream imageStream = getContentResolver().openInputStream(selectedImage);
                selectedImageView.setImageBitmap(BitmapFactory.decodeStream(imageStream));
                Bitmap image = ((BitmapDrawable)selectedImageView.getDrawable()).getBitmap();
                imgtoupload = getBitmapAsByteArray(image);
                imageSet=true;
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {

            fromGallery=false;
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            selectedImageView.setImageBitmap(photo);
            Bitmap image = ((BitmapDrawable)selectedImageView.getDrawable()).getBitmap();
            imgtoupload = getBitmapAsByteArray(image);
            imageSet=true;
        }
    }

    public static byte[] getBitmapAsByteArray(Bitmap bitmap) {          //converting bitmap to byte array
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        if(fromGallery) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 10, outputStream);
        }
        else if(!fromGallery){
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
        }
        return outputStream.toByteArray();
    }


    private void fileUploader(){        //Firebase Fire Report Upload
        if(isNetworkAvailable()) {
            final ProgressDialog pd = new ProgressDialog(this);
            if (lang.equals("Ελληνικά")) {
                pd.setTitle("Μεταφόρτωση Αναφοράς...");
            } else if (lang.equals("English")) {
                pd.setTitle("Uploading Report...");
            } else if (lang.equals("Nederlands")) {
                pd.setTitle("Rapport uploaden...");
            }
            pd.show();

            final String imageID = String.valueOf(System.currentTimeMillis());
            firesFirebase.setUser(Login.unamewel);
            firesFirebase.setLatitude(latitude);
            firesFirebase.setLongitude(longitude);
            firesFirebase.setTimestamp(getDateTime());
            firesFirebase.setEventType("Fire Event + SMS");
            firesFirebase.setImageID(imageID);
            reff.child(String.valueOf(maxid + 1)).setValue(firesFirebase);

            StorageReference Ref = mStorageRef.child(imageID);

            Ref.putBytes(imgtoupload)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                pd.dismiss();
                                if (lang.equals("Ελληνικά")) {
                                    Toast.makeText(getBaseContext(), "Η εικόνα μεταφορτώθηκε με επιτυχία", Toast.LENGTH_LONG).show();
                                } else if (lang.equals("English")) {
                                    Toast.makeText(getBaseContext(), "Image Uploaded Successfully", Toast.LENGTH_LONG).show();
                                } else if (lang.equals("Nederlands")) {
                                    Toast.makeText(getBaseContext(), "Afbeelding succesvol geüpload", Toast.LENGTH_LONG).show();
                                }
                                Intent i = new Intent(FireReport.this, Menu.class); //going to the Menu activity
                                startActivity(i);
                                finish();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                pd.dismiss();
                                if (lang.equals("Ελληνικά")) {
                                    Toast.makeText(getBaseContext(), "Η μεταφόρτωση εικόνας απέτυχε", Toast.LENGTH_LONG).show();
                                } else if (lang.equals("English")) {
                                    Toast.makeText(getBaseContext(), "Image Upload Failed", Toast.LENGTH_LONG).show();
                                } else if (lang.equals("Nederlands")) {
                                    Toast.makeText(getBaseContext(), "Upload van afbeelding mislukt", Toast.LENGTH_LONG).show();
                                }
                            }
                        })
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                                double progressPercent = (100.00 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                                if (lang.equals("Ελληνικά")) {
                                    pd.setMessage("Ποσοστό Μεταφόρτωσης: " + (int) progressPercent + "%");
                                } else if (lang.equals("English")) {
                                    pd.setMessage("Uploaded Percentage: " + (int) progressPercent + "%");
                                } else if (lang.equals("Nederlands")) {
                                    pd.setMessage("Percentage geüpload: " + (int) progressPercent + "%");
                                }
                            }
                        });
        }
        else{          //If there is no internet connection
            if (lang.equals("Ελληνικά")) {
                Toast.makeText(getBaseContext(), "Δεν υπάρχει σύνδεση στο Διαδίκτυο. Το συμβάν καταγράφηκε τοπικά", Toast.LENGTH_LONG).show();
            } else if (lang.equals("English")) {
                Toast.makeText(getBaseContext(), "No internet connection available. Event logged locally", Toast.LENGTH_LONG).show();
            } else if (lang.equals("Nederlands")) {
                Toast.makeText(getBaseContext(), "Geen internetverbinding beschikbaar. Gebeurtenis lokaal geregistreerd", Toast.LENGTH_LONG).show();
            }
            Intent i = new Intent(FireReport.this, Menu.class); //going to the Menu activity
            startActivity(i);
            finish();
        }
    }


    @Override
    protected void onResume() {   //checking the language, internet connection and starting the location listeners
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
    protected void onPause() {  //stopping the gps location listener
        super.onPause();
        //locationManager.removeUpdates((LocationListener) this);  //to stop the location manager when this activity isn't on foreground
        locationManager.removeUpdates(locationListenerGps);
    }

    @SuppressLint("MissingPermission")
    private void doStuff(){              //location listeners initialization
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);


        //if (gps_enabled)
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0,
                    locationListenerGps);
        if (network_enabled)
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 0,
                    locationListenerNetwork);
        timer=new Timer();
        timer.schedule(new GetLastLocation(), 20000);
    }

    //Getting current timestamp
    private String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

    private void languageCheck()  //Language UI Management
    {
        if(lang.equals("Ελληνικά"))
        {
            t1.setText ("Αναφορά μιας Πυρκαγιάς");
            b1.setText ("Προσθηκη φωτογραφιας απο τη συλλογη");
            b2.setText ("Προσθηκη φωτογραφιας απο την καμερα");
            b3.setText ("Αναφορα πυρκαγιας!");
        }
        else if(lang.equals("English"))
        {
            t1.setText("Report a Fire");
            b1.setText("Add Photo From Gallery");
            b2.setText("Add Photo From Camera");
            b3.setText("Report Fire !");
        }
        else if(lang.equals("Nederlands"))
        {
            t1.setText ("Meld een brand");
            b1.setText ("Foto uit galerij toevoegen");
            b2.setText ("Foto van camera toevoegen");
            b3.setText ("Meld brand!");
        }
    }

    private boolean isNetworkAvailable() {       //Checking if device is connected to the internet
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
