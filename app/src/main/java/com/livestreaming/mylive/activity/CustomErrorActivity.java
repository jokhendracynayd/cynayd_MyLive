package com.livestreaming.mylive.activity;


import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


import androidx.appcompat.app.AlertDialog;

import com.livestreaming.common.activity.AbsActivity;

import cat.ereza.customactivityoncrash.CustomActivityOnCrash;
import cat.ereza.customactivityoncrash.config.CaocConfig;

public class CustomErrorActivity extends AbsActivity implements View.OnClickListener {

    String pacakgeName;

    @Override
    protected void main(Bundle savedInstanceState) {
        super.main(savedInstanceState);

        pacakgeName = getApplicationContext().getPackageName();
        Button restartButton = findViewById(com.livestreaming.common.R.id.restart_button);

        final CaocConfig config = CustomActivityOnCrash.getConfigFromIntent(getIntent());

        if (config == null) {
            finish();
            return;
        }

        if (config.isShowRestartButton() && config.getRestartActivityClass() != null) {
            restartButton.setText("Restart");
            restartButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(CustomErrorActivity.this, LauncherActivity.class));
                    finish();
                }
            });
        }
        else {
            restartButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CustomActivityOnCrash.closeApplication(CustomErrorActivity.this, config);
                }
            });
        }


        findViewById(com.livestreaming.common.R.id.detail_button).setOnClickListener(this);

        showSendReport();

    }

    @Override
    protected int getLayoutId() {
        return  com.livestreaming.common.R.layout.activity_custom_error;
    }


    @Override
    public void onClick(View v) {

        int id = v.getId();
        if (id == com.livestreaming.common.R.id.send_reposrt) {
        } else if (id == com.livestreaming.common.R.id.detail_button) {
            showAlert();
        }
    }


    public void showAlert(){
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(cat.ereza.customactivityoncrash.R.string.customactivityoncrash_error_activity_error_details_title)
                .setMessage(CustomActivityOnCrash.getAllErrorDetailsFromIntent(CustomErrorActivity.this, getIntent()))
                .setPositiveButton(cat.ereza.customactivityoncrash.R.string.customactivityoncrash_error_activity_error_details_close, null)
                .setNeutralButton(cat.ereza.customactivityoncrash.R.string.customactivityoncrash_error_activity_error_details_copy,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                copyErrorToClipboard();
                            }
                        })
                .show();
    }


    private void copyErrorToClipboard() {
        String errorInformation = CustomActivityOnCrash.getAllErrorDetailsFromIntent(CustomErrorActivity.this, getIntent());
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        //Are there any devices without clipboard...?
        if (clipboard != null) {
            ClipData clip = ClipData.newPlainText(getString(cat.ereza.customactivityoncrash.R.string.customactivityoncrash_error_activity_error_details_clipboard_label), errorInformation);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(CustomErrorActivity.this, cat.ereza.customactivityoncrash.R.string.customactivityoncrash_error_activity_error_details_copied, Toast.LENGTH_SHORT).show();
        }
    }




    public void showSendReport(){
        Button sendReposrt = findViewById(com.livestreaming.common.R.id.send_reposrt);

        if (pacakgeName.contains("qboxus")) {
            sendReposrt.setVisibility(View.VISIBLE);
            sendReposrt.setOnClickListener(this);
        } else {
            sendReposrt.setVisibility(View.GONE);
        }
    }


}