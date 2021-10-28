package com.example.ocrapplication;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class ImportExportDatabase extends AppCompatActivity
{
    ActivityResultLauncher<Intent> importLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_export_database);

        Button btnExport = findViewById(R.id.importExport_btnExport);
        Button btnImport = findViewById(R.id.importExport_btnImport);

        importLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result ->
        {
            if (result.getResultCode() == Activity.RESULT_OK)
            {
                if (result.getData() != null)
                {
                    importDatabase(result.getData().getData());
                }
            }
            else
            {
                displayToast(getString(R.string.java_error_import_fail));
                Log.e("Import failure", "No file selected");
            }
        });

        btnExport.setOnClickListener(v -> exportDatabase());

        findViewById(R.id.importExport_txtImport).setVisibility(View.INVISIBLE);
        btnImport.setVisibility(View.INVISIBLE);
        //btnImport.setOnClickListener(v -> selectFile());
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

    private void replaceFileContent(FileInputStream fromStream, FileOutputStream toStream) throws IOException
    {
        try
        {
            byte[] mBuffer = new byte[1024];
            int mLength;
            while ((mLength = fromStream.read(mBuffer)) > 0)
            {
                toStream.write(mBuffer, 0, mLength);
            }
            toStream.flush();
            toStream.close();
            fromStream.close();
        }
        finally
        {
            try
            {
                if (fromStream != null)
                {
                    fromStream.close();
                }
            }
            finally
            {
                if (toStream != null)
                {
                    toStream.close();
                }
            }
        }
    }

    private void exportDatabase()
    {
        DBHelper dbHelper = new DBHelper(this);
        String currentDBPath = this.getDatabasePath(dbHelper.getDatabaseName()).toString();
        File currentDB = new File(currentDBPath);
        FileInputStream fromStream;
        FileOutputStream toStream;

        if (currentDB.exists())
        {
            try
            {
                fromStream = new FileInputStream(currentDB);

                ContentResolver resolver = getContentResolver();
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "Logbook OCR Backup Database");
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "application/vnd.sqlite3");
                Uri uri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues);
                toStream = (FileOutputStream) resolver.openOutputStream(uri);

                copyFile(fromStream, toStream);
                displayToast(getString(R.string.java_message_export_success));
            }
            catch (IOException ex)
            {
                Log.e("Export failure", "In exportDatabase: " + ex.getMessage());
                displayToast(getString(R.string.java_error_export_fail));
            }
        }
        else
        {
            Log.e("Export failure", "Database not exist");
            displayToast(getString(R.string.java_error_export_fail));
        }
    }

    /**
     *
     */
    public void selectFile()
    {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/vnd.sqlite3");
        intent = Intent.createChooser(intent, "Choose a file");

        importLauncher.launch(intent);
    }

    /**
     * Import database
     *
     * @param uri Uri of selected file
     */
    public void importDatabase(Uri uri)
    {
        DBHelper dbHelper = new DBHelper(this);
        String currentDBPath = this.getDatabasePath(dbHelper.getDatabaseName()).toString();
        File currentDB = new File(currentDBPath);

        FileInputStream fromStream;
        FileOutputStream toStream;

        if (currentDB.exists())
        {
            try
            {
                toStream = new FileOutputStream(currentDB);
                fromStream = (FileInputStream) getContentResolver().openInputStream(uri);
                replaceFileContent(fromStream, toStream);
            }
            catch (IOException ex)
            {
                Log.e("Import failure", "In importDatabase: " + ex.getMessage());
                displayToast(getString(R.string.java_error_import_fail));
            }

        }
        else
        {
            Log.e("Import failure", "Database not exist");
            displayToast(getString(R.string.java_error_import_fail));
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
