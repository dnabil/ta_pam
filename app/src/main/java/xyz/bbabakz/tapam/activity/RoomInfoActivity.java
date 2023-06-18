package xyz.bbabakz.tapam.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import xyz.bbabakz.tapam.R;
import xyz.bbabakz.tapam.model.Room;
import xyz.bbabakz.tapam.util.FirebaseAuthUtil;
import xyz.bbabakz.tapam.util.LoadingDialogUtil;

public class RoomInfoActivity extends AppCompatActivity {

    private String roomId;
    FirebaseAuth mAuth;
    CollectionReference roomCollection;
    LoadingDialogUtil loadingDialog;

    ImageView imgBack;
    TextView tvBarTitle;

    TextView tvRoomCode, tvRoomTitle, tvRoomDescription;
    LinearLayout layoutRoomCode;
    ImageView imgRefreshCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_info);
        mAuth = FirebaseAuth.getInstance();
        if (!FirebaseAuthUtil.auth(mAuth, this)){
            finish();
            return;
        }
        roomCollection = FirebaseFirestore.getInstance().collection(Room.COLLECTION_NAME);
        roomId = getIntent().getStringExtra("roomId");
        if (roomId == null || roomId == ""){
            Toast.makeText(getApplicationContext(), "room doesn't exists", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        loadingDialog = new LoadingDialogUtil(this);

        imgBack = findViewById(R.id.img_back);
        imgBack.setOnClickListener(view -> {
            finish();
        });

        tvBarTitle = findViewById(R.id.tv_barTitle);
        tvBarTitle.setText("Room Details");

        layoutRoomCode = findViewById(R.id.layout_roomCode);
        layoutRoomCode.setVisibility(View.GONE);
        tvRoomCode = findViewById(R.id.tv_roomCode);
        tvRoomTitle = findViewById(R.id.tv_roomTitle);
        tvRoomDescription = findViewById(R.id.tv_roomDescription);

        imgRefreshCode = findViewById(R.id.img_refreshCode);
        imgRefreshCode.setClickable(false);
        imgRefreshCode.setOnClickListener(view -> {
            imgRefreshCode.setVisibility(View.GONE);
            loadingDialog.startLoading();

            ExecutorService executor = Executors.newSingleThreadExecutor();
            Handler handler = new Handler(Looper.getMainLooper());

            String newCode = Room.generateRandomString();
            roomCollection.document(roomId)
                    .update("code", newCode)
                    .addOnSuccessListener(unused -> {
                        tvRoomCode.setText(newCode);
                        Toast.makeText(this, "room code updated", Toast.LENGTH_SHORT).show();
                        loadingDialog.stopLoading();

                        // buat countdown 5 detik jdi gk dispam
                        executor.execute(() -> {
                            try {
                                Thread.sleep(5*1000); //5 seconds
                            } catch (InterruptedException ignored) {
                            }
                            handler.post(() -> {
                                imgRefreshCode.setVisibility(View.VISIBLE);
                                executor.shutdown();
                            });
                        });
                    })
                    .addOnFailureListener(e -> {
                        String msg = "failed to update code";
                        Log.e("tapam", String.format("%s: %s", msg, e));
                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                        finish();
                        loadingDialog.stopLoading();
                    });
        });

        loadingDialog.startLoading();
        roomCollection.document(roomId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Room room = documentSnapshot.toObject(Room.class);
                    tvRoomTitle.setText(room.getName());
                    tvRoomDescription.setText(room.getDescription());

                    // if it's the owner then show the code
                    if (room.getUserId().equals(mAuth.getCurrentUser().getUid())){
                        layoutRoomCode.setVisibility(View.VISIBLE);
                        imgRefreshCode.setClickable(true);
                        tvRoomCode.setText(room.getCode());
                    }
                    loadingDialog.stopLoading();
                })
                .addOnFailureListener(e -> {
                    String msg = "failed to load room data";
                    Log.e("tapam", String.format("%s: %s", msg, e));
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                    finish();
                    loadingDialog.stopLoading();
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!FirebaseAuthUtil.auth(mAuth, this)){
            finish();
            return;
        }
    }
}