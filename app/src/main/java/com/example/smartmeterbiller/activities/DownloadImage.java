package com.example.smartmeterbiller.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.smartmeterbiller.R;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class DownloadImage extends AppCompatActivity {

    ImageView ivDownloadImageReading;
    Button btnDownloadImageReading;
    TextView tvTestUnits, tvTestDate, tvTestURL;
    
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_download_image);

        ivDownloadImageReading = findViewById(R.id.ivDownloadReading);
        btnDownloadImageReading = findViewById(R.id.btnDownloadReading);
        tvTestURL = findViewById(R.id.tvTestURL);
        tvTestUnits = findViewById(R.id.tvTestUnits);
        tvTestDate = findViewById(R.id.tvTestDate);

        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        getIncomingIntent();
        //Toast.makeText(DownloadImage.this, "no extras", Toast.LENGTH_SHORT).show();

    }
    
    private void getIncomingIntent()
    {

        if(getIntent().hasExtra("date") && getIntent().hasExtra("units")
            && getIntent().hasExtra("image"))
        {
            String image_url = getIntent().getStringExtra("image");
            String date = getIntent().getStringExtra("date");
            String units = getIntent().getStringExtra("units");

            //Toast.makeText(this, date + "\n" + units + "\n" + image_url, Toast.LENGTH_SHORT).show();

            tvTestUnits.setText("Units: " + units);
            tvTestDate.setText("Date: " + date);
            //tvTestURL.setText(image_url);
            //setImage(image_url);
        }
        else
        {
            Toast.makeText(this, "no extras", Toast.LENGTH_SHORT).show();
        }
    }

    private void setImage(String image_url)
    {
        Glide.with(DownloadImage.this)
                .asBitmap()
                .load(image_url)
                .into(ivDownloadImageReading);
    }

}