package com.example.ocrapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Login extends AppCompatActivity
{
    private EditText etUsername;
    private EditText etPassword;
    private SharedPreferences pref; //use to retrieve fastLogin. username and password set by the user

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); //disable dark mode
        super.onCreate(savedInstanceState);

        pref = PreferenceManager.getDefaultSharedPreferences(this);
        if (pref == null) //if pref is null, the user use the app for the first time
        {
            setDefaultSharedPreferences();
        }

        setAppLanguage();

        if (checkFastLogin()) //if fast login is enable, direct user to main page
        {
            gotoPage(MainActivity.class, true);
        }
        else
        {
            setContentView(R.layout.activity_login);

            etUsername = findViewById(R.id.login_etUsername);
            etPassword = findViewById(R.id.login_etPassword);

            Button btnLogin = findViewById(R.id.login_btnLogin);
            btnLogin.setOnClickListener(v ->
            {
                String inputUsername = etUsername.getText().toString().trim();
                String inputPassword = etPassword.getText().toString().trim();

                if(checkEmptyField(inputUsername, inputPassword))
                {
                    displayToast(getString(R.string.java_message_enter_username_password));
                }
                else if (validationPass(inputUsername, inputPassword))
                {
                    gotoPage(MainActivity.class, true);
                }
                else //validation fail
                {
                    displayToast(getString(R.string.java_error_invalid_username_password));
                }
            });

            Button btnForget = findViewById(R.id.login_btnForget);
            btnForget.setOnClickListener(v -> gotoPage(EmailLogin.class, false));
        }

    }


    /**
     * Set language of the application based on value in shared preference
     *
     */
    public void setAppLanguage()
    {
        String languageCode =
                pref.getString("language", "en-US");
        LanguageManager.setLocale(this, languageCode);
    }

    /**
     * Set default value in shared preferences for user who use the app for the first time.
     */
    public void setDefaultSharedPreferences()
    {
        //initialize shared preference for language and fastLogin (can edit in settings.xml)
        androidx.preference.PreferenceManager
                .setDefaultValues(this, R.xml.settings, false);

        //initialize shared preference for username, password, and recovery email (can edit in manage account)
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("username", "User");
        editor.putString("password", "Password");
        editor.putString("recoveryEmail", "defaultEmail@gmail.com");
        editor.apply();
    }

    /**
     * Check if the fast login setting has been set enabled by the user.
     *
     * @return true if the user has enable fast login setting, false otherwise
     */
    public boolean checkFastLogin()
    {
        return pref.getBoolean("fastLogin", false);
    }


    /**
     * Direct user to another page.
     *
     * @param className         class of any type
     * @param activityClearTop  clear activity on top of the stack
     */
    public void gotoPage(Class<?> className, boolean activityClearTop)
    {
        Intent intent = new Intent(this, className);
        if(activityClearTop)
        {
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
        startActivity(intent);
    }

    /**
     * Validate the username and password combination to ensure both are filled and correct.
     *
     * @param inputUsername  username entered by the user
     * @param inputPassword  password entered by the user
     * @return               true if the username and password is valid, false otherwise
     */
    public boolean validationPass(String inputUsername, String inputPassword)
    {
        return checkUsername(inputUsername) && checkPassword(inputPassword);
    }

    /**
     * Check whether the username entered by the user matched with the username in shared preference
     * (valid username)
     *
     * @param inputUsername  username entered by the user
     * @return               true if the username is correct, false otherwise
     */
    public boolean checkUsername(@NonNull String inputUsername)
    {
        String correctUsername = pref.getString("username", "User");

        return inputUsername.equals(correctUsername);
    }

    /**
     * Check whether the username entered by the user matched with the username in shared preference
     * (valid password)
     *
     * @param inputPassword  password entered by the user
     * @return               true if the password is correct, false otherwise
     */
    public boolean checkPassword(@NonNull String inputPassword)
    {
        String correctPassword = pref.getString("password", "Password");

        return inputPassword.equals(correctPassword);
    }

    /**
     * Check whether the user entered username and password
     *
     * @param inputUsername  username entered by the user
     * @param inputPassword  password entered by the user
     * @return               true if the inputUsername and inputPassword are not empty string, false
     *                       otherwise
     */
    public boolean checkEmptyField(@NonNull String inputUsername, String inputPassword)
    {
        return inputUsername.equals("") && inputPassword.equals("");
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



}