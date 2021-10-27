package com.example.ocrapplication;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class ImportExportDatabase extends AppCompatActivity
{
    private final int EXPORT_REQUEST_CODE = 1;
    private final int IMPORT_REQUEST_CODE = 2;
    private ActivityResultLauncher<Intent> exportLauncher;
    private ActivityResultLauncher<Intent> importLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_export_database);

        Button btnExport = findViewById(R.id.importExport_btnExport);
        Button btnImport = findViewById(R.id.importExport_btnImport);

        exportLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result ->
        {
            if (result.getResultCode() == Activity.RESULT_OK)
            {
                if (result.getData() != null)
                {
                    copyDatabaseToSelectedFile(result.getData().getData());
                }
            }
            else
            {
                displayToast(getString(R.string.java_error_export_fail));
                Log.e("Export failure", "In else block of exportLauncher");
            }
        });

        importLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result ->
        {
            if (result.getResultCode() == Activity.RESULT_OK)
            {
                if (result.getData() != null)
                {
                    copyBackupDatabaseToReplaceCurrentDatabase(result.getData().getData());
                }
            }
            else
            {
                displayToast(getString(R.string.java_error_import_fail));
                Log.e("Import failure", "In else block of importLauncher");
            }
        });

        btnExport.setOnClickListener(v -> checkExportPermission());
        btnImport.setOnClickListener(v -> importDatabase());
    }

    public void checkExportPermission()
    {
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        {
            exportDatabase();
        }
        else
        {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, EXPORT_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case EXPORT_REQUEST_CODE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    exportDatabase();
                }
                else
                {
                    displayToast("Unable to export because permission denied");
                }
                return;
            case IMPORT_REQUEST_CODE:

        }
        // Other 'case' lines to check for other
        // permissions this app might request.
    }


    private void exportDatabase()
    {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/vnd.sqlite3");
        intent.putExtra(Intent.EXTRA_TITLE, "OCRBackup.db");

        exportLauncher.launch(intent);
    }

    /**
     * Import database
     */
    public void importDatabase()
    {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        importLauncher.launch(intent);
    }

    private void copyFile(FileInputStream fromFile, FileOutputStream toFile) throws IOException
    {
        FileChannel fromChannel = null;
        FileChannel toChannel = null;
        try
        {
            fromChannel = fromFile.getChannel();
            toChannel = toFile.getChannel();
            fromChannel.transferTo(0, fromChannel.size(), toChannel);
        }
        finally
        {
            try
            {
                if (fromChannel != null)
                {
                    fromChannel.close();
                }
            }
            finally
            {
                if (toChannel != null)
                {
                    toChannel.close();
                }
            }
        }
    }

    private void copyDatabaseToSelectedFile(Uri uri)
    {
        String currentDBPath = this.getDatabasePath("OCRDatabase").toString();
        File currentDB = new File(currentDBPath);
        File backupDB = new File(uri.getPath());

        if (currentDB.exists())
        {
            if (backupDB.mkdir())
            {

                try
                {
                    FileInputStream fromStream = new FileInputStream(currentDB);
                    FileOutputStream toStream = new FileOutputStream(backupDB);
                    copyFile(fromStream, toStream);
                    displayToast(getString(R.string.java_message_export_success));
                }
                catch (IOException ex)
                {
                    Log.e("Export failure", "In copyDatabaseToSelectedFile: " + ex.getMessage());
                    displayToast(getString(R.string.java_error_export_fail));
                }
            }
            else
            {
                Log.e("Export failure", "Unable to create directory");
                displayToast(getString(R.string.java_error_export_fail));
            }

        }
        else
        {
            Log.e("Export failure", "Database not yet exist");
            displayToast(getString(R.string.java_error_database_not_exist));
        }
    }


    private void copyBackupDatabaseToReplaceCurrentDatabase(Uri uri)
    {
        String currentDBPath = this.getDatabasePath("OCRDatabase").toString();
        File currentDB = new File(currentDBPath);
        File backupDB = new File(uri.getPath());
        DBHelper dbHelper = new DBHelper(this);

        if (currentDB.exists())
        {
            try
            {
                dbHelper.close();
                FileInputStream fromStream = new FileInputStream(backupDB);
                FileOutputStream toStream = new FileOutputStream(currentDB);
                copyFile(fromStream, toStream);
                // Access the copied database so SQLiteHelper will cache it and mark
                // it as created.
                dbHelper.getWritableDatabase().close();
                displayToast(getString(R.string.java_message_export_success));
            }
            catch (IOException ex)
            {
                Log.e("Import failure", "In copyDatabaseToSelectedFile: " + ex.getMessage());
                displayToast(getString(R.string.java_error_export_fail));
            }
        }
        else
        {
            Log.e("Import failure", "Database not exist");
            displayToast(getString(R.string.java_error_export_fail));
        }
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
