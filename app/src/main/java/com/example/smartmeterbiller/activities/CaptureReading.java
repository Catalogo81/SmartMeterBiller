package com.example.smartmeterbiller.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.smartmeterbiller.R;
import com.example.smartmeterbiller.classes.CapturedReadings;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class CaptureReading extends AppCompatActivity {

    public static final int CAMERA_PERM_CODE = 101;
    public static final int CAMERA_REQUEST_CODE = 102;
    public static final int GALLERY_REQUEST_CODE = 103;

    ImageView ivReading;
    Dialog uploadDialog, successDialog;
    Button btnCamera, btnGallery, btnUpload, btnUploadClose, btnSuccessClose;
    ImageView ivUpload, ivBack; /*ivCalendar*/;
    EditText etDate, etUnits;
    CheckBox cbValid;
    ProgressBar mProgressBar;

    String currentPhotoPath, id, date, units, totalCostPerUnit, valid, downloadLink;
    double totalCost=0, currentUnit=0;

    StorageReference storageReference;
    DatabaseReference databaseReference;
    FirebaseAuth firebaseAuth;
    Bill_Report bill_report = new Bill_Report();

    private ProgressDialog progressDialog;
    private DatePickerDialog.OnDateSetListener onDateSetListener;

    /*------------------sets the date format---------------------*/
    String DateFormat = "dd/MM/yyyy";
    String currentDateString = new SimpleDateFormat(DateFormat, Locale.getDefault()).format(new Date());

    private static final DecimalFormat df2 = new DecimalFormat("#.##");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_capture_reading);

        ivReading = findViewById(R.id.ivReading);
        ivUpload = findViewById(R.id.ivUpload);
        ivBack = findViewById(R.id.ivBack);
        btnCamera = findViewById(R.id.btnCamera);
        btnGallery = findViewById(R.id.btnGallery);
        mProgressBar = findViewById(R.id.mProgressBar);

        storageReference = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();

        id = firebaseAuth.getCurrentUser().getUid();

        progressDialog = new ProgressDialog(this);

        /*------------------upload dialog--------------------*/
        uploadDialog = new Dialog(CaptureReading.this);
        uploadDialog.setContentView(R.layout.upload_dialog_box);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            Objects.requireNonNull(uploadDialog.getWindow()).setBackgroundDrawable(getDrawable(R.drawable.dialog_background));
        }
        uploadDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        uploadDialog.setCancelable(true);//when you click the outside of the dialog it will disappear
        uploadDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation;

        btnUpload = uploadDialog.findViewById(R.id.btnUpload);
        btnUploadClose = uploadDialog.findViewById(R.id.btnUploadClose);
        etDate = uploadDialog.findViewById(R.id.etDate);
        etUnits = uploadDialog.findViewById(R.id.etUnits);
        cbValid = uploadDialog.findViewById(R.id.cbValid);
        //ivCalendar = uploadDialog.findViewById(R.id.ivCalender);

        /*------------------success dialog--------------------*/
        successDialog = new Dialog(CaptureReading.this);
        successDialog.setContentView(R.layout.success_upload_dialog);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            Objects.requireNonNull(successDialog.getWindow()).setBackgroundDrawable(getDrawable(R.drawable.dialog_background));
        }
        successDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        successDialog.setCancelable(true);//when you click the outside of the dialog it will disappear
        successDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation;

        btnSuccessClose = successDialog.findViewById(R.id.btnSuccessClose);

        btnCamera.setOnClickListener(v -> {
            //ask for camera permission
            askCameraPermissions();
        });

        btnGallery.setOnClickListener(v -> {
            Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(gallery, GALLERY_REQUEST_CODE);
        });

        ivUpload.setOnClickListener(v -> {
            etDate.setText(currentDateString);
            uploadDialog.show();
        });

        btnUploadClose.setOnClickListener(v -> {
            //Close Dialog box
            uploadDialog.dismiss();
        });

        btnSuccessClose.setOnClickListener(v -> {
            //Close Dialog box
            successDialog.dismiss();
        });

        ivBack.setOnClickListener(v -> CaptureReading.this.finish());

