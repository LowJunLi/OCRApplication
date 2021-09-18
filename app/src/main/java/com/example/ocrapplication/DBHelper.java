package com.example.ocrapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DBHelper extends SQLiteOpenHelper
{
    //Database Info
    private static final String DATABASE_NAME = "OCRDatabase";
    private static final int DATABASE_VERSION = 1;

    //Table Names
    private static final String TABLE_NAME = "records";

    //Records table columns
    private static final String COL_ID = "id";
    private static final String COL_ENTERDATETIME = "enterDateTime";
    private static final String COL_NAME = "name";
    private static final String COL_TEMPERATURE = "temperature";
    private static final String COL_PHONE = "phone";
    private static final String COL_REMARK = "remark";

    DBHelper(@Nullable Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        String sql = "CREATE TABLE " + TABLE_NAME + " (\n" +
                "   " + COL_ID + " INTEGER NOT NULL CONSTRAINT employees_pk PRIMARY KEY AUTOINCREMENT ,\n" +
                "   " + COL_ENTERDATETIME + " datetime NOT NULL,\n" +
                "   " + COL_NAME + " varchar(200) NOT NULL,\n" +
                "   " + COL_TEMPERATURE + " double NOT NULL,\n" +
                "   " + COL_PHONE + " varchar(11) NOT NULL,\n" +
                "   " + COL_REMARK + " varchar(300) \n" +
                ");";
        //Executing the string to create table
        db.execSQL(sql);

        initializeData();
    }

    public void initializeData()
    {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        String sql = "DROP TABLE IF EXISTS " + TABLE_NAME + ";";
        db.execSQL(sql);
        onCreate(db);
    }

    /*
    * insert new data into database
    *
    * @param enterDateTime  the date and time the person enter
    * @param name           the name of the person
    * @param temperature    the temperature of the person
    * @param phone          the hand phone number of the person
    * @param remark         the remark written by the person
    * @return               true if the insertion is successful, otherwise false
     */
    boolean insertRecord(String enterDateTime, String name, double temperature, String phone, String remark)
    {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_ENTERDATETIME, enterDateTime);
        contentValues.put(COL_NAME, name);
        contentValues.put(COL_TEMPERATURE, temperature);
        contentValues.put(COL_PHONE, phone);
        contentValues.put(COL_REMARK, remark);
        SQLiteDatabase db = getWritableDatabase();
        return db.insert(TABLE_NAME, null, contentValues) != -1;
    }

    /*
     * insert new data into database
     *
     * @param id             the id of the record to be updated
     * @param enterDateTime  the updated date and time
     * @param name           the updated name
     * @param temperature    the updated temperature
     * @param phone          the updated hand phone number
     * @param remark         the updated remark
     * @return               true if the one record is updated (update successful), otherwise false
     */
    boolean updateRecord(int id, String enterDateTime, String name, double temperature, String phone, String remark)
    {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_ENTERDATETIME, enterDateTime);
        contentValues.put(COL_NAME, name);
        contentValues.put(COL_TEMPERATURE, temperature);
        contentValues.put(COL_PHONE, phone);
        contentValues.put(COL_REMARK, remark);
        return db.update(TABLE_NAME, contentValues, COL_ID + "=?", new String[]{String.valueOf(id)}) == 1;
    }

    /*
    * Delete a row (record) in database
    *
    * @param id  the id of the record that need to be deleted
    * @return    true if the one record is deleted (deletion successful), otherwise false
     */
    boolean deleteRecord(int id)
    {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_NAME, COL_ID + "=?", new String[]{String.valueOf(id)}) == 1;
    }

    /*
    * Get all the rows (records) in the table
    *
    * @return  cursor pointing to the database query result
     */
    Cursor getAllRecords()
    {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
    }
}
