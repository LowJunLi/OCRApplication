package com.example.ocrapplication;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class OcrResult extends AppCompatActivity implements LoaderManager.LoaderCallbacks<ArrayList<LogbookRow>>
{
    private ActivityResultLauncher<Intent> activityResultLauncher;
    private String currentPhotoPath;
    private TextView txtStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr_result);

        ImageView imgPhoto = findViewById(R.id.result_imgPhoto);
        txtStatus = findViewById(R.id.result_txtStatus);

        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result ->
        {
            if (result.getResultCode() == Activity.RESULT_OK)
            {
                Bitmap photo = BitmapFactory.decodeFile(currentPhotoPath); //get picture from the directory
                deletePhoto();
                imgPhoto.setImageBitmap(photo);
                performOCR(photo);
            }
        });

        Button btnRescan = findViewById(R.id.result_btnRescan);
        btnRescan.setOnClickListener(v -> takePicture());

        takePicture(); //Let user take photo when the user access this page from main menu
    }

    /**
     *  Use application camera to take picture
     *
     *  @see <a href="https://developer.android.com/training/camera/photobasics" Take photos</a>
     */
    private void takePicture()
    {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null)
        {
            // Create the File where the photo should go
            File imageFile = null;
            try
            {
                imageFile = createImageFile();
            }
            catch (IOException ex)
            {
                displayToast("Fail to create photo file");
            }
            // Continue only if the File was successfully created
            if (imageFile != null)
            {
                Uri imageURI = FileProvider.getUriForFile(this, "com.example.ocrapplication", imageFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageURI);
                activityResultLauncher.launch(takePictureIntent);
            }
        }
    }

    /**
     * Create an image file
     *
     * @return image file
     * @throws IOException Fail to create photo file
     * @see <a href="https://developer.android.com/training/camera/photobasics" Take photos</a>
     */
    private File createImageFile() throws IOException
    {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imageFile = File.createTempFile(imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */);

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = imageFile.getAbsolutePath();
        return imageFile;
    }

    /**
     *  Delete the photo in the directory
     */
    public void deletePhoto()
    {
        File fdelete = new File(currentPhotoPath);
        if (fdelete.exists())
        {
            fdelete.delete();
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

    /**
     * Perform OCR on the picture
     *
     * @param photo the picture taken by the user
     */
    public void performOCR(Bitmap photo)
    {
        if(photo != null)
        {
            Bundle photoBundle = new Bundle();
            photoBundle.putParcelable("photo", photo);
            LoaderManager.getInstance(this).restartLoader(0, photoBundle, this);
            txtStatus.setText(R.string.result_processing);
        }
        else
        {
            txtStatus.setText(R.string.java_message_please_take_picture);
        }

    }

    @NonNull
    @Override
    public Loader<ArrayList<LogbookRow>> onCreateLoader(int id, @Nullable Bundle args)
    {
        Bitmap photo = args.getParcelable("photo");
        return new OcrLoader(this, photo);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<ArrayList<LogbookRow>> loader, ArrayList<LogbookRow> data)
    {
        txtStatus.setText(R.string.java_message_ocr_completion);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<ArrayList<LogbookRow>> loader)
    {

    }
}