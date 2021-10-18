package com.example.ocrapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

public class DisplayData extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_data);

        Button btnFilter = findViewById(R.id.display_btnFilter);
        btnFilter.setOnClickListener(v -> showFilterOptions());
    }

    public void showFilterOptions()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_change_password, null);
        builder.setView(view);
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}