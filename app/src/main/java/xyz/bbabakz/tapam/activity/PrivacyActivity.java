package xyz.bbabakz.tapam.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import xyz.bbabakz.tapam.R;
import xyz.bbabakz.tapam.util.FirebaseAuthUtil;
import xyz.bbabakz.tapam.util.LoadingDialogUtil;

public class PrivacyActivity extends AppCompatActivity {

    private ImageView imgBarBack;
    private TextView tvBarTitle;

    LoadingDialogUtil loadingDialog;

    ImageView imgProfilePicture, imgEditProfilePicture;
    Uri uploadedImgUri;
    EditText etName;
    AppCompatButton btnUpdate;

    FirebaseAuth mAuth;
    StorageReference storageRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy);
        mAuth = FirebaseAuth.getInstance();
        if(!FirebaseAuthUtil.auth(mAuth, this)){
            return;
        }
        FirebaseUser user = mAuth.getCurrentUser();
        storageRef = FirebaseStorage.getInstance().getReference();

        loadingDialog = new LoadingDialogUtil(this);

        imgBarBack = findViewById(R.id.img_back);
        imgBarBack.setOnClickListener(view -> finish());

        tvBarTitle = findViewById(R.id.tv_barTitle);
        tvBarTitle.setText("Privacy");

        imgProfilePicture = findViewById(R.id.img_profilePicture);
        Glide.with(this).load(user.getPhotoUrl()).circleCrop().into(imgProfilePicture);

        imgEditProfilePicture = findViewById(R.id.img_editProfilePicture);
        ActivityResultLauncher<Intent> imgUploadLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.StartActivityForResult(),
                        result -> {
                            ExecutorService executor = Executors.newSingleThreadExecutor();
                            Handler handler = new Handler(Looper.getMainLooper());

                            int code = result.getResultCode();
                            Intent data = result.getData();

                            if (data == null || !(code == Activity.RESULT_OK)) {
                                return;
                            }
                            uploadedImgUri = data.getData();

                            loadingDialog.startLoading();
                            Glide.with(this).load(uploadedImgUri).circleCrop().into(imgProfilePicture);
                            StorageReference imgRef = storageRef.child("/user/profile_pic/" + user.getUid() + ".jpeg");
                            imgRef.putFile(uploadedImgUri)
                                    .addOnSuccessListener(taskSnapshot -> {
                                        // get url of the img
                                        imgRef.getDownloadUrl()
                                                .addOnSuccessListener(uri -> {
                                                    // updates the photo
                                                    UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                                                            .setPhotoUri(uri)
                                                            .build();

                                                    mAuth.getCurrentUser()
                                                            .updateProfile(profileUpdate)
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void unused) {
                                                                    handler.post(() -> {
                                                                        Toast.makeText(getApplicationContext(),
                                                                                "Photo profile updated",
                                                                                Toast.LENGTH_SHORT).show();
                                                                        loadingDialog.stopLoading();
                                                                    });
                                                                }
                                                            })
                                                            .addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Log.e("tapam", "updating photo profile failed: " + e);
                                                                    handler.post(() -> {
                                                                        Toast.makeText(PrivacyActivity.this,
                                                                                "failed to update photo profile, retry again soon",
                                                                                Toast.LENGTH_SHORT).show();
                                                                        loadingDialog.stopLoading();
                                                                    });
                                                                }
                                                            });
                                                })
                                                .addOnFailureListener(e -> {
                                                    // when failed getting url image
                                                    handler.post(() -> {
                                                        loadingDialog.stopLoading();
                                                        String msg = "failed to get img url, try again later";
                                                        Log.e("tapam", String.format("%s %s", msg, e));
                                                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                                                        finish();
                                                    });
                                                });
                                    })
                                    .addOnFailureListener(e -> {
                                        // when failed uploading image
                                        handler.post(() -> {
                                            loadingDialog.stopLoading();
                                            String msg = "failed uploading img, try again later";
                                            Log.e("tapam", String.format("%s %s", msg, e));
                                            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                                            finish();
                                        });
                                    });
                        }
                );
        imgEditProfilePicture.setOnClickListener(view -> {
            Intent imgUpload = new Intent(Intent.ACTION_GET_CONTENT);
            imgUpload.setType("image/jpeg"); //jpeg files only
            imgUploadLauncher.launch(imgUpload);
        });

        etName = findViewById(R.id.et_name);
        etName.setText(user.getDisplayName());

        btnUpdate = findViewById(R.id.btn_update);
        btnUpdate.setOnClickListener(view -> {
            if (!validateForm()) {
                return;
            }
            loadingDialog.startLoading();
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Handler handler = new Handler(Looper.getMainLooper());

            executor.execute(() -> {
                UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                        .setDisplayName(etName.getText().toString())
                        .build();

                mAuth.getCurrentUser()
                        .updateProfile(profileUpdate)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                handler.post(() -> {
                                    Toast.makeText(getApplicationContext(),
                                            "profile updated",
                                            Toast.LENGTH_SHORT).show();
                                    loadingDialog.stopLoading();
                                    finish();
                                });
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                handler.post(() -> {
                                    Log.e("tapam", "updating user profile failed: " + e);
                                    Toast.makeText(getApplicationContext(),
                                            "failed to update profile, retry again soon",
                                            Toast.LENGTH_SHORT).show();
                                    loadingDialog.stopLoading();
                                    finish();
                                });
                            }
                        });
            });
            executor.shutdown();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!FirebaseAuthUtil.auth(mAuth, this)){
            return;
        }
    }

    //
    private boolean validateForm() {
        if (TextUtils.isEmpty(etName.getText().toString())) {
            etName.setError("required");
            return false;
        }
        return true;
    }
}