//        ivCalendar.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                OpenDatePickerDialog();
//            }
//        });

    }

    private void askCameraPermissions()
    {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.CAMERA}, CAMERA_PERM_CODE);
        }
        else
        {
            /*-----Opens our camera-----*/
            dispatchTakePictureIntent();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERM_CODE)
        {
            /*When the user accepts the request for the system to access camera*/
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                /*-----Opens our camera-----*/
                dispatchTakePictureIntent();
            }
            else/*When the user denies the request for the system to access camera*/
            {
                Toast.makeText(this, "Camera Permission is Required to use camera", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        /*-----if statement for the Camera Button request-----*/
        if(requestCode == CAMERA_REQUEST_CODE)
        {
            //sets our captured image to the image view
            if(resultCode == Activity.RESULT_OK)
            {
                File f = new File(currentPhotoPath);
                ivReading.setImageURI(Uri.fromFile(f));
                //Toast.makeText(this, "URL of image is: " + Uri.fromFile(f), Toast.LENGTH_SHORT).show();
            
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri contentUri = Uri.fromFile(f);
                mediaScanIntent.setData(contentUri);
                this.sendBroadcast(mediaScanIntent);

                /*------uploading the image to Firebase------*/
                //uploadImageToFirebase(f.getName(),contentUri);
                if(!f.getName().isEmpty() || !etDate.getText().toString().isEmpty() || !etUnits.getText().toString().isEmpty())
                {
                    btnUpload.setOnClickListener(v -> {
                        uploadImageToFirebase(f.getName(),contentUri);
                        uploadDialog.dismiss();
                    });
                }
                else
                {
                    btnUpload.setOnClickListener(v -> {
                        progressDialog.dismiss();
                        Toast.makeText(CaptureReading.this, "No image to upload", Toast.LENGTH_SHORT).show();
                    });

                }

            }
        }

        /*-----if statement for the Gallery Button request-----*/
        if(requestCode == GALLERY_REQUEST_CODE)
        {
            if(resultCode == Activity.RESULT_OK)
            {
              Uri contentUri = data.getData();
              String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
              String imageFileName = "JPEG_" + timeStamp + "." + getFileExt(contentUri);

                //Toast.makeText(this, "Gallery Image Uri: "+ imageFileName, Toast.LENGTH_SHORT).show();
              ivReading.setImageURI(contentUri);

              //uploading the image to Firebase
               //uploadImageToFirebase(imageFileName,contentUri);
                if(imageFileName != null || !etUnits.getText().toString().isEmpty() || !etUnits.getText().toString().isEmpty())
                {
                    btnUpload.setOnClickListener(v -> {
                        uploadImageToFirebase(imageFileName,contentUri);
                    });
                }
                else
                {
                    progressDialog.dismiss();
                    Toast.makeText(CaptureReading.this, "No image to upload", Toast.LENGTH_SHORT).show();
                }

            }
        }


    }

    private void uploadImageToFirebase(String name, Uri contentUri)
    {
        //contains the path of our image and saves it in firebase images folder
        final StorageReference image = storageReference.child("CapturedReadings/" + name);
        //final StorageReference image = storageReference.child("CapturedReadings/" + firebaseAuth.getCurrentUser().getUid()).child("readings/" + name);

        //mProgressBar.setVisibility(View.VISIBLE);
        progressDialog.setMessage("Uploading...");
        progressDialog.show();

        if(etDate.getText().toString().isEmpty() || etUnits.getText().toString().isEmpty())
        {
            progressDialog.dismiss();
            Toast.makeText(CaptureReading.this, "Please enter enquired fields", Toast.LENGTH_SHORT).show();
        }
        else
        {
            image.putFile(contentUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    /*------Reading details----*/
                     date = etDate.getText().toString().trim();
                     units = etUnits.getText().toString().trim();
                     downloadLink = image.getDownloadUrl().toString();
                     currentUnit = Double.parseDouble(units);

                    SimpleDateFormat format1 =new SimpleDateFormat("dd/MM/yyyy");
                    java.text.DateFormat format2=new SimpleDateFormat("MMMM");

                    Date dt1= null;
                    try {
                        dt1 = format1.parse(date);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    String finalDay = format2.format(dt1);

                    /*------------------------- Winter cost calculation ------------------------*/
                    if(finalDay.equals("March") || finalDay.equals("April") || finalDay.equals("May")
                            ||finalDay.equals("June") || finalDay.equals("July") || finalDay.equals("August"))
                    {
                        if(currentUnit != 0)
                        {
                            //Block1 cost calculation
                            if(currentUnit <= 350)
                            {
                                totalCost = currentUnit * bill_report.winterUnitCostBlock1;
                                totalCostPerUnit = String.valueOf(df2.format(totalCost));
                            }
                            //Block2 cost calculation
                            else
                            {
                                totalCost = currentUnit * bill_report.winterUnitCostBlock2;
                                totalCostPerUnit = String.valueOf(df2.format(totalCost));
                            }
                        }
                        else
                        {
                            Toast.makeText(bill_report, "Please enter the units in correct format: '0.00'", Toast.LENGTH_SHORT).show();
                        }

                    }
                    /*------------------------- Winter cost calculation ------------------------*/
                    else if (finalDay.equals("September") || finalDay.equals("October") || finalDay.equals("November")
                            ||finalDay.equals("December") || finalDay.equals("January") || finalDay.equals("February"))
                    {

                        if(currentUnit != 0)
                        {
                            //Block1 cost calculation
                            if(currentUnit <= 350 && currentUnit != 0)
                            {
                                totalCost = currentUnit * bill_report.summerUnitCostBlock1;
                                totalCostPerUnit = String.valueOf(df2.format(totalCost));
                            }
                            //Block2 cost calculation
                            else
                            {
                                totalCost = currentUnit * bill_report.summerUnitCostBlock2;
                                totalCostPerUnit = String.valueOf(df2.format(totalCost));

                            }
                        }
                        else
                        {
                            Toast.makeText(bill_report, "Please enter the units in correct format: '0.00'", Toast.LENGTH_SHORT).show();
                        }


                    }


                    // Check which checkbox was clicked
                    if(cbValid.isChecked())
                        valid = "true";
                    else
                        valid = "false";


                    /*checks if the image is successfully uploaded*/
                    image.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {

                            //getting the image from the database in our ImageView using Picasso
                            //Picasso.with(CaptureReading.this).load(uri).into(ivReading);
                            //Toast.makeText(CaptureReading.this, "image taken from picasso", Toast.LENGTH_SHORT).show();
                        }
                    });
                    //Toast.makeText(CaptureReading.this, "Image Uploaded", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();

                    uploadDialog.dismiss();
                    successDialog.show();

                    CapturedReadings capturedReading = new CapturedReadings(id,downloadLink, taskSnapshot.getUploadSessionUri().toString(), units, date, totalCostPerUnit, valid);
                    String imageUploadedId = databaseReference.push().getKey();
                    assert imageUploadedId != null;
                    databaseReference.child("CapturedReadings").child(imageUploadedId).setValue(capturedReading);
                }
            }).addOnFailureListener(e ->
                    Toast.makeText(CaptureReading.this, "Upload Failed", Toast.LENGTH_SHORT).show()

            ).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                    mProgressBar.setProgress((int) progress);
                }
            });
        }

    }

