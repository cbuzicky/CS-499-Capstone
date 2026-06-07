package com.zybooks.c499_buzicky_cheryl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class InventoryDatabase extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "Inventory.db";
    public static final int DATABASE_VERSION = 3;

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

    public InventoryDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION); // ✅ Important
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
                COL_PROFILE_IMAGE + " TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_ITEMS + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_NAME + " TEXT, "
                + COL_SKU + " TEXT, "
                + COL_QTY + " INTEGER, "
                + COL_IMAGE_URI + " TEXT"
                + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMS);
        onCreate(db);
    }

    // Array of all items in the database to display on the inventory grid screen (recyclerview)
    public ArrayList<InventoryItem> getAllItems() {
        ArrayList<InventoryItem> items = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM items", null);

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

    // Insert a new inventory item record into the database
    public void insertItem(String name, String sku, int qty, String imageUri) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("sku", sku);
        values.put("quantity", qty);
        values.put("image_uri", imageUri);

        db.insert("items", null, values);
    }

    // Update an existing inventory item record in the database
    public void updateItem(int id, String name, String sku, int quantity, String imageUri) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("sku", sku);
        values.put("quantity", quantity);
        values.put("image_uri", imageUri);

        db.update("items", values, "id=?", new String[]{String.valueOf(id)});
    }
    // Update the user profile when the user changes the first name, last name, email, username, or profile picture
    public void updateUserProfile(String username, String firstName, String lastName, String email, String profileImageUri
    ) {

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
}
