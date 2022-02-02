package com.example.smartmeterbiller.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.allyants.notifyme.NotifyMe;
import com.example.smartmeterbiller.R;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.util.Calendar;

public class Alerts extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    ImageView ivBack;

    /*------------ Instant Fields --------------*/
    Calendar now = Calendar.getInstance();
    TimePickerDialog tpd;
    DatePickerDialog dpd;
    EditText etNotificationDescription, etNotificationMessage;
    Button btnCancelNotification, btnScheduleNotification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_alerts);

        ivBack = findViewById(R.id.ivBack);
        etNotificationDescription = findViewById(R.id.etNotificationDescription);
        etNotificationMessage = findViewById(R.id.etNotificationMessage);
        btnCancelNotification = findViewById(R.id.btnCancelNotification);
        btnScheduleNotification = findViewById(R.id.btnScheduleNotification);

        ivBack.setOnClickListener(v -> Alerts.this.finish());

        btnScheduleNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dpd.show(getFragmentManager(), "Datepickerdialog");
            }
        });

        btnCancelNotification.setOnClickListener(v -> ClearTheData());

        //get current date and set it to DatePickerDialog
        dpd = DatePickerDialog.newInstance(
                Alerts.this,
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
        );

        //initialize timepickerdialog with current time
        tpd = TimePickerDialog.newInstance(
                Alerts.this,
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                now.get(Calendar.SECOND),
                false
        );
    }

    private void ClearTheData()
    {
        if(!etNotificationDescription.getText().toString().isEmpty() || !etNotificationMessage.getText().toString().isEmpty())
        {
            etNotificationMessage.setText("");
            etNotificationDescription.setText("");
        }

        NotifyMe.cancel(getApplicationContext(), "test");

    }


    /*------------- setting custom date to datepickerdialog ----------------*/
    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {

        now.set(Calendar.YEAR, year);
        now.set(Calendar.MONTH, monthOfYear);
        now.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        tpd.show(getFragmentManager(), "Timepickerdialog");
    }

    /*------------- setting custom time to timepickerdialog ----------------*/
    @Override
    public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {

        now.set(Calendar.HOUR_OF_DAY, hourOfDay);
        now.set(Calendar.MINUTE, minute);
        now.set(Calendar.SECOND, second);

        //initialize notification
        NotifyMe notifyMe = new NotifyMe.Builder(getApplicationContext())
                .title(etNotificationDescription.getText().toString())
                .content(etNotificationMessage.getText().toString())
                .color(255,0,0,255)
                .led_color(255, 255, 255, 255)
                .time(now)
                .addAction(new Intent(), "Snooze", false)
                .key("test")
                .addAction(new Intent(), "Dismiss", true, false)
                .addAction(new Intent(), "Done")
                .large_icon(R.mipmap.ic_launcher_round)
                .build();

    }
}