//    private void OpenDatePickerDialog()
//    {
//        Calendar cal = Calendar.getInstance();
//        int year = cal.get(Calendar.YEAR);
//        int month = cal.get(Calendar.MONTH);
//        int day = cal.get(Calendar.DAY_OF_MONTH);
//
//        DatePickerDialog dialog = new DatePickerDialog(
//                CaptureReading.this,
//                R.style.Theme_AppCompat_Light_Dialog_MinWidth,
//                onDateSetListener,
//                year, month, day);
//        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
//        dialog.show();
//
//        onDateSetListener = new DatePickerDialog.OnDateSetListener() {
//            @Override
//            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
//                month = month+1;
//
//                String date = dayOfMonth + "/" + month + "/" + year;
//                //Toast.makeText(CaptureReading.this, "" + date, Toast.LENGTH_SHORT).show();
//                etDate.setText(date);
//            }
//        };
//
//
//    }

    private String getFileExt(Uri contentUri)
    {
        //This method takes the different file extensions of images
        ContentResolver c = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();

        return mime.getExtensionFromMimeType(c.getType(contentUri));
    }

    private File createImageFile() throws IOException
    {
        //Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        //File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        //saves the image to our Gallery
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName, /*prefix*/
                ".jpg", /*suffix*/
                storageDir /*directory*/
        );

        //Save a file: path for yse with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @SuppressLint("QueryPermissionsNeeded")
    private void dispatchTakePictureIntent()
    {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
            }
        }
    }
}