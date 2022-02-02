package com.example.smartmeterbiller.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.smartmeterbiller.R;
import com.example.smartmeterbiller.ReadingsAdapter;
import com.example.smartmeterbiller.classes.CapturedReadings;
import com.example.smartmeterbiller.classes.Customers;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class Readings extends AppCompatActivity implements ReadingsAdapter.OnItemClickListener {

    FirebaseFirestore db;
    TextView tvTotalCostInList;

    private RecyclerView recyclerView;
    private List<CapturedReadings> capturedReadingsList;
    private ReadingsAdapter readingsAdapter;
    ReadingsAdapter.OnItemClickListener mListener;
    private Context mContext;

    StorageReference storageReference;
    FirebaseStorage mStorage;
    DatabaseReference databaseReference;
    private ValueEventListener mDBListener;
    FirebaseAuth firebaseAuth;
    String id, userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_readings);


        /*---------------- FireBase -------------------*/
        db = FirebaseFirestore.getInstance();


        storageReference = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference("CapturedReadings");
        mStorage = FirebaseStorage.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        //gets the user that is currently logged in
        userID = firebaseAuth.getCurrentUser().getUid();

        /*---------------- RecyclerView -------------------*/
        recyclerView = findViewById(R.id.rvReadings);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        capturedReadingsList = new ArrayList<>();

        readingsAdapter = new ReadingsAdapter(Readings.this, capturedReadingsList, mListener);

        recyclerView.setAdapter(readingsAdapter);

        readingsAdapter.setOnItemClickListener(Readings.this);

        tvTotalCostInList = findViewById(R.id.tvTotalCostInList);


        //clear arraylist:
        ClearAll();
        //get data from firebase method
        //readUserData();
        GetDataFromFirebase();


        mDBListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                capturedReadingsList.clear();

                for(DataSnapshot postSnapshot : snapshot.getChildren())
                {
                    CapturedReadings capturedReadings = postSnapshot.getValue(CapturedReadings.class);
                    assert capturedReadings != null;
                    capturedReadings.setKey(postSnapshot.getKey());
                    capturedReadingsList.add(capturedReadings);
                }

                readingsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Readings.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

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
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void GetDataFromFirebase()
    {
        Query query = databaseReference.child("CapturedReadings");

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for(DataSnapshot dataSnapshot : snapshot.getChildren())
                {
                    CapturedReadings capturedReadings = new CapturedReadings();

                    capturedReadings.setCapturedDate(dataSnapshot.child("capturedDate").getValue().toString());
                    capturedReadings.setCapturedReading(dataSnapshot.child("capturedReading").getValue().toString());
                    capturedReadings.setCapturedReadingId(dataSnapshot.child("capturedReadingId").getValue().toString());
                    capturedReadings.setCapturedReadingUrl(dataSnapshot.child("capturedReadingUri").getValue().toString());

                    capturedReadingsList.add(capturedReadings);
                    Toast.makeText(mContext, "This works", Toast.LENGTH_SHORT).show();

                }

                readingsAdapter = new ReadingsAdapter(getApplicationContext() , capturedReadingsList, mListener);
                recyclerView.setAdapter(readingsAdapter);
                readingsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void ClearAll()
    {
        if(capturedReadingsList != null)
        {
            capturedReadingsList.clear();

            if(readingsAdapter != null)
            {
                readingsAdapter.notifyDataSetChanged();
            }
        }
        else
        {
            capturedReadingsList = new ArrayList<>();
        }
    }

    @Override
    public void onItemClick(int position) {
        Toast.makeText(Readings.this, "normal click at position: " + position, Toast.LENGTH_SHORT).show();
        startActivity(new Intent(getApplicationContext(), Bill_Report.class));
    }

    @Override
    public void onWhatEverClicked(int position) {
        Toast.makeText(this, "whatever click at position: " + position, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onDeleteClick(int position) {
        CapturedReadings selectedItem = capturedReadingsList.get(position);
        final String selectedKey = selectedItem.getKey();
        Toast.makeText(Readings.this, "Deleted Clicked", Toast.LENGTH_SHORT).show();

        StorageReference imageRef = mStorage.getReferenceFromUrl(selectedItem.getCapturedReadingUrl());
        imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                databaseReference.child(selectedKey).removeValue();
                Toast.makeText(Readings.this, "Item Deleted", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Readings.this, "Unable to delete item", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        databaseReference.removeEventListener(mDBListener);
    }

}