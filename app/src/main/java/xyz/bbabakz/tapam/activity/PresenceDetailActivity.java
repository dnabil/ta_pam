package xyz.bbabakz.tapam.activity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import xyz.bbabakz.tapam.R;
import xyz.bbabakz.tapam.model.Presence;
import xyz.bbabakz.tapam.util.FirebaseAuthUtil;
import xyz.bbabakz.tapam.util.LoadingDialogUtil;

public class PresenceDetailActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private String presenceId;
    LoadingDialogUtil loadingDialog;

    ImageView imgBack, imgMenu;
    TextView tvBarTitle;

    TextView tvPresenceCreatedAt, tvPresenceTitle, tvPresenceDescription;
    AppCompatButton btnPresent, btnAbsent;

    CollectionReference presenceCollection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent() == null) {
            Toast.makeText(getApplicationContext(), "invalid presence id", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        presenceId = getIntent().getStringExtra("id");
        if (presenceId.equals("")) {
            Toast.makeText(getApplicationContext(), "invalid presence id", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        setContentView(R.layout.activity_presence_detail);
        mAuth = FirebaseAuth.getInstance();
        if (!FirebaseAuthUtil.auth(mAuth, this)) {
            finish();
            return;
        }
        presenceCollection = FirebaseFirestore.getInstance().collection(Presence.COLLECTION_NAME);
        loadingDialog = new LoadingDialogUtil(this);

        imgBack = findViewById(R.id.img_back);
        imgBack.setOnClickListener(view -> {
            finish();
        });
        imgMenu = findViewById(R.id.img_menu);
        imgMenu.setOnClickListener(view -> {
            // TODO: menu for presence detail
            Toast.makeText(this, "not yet implemented", Toast.LENGTH_SHORT).show();
        });
        tvBarTitle = findViewById(R.id.tv_barTitle);
        tvBarTitle.setText("Presence Detail");

        tvPresenceCreatedAt = findViewById(R.id.tv_presenceCreatedAt);
        tvPresenceTitle = findViewById(R.id.tv_presenceTitle);
        tvPresenceDescription = findViewById(R.id.tv_presenceDescription);

        btnPresent = findViewById(R.id.btn_present);
        btnAbsent = findViewById(R.id.btn_absent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadingDialog.startLoading();
        if (!FirebaseAuthUtil.auth(mAuth, this)) {
            finish();
            return;
        }

        presenceCollection
                .document(presenceId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Presence presence = documentSnapshot.toObject(Presence.class);

                    Date createdAt = presence.getCreatedAt().toDate();
                    SimpleDateFormat ddMMMMyyyyFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
                    tvPresenceCreatedAt.setText(ddMMMMyyyyFormat.format(createdAt));

                    tvPresenceTitle.setText(presence.getTitle());

                    tvPresenceDescription.setText(presence.getDescription());

                    loadingDialog.stopLoading();
                })
                .addOnFailureListener(e -> {
                    loadingDialog.stopLoading();

                });
    }
}