package com.example.smartmeterbiller.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.smartmeterbiller.Login;
import com.example.smartmeterbiller.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class ForgotPassword extends AppCompatActivity {

    Dialog successDialog;
    Button btnSubmit, btnSuccessClose;
    TextView textSuccess;
    EditText etResetEmail;
    ImageView ivBack;

    FirebaseAuth firebaseAuth;

    View progressBarLayout, contentLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        /*------------------ Hooks -----------------------*/
        etResetEmail = findViewById(R.id.etResetEmail);
        btnSubmit = findViewById(R.id.btnSubmit);
        ivBack = findViewById(R.id.ivBack);

        /*------------------Progressbar layout Hooks-----------------------*/
        progressBarLayout = findViewById(R.id.progressBarLayout);
        contentLayout = findViewById(R.id.contentLayout);

        /*------------------Firebase authentication Hooks-----------------------*/
        firebaseAuth = FirebaseAuth.getInstance();

        /*------------------success dialog--------------------*/
        successDialog = new Dialog(ForgotPassword.this);
        successDialog.setContentView(R.layout.success_upload_dialog);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            Objects.requireNonNull(successDialog.getWindow()).setBackgroundDrawable(getDrawable(R.drawable.dialog_background));
        }
        successDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        successDialog.setCancelable(true);//when you click the outside of the dialog it will disappear
        successDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation;

        btnSuccessClose = successDialog.findViewById(R.id.btnSuccessClose);
        textSuccess = successDialog.findViewById(R.id.textSuccess);

        btnSuccessClose.setOnClickListener(v -> successDialog.dismiss());

        ivBack.setOnClickListener(v -> ForgotPassword.this.finish());


        btnSubmit.setOnClickListener(v -> {

            //test if the user entered a email address or not
            if (etResetEmail.getText().toString().isEmpty()) {
                Toast.makeText(ForgotPassword.this, "please enter your email address", Toast.LENGTH_SHORT).show();
            } else {
                //hides the progressbar and displays the content layout of Student Home Activity
                progressBarLayout.setVisibility(View.VISIBLE);
                contentLayout.setVisibility(View.GONE);

                String resetEmail = etResetEmail.getText().toString().trim();

                //sent a request to firebase to reset the users password
                firebaseAuth.sendPasswordResetEmail(etResetEmail.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        //if task of sending 'forgot password' request to firebase is successful,
                        //- progressbar will disappear
                        //- Student Homepage content will appear
                        //a toast will appear stating that the password is sent successfully to the users email address
                        if (task.isSuccessful()) {
                            progressBarLayout.setVisibility(View.GONE);
                            contentLayout.setVisibility(View.VISIBLE);

                            textSuccess.setText("Password Reset Link Successfully Sent To " + resetEmail);
                            successDialog.show();
                            Toast.makeText(ForgotPassword.this, "password successfully sent to your email address",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            //if task of sending 'forgot password' request to firebase is unsuccessful,
                            //- progressbar will disappear
                            //- Student Homepage content will appear
                            //a toast will appear stating that the password not sent to the users email address stating reason why
                            progressBarLayout.setVisibility(View.GONE);
                            contentLayout.setVisibility(View.VISIBLE);
                            Toast.makeText(ForgotPassword.this, "Error: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }

        });
    }

    //for when the user presses the back button in current activity, the activity finishes and goes back to login activity
    @Override
    public void onBackPressed() {

        startActivity(new Intent(getApplicationContext(), Login.class));
        finish();

        super.onBackPressed();
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//
//        getMenuInflater().inflate(R.menu.forgot_password, menu);
//
//        return super.onCreateOptionsMenu(menu);
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//
//        if (item.getItemId() == R.id.back) {
//            startActivity(new Intent(getApplicationContext(), Login.class));
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
}
