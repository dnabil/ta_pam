package xyz.bbabakz.tapam.activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;

import java.util.Calendar;

import xyz.bbabakz.tapam.R;
import xyz.bbabakz.tapam.model.Presence;
import xyz.bbabakz.tapam.util.FirebaseAuthUtil;
import xyz.bbabakz.tapam.util.LoadingDialogUtil;

public class CreatePresenceActivity extends AppCompatActivity {

    String roomId;
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    CollectionReference presenceCollection;
    LoadingDialogUtil loadingDialog;
    ImageView imgBarBack;
    TextView tvBarTitle;

    TextView etPresenceTitle, etPresenceDescription;
    TextView etStartTime, etStartDate;
    TextView etEndTime, etEndDate;

    private class DateTimePicker {
        int hourOfDay, minute;
        int year, monthOfYear, dayOfMonth;
    }

    DateTimePicker startDateTime;
    DateTimePicker endDateTime;

    AppCompatButton btnCreate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_presence);
        roomId = getIntent().getStringExtra("roomId");
        if (roomId == null || roomId == "") {
            Toast.makeText(getApplicationContext(), "Room doesn't exists", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mAuth = FirebaseAuth.getInstance();
        if (!FirebaseAuthUtil.auth(mAuth, this)) {
            return;
        }
        db = FirebaseFirestore.getInstance();
        presenceCollection = db.collection(Presence.COLLECTION_NAME);
        loadingDialog = new LoadingDialogUtil(this);

        imgBarBack = findViewById(R.id.img_back);
        imgBarBack.setOnClickListener(view -> finish());
        tvBarTitle = findViewById(R.id.tv_barTitle);
        tvBarTitle.setText("Create presence");

        etPresenceTitle = findViewById(R.id.et_presenceTitle);
        etPresenceDescription = findViewById(R.id.et_presenceDescription);

        startDateTime = new DateTimePicker();
        etStartTime = findViewById(R.id.et_startTime);
        etStartTime.setOnClickListener(view -> {
            Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    CreatePresenceActivity.this,
                    new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                            etStartTime.setText(String.format("%02d:%02d", hourOfDay, minute));
                            startDateTime.hourOfDay = hourOfDay;
                            startDateTime.minute = minute;
                        }
                    },
                    hour,
                    minute,
                    false
            );
            timePickerDialog.show();
        });
        etStartDate = findViewById(R.id.et_startDate);
        etStartDate.setOnClickListener(view -> {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    // on below line we are passing context.
                    CreatePresenceActivity.this,
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            etStartDate.setText(String.format("%02d-%02d-%04d", dayOfMonth, ++monthOfYear, year));
                            startDateTime.year = year;
                            startDateTime.monthOfYear = monthOfYear;
                            startDateTime.dayOfMonth = dayOfMonth;
                        }
                    },
                    // on below line we are passing year,
                    // month and day for selected date in our date picker.
                    year, month, day);
            datePickerDialog.show();
        });


        endDateTime = new DateTimePicker();
        etEndTime = findViewById(R.id.et_endTime);
        etEndTime.setOnClickListener(view -> {
            Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    CreatePresenceActivity.this,
                    new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                            etEndTime.setText(String.format("%02d:%02d", hourOfDay, minute));
                            endDateTime.hourOfDay = hourOfDay;
                            endDateTime.minute = minute;
                        }
                    },
                    hour,
                    minute,
                    false
            );
            timePickerDialog.show();
        });
        etEndDate = findViewById(R.id.et_endDate);
        etEndDate.setOnClickListener(view -> {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    // on below line we are passing context.
                    CreatePresenceActivity.this,
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            etEndDate.setText(String.format("%02d-%02d-%04d", dayOfMonth, ++monthOfYear, year));
                            endDateTime.year = year;
                            endDateTime.monthOfYear = monthOfYear;
                            endDateTime.dayOfMonth = dayOfMonth;
                        }
                    },
                    // on below line we are passing year,
                    // month and day for selected date in our date picker.
                    year, month, day);
            datePickerDialog.show();
        });

        btnCreate = findViewById(R.id.btn_createPresence);
        btnCreate.setOnClickListener(view -> {
            if (!validateForm())
                return;

            loadingDialog.startLoading();
            // karena data udah divalidasi, dipastikan
            // semuanya ada.
            Calendar calStart = Calendar.getInstance();
            calStart.set(
                    startDateTime.year, startDateTime.monthOfYear, startDateTime.dayOfMonth,
                    startDateTime.hourOfDay, startDateTime.minute
            );
            Timestamp startTimestamp = new Timestamp(calStart.getTime());

            Calendar calEnd = Calendar.getInstance();
            calEnd.set(endDateTime.year, endDateTime.monthOfYear, endDateTime.dayOfMonth,
                    endDateTime.hourOfDay, endDateTime.minute
            );
            Timestamp endTimestamp = new Timestamp(calEnd.getTime());

            // validate the time
            if (!Presence.validateTime(startTimestamp, endTimestamp)) {
                etStartTime.setError("not valid");
                etStartDate.setError("not valid");
                etEndTime.setError("not valid");
                etEndDate.setError("not valid");
                Toast.makeText(
                        this,
                        "Start time must be lower than end time.",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            db.runTransaction(new Transaction.Function<Void>() {
                        @Nullable
                        @Override
                        public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                            DocumentReference docRef = presenceCollection.document(); // Generate a unique ID

                            // Set the data for the document
                            Presence p = new Presence(
                                    docRef.getId(),
                                    etPresenceTitle.getText().toString(),
                                    etPresenceDescription.getText().toString(),
                                    roomId,
                                    startTimestamp,
                                    endTimestamp
                            );
                            transaction.set(docRef, p);
                            return null;
                        }
                    })
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(getApplicationContext(), "Presence created", Toast.LENGTH_SHORT).show();
                            // Transaction succeeded
                            loadingDialog.stopLoading();
                            finish();
                            return;
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            String msg = "failed to create presence";
                            Log.e("tapam", msg + " " + e);
                            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                            // Transaction failed
                            loadingDialog.stopLoading();
                            finish();
                            return;
                        }
                    });
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!FirebaseAuthUtil.auth(mAuth, this)) {
            return;
        }
    }


    //
    private boolean validateForm() {
        String required = "required";
        boolean result = true;
        if (TextUtils.isEmpty(etPresenceTitle.getText().toString())) {
            etPresenceTitle.setError(required);
            result = false;
        }
        if (TextUtils.isEmpty(etStartTime.getText().toString())) {
            etStartTime.setError(required);
            result = false;
        }
        if (TextUtils.isEmpty(etStartDate.getText().toString())) {
            etStartDate.setError(required);
            result = false;
        }
        if (TextUtils.isEmpty(etEndTime.getText().toString())) {
            etEndTime.setError(required);
            result = false;
        }
        if (TextUtils.isEmpty(etEndDate.getText().toString())) {
            etEndDate.setError(required);
            result = false;
        }
        return result;
    }
}