package com.example.smartmeterbiller.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.smartmeterbiller.Login;
import com.example.smartmeterbiller.R;
import com.example.smartmeterbiller.classes.Customers;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CustomerProfile extends AppCompatActivity {

    public static final int GALLERY_REQUEST_CODE = 1;
    ImageView ivProfilePhoto, ivProfilePhotoChange;
    TextView tvFullNameProfile, tvEmailAddressProfile, tvHomeAddressProfile, tvMeterNumberProfile, tvPhoneNumberProfile,
    tvSuccessMessage;
    EditText etNameChange, etSurnameChange, etHomeAddressChange, etMeterNumberChange, etPhoneNumberChange;
    Button btnEdit, btnLogoutProfile, btnSave, btnSuccessUpdateClose, btnLogout, btnCancelLogout;
    ImageView ivBack;

    Dialog editProfileDialog, successUpdateDialog, logout_dialog;

    ProgressBar progressBar;
    private ProgressDialog progressDialog;

    FirebaseAuth firebaseAuth;
    FirebaseStorage firebaseStorage;
    FirebaseFirestore firebaseFirestore;
    FirebaseUser user;
    StorageReference storageReference;
    DatabaseReference userRer, userDatabaseRef;

    String DISPLAY_NAME = null;

    /*----------strings I use to retrieve Customer(user) details from realtime database------------*/
    String userID, id, name, surname, emailAddress,
    homeAddress, meterNumber, newName,
    newSurname, newHomeAddress, newMeterNumber, phoneNumber, newPhoneNumber;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_customer_profile);

        /*---------------- ImageViews ------------------*/
        ivProfilePhoto = findViewById(R.id.ivProfilePhoto);
        ivProfilePhotoChange = findViewById(R.id.ivProfilePhotoChange);
        ivBack = findViewById(R.id.ivBack);

        /*---------------- TextViews ------------------*/
        tvFullNameProfile = findViewById(R.id.tvFullNameProfile);
        tvEmailAddressProfile = findViewById(R.id.tvEmailAddressProfile);
        tvHomeAddressProfile = findViewById(R.id.tvHomeAddressProfile);
        tvMeterNumberProfile = findViewById(R.id.tvMeterNumberProfile);
        tvPhoneNumberProfile = findViewById(R.id.tvPhoneNumberProfile);

        /*---------------- Buttons ------------------*/
        btnEdit = findViewById(R.id.btnEdit);
        btnLogoutProfile = findViewById(R.id.btnLogoutProfile);

        /*---------------- Firebase ------------------*/
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        user = FirebaseAuth.getInstance().getCurrentUser();
        userID = firebaseAuth.getCurrentUser().getUid();
        userRer = FirebaseDatabase.getInstance().getReference().child("Customers");


        //use a progress bar
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait while Updating Profile in...");
        progressDialog.setTitle("Profile Update");
        progressDialog.setCanceledOnTouchOutside(false);

        //get current user
        if(user != null && user.isEmailVerified())
        {
            DISPLAY_NAME = user.getDisplayName();
        }

        /*------------------Success Upload Dialog--------------------*/
        editProfileDialog = new Dialog(CustomerProfile.this);
        editProfileDialog.setContentView(R.layout.edit_profile_dialog_box);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            Objects.requireNonNull(editProfileDialog.getWindow()).setBackgroundDrawable(getDrawable(R.drawable.dialog_background));
        }
        editProfileDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        editProfileDialog.setCancelable(true);//when you click the outside of the dialog it will disappear
        editProfileDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation;

        /*---------------- Dialog Buttons ------------------*/
        btnSave = editProfileDialog.findViewById(R.id.btnSave);

        /*---------------- Dialog EditTexts ------------------*/
        etNameChange = editProfileDialog.findViewById(R.id.etNameChange);
        etSurnameChange = editProfileDialog.findViewById(R.id.etSurnameChange);
        etHomeAddressChange = editProfileDialog.findViewById(R.id.etHomeAddressChange);
        etMeterNumberChange = editProfileDialog.findViewById(R.id.etMeterNumberChange);
        etPhoneNumberChange = editProfileDialog.findViewById(R.id.etPhoneNumberChange);

        /*------------- Success Update Dialog -------------*/
        successUpdateDialog = new Dialog(CustomerProfile.this);
        successUpdateDialog.setContentView(R.layout.success_profile_update);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            Objects.requireNonNull(successUpdateDialog.getWindow()).setBackgroundDrawable(getDrawable(R.drawable.dialog_background));
        }
        successUpdateDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        successUpdateDialog.setCancelable(true);//when you click the outside of the dialog it will disappear
        successUpdateDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation;

        /*---------------- Dialog Buttons & TextView------------------*/
        btnSuccessUpdateClose = successUpdateDialog.findViewById(R.id.btnSuccessUpdateClose);
        tvSuccessMessage = successUpdateDialog.findViewById(R.id.textSuccess);

        /*------------------logout dialog--------------------*/
        logout_dialog = new Dialog(CustomerProfile.this);
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

        /*---------------- ProgressBar ------------------*/
        progressBar = findViewById(R.id.profileProgressBar);

        //call the method that gets the users details
        readUserData();

        //load the profile image from the database
        StorageReference profileRef = storageReference.child("Customers/" + firebaseAuth.getCurrentUser().getUid() + "/profile").child("profile.jpg");
        progressBar.setVisibility(View.VISIBLE);
        profileRef.getDownloadUrl().addOnSuccessListener(uri -> {
            //Picasso.get().load(uri).into(ivProfilePhotoChange);
            Picasso.with(CustomerProfile.this).load(uri).into(ivProfilePhotoChange);
            progressBar.setVisibility(View.GONE);
        });

        ivProfilePhoto.setOnClickListener(v -> {

            //edits the user profile photo
            changeProfilePhoto();

        });

        btnEdit.setOnClickListener(v -> {

            //opens the edit profile dialog boxes
            etNameChange.setText(name);
            etSurnameChange.setText(surname);
            etMeterNumberChange.setText(meterNumber);
            etHomeAddressChange.setText(homeAddress);
            etPhoneNumberChange.setText(phoneNumber);
            editProfileDialog.show();
        });

        btnLogoutProfile.setOnClickListener(v -> {
            //displays the logout dialog
            logout_dialog.show();
        });

        btnLogout.setOnClickListener(v -> {
            //logs user out
            logout();
        });

        btnCancelLogout.setOnClickListener(v -> logout_dialog.dismiss());

        btnSave.setOnClickListener(v -> {

            //sets the updated details in the profile
            saveChangedDetails();

            //closes the dialog
            editProfileDialog.dismiss();
        });

        btnSuccessUpdateClose.setOnClickListener(v -> successUpdateDialog.dismiss());

        ivBack.setOnClickListener(v -> CustomerProfile.this.finish());
        
    }

    private void changeProfilePhoto()
    {
        //take user to gallery
        Intent openGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(openGallery, GALLERY_REQUEST_CODE);

    }

    private void readUserData()
    {
        /*---------------Reads the data entered when the user registered----------------*/
        userDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Customers");
        userDatabaseRef.addValueEventListener(new ValueEventListener() {
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
                    phoneNumber = customer.getPhoneNumber();


                    if(userID.equals(id))
                    {
                        if(name == null || surname == null)
                        {
                            tvFullNameProfile.setText("No data to display");
                        }
                        else
                        {
                            tvFullNameProfile.setText(name + " " + surname);
                        }
                        if(emailAddress == null)
                        {
                            tvEmailAddressProfile.setText("No data to display");
                        }
                        else
                        {
                            tvEmailAddressProfile.setText(emailAddress);
                        }
                        if(homeAddress == null)
                        {
                            tvHomeAddressProfile.setText("No data to display");
                        }
                        else
                        {
                            tvHomeAddressProfile.setText(homeAddress);
                        }
                        if(meterNumber == null)
                        {
                            tvMeterNumberProfile.setText("No data to display");
                        }
                        else
                        {
                            tvMeterNumberProfile.setText(meterNumber);
                        }
                        if(phoneNumber == null)
                        {
                            tvPhoneNumberProfile.setText("No data to display");
                        }
                        else
                        {
                            tvPhoneNumberProfile.setText(phoneNumber);
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_REQUEST_CODE)
        {
            if(resultCode == Activity.RESULT_OK)
            {
                //get the URI of the image & set it to our image view
                Uri imageUri = data.getData();
                
                //ivProfilePhotoChange.setImageURI(imageUri);

                //upload image to firebase
                uploadImageToFirebase(imageUri);
            }
        }

    }

    private void uploadImageToFirebase(Uri imageUri)
    {
        progressBar.setVisibility(View.VISIBLE);

        //upload image to firebase
        //firebaseAuth.getCurrentUser().getUid() saves the entry with the current user that's logged in
        StorageReference fileReference = storageReference.child("Customers/" + firebaseAuth.getCurrentUser().getUid() + "/profile").child("profile.jpg");
        fileReference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        //Picasso.get().load(uri).into(ivProfilePhotoChange);
                        Picasso.with(CustomerProfile.this).load(uri).into(ivProfilePhotoChange);
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(CustomerProfile.this, "Upload Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /*Updating the user details*/
    private void saveChangedDetails()
    {
         newName = etNameChange.getText().toString().trim();
         newSurname = etSurnameChange.getText().toString().trim();
         newHomeAddress = etHomeAddressChange.getText().toString().trim();
         newMeterNumber = etMeterNumberChange.getText().toString().trim();
         newPhoneNumber = etPhoneNumberChange.getText().toString().trim();

        if(!newName.equals(name))
        {
            HashMap nameHashMap = new HashMap();
            nameHashMap.put("name", newName);
            userDatabaseRef.child(userID).updateChildren(nameHashMap).addOnSuccessListener(o -> successUpdateDialog.show());
        }

        if(!newSurname.equals(surname))
        {
            HashMap surnameHashMap = new HashMap();
            surnameHashMap.put("surname", newSurname);
            userDatabaseRef.child(userID).updateChildren(surnameHashMap).addOnSuccessListener(o -> successUpdateDialog.show());
        }

        if(!newHomeAddress.equals(homeAddress))
        {
            HashMap homeAddressHashMap = new HashMap();
            homeAddressHashMap.put("homeAddress", newHomeAddress);
            userDatabaseRef.child(userID).updateChildren(homeAddressHashMap).addOnSuccessListener(o -> successUpdateDialog.show());
        }


        if(!newPhoneNumber.equals(phoneNumber))
        {
            HashMap phoneNumberHashMap = new HashMap();
            phoneNumberHashMap.put("phoneNumber", newPhoneNumber);
            userDatabaseRef.child(userID).updateChildren(phoneNumberHashMap).addOnSuccessListener(o -> successUpdateDialog.show());
        }


        if(!newMeterNumber.equals(meterNumber))
        {
            HashMap meterNumberHashMap = new HashMap();
            meterNumberHashMap.put("meterNumber", newMeterNumber);
            userDatabaseRef.child(userID).updateChildren(meterNumberHashMap).addOnSuccessListener(o -> successUpdateDialog.show());
        }


    }


    /*Logging the user out*/
    private void logout()
    {
        Toast.makeText(this, "User Logged Out", Toast.LENGTH_SHORT).show();

        FirebaseAuth.getInstance().signOut();//used for logging out the user
        startActivity(new Intent(getApplicationContext(), Login.class));
        finish();
    }

    //closes the activity when the user presses the phone 'back' button
    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }
}