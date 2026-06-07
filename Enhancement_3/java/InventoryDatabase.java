package com.zybooks.c499_buzicky_cheryl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class InventoryDatabase extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "Inventory.db";
    public static final int DATABASE_VERSION = 6;

    // User login information table
    public static final String TABLE_USERS = "users";
    public static final String COL_USER = "username";
    public static final String COL_PASS = "password";
    public static final String COL_FIRSTNAME = "firstName";
    public static final String COL_LASTNAME = "lastName";
    public static final String COL_EMAIL = "email";
    public static final String COL_PROFILE_IMAGE = "profile_image_uri";

    // Inventory grid table
    public static final String TABLE_ITEMS = "items";
    public static final String COL_ID = "id";
    public static final String COL_NAME = "name";
    public static final String COL_SKU = "sku";
    public static final String COL_QTY = "quantity";
    public static final String COL_IMAGE_URI = "image_uri";
    public static final String COL_USERNAME = "username";

    // Inventory Tracking Table
    public static final String TABLE_TRACKING = "inventory_tracking";

    public static final String COL_TRACKING_ID = "tracking_id";
    public static final String COL_TRACKING_NAME = "name";
    public static final String COL_TRACKING_SKU = "sku";
    public static final String COL_CHANGE_TYPE = "change_type";
    public static final String COL_OLD_VALUE = "old_value";
    public static final String COL_NEW_VALUE = "new_value";
    public static final String COL_TIMESTAMP = "timestamp";

    private Context context;
    public InventoryDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION); // ✅ Important
        this.context = context;
    }

    // Create the database tables and primary keys
    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE " + TABLE_USERS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_USER + " TEXT UNIQUE, " +
                COL_PASS + " TEXT, " +
                COL_FIRSTNAME + " TEXT, " +
                COL_LASTNAME + " TEXT, " +
                COL_EMAIL + " TEXT, " +
                COL_PROFILE_IMAGE + " TEXT" +
                ")");

        db.execSQL("CREATE TABLE " + TABLE_ITEMS + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_NAME + " TEXT, "
                + COL_SKU + " TEXT, "
                + COL_QTY + " INTEGER, "
                + COL_IMAGE_URI + " TEXT, "
                + COL_USERNAME + " TEXT"
                + ")");

        db.execSQL("CREATE TABLE " + TABLE_TRACKING + " ("
                + COL_TRACKING_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_TRACKING_NAME + " TEXT, "
                + COL_TRACKING_SKU + " TEXT, "
                + COL_CHANGE_TYPE + " TEXT, "
                + COL_OLD_VALUE + " TEXT, "
                + COL_NEW_VALUE + " TEXT, "
                + COL_TIMESTAMP + " TEXT"
                + ")");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRACKING);

        onCreate(db);
    }

    // Array of all items in the database to display on the inventory grid screen (recyclerview)
    public ArrayList<InventoryItem> getAllItems() {
        ArrayList<InventoryItem> items = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String username = context.getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
                        .getString("username", "");

        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_ITEMS + " WHERE " + COL_USERNAME + "=?",
                new String[]{username}
        );

        while (cursor.moveToNext()) {

            InventoryItem item = new InventoryItem(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_SKU)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_QTY)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_IMAGE_URI))
            );

            items.add(item);
        }
        cursor.close();
        return items;
    }
    // Get all inventory tracking records
    public ArrayList<InventoryTracking> getAllTrackingRecords() {

        ArrayList<InventoryTracking> records = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_TRACKING + " ORDER BY " + COL_TRACKING_ID + " DESC",
                null
        );

        while (cursor.moveToNext()) {

            InventoryTracking record = new InventoryTracking(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_TRACKING_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_TRACKING_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_TRACKING_SKU)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_CHANGE_TYPE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_OLD_VALUE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_NEW_VALUE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_TIMESTAMP))
            );

            records.add(record);
        }

        cursor.close();
        return records;
    }

    // Insert a new inventory item record into the database
    public void insertItem(String name, String sku, int qty, String imageUri) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        String username = context.getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
                        .getString("username", "");

        values.put("name", name);
        values.put("sku", sku);
        values.put("quantity", qty);
        values.put("image_uri", imageUri);
        values.put("username", username);

        db.insert("items", null, values);
    }

    // Updates an existing inventory item and records any changes in the Inventory Tracking table
    public void updateItem(int id, String name, String sku, int quantity, String imageUri) {

        SQLiteDatabase db = this.getWritableDatabase();

        String username = context.getSharedPreferences(
                        "LoginPrefs", Context.MODE_PRIVATE)
                .getString("username", "");

        // Get current values before updating
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_ITEMS +
                        " WHERE " + COL_ID + "=? AND " + COL_USERNAME + "=?",
                new String[]{String.valueOf(id), username});

        String oldName = "";
        String oldSku = "";
        int oldQuantity = 0;

        // Retrieve current database values before making updates
        if (cursor.moveToFirst()) {
            oldName = cursor.getString(
                    cursor.getColumnIndexOrThrow(COL_NAME));
            oldSku = cursor.getString(
                    cursor.getColumnIndexOrThrow(COL_SKU));
            oldQuantity = cursor.getInt(
                    cursor.getColumnIndexOrThrow(COL_QTY));
        }

        cursor.close();

        // Create timestamp for tracking records
        String timestamp = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss",
                Locale.getDefault()).format(new Date());

        // Track Name changes
        if (!oldName.equals(name)) {
            insertTrackingRecord(name, sku, "Name", oldName, name, timestamp);
        }

        // Track SKU changes
        if (!oldSku.equals(sku)) {
            insertTrackingRecord(name, sku, "SKU", oldSku, sku, timestamp);
        }

        // Track Quantity changes
        if (oldQuantity != quantity) {
            insertTrackingRecord(name, sku, "Quantity", String.valueOf(oldQuantity), String.valueOf(quantity), timestamp
            );
        }

        // Update item in the database
        ContentValues values = new ContentValues();

        values.put(COL_NAME, name);
        values.put(COL_SKU, sku);
        values.put(COL_QTY, quantity);
        values.put(COL_IMAGE_URI, imageUri);
        values.put(COL_USERNAME, username);

        db.update(
                TABLE_ITEMS,
                values,
                COL_ID + "=? AND " + COL_USERNAME + "=?",
                new String[]{String.valueOf(id), username}
        );
    }
    // Update the user profile when the user changes the first name, last name, email, username, or profile picture
    public void updateUserProfile(String username, String firstName, String lastName, String email, String profileImageUri) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        try {
            values.put(COL_FIRSTNAME, firstName);
        } catch (Exception e) {
            values.putNull(COL_FIRSTNAME);
        }

        try {
            values.put(COL_LASTNAME, lastName);
        } catch (Exception e) {
            values.putNull(COL_LASTNAME);
        }

        try {
            values.put(COL_EMAIL, email);
        } catch (Exception e) {
            values.putNull(COL_EMAIL);
        }

        try {
            values.put(COL_PROFILE_IMAGE, profileImageUri);
        } catch (Exception e) {
            values.putNull(COL_PROFILE_IMAGE);
        }

        db.update(TABLE_USERS, values, COL_USER + "=?",
                new String[]{username}
        );

        db.close();
    }
    // Retrieves full user profile data for a username
    public Cursor getUserProfile(String username) {

        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery(
                "SELECT * FROM " + TABLE_USERS +
                        " WHERE " + COL_USER + "=?",
                new String[]{username}
        );
    }
    // Updates the user's password in the database
    public void updatePassword(String username, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("password", newPassword);

        db.update("users", values, "username=?",
                new String[]{username});
    }

    // Adds data to te Inventory Tracking table when changes are made to items in the Inventory List
    public void insertTrackingRecord(String name, String sku, String changeType, String oldValue, String newValue, String timestamp) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COL_TRACKING_NAME, name);
        values.put(COL_TRACKING_SKU, sku);
        values.put(COL_CHANGE_TYPE, changeType);
        values.put(COL_OLD_VALUE, oldValue);
        values.put(COL_NEW_VALUE, newValue);
        values.put(COL_TIMESTAMP, timestamp);

        db.insert(TABLE_TRACKING, null, values);
    }
}
