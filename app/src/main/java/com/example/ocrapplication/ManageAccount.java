package com.example.ocrapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class ManageAccount extends AppCompatActivity
{
    SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_account);

        pref = PreferenceManager.getDefaultSharedPreferences(this);

        setDisplayUsernameAndRecoveryEmail();

        Button btnChangeProfile = findViewById(R.id.account_btnChangeProfile);
        Button btnChangePassword = findViewById(R.id.account_btnChangePassword);

        btnChangeProfile.setOnClickListener(v -> changeProfile());
        btnChangePassword.setOnClickListener(v -> changePassword());
    }

    public void setDisplayUsernameAndRecoveryEmail()
    {
        TextView txtUsername = findViewById(R.id.account_txtUsername);
        TextView txtRecoveryEmail = findViewById(R.id.account_txtRecoveryEmail);
        String currentUsername = pref.getString("username", "User");
        String currentRecoveryEmail = pref.getString("recoveryEmail", "defaultEmail@gmail.com");

        txtUsername.setText(getString(R.string.account_username_param, currentUsername));
        txtRecoveryEmail.setText(getString(R.string.account_recoveryEmail_param, currentRecoveryEmail));
    }

    /**
     * Display the alert dialog for user to change profile
     */
    public void changeProfile()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_change_profile, null);
        builder.setView(view);
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

        //EditText from dialog_change_profile.xml
        final EditText etUsername = view.findViewById(R.id.dialog_changeProfile_etUsername);
        final EditText etRecoveryEmail = view.findViewById(R.id.dialog_changeProfile_etRecoveryEmail);

        //set default profile at EditText
        etUsername.setText(pref.getString("username", "User"));
        etRecoveryEmail.setText(pref.getString("recoveryEmail", "defaultEmail@gmail.com"));

        view.findViewById(R.id.dialog_changeProfile_btnCancel).setOnClickListener(view1 -> alertDialog.dismiss());
        view.findViewById(R.id.dialog_changeProfile_btnUpdateProfile).setOnClickListener(view1 ->
                {
                    String username = etUsername.getText().toString().trim();
                    String recoveryEmail = etRecoveryEmail.getText().toString().trim();
                    boolean validationPass = true; //if validation fail, this variable will set to false

                    if(username.isEmpty())
                    {
                        etUsername.setError(getString(R.string.java_error_empty_username));
                        etUsername.requestFocus();
                        validationPass = false;
                    }

                    if(recoveryEmail.isEmpty())
                    {
                        etRecoveryEmail.setError(getString(R.string.java_error_empty_recovery_email));
                        etRecoveryEmail.requestFocus();
                        validationPass = false;
                    }
                    else if(!verifyEmail(recoveryEmail))
                    {
                        etRecoveryEmail.setError(getString(R.string.java_error_invalid_email));
                        etRecoveryEmail.requestFocus();
                        validationPass = false;
                    }

                    if(validationPass)
                    {
                        saveProfile(username, recoveryEmail);
                        setDisplayUsernameAndRecoveryEmail();
                        alertDialog.dismiss();
                    }
                });
    }

    /**
     * Display the alert dialog for user to change password
     */
    public void changePassword()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_change_password, null);
        builder.setView(view);
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

        //EditText from dialog_change_password.xml
        EditText etCurrentPassword = view.findViewById(R.id.dialog_changePassword_etCurrentPassword);
        EditText etNewPassword = view.findViewById(R.id.dialog_changePassword_etNewPassword);
        EditText etConfirmPassword = view.findViewById(R.id.dialog_changePassword_etConfirmPassword);

        view.findViewById(R.id.dialog_changePassword_btnCancel).setOnClickListener(view1 -> alertDialog.dismiss());
        view.findViewById(R.id.dialog_changePassword_btnUpdatePassword).setOnClickListener(view1 ->
                {
                    String currentPassword = etCurrentPassword.getText().toString().trim();
                    String newPassword = etNewPassword.getText().toString().trim();
                    String confirmPassword = etConfirmPassword.getText().toString().trim();
                    boolean validationPass = true;

                    String correctPassword = pref.getString("password", "Password");
                    if(!currentPassword.equals(correctPassword))
                    {
                        etCurrentPassword.setError(getString(R.string.java_error_wrong_current_password));
                        etCurrentPassword.requestFocus();
                        validationPass = false;
                    }

                    if(newPassword.isEmpty())
                    {
                        etNewPassword.setError(getString(R.string.java_error_empty_new_password));
                        etNewPassword.requestFocus();
                        validationPass = false;
                    }
                    else if(!newPassword.equals(confirmPassword))
                    {
                        etConfirmPassword.setError(getString(R.string.java_error_different_confirm_password));
                        etConfirmPassword.requestFocus();
                        validationPass = false;
                    }
                    if(validationPass)
                    {
                        savePassword(newPassword);
                        alertDialog.dismiss();
                    }
                });
    }

    /**
     * Save the profile entered by the user
     *
     * @param username      username to be saved
     * @param recoveryEmail recovery email to be saved
     */
    public void saveProfile(String username, String recoveryEmail)
    {
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("username", username);
        editor.putString("recoveryEmail", recoveryEmail);
        editor.apply();
    }

    /**
     * Save the password entered by the user
     *
     * @param password  password to be saved
     */
    public void savePassword(String password)
    {
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("password", password);
        editor.apply();
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