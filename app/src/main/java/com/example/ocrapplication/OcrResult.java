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
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OcrResult extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<String>>
{
    private ActivityResultLauncher<Intent> activityResultLauncher;
    private String currentPhotoPath;
    private Bitmap photo;
    private OpenCVOcr openCVOcr;
    private TextView txtStatus;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr_result);

        txtStatus = findViewById(R.id.result_txtStatus);

        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result ->
        {
            if (result.getResultCode() == Activity.RESULT_OK)
            {
                //Uri uri = Uri.parse(currentPhotoPath);
                Uri uri = Uri.fromFile(new File((currentPhotoPath)));
                openCropActivity(uri, uri); //same source and destination uri
            }
        });
        dbHelper = new DBHelper(this);

        Button btnRescan = findViewById(R.id.result_btnRescan);
        Button btnOk = findViewById(R.id.result_btnOk);
        btnRescan.setOnClickListener(v -> takePicture());
        btnOk.setOnClickListener(v -> saveToDatabase());
        Button btnAddRow = findViewById(R.id.result_btnAddRow);
        btnAddRow.setOnClickListener(v -> addRow());

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
     *  Let user crop the image
     *
     * @param sourceUri       the source of the image
     * @param destinationUri  the destination of the cropped image
     */
    private void openCropActivity(Uri sourceUri, Uri destinationUri)
    {
        UCrop.of(sourceUri, destinationUri)
                .start(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK)
        {
            Uri imageUri = UCrop.getOutput(data);
            Bitmap bitmap;
            try
            {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                ImageView imgPhoto = findViewById(R.id.result_imgOriginalPhoto);
                imgPhoto.setImageBitmap(bitmap);
                performOCR(bitmap);
            }
            catch(IOException ex)
            {
                Log.e("URI resolve error", ex.getMessage());
            }

        }
    }

    /**
     * Perform OCR on the picture
     */
    public void performOCR(Bitmap photo)
    {
        this.photo = photo;
        txtStatus.setText(getString(R.string.result_processing));
        LoaderManager.getInstance(this).restartLoader(0, null, this);
    }

    @NonNull
    @Override
    public Loader<List<String>> onCreateLoader(int id, @Nullable Bundle args)
    {
        openCVOcr = new OpenCVOcr();
        return new OcrLoader(this, photo, openCVOcr);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<List<String>> loader, List<String> data)
    {
        txtStatus.setText(R.string.java_message_ocr_completion);
        ImageView imgProcessedPhoto = findViewById(R.id.result_imgProcessedPhoto);
        imgProcessedPhoto.setImageBitmap(openCVOcr.getProcessedImage());
        displayEditableTable(data);//original image is used as the image is displayed with rectangle
    }

    @Override
    public void onLoaderReset(@NonNull Loader<List<String>> loader)
    {

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

        addHeader(table);

        for (int i = 0; i < data.size();)
        {
            TableRow row = new TableRow(this);
            row.setBackgroundResource(R.drawable.row_border);

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

            Button btnDelete = new Button(this);
            btnDelete.setText(R.string.btnDelete);

            //https://stackoverflow.com/questions/11050059/delete-row-dynamically-from-table-layout-in-android
            btnDelete.setOnClickListener(v ->
            {
                // row is your row, the parent of the clicked button
                View row1 = (View) v.getParent();
                // container contains all the rows, you could keep a variable somewhere else to the container which you can refer to here
                ViewGroup container = ((ViewGroup) row1.getParent());
                // delete the row and invalidate your view so it gets redrawn
                container.removeView(row1);
                container.invalidate();
            });

            row.addView(etDate);
            row.addView(etTime);
            row.addView(etName);
            row.addView(etTemperature);
            row.addView(etPhone);
            row.addView(etRemark);
            row.addView(btnDelete);

            table.addView(row);
        }
    }

    /**
     * Add header to the table
     *
     * @param table  the table to add header
     */
    public void addHeader(TableLayout table)
    {
        TableRow header = new TableRow(this);
        header.setBackgroundResource(R.drawable.row_border);

        TextView tvDate = new TextView(this);
        tvDate.setText(R.string.java_table_header_date);
        TextView tvTime = new TextView(this);
        tvTime.setText(R.string.java_table_header_time);
        TextView tvName = new TextView(this);
        tvName.setText(R.string.java_table_header_name);
        TextView tvTemperature = new TextView(this);
        tvTemperature.setText(R.string.java_table_header_temperature);
        TextView tvPhone = new TextView(this);
        tvPhone.setText(R.string.java_table_header_phone);
        TextView tvRemark = new TextView(this);
        tvRemark.setText(R.string.java_table_header_remark);

        header.addView(tvDate);
        header.addView(tvTime);
        header.addView(tvName);
        header.addView(tvTemperature);
        header.addView(tvPhone);
        header.addView(tvRemark);

        table.addView(header);
    }

    /**
     *  Add empty row to the table
     */
    public void addRow()
    {
        TableLayout table = findViewById(R.id.result_tableLayout);

        if(table.getChildCount() == 0) //if table has no child
        {
            addHeader(table);
        }

        TableRow row = new TableRow(this);
        row.setBackgroundResource(R.drawable.row_border);

        EditText etDate = new EditText(this);
        etDate.setText("");
        EditText etTime = new EditText(this);
        etTime.setText("");
        EditText etName = new EditText(this);
        etName.setText("");
        EditText etTemperature = new EditText(this);
        etTemperature.setText("");
        EditText etPhone = new EditText(this);
        etPhone.setText("");
        EditText etRemark = new EditText(this);
        etRemark.setText("");

        Button btnDelete = new Button(this);
        btnDelete.setText(R.string.btnDelete);

        //https://stackoverflow.com/questions/11050059/delete-row-dynamically-from-table-layout-in-android
        btnDelete.setOnClickListener(v ->
        {
            // row is your row, the parent of the clicked button
            View row1 = (View) v.getParent();
            // container contains all the rows, you could keep a variable somewhere else to the container which you can refer to here
            ViewGroup container = ((ViewGroup) row1.getParent());
            // delete the row and invalidate your view so it gets redrawn
            container.removeView(row1);
            container.invalidate();
        });

        row.addView(etDate);
        row.addView(etTime);
        row.addView(etName);
        row.addView(etTemperature);
        row.addView(etPhone);
        row.addView(etRemark);
        row.addView(btnDelete);

        table.addView(row);
    }

    /**
     * Get all data from table
     *
     * @param tableID  the resource ID of table layout
     * @return         the table data in 2D array. Return null if any cell contains invalid data.
     */
    @Nullable
    public String[][] getContentInTable(int tableID)
    {
        TableLayout table = findViewById(tableID);
        final int colNum = 6;
        final int rowNum = table.getChildCount();
        String[][] data = new String[rowNum - 1][colNum];

        //start at i = 1 because i = 0 is header
        for(int i = 1; i < rowNum; i++)
        {
            View child = table.getChildAt(i);
            if (child instanceof TableRow)
            {
                TableRow row = (TableRow) child;
                for (int j = 0; j < colNum; j++)
                {
                    View view = row.getChildAt(j);
                    if(view instanceof EditText)
                    {
                        EditText editText = ((EditText) view);

                        if(validateCell(editText, j))
                        {
                            data[i - 1][j] = editText.getText().toString();
                        }
                        else
                        {
                            return null;
                        }

                    }

                }

            }
        }
        return data;
    }

    /**
     *
     */
    public boolean validateCell(EditText editText, int colIndex)
    {
        String input = editText.getText().toString();
        switch(colIndex)
        {
            case 0: //date
                if(!checkIsValidDate(input))
                {
                    editText.setError(getString(R.string.java_error_invalid_date));
                    return false;
                }
                return true;
            case 1: //time
                if(!checkIsValidTime(input))
                {
                    editText.setError(getString(R.string.java_error_invalid_time));
                    return false;
                }
                return true;
            case 2: //name
                return true;
            case 3: //temperature
                try
                {
                    double temperature = Double.parseDouble(input);
                    if(temperature < 35 || temperature > 42)
                    {
                        editText.setError(getString(R.string.java_error_invalid_temperature));
                        return false;
                    }
                }
                catch(NumberFormatException ex)
                {
                    editText.setError(getString(R.string.java_error_non_numeric_input));
                    return false;
                }
                return true;
            case 4: //phone
                if(input.matches("^(\\+?6?01)[0-46-9]-*[0-9]{7,8}$"))//regex to check if the number is malaysian phone number
                {
                    return true;
                }
                else
                {
                    editText.setError(getString(R.string.java_error_invalid_malaysia_phone_number));
                    return false;
                }

            case 5: //remark
                return true;
            default:
                return false;
        }
    }


    /**
     * Check if the date is valid
     *
     * @param input  date input
     * @return       true if the input is a valid date, otherwise false
     */
    public boolean checkIsValidDate(String input)
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
        dateFormat.setLenient(false);
        try
        {
            dateFormat.parse(input.trim());
        }
        catch (ParseException ex)
        {
            return false;
        }
        return true;
    }

    /**
     * Check if the time is valid
     *
     * @param input  time input
     * @return       true if the input is a valid time, otherwise false
     */
    public boolean checkIsValidTime(String input)
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
        dateFormat.setLenient(false);
        try
        {
            dateFormat.parse(input.trim());
        }
        catch (ParseException ex)
        {
            return false;
        }
        return true;
    }


    /**
     *  Save data displayed in the table to database
     */
    public void saveToDatabase()
    {
        String[][] data = getContentInTable(R.id.result_tableLayout);
        if(data == null)
        {
            displayToast(getString(R.string.java_error_unable_to_save));
            return;
        }

        for (String[] datum : data)
        {
            Date dateTime;
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.ENGLISH);
            try
            {
                dateTime = dateFormat.parse(datum[0] + " " + datum[1]);
            }
            catch (ParseException ex)
            {
                Log.e("DateTime Parse Error", "In saveToDatabase");
                return;
            }

            long enterDateTime = 0;
            if (dateTime != null)
            {
                enterDateTime = dateTime.getTime();
            }
            String name = datum[2];
            double temperature;
            try
            {
                temperature = Double.parseDouble(datum[3]);
            }
            catch (NumberFormatException ex)
            {
                Log.e("Number Format error", "In saveToDatabase");
                return;
            }
            String phone = datum[4];
            String remark = datum[5];

            if(dbHelper.insertRecord(enterDateTime, name, temperature, phone, remark))
            {
                displayToast(getString(R.string.java_message_insert_success));
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        }

    }
}