package com.krsh.onroad;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.krsh.onroad.utils.ReusableClass;

public class SettingsActivity extends AppCompatActivity {

    TextView editTextSaveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        editTextSaveButton = (TextView)findViewById(R.id.editTextSaveButton);

        if (ReusableClass.getFromPreference("emergencyMobileNo", SettingsActivity.this).toString().trim().length() > 0)
            editTextSaveButton.setText(ReusableClass.getFromPreference("emergencyMobileNo", SettingsActivity.this));
    }

    @Override
    public void onBackPressed()
    {
        Intent i = new Intent(this, MainActivity.class);
        finish();
        startActivity(i);
    }

    public void savingMobileNo(View view)
    {
        if(editTextSaveButton.getText().toString().trim().length()>0) {
            ReusableClass.saveInPreference("emergencyMobileNo", editTextSaveButton.getText().toString(), this);
            Toast.makeText(SettingsActivity.this, "You are good to go !!", Toast.LENGTH_SHORT).show();

            Intent i = new Intent(this, MainActivity.class);
            finish();
            startActivity(i);
        }
        else
            Toast.makeText(SettingsActivity.this, "Please Enter a mobile no !!", Toast.LENGTH_SHORT).show();
    }
}
