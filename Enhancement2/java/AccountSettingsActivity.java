package com.zybooks.c499_buzicky_cheryl;

import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class AccountSettingsActivity extends AppCompatActivity {

    InventoryDatabase dbHelper;
    EditText firstNameEdit;
    EditText lastNameEdit;
    TextView usernameText;
    EditText emailEdit;

    ImageView profileImage;
    Button saveButton;
    String loggedInUsername;
    String selectedImageUri;


     // Initializes the Account Settings screen, loads user data from the database,
     // and sets up processes such as profile image selection and saving updates.
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accountsettings);
        // save button setup
        saveButton = findViewById(R.id.buttonSave);
        // Change password button setup - opens new dialog window
        Button changePasswordButton = findViewById(R.id.buttonChangePassword);
        changePasswordButton.setOnClickListener(v -> openResetPasswordDialog());

        // Database
        dbHelper = new InventoryDatabase(this);

        // Toolbar setup onthe page
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Items on the page to be loaded/viewed
        firstNameEdit = findViewById(R.id.editTextFirstName);
        lastNameEdit = findViewById(R.id.editTextLastName);
        emailEdit = findViewById(R.id.editTextEmail);
        usernameText = findViewById(R.id.textUsername);
        profileImage = findViewById(R.id.profileImage);
        saveButton = findViewById(R.id.buttonSave);

        // When profile image is clicked, user can change to new image from gallery
        profileImage.setOnClickListener(v -> {

            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("image/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivityForResult(intent, 1001);
        });

        // Populate the current username on the page based on who is logged in.
        loggedInUsername = getSharedPreferences("LoginPrefs", MODE_PRIVATE)
                .getString("username", "");

        usernameText.setText(loggedInUsername);

        // Load any existing values for the other fields (name, email, picture) on the Account
        // Settings page
        Cursor cursor = dbHelper.getUserProfile(loggedInUsername);

        if (cursor.moveToFirst()) {
            // First Name
            try {
                firstNameEdit.setText(
                        cursor.getString(cursor.getColumnIndexOrThrow(InventoryDatabase.COL_FIRSTNAME)));

            } catch (Exception ignored) {
            }
            // Last Name
            try {
                lastNameEdit.setText(cursor.getString(cursor.getColumnIndexOrThrow(InventoryDatabase.COL_LASTNAME)));

            } catch (Exception ignored) {
            }
            // Email
            try {
                emailEdit.setText(cursor.getString(cursor.getColumnIndexOrThrow(InventoryDatabase.COL_EMAIL)));

            } catch (Exception ignored) {
            }
            // Profile Image
            try {
                String imageUri = cursor.getString(cursor.getColumnIndexOrThrow(InventoryDatabase.COL_PROFILE_IMAGE));

                if (imageUri != null &&
                        !imageUri.isEmpty()) {

                    selectedImageUri = imageUri;

                    profileImage.setImageURI(
                            Uri.parse(imageUri));
                }
            } catch (Exception ignored) {

                profileImage.setImageResource(R.drawable.image_placeholder);
            }
        }
        cursor.close();

        // Save button allows these to be saved to the database once entered and the button is clicked

        saveButton.setOnClickListener(v -> {

            // Read name and email inputs from the UI and convert to string
            String firstName = firstNameEdit.getText().toString().trim();
            String lastName = lastNameEdit.getText().toString().trim();
            String email = emailEdit.getText().toString().trim();

            // Validate first name has only letters
            if (!firstName.isEmpty() && !firstName.matches("[a-zA-Z]+")) {
                firstNameEdit.setError("Letters only");
                return;}

            // Validate last name has only letters
            if (!lastName.isEmpty() && !lastName.matches("[a-zA-Z]+")) {
                lastNameEdit.setError("Letters only");
                return;}

            // Validate that email has the "@" symbol
            if (!email.isEmpty() && !email.contains("@")) {
                emailEdit.setError("Invalid email format");
                return;}

            // Save any changes or updates to the database
            dbHelper.updateUserProfile(loggedInUsername, firstName, lastName, email, selectedImageUri);

            Toast.makeText(this, "Account settings saved", Toast.LENGTH_SHORT)
                    .show();
        });
    }
    // Toolbar Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.toolbar_menu, menu);

        return true;
    }

    // Toolbar Navigation to different pages
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        // Inventory Grid
        if (id == R.id.inventory_list) {

            Intent invenIntent = new Intent(
                    this,
                    InventoryGrid.class);

            startActivity(invenIntent);

            return true;
        }

        // Already on Account Settings
        else if (id == R.id.account_settings) {

            return true;
        }
        // Notification Settings
        else if (id == R.id.notification_settings) {

            Intent notifIntent = new Intent(
                    this,
                    SMSNotificationsActivity.class);

            notifIntent.addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP |
                            Intent.FLAG_ACTIVITY_SINGLE_TOP
            );
            startActivity(notifIntent);

            return true;
        }
        // Signing out of the application
        else if (id == R.id.sign_out) {

            Intent logoutIntent = new Intent(
                    this,
                    MainActivity.class);

            logoutIntent.setFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP |
                            Intent.FLAG_ACTIVITY_NEW_TASK);

            startActivity(logoutIntent);

            finish();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    // Processes result from profile image picker and displays selected image URO

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1001 &&
                resultCode == RESULT_OK &&
                data != null) {

            try {

                Uri uri = data.getData();
                if (uri != null) {
                    // Save permission so image doesn't disappear later
                    getContentResolver().takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                    );
                    selectedImageUri = uri.toString();

                    profileImage.setImageURI(uri);
                }

            } catch (Exception e) {

                profileImage.setImageResource(R.drawable.image_placeholder);
            }
        }
    }

    private void openResetPasswordDialog() {

        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.edit_password);

        InventoryDatabase dbHelper = new InventoryDatabase(this);

        EditText currentPassword = dialog.findViewById(R.id.editTextTextPassword2);
        EditText newPassword = dialog.findViewById(R.id.editTextTextPassword3);
        EditText confirmPassword = dialog.findViewById(R.id.editTextTextPassword4);

        Button cancelBtn = dialog.findViewById(R.id.button_cancel);
        Button saveBtn = dialog.findViewById(R.id.button_save);

        String loggedInUsername =
                getSharedPreferences("LoginPrefs", MODE_PRIVATE)
                        .getString("username", "");

        cancelBtn.setOnClickListener(v -> dialog.dismiss());

        saveBtn.setOnClickListener(v -> {

            String current = currentPassword.getText().toString().trim();
            String newPass = newPassword.getText().toString().trim();
            String confirm = confirmPassword.getText().toString().trim();

            // Checks if any fields are left blank, and displays toast message
            if (current.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Checks if both new password and re-enter new password fields match
            if (!newPass.equals(confirm)) {
                Toast.makeText(this, "New passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            // Pulls username from the database
            Cursor cursor = dbHelper.getUserProfile(loggedInUsername);

            // Checks that username exists before allowing password change
            if (cursor == null || !cursor.moveToFirst()) {
                Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
                if (cursor != null) cursor.close();
                return;
            }

            String storedPassword = cursor.getString(
                    cursor.getColumnIndexOrThrow("password")
            );

            cursor.close();

            // Checks current password to be sure this is correct before allowing a reset
            if (!storedPassword.equals(current)) {
                Toast.makeText(this, "Current password is incorrect", Toast.LENGTH_SHORT).show();
                return;
            }

            // Updates new password to be the current password
            dbHelper.updatePassword(loggedInUsername, newPass);

            Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show();

            dialog.dismiss();
        });

        dialog.show();
    }
}
