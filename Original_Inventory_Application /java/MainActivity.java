package com.zybooks.cs360_buzicky_cheryl;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private InventoryDatabase dbHelper;
    private EditText usernameEditText, passwordEditText;
    private Button loginButton, createAccountButton, saveAccountButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Database Helper
        dbHelper = new InventoryDatabase(this);

        // Get references to views
        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginbutton);
        createAccountButton = findViewById(R.id.accountbutton);
        saveAccountButton = findViewById(R.id.save_account);

        // Save button is hidden
        saveAccountButton.setVisibility(View.INVISIBLE);

        // Login button
        loginButton.setOnClickListener(v -> attemptLogin());

        // Create account button, save account visible after clicked
        createAccountButton.setOnClickListener(v -> {
            saveAccountButton.setVisibility(View.VISIBLE);
        });

        // Save account button
        saveAccountButton.setOnClickListener(v -> createAccount());
    }

    private void attemptLogin() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // If user presses login button without entering the password
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Reads the database to check for the username and password
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM users WHERE username=? AND password=?",
                new String[]{username, password}
        );
        // If login successful, navigate to Inventory Grid page
        if (cursor.moveToFirst()) {
            cursor.close();
            Intent intent = new Intent(this, InventoryGrid.class);
            startActivity(intent);
            finish();

            // Otherwise toast message of invalid username and password
        } else {
            cursor.close();
            Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
        }
    }

    //  Creating a new user account
    private void createAccount() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // If username is entered without password when creating the account
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Username and password cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // Inserts the new username and password into the database to create the account
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            db.execSQL(
                    "INSERT INTO users (username, password) VALUES (?, ?)",
                    new Object[]{username, password}
            );
            Toast.makeText(this, "Account created successfully!", Toast.LENGTH_LONG).show();
            saveAccountButton.setVisibility(View.INVISIBLE);

        }
        // Catches any exceptions, account not created/saved
        catch (Exception e) {
            Toast.makeText(this, "Error creating account: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
