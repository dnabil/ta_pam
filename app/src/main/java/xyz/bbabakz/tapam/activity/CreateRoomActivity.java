package xyz.bbabakz.tapam.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;

import xyz.bbabakz.tapam.R;
import xyz.bbabakz.tapam.model.Room;
import xyz.bbabakz.tapam.util.FirebaseAuthUtil;
import xyz.bbabakz.tapam.util.LoadingDialogUtil;

public class CreateRoomActivity extends AppCompatActivity {

    private ImageView imgBarBack;
    private TextView tvBarTitle;
    private AppCompatButton btnCreateRoom;
    private EditText etRoomName;
    private EditText etRoomDescription;
    private FirebaseAuth mAuth;

    private FirebaseFirestore db;

    static final String TAG = "tampam: Create Room";

    //other ui utils:
    LoadingDialogUtil loadingDialog;
    //----

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_create_room);
        mAuth = FirebaseAuth.getInstance();

        loadingDialog = new LoadingDialogUtil(CreateRoomActivity.this);

        imgBarBack = findViewById(R.id.img_back);
        imgBarBack.setOnClickListener(view -> finish());

        tvBarTitle = findViewById(R.id.tv_barTitle);
        tvBarTitle.setText("Create Room");

        db = FirebaseFirestore.getInstance();

        etRoomName = findViewById(R.id.et_roomName);
        etRoomDescription = findViewById(R.id.et_roomDescription);

        btnCreateRoom = findViewById(R.id.btn_createRoom);
        btnCreateRoom.setOnClickListener(view -> createRoom());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!FirebaseAuthUtil.auth(mAuth, this)){
            return;
        }
    }

    private void createRoom() {
        String required = "Required";
        // form validation
        if (TextUtils.isEmpty(etRoomName.getText().toString())) {
            etRoomName.setError(required);
            return;
        }
        if (TextUtils.isEmpty(etRoomDescription.getText().toString())) {
            etRoomDescription.setError(required);
            return;
        }
        // end of validation

        // database operations:
        loadingDialog.startLoading();
        db.runTransaction(new Transaction.Function<Void>() {
                    final CollectionReference roomCollection = db.collection(Room.COLLECTION_NAME);

                    @Nullable
                    @Override
                    public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                        DocumentReference docRef = roomCollection.document(); // Generate a unique ID

                        // Set the data for the document
                        Room newRoom = new Room(
                                docRef.getId(),
                                etRoomName.getText().toString(),
                                etRoomDescription.getText().toString(),
                                mAuth.getCurrentUser().getUid()
                        );
                        transaction.set(docRef, newRoom);
                        return null;
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getApplicationContext(), "Room created", Toast.LENGTH_SHORT).show();
                        // Transaction succeeded
                        loadingDialog.stopLoading();
                        finish();
                        return;
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, e.toString());
                        Toast.makeText(getApplicationContext(), "Failed to create room", Toast.LENGTH_SHORT).show();
                        // Transaction failed
                        loadingDialog.stopLoading();
                        finish();
                        return;
                    }
                });
    }
}