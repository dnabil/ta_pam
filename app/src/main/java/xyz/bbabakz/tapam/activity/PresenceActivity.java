package xyz.bbabakz.tapam.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;

import xyz.bbabakz.tapam.R;
import xyz.bbabakz.tapam.util.FirebaseAuthUtil;

public class PresenceActivity extends AppCompatActivity {

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_presence);
        mAuth = FirebaseAuth.getInstance();
        if(FirebaseAuthUtil.auth(mAuth, this)){
            return;
        }

    }

    @Override
    protected void onResume(){
        super.onResume();
        if(FirebaseAuthUtil.auth(mAuth, this)){
            return;
        }
    }
}