package com.example.ocrapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class EmailLogin extends AppCompatActivity
{
    SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_login);

        pref = PreferenceManager.getDefaultSharedPreferences(this);

        EditText etRecoveryEmail = findViewById(R.id.emailLogin_etRecoveryEmail);
        Button btnLogin = findViewById(R.id.emailLogin_btnLogin);
        btnLogin.setOnClickListener(v ->
        {
            String recoveryEmail = etRecoveryEmail.getText().toString().trim();


            if(recoveryEmail.isEmpty())
            {
                displayToast(getString(R.string.java_error_empty_recovery_email));
            }
            else if(!verifyEmail(recoveryEmail))
            {
                displayToast(getString(R.string.java_error_invalid_email));
            }
            //correct recovery email
            else if(recoveryEmail.equals(pref.getString("recoveryEmail", "defaultEmail@gmail.com")))
            {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * Display toast message
     *
     * @param message the message to be displayed
     */
    public void displayToast(String message)
    {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Check whether the email address is valid or not
     *
     * @param email  Email address that need to be checked
     * @return       true if the email is valid, otherwise false
     */
    public boolean verifyEmail(String email)
    {
        //Check whether email is valid or not using regex
        return email.matches("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+");
    }
}