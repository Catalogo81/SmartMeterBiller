package com.example.smartmeterbiller.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.smartmeterbiller.R;
import com.example.smartmeterbiller.classes.CapturedReadings;
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
import java.time.MonthDay;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;

public class Bill_Report extends AppCompatActivity {

    ImageView ivBack;
    TextView tvTotalCostInRand,
            tvDateReadingPosted, tvCurrentUnits, tvCurrentBillingDescription;

    double totalCostInRand;
    final double winterUnitCostBlock1 = 2.52;
    final double winterUnitCostBlock2 = 3.05;
    final double summerUnitCostBlock1 = 2.03;
    final double summerUnitCostBlock2 = 2.35;

    public double globalTotalCostAmount;

    private static final DecimalFormat df2 = new DecimalFormat("#.##");

//    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
//    String currentDate = sdf.format(new Date());

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firestore;
    FirebaseUser firebaseUser;
    String userID;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_bill__report);

        /* --------------ImageViews ------------------*/
        ivBack = findViewById(R.id.ivBack);

        /* --------------TextViews ------------------*/
        tvTotalCostInRand = findViewById(R.id.tvTotalCostInRand);
        tvDateReadingPosted = findViewById(R.id.tvDateReadingPosted);
        tvCurrentUnits = findViewById(R.id.tvCurrentUnits);
        tvCurrentBillingDescription = findViewById(R.id.tvCurrentBillingDescription);

        ivBack.setOnClickListener(v -> Bill_Report.this.finish());

        /*--------------------Firebase Instance retrievals----------------------*/
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        //gets the user that is currently logged in
        userID = firebaseAuth.getCurrentUser().getUid();

        capturedReadingData();

    }

    private void capturedReadingData()
    {
        /*---------------Reads the captured reading data----------------*/
        //String imageUploadedId = databaseReference.push().getKey();
        //assert imageUploadedId != null;
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
                        tvDateReadingPosted.setText("Reading Date Posted: " + capturedReadingDate);
                        tvCurrentUnits.setText("Reading Units: " + capturedUnits);

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
                                totalCostInRand = capturedUnits * winterUnitCostBlock1;
                                tvCurrentBillingDescription.setText("Winter Reading Cost(per unit): R" + winterUnitCostBlock1);
                                tvTotalCostInRand.setText("R" + df2.format(totalCostInRand));
                            }
                            //Block2 cost calculation
                            else
                            {
                                totalCostInRand = capturedUnits * winterUnitCostBlock2;
                                tvCurrentBillingDescription.setText("Winter Reading Cost(per unit): R" + winterUnitCostBlock2);
                                tvTotalCostInRand.setText("R" + df2.format(totalCostInRand));
                            }

                        }
                        /*------------------------- Winter cost calculation ------------------------*/
                        else if (finalDay.equals("September") || finalDay.equals("October") || finalDay.equals("November")
                                ||finalDay.equals("December") || finalDay.equals("January") || finalDay.equals("February"))
                        {

                            //Block1 cost calculation
                            if(capturedUnits <= 350)
                            {
                                totalCostInRand = capturedUnits * summerUnitCostBlock1;
                                tvCurrentBillingDescription.setText("Summer Reading Cost(per unit): R" + summerUnitCostBlock1);
                                tvTotalCostInRand.setText("R" + df2.format(totalCostInRand));
                            }
                            //Block2 cost calculation
                            else
                            {
                                totalCostInRand = capturedUnits * summerUnitCostBlock2;
                                tvCurrentBillingDescription.setText("Summer Reading Cost(per unit): R" + summerUnitCostBlock2);
                                tvTotalCostInRand.setText("R" + df2.format(totalCostInRand));
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
}