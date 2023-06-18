package xyz.bbabakz.tapam.util;

import android.view.LayoutInflater;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import xyz.bbabakz.tapam.R;

public class LoadingDialogUtil {

    private AppCompatActivity activity;
    private AlertDialog dialog;
//    TextView tvLoadText;

    public LoadingDialogUtil(AppCompatActivity activity) {
        this.activity = activity;
    }

    public void startLoading(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this.activity);

        LayoutInflater inflater = activity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.dialog_loading, null));
        builder.setCancelable(true);

        dialog = builder.create();
        dialog.show();
    }

    public void stopLoading(){
        dialog.dismiss();
    }

}
