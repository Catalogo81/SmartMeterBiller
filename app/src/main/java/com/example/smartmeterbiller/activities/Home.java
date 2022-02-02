package com.example.smartmeterbiller.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.smartmeterbiller.Login;
import com.example.smartmeterbiller.R;
import com.example.smartmeterbiller.classes.CapturedReadings;
import com.example.smartmeterbiller.classes.Customers;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class Home extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    TextView tvName_Surname, tvEmailAddress, tvHomeAddress, tvMeterNumber, tvReadingRand,
            tvReadingUnits, tvReadingDatePosted;
    Button btnDownloadReading, btnLogout, btnCancelLogout, btnInfoClose;
    ImageView ivLearnMore;

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    Dialog logout_dialog, app_info_dialog;

    Bill_Report bill_report = new Bill_Report();
    double totalCostInRand;
    private static final DecimalFormat df2 = new DecimalFormat("#.##");

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firestore;
    FirebaseUser firebaseUser;
    String userID;
    DatabaseReference databaseReference;
    String costAmount = "500";
    String id, name, surname, emailAddress, homeAddress, meterNumber, phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_home);

        /*-----------------Hooks---------------------*/
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);

        /*-----------------TextViews---------------------*/
        tvName_Surname = findViewById(R.id.tvName_Surname);
        tvEmailAddress = findViewById(R.id.tvEmailAddress);
        tvHomeAddress = findViewById(R.id.tvHomeAddress);
        tvMeterNumber = findViewById(R.id.tvMeterNumber);
        tvReadingRand = findViewById(R.id.tvReadingRand);
        tvReadingUnits = findViewById(R.id.tvReadingUnits);
        tvReadingDatePosted = findViewById(R.id.tvReadingDatePosted);

        TextView tvCentlecLink =(TextView) findViewById(R.id.tvCentlecLink);
        tvCentlecLink.setMovementMethod(LinkMovementMethod.getInstance());

        /*-----------------Buttons---------------------*/
        btnDownloadReading = findViewById(R.id.btnDownloadReading);

        /*-----------------Image View---------------------*/
        ivLearnMore = findViewById(R.id.ivLearnMore);

        /*-----------------Tool Bar---------------------*/
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        /*-----------------Navigation Drawer Menu---------------------*/
        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        /*------------------logout dialog--------------------*/
        logout_dialog = new Dialog(Home.this);
        logout_dialog.setContentView(R.layout.logout_dialog_box);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            Objects.requireNonNull(logout_dialog.getWindow()).setBackgroundDrawable(getDrawable(R.drawable.dialog_background));
        }
        logout_dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        logout_dialog.setCancelable(true);//when you click the outside of the dialog it will disappear
        logout_dialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation;

        btnLogout = logout_dialog.findViewById(R.id.btnLogout);
        btnCancelLogout = logout_dialog.findViewById(R.id.btnCancelLogout);

        /*------------------app info dialog--------------------*/
        app_info_dialog = new Dialog(Home.this);
        app_info_dialog.setContentView(R.layout.app_info_dialog_box);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            Objects.requireNonNull(app_info_dialog.getWindow()).setBackgroundDrawable(getDrawable(R.drawable.dialog_background));
        }
        app_info_dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        app_info_dialog.setCancelable(true);//when you click the outside of the dialog it will disappear
        app_info_dialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation;

        btnInfoClose = app_info_dialog.findViewById(R.id.btnInfoClose);

        /*--------------------Firebase Instance retrievals----------------------*/
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        //gets the user that is currently logged in
        userID = firebaseAuth.getCurrentUser().getUid();

        readUserData();
        capturedReadingData();

        btnDownloadReading.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Home.this, Readings.class);
                Bundle bundle = new Bundle();
                bundle.putString("cost", costAmount);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        btnLogout.setOnClickListener(v -> logout());

        btnCancelLogout.setOnClickListener(v -> logout_dialog.dismiss());

        ivLearnMore.setOnClickListener(v -> app_info_dialog.show());

        btnInfoClose.setOnClickListener(v -> app_info_dialog.dismiss());

    }

    private void readUserData()
    {
        /*---------------Reads the data entered when the user registered----------------*/
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Customers");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Customers customer = snapshot1.getValue(Customers.class);
                    assert customer != null;


                     id = customer.getId();
                     name = customer.getName();
                     surname = customer.getSurname();
                     emailAddress = customer.getEmailAddress();
                     homeAddress = customer.getHomeAddress();
                     meterNumber = customer.getMeterNumber();
                     phoneNumber = String.valueOf(customer.getPhoneNumber());

                     if(userID.equals(id))
                     {
                         tvName_Surname.setText("FULL NAMES: " + name + " " + surname);
                         tvEmailAddress.setText("EMAIL: " + emailAddress);
                         tvHomeAddress.setText("HOME ADDRESS: " + homeAddress);
                         tvMeterNumber.setText("METER NUMBER: " + meterNumber);
                     }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void capturedReadingData()
    {
        /*---------------Reads the captured reading data----------------*/
        String imageUploadedId = databaseReference.push().getKey();
        assert imageUploadedId != null;
        DatabaseReference readingDatabaseReference = FirebaseDatabase.getInstance().getReference().child("CapturedReadings");
        readingDatabaseReference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    CapturedReadings captureReadings = snapshot1.getValue(CapturedReadings.class);
                    assert captureReadings != null;

                    String capturedReadingId = captureReadings.getCapturedReadingId();
                    String capturedReadingDate = captureReadings.getCapturedDate();
                    double capturedUnits = Double.parseDouble(captureReadings.getCapturedReading());
                    //String costPerUnit;

                    if(userID.equals(capturedReadingId))
                    {
                        tvReadingDatePosted.setText("Reading Date Posted: " + capturedReadingDate);
                        tvReadingUnits.setText("Reading Units: " + capturedUnits);

                        //tvReadingRand.setText("Reading Cost(per unit): R" + bill_report.globalTotalCostAmount);

                        SimpleDateFormat format1 =new SimpleDateFormat("dd/MM/yyyy");
                        DateFormat format2=new SimpleDateFormat("MMMM");

                        Date dt1= null;
                        try {
                            dt1 = format1.parse(capturedReadingDate);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        String finalDay = format2.format(dt1);

                        /*------------------------- Winter cost calculation ------------------------*/
                        if(finalDay.equals("March") || finalDay.equals("April") || finalDay.equals("May")
                                ||finalDay.equals("June") || finalDay.equals("July") || finalDay.equals("August"))
                        {
                            //Block1 cost calculation
                            if(capturedUnits <= 350)
                            {
                                totalCostInRand = capturedUnits * bill_report.winterUnitCostBlock1;
                                tvReadingRand.setText("Recent Total Cost R" + df2.format(totalCostInRand));
                            }
                            //Block2 cost calculation
                            else
                            {
                                totalCostInRand = capturedUnits * bill_report.winterUnitCostBlock2;
                                tvReadingRand.setText("Recent Total Cost R" + df2.format(totalCostInRand));
                            }

                        }
                        /*------------------------- Winter cost calculation ------------------------*/
                        else if (finalDay.equals("September") || finalDay.equals("October") || finalDay.equals("November")
                                ||finalDay.equals("December") || finalDay.equals("January") || finalDay.equals("February"))
                        {

                            //Block1 cost calculation
                            if(capturedUnits <= 350)
                            {
                                totalCostInRand = capturedUnits * bill_report.summerUnitCostBlock1;
                                tvReadingRand.setText("Recent Total Cost R" + df2.format(totalCostInRand));
                            }
                            //Block2 cost calculation
                            else
                            {
                                totalCostInRand = capturedUnits * bill_report.summerUnitCostBlock2;
                                tvReadingRand.setText("Recent Total Cost R" + df2.format(totalCostInRand));
                            }

                        }


                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START))
        {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else
        {
            super.onBackPressed();
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId())
        {
            case R.id.nav_home:
                startActivity(new Intent(getApplicationContext(), Home.class));
                break;

            case R.id.nav_alert:
                startActivity(new Intent(getApplicationContext(), Alerts.class));
                break;

            case R.id.nav_reading:
                startActivity(new Intent(getApplicationContext(), Readings.class));
                break;

            case R.id.nav_report:
                startActivity(new Intent(getApplicationContext(), Bill_Report.class));
                break;

            case R.id.nav_profile:

                Intent intent = new Intent(getApplicationContext(), CustomerProfile.class);

                //pass the users details to the next activity using an intent
                intent.putExtra("name", name);
                intent.putExtra("surname", surname);
                intent.putExtra("email", emailAddress);
                intent.putExtra("phoneNumber", phoneNumber);
                intent.putExtra("homeAddress", homeAddress);
                intent.putExtra("meterNumber", meterNumber);

                startActivity(intent);
                break;

            case R.id.nav_capture_reading:
                startActivity(new Intent(getApplicationContext(), CaptureReading.class));
                break;

            case R.id.nav_logout:
                logout_dialog.show();
                break;
        }

        return true;
    }

    private void logout()
    {
        Toast.makeText(this, "User Logged Out...", Toast.LENGTH_SHORT).show();

        FirebaseAuth.getInstance().signOut();//used for logging out the user
        startActivity(new Intent(getApplicationContext(), Login.class));
        finish();
    }
}