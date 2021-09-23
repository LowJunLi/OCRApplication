package com.example.ocrapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Locale;

public class MainActivity extends AppCompatActivity
{
    private long pressedTime = 0;
    private Locale currentLocale; //the locale when the activity is created

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        currentLocale = getResources().getConfiguration().locale;

        Button btnViewAllData = findViewById(R.id.main_btnViewAllData);
        //Button btnDownload = findViewById(R.id.main_btnDownload);
        Button btnManageAccount = findViewById(R.id.main_btnManageAccount);
        FloatingActionButton btnCamera = findViewById(R.id.main_btnCamera);

        btnViewAllData.setOnClickListener(v -> gotoPage(AllRecords.class));
        btnManageAccount.setOnClickListener(v -> gotoPage(ManageAccount.class));

        btnCamera.setOnClickListener(v -> gotoPage(OcrResult.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //if the user click the "Settings" button
        if (id == R.id.action_settings)
        {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Direct user to another page.
     *
     * @param className class of any type
     */
    public void gotoPage(Class<?> className)
    {
        Intent intent = new Intent(this, className);
        startActivity(intent);
    }

    /**
    *  If the locale currently displaying (the currentLocale variable) is different from locale in
     *  the configuration, recreate the page to display new locale
     */
    @Override
    public void onResume()
    {
        Locale oldLocale = currentLocale;
        currentLocale = getResources().getConfiguration().locale;
        if(currentLocale != oldLocale)
        {
            recreate();
        }
        super.onResume();
    }

    /**
     * Exit application when the user click back button twice consecutively
     *
     * When user click the back button for the first time, a toast message will appear to tell user
     * if he click the back button again he will exit the application
     * If the user click next button again within 2 seconds, the user will exit the application
     *
     * @see <a href="https://www.geeksforgeeks.org/how-to-implement-press-back-again-to-exit-in-
     * android/" How to Implement Press Back Again to Exit in Android?</a>
     */
    @Override
    public void onBackPressed()
    {
        final long waitTime = 2000; //2 seconds

        if (pressedTime + waitTime > System.currentTimeMillis())
        {
            super.onBackPressed();
            //since the login page has called finish(), this is the only activity, calling finish()
            // will close the app
            finish();
            //Kill all existing app process
            moveTaskToBack(true);
            android.os.Process.killProcess(android.os.Process.myPid());

            //close the app
            System.exit(0);
        }
        else
        {
            Toast.makeText(this, getString(R.string.java_message_press_again_to_exit),
                    Toast.LENGTH_SHORT).show();
        }
        pressedTime = System.currentTimeMillis();
    }


}