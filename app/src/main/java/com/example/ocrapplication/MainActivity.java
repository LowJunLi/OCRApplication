package com.example.ocrapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Locale;

public class MainActivity extends AppCompatActivity
{
    Locale currentLocale; //the locale when the activity is created

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

        btnViewAllData.setOnClickListener(v -> gotoPage(DatabaseRecords.class));
        btnManageAccount.setOnClickListener(v -> gotoPage(ManageAccount.class));
        btnCamera.setOnClickListener(v -> gotoPage(UseCamera.class));
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


}