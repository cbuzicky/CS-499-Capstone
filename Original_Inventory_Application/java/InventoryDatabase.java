package com.zybooks.cs360_buzicky_cheryl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class InventoryDatabase extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "Inventory.db";
    public static final int DATABASE_VERSION = 1;

    // User login information table
    public static final String TABLE_USERS = "users";
    public static final String COL_USER = "username";
    public static final String COL_PASS = "password";

    // Inventory grid table
    public static final String TABLE_ITEMS = "items";
    public static final String COL_ID = "id";
    public static final String COL_NAME = "name";
    public static final String COL_QTY = "quantity";

    public InventoryDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION); // ✅ Important
    }

    // Create the database tables and primary keys
    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE " + TABLE_USERS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_USER + " TEXT UNIQUE, " +
                COL_PASS + " TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_ITEMS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_NAME + " TEXT, " +
                "sku TEXT," +
                COL_QTY + " INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMS);
        onCreate(db);
    }

    // Add new user to the database
    public boolean addUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USER, username);
        values.put(COL_PASS, password);

        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    // Check login credentials from the database
    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COL_USER},
                COL_USER + "=? AND " + COL_PASS + "=?",
                new String[]{username, password},
                null, null, null);

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }
    // Array of all items in the database to display on the inventory grid screen (recyclerview)
    public ArrayList<InventoryItem> getAllItems() {
        ArrayList<InventoryItem> items = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM items", null);

        while (cursor.moveToNext()) {
            items.add(new InventoryItem(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getInt(3)
            ));
        }
        cursor.close();
        return items;
    }

    // Insert a new inventory item record into the database
    public void insertItem(String name, String sku, int qty) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("sku", sku);
        values.put("quantity", qty);
        db.insert("items", null, values);
    }

    // Update an existing inventorty item record in the database
    public void updateItem(int id, String name, String sku, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("sku", sku);
        values.put("quantity", quantity);

        db.update("items", values, "id=?", new String[]{String.valueOf(id)});
    }


}
