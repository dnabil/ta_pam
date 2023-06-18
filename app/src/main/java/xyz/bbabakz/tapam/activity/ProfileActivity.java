package xyz.bbabakz.tapam.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import xyz.bbabakz.tapam.R;
import xyz.bbabakz.tapam.util.FirebaseAuthUtil;

public class ProfileActivity extends AppCompatActivity {
    private ImageView imgBarBack;
    private TextView tvBarTitle;
    private FirebaseAuth mAuth;

    LinearLayout lPrivacy;
    LinearLayout lHistory;
    LinearLayout lHelpSupport;
    LinearLayout lLogout;

    ImageView imgProfilePicture;
    TextView tvEmail;
    TextView tvName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        mAuth = FirebaseAuth.getInstance();
        if (!FirebaseAuthUtil.auth(mAuth, this)) {
            return;
        }

        imgBarBack = findViewById(R.id.img_back);
        imgBarBack.setOnClickListener(view -> finish());

        tvBarTitle = findViewById(R.id.tv_barTitle);
        tvBarTitle.setText("Profile");

        imgProfilePicture = findViewById(R.id.img_profilePicture);
        tvEmail = findViewById(R.id.tv_email);
        tvName = findViewById(R.id.tv_name);

        lPrivacy = findViewById(R.id.l_privacy);
        lPrivacy.setOnClickListener(view -> {
            Intent toPrivacyPage = new Intent(this, PrivacyActivity.class);
            startActivity(toPrivacyPage);
        });

        lHistory = findViewById(R.id.l_roomHistory);
        lHistory.setOnClickListener(view -> {
            // TODO: implement history feature (?)
            Toast.makeText(this, "not yet implemented", Toast.LENGTH_SHORT).show();
        });

        lHelpSupport = findViewById(R.id.l_helpSupport);
        lHelpSupport.setOnClickListener(view -> {
            // TODO: implement help&support feature (?)
            Toast.makeText(this, "not yet implemented", Toast.LENGTH_SHORT).show();
        });

        lLogout = findViewById(R.id.l_Logout);
        lLogout.setOnClickListener(view -> {
            mAuth.signOut();
            if (!FirebaseAuthUtil.auth(mAuth, this)) {
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!FirebaseAuthUtil.auth(mAuth, this)) {
            return;
        }
        FirebaseUser user = mAuth.getCurrentUser();

        // load profile image
        Glide.with(this)
                .load(user.getPhotoUrl())
                .circleCrop()
                .into(imgProfilePicture);

        // load user's email
        tvEmail.setText(user.getEmail());

        // load user's name
        tvName.setText(user.getDisplayName());
    }
}