package com.example.ocrapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.app.AlertDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DisplayData extends AppCompatActivity
{
    //identify which text view to change when user select different date / time picker in dialog_filter_options
    private TextView textViewToModify;
    private boolean dateTimeFilterEnabled;
    private boolean temperatureFilterEnabled;
    private long filterStartDateTime;
    private long filterEndDateTime;
    private double filterMinTemperature;
    private double filterMaxTemperature;
    private boolean filterIsAscending;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_data);

        dbHelper =  new DBHelper(this);
        resetFilter();

        Button btnFilter = findViewById(R.id.display_btnFilter);
        btnFilter.setOnClickListener(v -> showFilterOptions());
        Button btnSort = findViewById(R.id.display_btnSort);
        btnSort.setOnClickListener(v -> flipSortingOrder());
        ImageButton btnSearch = findViewById(R.id.display_btnSearch);
        btnSearch.setOnClickListener(v -> displayDataInTableBasedOnFilter());

        displayDataInTableBasedOnFilter();

    }

    /**
     * close all filter
     */
    public void resetFilter()
    {
        dateTimeFilterEnabled = false;
        temperatureFilterEnabled = false;
        filterIsAscending = true;
    }

    /**
     * Display the data (with filter) on the table layout
     */
    public void displayDataInTableBasedOnFilter()
    {
        TableLayout table = findViewById(R.id.display_tableLayout);
        table.removeAllViews();

        addHeader(table);

        String name;
        long startDateTime, endDateTime;
        double minTemperature, maxTemperature;


        Cursor cursor;
        /////name searching/////
        EditText editText = findViewById(R.id.display_etSearch);
        name = editText.getText().toString();

        /////date time filter/////
        if (dateTimeFilterEnabled) //if this filter enabled, the filter variable has been set (not null)
        {
            startDateTime = filterStartDateTime;
            endDateTime = filterEndDateTime;
        }
        else
        {
            startDateTime = -1;
            endDateTime = -1;
        }

        /////temperature filter /////
        if (temperatureFilterEnabled) //if this filter enabled, the filter variable has been set (not null)
        {
            minTemperature = filterMinTemperature;
            maxTemperature = filterMaxTemperature;
        }
        else
        {
            minTemperature = -1;
            maxTemperature = -1;
        }

        cursor = dbHelper.filterRecords(name, startDateTime, endDateTime, minTemperature, maxTemperature, filterIsAscending);
        if (cursor.moveToFirst())
        {
            do
            {
                TableRow row = new TableRow(this);
                row.setBackgroundResource(R.drawable.row_border);

                TextView tvID = new TextView(this);
                tvID.setText(String.valueOf(cursor.getInt(0)));

                TextView tvDateTime = new TextView(this);
                long unixTime = cursor.getLong(1);
                Date dateTime = new Date(unixTime);
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.ENGLISH);
                tvDateTime.setText(dateFormat.format(dateTime));

                TextView tvName = new TextView(this);
                tvName.setText(cursor.getString(2));

                TextView tvTemperature = new TextView(this);
                tvTemperature.setText(String.valueOf(cursor.getDouble(3)));

                TextView tvPhone = new TextView(this);
                tvPhone.setText(cursor.getString(4));

                TextView tvRemark = new TextView(this);
                tvRemark.setText(cursor.getString(5));

                Button btnDelete = new Button(this);
                btnDelete.setText(R.string.btnDelete);

                btnDelete.setOnClickListener(v ->
                {
                    TableRow rowToDelete = (TableRow) v.getParent();
                    TextView textView = (TextView) rowToDelete.getChildAt(0);
                    int id = Integer.parseInt(textView.getText().toString());
                    if(dbHelper.deleteRecord(id))
                    {
                        displayToast(getString(R.string.java_message_data_deleted));
                    }
                    else
                    {
                        displayToast(getString(R.string.java_error_delete_fail));
                    }
                    displayDataInTableBasedOnFilter();
                });

                Button btnEdit = new Button(this);
                btnEdit.setText(R.string.btnEdit);

                btnEdit.setOnClickListener(v ->
                {
                    TableRow rowToEdit = (TableRow) v.getParent();
                    editRow(rowToEdit);
                });

                row.addView(tvID);
                row.addView(tvDateTime);
                row.addView(tvName);
                row.addView(tvTemperature);
                row.addView(tvPhone);
                row.addView(tvRemark);
                row.addView(btnDelete);
                row.addView(btnEdit);

                table.addView(row);

            } while (cursor.moveToNext());
        }


    }

    /**
     * Add header to the table
     *
     * @param table the table to add header
     */
    public void addHeader(TableLayout table)
    {
        TableRow header = new TableRow(this);
        header.setBackgroundResource(R.drawable.row_border);

        TextView tvID = new TextView(this);
        tvID.setText(R.string.java_table_header_ID);
        TextView tvDateTime = new TextView(this);
        tvDateTime.setText(R.string.java_table_header_date_time);
        TextView tvName = new TextView(this);
        tvName.setText(R.string.java_table_header_name);
        TextView tvTemperature = new TextView(this);
        tvTemperature.setText(R.string.java_table_header_temperature);
        TextView tvPhone = new TextView(this);
        tvPhone.setText(R.string.java_table_header_phone);
        TextView tvRemark = new TextView(this);
        tvRemark.setText(R.string.java_table_header_remark);

        header.addView(tvID);
        header.addView(tvDateTime);
        header.addView(tvName);
        header.addView(tvTemperature);
        header.addView(tvPhone);
        header.addView(tvRemark);

        table.addView(header);
    }

    /**
     * Display a dialog for user to set up filter
     */
    public void showFilterOptions()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_filter_options, null);
        builder.setView(view);
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

        //find all the views in dialog_filter_options
        CheckBox cbDate = view.findViewById(R.id.dialog_filter_cbDate);
        CheckBox cbTemperature = view.findViewById(R.id.dialog_filter_cbTemperature);

        TextView txtStartDate = view.findViewById(R.id.dialog_filter_txtFromDate);
        TextView txtEndDate = view.findViewById(R.id.dialog_filter_txtToDate);
        TextView txtStartTime = view.findViewById(R.id.dialog_filter_txtFromTime);
        TextView txtEndTime = view.findViewById(R.id.dialog_filter_txtToTime);

        EditText etMinTemperature = view.findViewById(R.id.dialog_filter_etMinTemperature);
        EditText etMaxTemperature = view.findViewById(R.id.dialog_filter_etMaxTemperature);

        ImageButton btnStartDatePicker = view.findViewById(R.id.dialog_filter_date_btnDatePicker1);
        ImageButton btnStartTimePicker = view.findViewById(R.id.dialog_filter_date_btnTimePicker1);
        ImageButton btnEndDatePicker = view.findViewById(R.id.dialog_filter_date_btnDatePicker2);
        ImageButton btnEndTimePicker = view.findViewById(R.id.dialog_filter_date_btnTimePicker2);

        Button btnApplyFilter = view.findViewById(R.id.dialog_filter_btnApplyFilter);
        Button btnCancel = view.findViewById(R.id.dialog_filter_btnCancel);

        //Button logic
        btnStartDatePicker.setOnClickListener(v -> showDatePicker(txtStartDate));
        btnStartTimePicker.setOnClickListener(v -> showTimePicker(txtStartTime));
        btnEndDatePicker.setOnClickListener(v -> showDatePicker(txtEndDate));
        btnEndTimePicker.setOnClickListener(v -> showTimePicker(txtEndTime));

        btnCancel.setOnClickListener(v -> alertDialog.dismiss());

        btnApplyFilter.setOnClickListener(v ->
        {
            double minTemperature = 0, maxTemperature = 0;
            boolean emptyDateTimeError = false;
            boolean emptyTemperatureError = false;
            boolean temperatureError = false;

            /////start guard clause/////
            /////check for empty input /////
            if (cbDate.isChecked())
            {
                //if user not change the default word "Date"
                if (txtStartDate.getText().toString().equals(getString(R.string.java_table_header_date)))
                {
                    emptyDateTimeError = true;
                }

                if (txtStartTime.getText().toString().equals(getString(R.string.java_table_header_time)))
                {
                    emptyDateTimeError = true;
                }

                if (txtEndDate.getText().toString().equals(getString(R.string.java_table_header_date)))
                {
                    emptyDateTimeError = true;
                }

                if (txtEndTime.getText().toString().equals(getString(R.string.java_table_header_time)))
                {
                    emptyDateTimeError = true;
                }
            }

            if (cbTemperature.isChecked())
            {
                if (etMaxTemperature.getText().toString().equals(""))
                {
                    etMaxTemperature.setError(getString(R.string.java_error_no_temperature_input));
                    emptyTemperatureError = true;
                }

                if (etMinTemperature.getText().toString().equals(""))
                {
                    etMinTemperature.setError(getString(R.string.java_error_no_temperature_input));
                    emptyTemperatureError = true;
                }

            }

            if (emptyDateTimeError)
            {
                displayToast(getString(R.string.java_error_enter_date_time));
                return;
            }

            if (emptyTemperatureError)
            {
                return;
            }

            /////check for invalid temperature input
            if(cbTemperature.isChecked())
            {
                try
                {
                    minTemperature = Double.parseDouble(etMinTemperature.getText().toString());
                }
                catch (NumberFormatException ex)
                {
                    etMinTemperature.setError(getString(R.string.java_error_non_numeric_input));
                    temperatureError = true;
                }

                try
                {
                    maxTemperature = Double.parseDouble(etMaxTemperature.getText().toString());
                }
                catch (NumberFormatException ex)
                {
                    etMaxTemperature.setError(getString(R.string.java_error_non_numeric_input));
                    temperatureError = true;
                }

                if (temperatureError)
                {
                    return;
                }

                if (minTemperature > maxTemperature)
                {
                    displayToast(getString(R.string.java_error_invalid_temperature_range));
                    return;
                }
            }

            /////end guard clause/////

            dateTimeFilterEnabled = cbDate.isChecked();
            temperatureFilterEnabled = cbTemperature.isChecked();

            if(cbDate.isChecked())
            {
                String startDateTime = txtStartDate.getText().toString() + " " + txtStartTime.getText().toString();
                String endDateTime = txtEndDate.getText().toString() + " " + txtEndTime.getText().toString();

                Date dateTime;
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.ENGLISH);
                try
                {
                    dateTime = dateFormat.parse(startDateTime);
                }
                catch (ParseException ex)
                {
                    Log.e("DateTime Parse Error", "In parsing start date time in DisplayData.java");
                    return;
                }

                if (dateTime != null)
                {
                    filterStartDateTime = dateTime.getTime();
                }

                try
                {
                    dateTime = dateFormat.parse(endDateTime);
                }
                catch (ParseException ex)
                {
                    Log.e("DateTime Parse Error", "In parsing end date time in DisplayData.java");
                    return;
                }

                if (dateTime != null)
                {
                    filterEndDateTime = dateTime.getTime();
                }
            }

            if(cbTemperature.isChecked())
            {
                filterMinTemperature = minTemperature;
                filterMaxTemperature = maxTemperature;
            }

            displayToast(getString(R.string.java_message_filter_applied));
            displayDataInTableBasedOnFilter();
            alertDialog.dismiss();
        });
    }

    public void editRow(TableRow rowToEdit)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_edit_row, null);
        builder.setView(view);
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

        TextView tvID = view.findViewById(R.id.dialog_editRow_txtID);
        EditText etDate = view.findViewById(R.id.dialog_editRow_etDate);
        EditText etTime = view.findViewById(R.id.dialog_editRow_etTime);
        EditText etName = view.findViewById(R.id.dialog_editRow_etName);
        EditText etTemperature = view.findViewById(R.id.dialog_editRow_etTemperature);
        EditText etPhone = view.findViewById(R.id.dialog_editRow_etPhone);
        EditText etReview = view.findViewById(R.id.dialog_editRow_etReview);

        Button btnUpdate = view.findViewById(R.id.dialog_editRow_btnUpdateRow);
        Button btnCancel = view.findViewById(R.id.dialog_editRow_btnCancel);

        int id = Integer.parseInt(((TextView) rowToEdit.getChildAt(0)).getText().toString());
        tvID.append(" " + id);
        Cursor cursor = dbHelper.searchRecord(id);
        cursor.moveToFirst();

        //initialize all edit fields
        long unixTime = cursor.getLong(1);
        Date dateTime = new Date(unixTime);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
        etDate.setText(dateFormat.format(dateTime));
        dateFormat = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
        etTime.setText(dateFormat.format(dateTime));

        etName.setText(cursor.getString(2));
        etTemperature.setText(String.valueOf(cursor.getDouble(3)));
        etPhone.setText(cursor.getString(4));
        etReview.setText(cursor.getString(5));



        btnCancel.setOnClickListener(v -> alertDialog.dismiss());
        btnUpdate.setOnClickListener(v ->
        {
            boolean validationPass = true;
            //get all values from EditText
            String date = etDate.getText().toString();
            String time = etTime.getText().toString();
            String name = etName.getText().toString();
            double temperature = 35; //this variable is modified when validating
            String phone = etPhone.getText().toString();
            String review = etReview.getText().toString();

            //validation is similar to validateCell in OcrResult.java
            /////start guard clause/////
            //date validation
            SimpleDateFormat dateFormat2 = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
            dateFormat2.setLenient(false);
            try
            {
                dateFormat2.parse(date.trim());
            }
            catch (ParseException ex)
            {
                etDate.setError(getString(R.string.java_error_invalid_date));
                validationPass = false;
            }

            //time validation
            dateFormat2 = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
            dateFormat2.setLenient(false);
            try
            {
                dateFormat2.parse(time.trim());
            }
            catch (ParseException ex)
            {
                etTime.setError(getString(R.string.java_error_invalid_time));
                validationPass = false;
            }
            //temperature validation
            try
            {
                temperature = Double.parseDouble(etTemperature.getText().toString());
                if(temperature < 35 || temperature > 42)
                {
                    etTemperature.setError(getString(R.string.java_error_invalid_temperature));
                    validationPass = false;
                }
            }
            catch(NumberFormatException ex)
            {
                etTemperature.setError(getString(R.string.java_error_non_numeric_input));
                validationPass = false;
            }
            //phone number validation
            if(!phone.matches("^(\\+?6?01)[0-46-9]-*[0-9]{7,8}$"))//regex to check if the number is malaysian phone number
            {
                etPhone.setError(getString(R.string.java_error_invalid_malaysia_phone_number));
                validationPass = false;
            }

            //action if validation fail
            if(!validationPass)
            {
                return;
            }

            /////end guard clause/////
            dateFormat2 = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.ENGLISH);
            Date dateTime2;
            long enterDateTime = 0;
            try
            {
                dateTime2 = dateFormat2.parse(date + " " + time);
            }
            catch (ParseException ex)
            {
                Log.e("DateTime Parse Error", "In DisplayData.java when updating record");
                return;
            }

            if (dateTime2 != null)
            {
                enterDateTime = dateTime2.getTime();
            }

            if(dbHelper.updateRecord(id, enterDateTime, name, temperature, phone, review))
            {
                displayToast(getString(R.string.java_message_update_success));
                displayDataInTableBasedOnFilter();
                alertDialog.dismiss();
            }
        });

    }

    public void showDatePicker(TextView relatedTextView)
    {
        textViewToModify = relatedTextView;
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    public void showTimePicker(TextView relatedTextView)
    {
        textViewToModify = relatedTextView;
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getSupportFragmentManager(), "timePicker");
    }

    public void processDatePickerResult(int year, int month, int day)
    {
        textViewToModify.setText(getString(R.string.java_format_date, day, month + 1, year));
    }

    public void processTimePickerResult(int hour, int minute)
    {
        String stringMinute;

        if (minute < 10)
        {
            //add a string "0" to avoid showing minute like 5:4 (5:04)
            stringMinute = "0" + minute;
        }
        else
        {
            stringMinute = Integer.toString(minute);
        }
        textViewToModify.setText(getString(R.string.java_format_time, hour, stringMinute));
    }

    /**
     *
     */
    public void flipSortingOrder()
    {
        Button btnSort = findViewById(R.id.display_btnSort);
        if(btnSort.getText().equals(getString(R.string.display_label_ascending)))
        {
            btnSort.setText(R.string.display_label_descending);
            filterIsAscending = false;
        }
        else
        {
            btnSort.setText(R.string.display_label_ascending);
            filterIsAscending = true;
        }
        displayDataInTableBasedOnFilter();
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