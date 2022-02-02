package com.example.smartmeterbiller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.smartmeterbiller.activities.Home;
import com.example.smartmeterbiller.classes.Customers;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class Register extends AppCompatActivity {

    TextView tvLoginPage;
    EditText etName, etSurname, etHomeAddress, etMeterNumber, etPhoneNumber, etEmail, etPassword, etConfirmPassword;
    Button btnRegister, btnEmailVerificationClose;
    Dialog emailVerificationDialog;

    View progressBarLayout, contentLayout;

    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    ProgressDialog progressDialog;

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    FirebaseDatabase rootNode;
    DatabaseReference reference;
    FirebaseFirestore firestore;
    String userID;

    /*----------strings I use to create & retrieve Customer(user) details from realtime database------------*/
    String id, name, surname, emailAddress, homeAddress, meterNumber, password, confirmPassword, phoneNumber;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_register);

        //TextViews
        tvLoginPage = findViewById(R.id.tvLoginPage);

        //EditTexts
        etName = findViewById(R.id.etName);
        etSurname = findViewById(R.id.etSurname);
        etHomeAddress = findViewById(R.id.etHomeAddress);
        etMeterNumber = findViewById(R.id.etMeterNumber);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        //Buttons
        btnRegister = findViewById(R.id.btnRegister);

        //Progress dialog/bar
        progressDialog = new ProgressDialog(this);
        progressBarLayout = findViewById(R.id.progressBarLayout);
        contentLayout = findViewById(R.id.contentLayout);

        //Firebase authentication
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        firestore = FirebaseFirestore.getInstance();

        /*------------------email verification dialog--------------------*/
        emailVerificationDialog = new Dialog(Register.this);
        emailVerificationDialog.setContentView(R.layout.email_verification_dialog);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            Objects.requireNonNull(emailVerificationDialog.getWindow()).setBackgroundDrawable(getDrawable(R.drawable.dialog_background));
        }
        emailVerificationDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        emailVerificationDialog.setCancelable(true);//when you click the outside of the dialog it will disappear
        emailVerificationDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation;

        btnEmailVerificationClose = emailVerificationDialog.findViewById(R.id.btnEmailVerificationClose);

        btnRegister.setOnClickListener(v -> {
            Authentication();
        });


        tvLoginPage.setOnClickListener(v -> {
            Intent intent = new Intent(Register.this, Login.class);
            startActivity(intent);
        });

        btnEmailVerificationClose.setOnClickListener(v -> {
            emailVerificationDialog.dismiss();
            Register.this.finish();
        });
    }

    private void Authentication()
    {
        name = etName.getText().toString().trim();
        surname = etSurname.getText().toString().trim();
        homeAddress = etHomeAddress.getText().toString().trim();
        meterNumber = etMeterNumber.getText().toString().trim();
        phoneNumber = etPhoneNumber.getText().toString().trim();
        emailAddress = etEmail.getText().toString().trim();
        password = etPassword.getText().toString().trim();
        confirmPassword = etConfirmPassword.getText().toString().trim();

        if(TextUtils.isEmpty(name))
        {
            etName.setError("Name address is Required");
            return;
        }
        else if(TextUtils.isEmpty(surname))
        {
            etSurname.setError("Surname address is Required");
            return;
        }
        else if(TextUtils.isEmpty(meterNumber))
        {
            etMeterNumber.setError("Meter Number is Required");
            return;
        }
        else if(etPhoneNumber.getText().toString().isEmpty())
        {
            etPhoneNumber.setError("Phone Number is Required");
            return;
        }
        else if(TextUtils.isEmpty(homeAddress))
        {
            etHomeAddress.setError("Home address is Required");
            return;
        }

        if(!emailAddress.matches(emailPattern))
        {
            etEmail.setError("Enter The Correct Email Address");
            //Toast.makeText(this, "", Toast.LENGTH_SHORT).show();
        }
        else if(password.isEmpty() || password.length()<6)
        {
            etPassword.setError("Enter The Correct Password");
        }
        else if(!password.equals(confirmPassword))
        {
            etPassword.setError("Passwords Do Not Match");
        }
        else
        {
            rootNode = FirebaseDatabase.getInstance();
            reference = rootNode.getReference("Customers");

            //use a progress bar
            progressDialog.setMessage("Please wait while Registering...");
            progressDialog.setTitle("Registration");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            firebaseAuth.createUserWithEmailAndPassword(emailAddress, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if(task.isSuccessful())
                    {
                        //send the verification email link
                        firebaseAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if(task.isSuccessful())
                                {
                                    //gets the user id of the current logged in user
                                    userID = firebaseAuth.getCurrentUser().getUid();
                                    id = userID;

                                    //saving the user with realtime database
                                    Customers customer = new Customers(id, name, surname, homeAddress, meterNumber, emailAddress, phoneNumber);

                                    reference.child(id).setValue(customer);

                                    clearData();
                                    emailVerificationDialog.show();

                                    progressDialog.dismiss();
                                    //sendUserToHomeActivity();

                                }
                                else
                                {
                                    progressDialog.dismiss();
                                    Toast.makeText(Register.this, "Error; " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }

                            }
                        });
                    }
                    else //if the user already exists in the database an error message will show
                    {
                        progressDialog.dismiss();
                        Toast.makeText(Register.this, "Error: " + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
    }

    private void clearData()
    {
        etName.setText("");
        etSurname.setText("");
        etHomeAddress.setText("");
        etMeterNumber.setText("");
        etPhoneNumber.setText("");
        etEmail.setText("");
        etPassword.setText("");
        etConfirmPassword.setText("");
    }

    private void sendUserToHomeActivity()
    {
        //this intent will activate when the user successfully registers
        Intent intent = new Intent (Register.this, Home.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}