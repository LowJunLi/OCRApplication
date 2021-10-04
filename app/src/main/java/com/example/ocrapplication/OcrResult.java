package com.example.ocrapplication;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OcrResult extends AppCompatActivity
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
                imgPhoto.setImageBitmap(photo);
                performOCR(photo);
            }
        });

        Button btnRescan = findViewById(R.id.result_btnRescan);
        btnRescan.setOnClickListener(v -> takePicture());

        takePicture(); //Let user take photo when the user access this page from main menu
    }

    /**
     * Use application camera to take picture
     *
     * @see <a href="https://developer.android.com/training/camera/photobasics" Take photos</a>
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
                displayToast(getString(R.string.java_error_fail_create_photo_file));
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
     */
    public void performOCR(Bitmap photo)
    {
        InputImage image = InputImage.fromBitmap(photo, 0);
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        recognizer.process(image).addOnSuccessListener(visionText ->
        {
            txtStatus.setText(R.string.java_message_ocr_completion);
            processTextRecognitionResult(visionText);
        }).addOnFailureListener(e -> txtStatus.setText(R.string.java_error_ocr_fail));
    }

    /**
     * Process the OCR result
     *
     * @param visionText result returned from successful OCR recognition of mlkit
     */
    public void processTextRecognitionResult(Text visionText)
    {
        ArrayList<String> blocks = new ArrayList<String>();
        for (Text.TextBlock block: visionText.getTextBlocks())
        {
            blocks.add(block.getText());
        }

        if (blocks.size() == 0)
        {
            txtStatus.append(getString(R.string.java_message_ocr_no_text_detected));
        }
        else
        {
            txtStatus.append(getString(R.string.java_message_ocr_text_detected));
            displayEditableTable(blocks);
        }
    }

    /**
     *  Display an editable table to let user edit the result of OCR recognition
     *
     * @param data all the text recognized by OCR
     */
    public void displayEditableTable(List<String> data)
    {
        TableLayout table = findViewById(R.id.result_tableLayout);
        table.removeAllViews();

        final int colNum = 6;
        int remainderCol = colNum - (data.size() % colNum);

        //add empty string to the arraylist to ensure the size of list is multiple of colNum
        for(int i = 0; i < remainderCol; i++)
        {
            data.add("");
        }

        TableRow header = new TableRow(this);

        TextView tvDate = new TextView(this);
        tvDate.setText("Date");
        TextView tvTime = new TextView(this);
        tvTime.setText("Time");
        TextView tvName = new TextView(this);
        tvName.setText("Name");
        TextView tvTemperature = new TextView(this);
        tvTemperature.setText("Temperature");
        TextView tvPhone = new TextView(this);
        tvPhone.setText("Phone");
        TextView tvRemark = new TextView(this);
        tvRemark.setText("Remark");

        header.addView(tvDate);
        header.addView(tvTime);
        header.addView(tvName);
        header.addView(tvTemperature);
        header.addView(tvPhone);
        header.addView(tvRemark);

        table.addView(header);


        for (int i = 0; i < data.size();)
        {
            TableRow row = new TableRow(this);

            EditText etDate = new EditText(this);
            etDate.setText(data.get(i++));
            EditText etTime = new EditText(this);
            etTime.setText(data.get(i++));
            EditText etName = new EditText(this);
            etName.setText(data.get(i++));
            EditText etTemperature = new EditText(this);
            etTemperature.setText(data.get(i++));
            EditText etPhone = new EditText(this);
            etPhone.setText(data.get(i++));
            EditText etRemark = new EditText(this);
            etRemark.setText(data.get(i++));

            row.addView(etDate);
            row.addView(etTime);
            row.addView(etName);
            row.addView(etTemperature);
            row.addView(etPhone);
            row.addView(etRemark);

            table.addView(row);
        }
    }
}