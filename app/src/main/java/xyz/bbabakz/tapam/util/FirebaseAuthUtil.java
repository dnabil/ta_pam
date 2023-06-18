package xyz.bbabakz.tapam.util;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import xyz.bbabakz.tapam.activity.MainActivity;

public class FirebaseAuthUtil {
    private static final String logKey = "FireBaseAuthUtil";
    private static final String notAssignedError = "parameter must be assigned first";

    static public boolean isLoggedIn(FirebaseAuth mAuth){
        if (mAuth == null) {
            Log.e(logKey, notAssignedError);
            throw new NullPointerException(notAssignedError);
        }
        FirebaseUser user = mAuth.getCurrentUser();
        return user != null;
    }

    // this method TENDANg oRang Ke welcome page kalau gK login
    // login dulu masbroooooooooooooooooooooooooooooooooooooooooooo
    static public boolean auth(FirebaseAuth mAuth, Context context){
        boolean login = isLoggedIn(mAuth);

        if (context == null) {
            Log.e(logKey, notAssignedError);
            throw new NullPointerException(notAssignedError);
        }

        if (!login) {
            Intent toWelcomePage = new Intent(context, MainActivity.class);
            context.startActivity(toWelcomePage);
            Toast.makeText(context.getApplicationContext(), "Register/Login first", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
