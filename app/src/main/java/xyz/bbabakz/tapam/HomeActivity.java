package xyz.bbabakz.tapam;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeActivity extends AppCompatActivity {
    Button btnSignOut;
    private FirebaseAuth mAuth;
    ImageView profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mAuth = FirebaseAuth.getInstance();

        profile = findViewById(R.id.img_profile);

        // pergi ke profile page
        profile.setOnClickListener(view -> {
            Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

//        btnSignOut.setOnClickListener(view -> {
//            FirebaseAuth.getInstance().signOut();
//            finish();
//            startActivity(new Intent(HomeActivity.this, MainActivity.class));
//        });

    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            finish(); // Finish the current activity
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent); // Start the main activity
        }


        if (currentUser != null) {
        }
    }
    // [END on_start_check_user]

}