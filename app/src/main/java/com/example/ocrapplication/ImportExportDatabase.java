package com.example.ocrapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

public class ImportExportDatabase extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_export_database);

        Button btnExport = findViewById(R.id.importExport_btnExport);
        Button btnImport = findViewById(R.id.importExport_btnImport);

        ///temporary
        findViewById(R.id.importExport_txtImport).setVisibility(View.INVISIBLE);
        btnImport.setVisibility(View.INVISIBLE);
        ///end of temporary

        btnExport.setOnClickListener(v -> exportDatabase());
        btnImport.setOnClickListener(v -> importDatabase());
    }

    /**
     * Export database to SD card
     *
     * @see <a href="https://stackoverflow.com/questions/9997976/android-pulling-sqlite-database-android-device"
     * Pulling SQlite database android device</a>
     */
    public void exportDatabase()
    {
        try {
            File sd = Environment.getExternalStorageDirectory();

            if (sd.canWrite()) {
                String currentDBPath = this.getDatabasePath("OCRDatabase").toString();
                String backupDBPath = "OCRBackup.db";
                File currentDB = new File(currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }
            }
        } catch (Exception e) {

        }
    }

    /**
     * Import database
     */
    public void importDatabase()
    {

    }


}