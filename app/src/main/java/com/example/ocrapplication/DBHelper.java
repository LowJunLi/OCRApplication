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

        initializeData(db);
    }

    public void initializeData(SQLiteDatabase db)
    {
        insertInitialRecord(db, 1634035305000L, "Shin Wei Siong", 37.4, "0145623870", "-");
        insertInitialRecord(db, 1634035305000L, "Low Jun Yi", 36.1, "0145613880", "none");
        insertInitialRecord(db, 1634120964000L, "Low Jun Li", 36.5, "01110955400", "none");
        insertInitialRecord(db,  1634121688000L, "Bock Chuang Zher", 36.2, "0123456789", "");
        insertInitialRecord(db, 1634121705000L, "Low Jun Li", 37, "0123456798", "No comment");
        insertInitialRecord(db, 1634208105000L, "Shin Wei Siong", 36.9, "0145623870", "No comment");
        insertInitialRecord(db, 1634208105000L, "Chew Xun", 37, "0123456666", "-");
    }

    public void insertInitialRecord(SQLiteDatabase db, long enterDateTime, String name, double temperature, String phone, String remark)
    {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_ENTERDATETIME, enterDateTime);
        contentValues.put(COL_NAME, name);
        contentValues.put(COL_TEMPERATURE, temperature);
        contentValues.put(COL_PHONE, phone);
        contentValues.put(COL_REMARK, remark);
        db.insert(TABLE_NAME, null, contentValues);
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
    public boolean insertRecord(long enterDateTime, String name, double temperature, String phone, String remark)
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
    public boolean updateRecord(int id, long enterDateTime, String name, double temperature, String phone, String remark)
    {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_ENTERDATETIME, enterDateTime);
        contentValues.put(COL_NAME, name);
        contentValues.put(COL_TEMPERATURE, temperature);
        contentValues.put(COL_PHONE, phone);
        contentValues.put(COL_REMARK, remark);
        return db.update(TABLE_NAME, contentValues, COL_ID + " = " + id, null) == 1;
    }

    /*
    * Delete a row (record) in database
    *
    * @param id  the id of the record that need to be deleted
    * @return    true if the one record is deleted (deletion successful), otherwise false
     */
    public boolean deleteRecord(int id)
    {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_NAME, COL_ID + " = " + id, null) == 1;
    }

    /*
    * Get all the rows (records) in the table
    *
    * @return  cursor pointing to the database query result
     */
    public Cursor getAllRecords()
    {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
    }

    /**
     *
     */
    public Cursor searchRecord(int id)
    {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + COL_ID + " = ?", new String[]{String.valueOf(id)});
    }

    /**
     *
     */
    public Cursor filterRecords(String name, long startDateTime, long endDateTime,
                                double minTemperature, double maxTemperature, boolean isAscending)
    {
        SQLiteDatabase db = getReadableDatabase();
        StringBuilder stringBuilder = new StringBuilder();
        boolean searchName = true;

        if(name.equals("")) //not searching for particular name
        {
            searchName = false;
        }

        if(searchName) //searching for name and may be have other filters
        {
            stringBuilder.append("SELECT * FROM " + TABLE_NAME + " WHERE " + COL_NAME + " = ?");

            if(startDateTime != -1 && endDateTime != -1) //filter enable
            {
                stringBuilder.append(" AND " + COL_ENTERDATETIME + " BETWEEN ").append(startDateTime).
                        append(" AND ").append(endDateTime);
            }

            if(minTemperature != -1 && maxTemperature != -1)
            {
                stringBuilder.append(" AND " + COL_TEMPERATURE + " BETWEEN ").append(minTemperature).
                        append(" AND ").append(maxTemperature);
            }
        }
        //no searching name and no filters
        else if(startDateTime == -1 && endDateTime == -1 && minTemperature == -1 && maxTemperature == -1)
        {
            stringBuilder.append("SELECT * FROM " + TABLE_NAME);
        }
        //no searching name but has other filter
        else
        {
            boolean putWhereClause = false;
            stringBuilder.append("SELECT * FROM " + TABLE_NAME);

            if(startDateTime != -1 && endDateTime != -1) //filter enable
            {
                stringBuilder.append(" WHERE " + COL_ENTERDATETIME + " BETWEEN ").append(startDateTime).
                        append(" AND ").append(endDateTime);
                putWhereClause = true;
            }

            if(minTemperature != -1 && maxTemperature != -1)
            {
                if(!putWhereClause)
                {
                    stringBuilder.append(" WHERE ");
                }
                else
                {
                    stringBuilder.append(" AND ");
                }

                stringBuilder.append(COL_TEMPERATURE + " BETWEEN ").append(minTemperature).
                        append(" AND ").append(maxTemperature);
            }
        }

        if(isAscending)
        {
            stringBuilder.append(" ORDER BY " + COL_ENTERDATETIME + " ASC ");
        }
        else
        {
            stringBuilder.append(" ORDER BY " + COL_ENTERDATETIME + " DESC ");
        }


        if(searchName)
        {
            return db.rawQuery(stringBuilder.toString(), new String[]{name});
        }
        else
        {
            return db.rawQuery(stringBuilder.toString(), null);
        }

    }
}
