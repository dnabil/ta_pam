package xyz.bbabakz.tapam.activity;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import xyz.bbabakz.tapam.R;
import xyz.bbabakz.tapam.model.Room;
import xyz.bbabakz.tapam.util.FirebaseAuthUtil;
import xyz.bbabakz.tapam.util.LoadingDialogUtil;

public class JoinRoomActivity extends AppCompatActivity {

    ImageView imgBarBack;
    TextView tvBarTitle;
    AppCompatButton btnJoinRoom;
    EditText etRoomCode;
    LoadingDialogUtil loadingDialog;
    CollectionReference roomCollection;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        if (!FirebaseAuthUtil.auth(mAuth, this)){
            finish();
            return;
        }
        setContentView(R.layout.activity_join_room);
        roomCollection = FirebaseFirestore.getInstance().collection(Room.COLLECTION_NAME);
        loadingDialog = new LoadingDialogUtil(this);

        imgBarBack = findViewById(R.id.img_back);
        imgBarBack.setOnClickListener(view -> finish());

        tvBarTitle = findViewById(R.id.tv_barTitle);
        tvBarTitle.setText("Join Room");

        etRoomCode = findViewById(R.id.et_roomCode);

        btnJoinRoom = findViewById(R.id.btn_joinRoom);
        String btnJoinRoomText = btnJoinRoom.getText().toString();
        btnJoinRoom.setOnClickListener(view -> {
            if (!validateForm())
                return;
            btnJoinRoom.setEnabled(false);

            // Start the countdown timer
            new CountDownTimer(3000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    // Update the button text with remaining time
                    btnJoinRoom.setText("Countdown: " + ((millisUntilFinished / 1000) + 1));
                }

                @Override
                public void onFinish() {
                    // Enable the button and restore its original text
                    btnJoinRoom.setEnabled(true);
                    btnJoinRoom.setText(btnJoinRoomText);
                }
            }.start();


            loadingDialog.startLoading();
            roomCollection
                    .whereEqualTo("code", etRoomCode.getText().toString())
                    .limit(1)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (queryDocumentSnapshots.size() == 0){
                            Toast.makeText(this, "room not found...", Toast.LENGTH_SHORT).show();
                            loadingDialog.stopLoading();
                            return;
                        }
                        Room room = queryDocumentSnapshots.getDocuments().get(0).toObject(Room.class);
                        String currentUserId = mAuth.getCurrentUser().getUid();

                        // validate if it's the room's owner, then reject
                        if (room.getUserId().equals(currentUserId)){
                            Toast.makeText(this, "owner can't join their own room", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // validate if the user is already joined or not
                        if (room.getMembersId() != null){
                            for (String memberId : room.getMembersId()){
                                if  (currentUserId.equals(memberId)) {
                                    loadingDialog.stopLoading();
                                    Toast.makeText(this, "you already joined this room", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            }
                        } else {
                            room.setMembersId(new ArrayList<String>());
                        }
                        room.getMembersId().add(currentUserId);

                        roomCollection
                                .document(room.getId())
                                .update("membersId", room.getMembersId())
                                .addOnSuccessListener(unused -> {
                                    loadingDialog.stopLoading();
                                    Toast.makeText(getApplicationContext(), "joined room", Toast.LENGTH_SHORT).show();
                                    finish();
                                    return;
                                })
                                .addOnFailureListener(e -> {
                                    String msg = "failed joining room, try again later";
                                    Log.e("tapam", msg + e);
                                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                                    loadingDialog.stopLoading();
                                    finish();
                                    return;
                                });
                    })
                    .addOnFailureListener(e -> {
                        String msg = "error occured, please try again later";
                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                        Log.e("tapam", e.toString());
                        finish();
                    });

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

    private boolean validateForm(){
        if (TextUtils.isEmpty(etRoomCode.getText().toString())) {
            etRoomCode.setError("required");
            return false;
        }
        return true;
    }
}