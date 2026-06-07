package com.zybooks.c499_buzicky_cheryl;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

public class SMSNotificationsActivity extends AppCompatActivity {

    private EditText quantityBlank;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smsnotifications);

        // Toolbar setup
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        quantityBlank = findViewById(R.id.quantity_blank);
        saveButton = findViewById(R.id.save_button);

        // Load saved threshold when the activity starts
        int savedThreshold = getSharedPreferences("NotificationPrefs", MODE_PRIVATE)
                .getInt("quantityThreshold", 0);
        quantityBlank.setText(String.valueOf(savedThreshold));

        // Save button click
        saveButton.setOnClickListener(v -> {
            String qtyText = quantityBlank.getText().toString();
            int threshold = qtyText.isEmpty() ? 0 : Integer.parseInt(qtyText);

            // Save threshold to SharedPreferences
            getSharedPreferences("NotificationPrefs", MODE_PRIVATE)
                    .edit()
                    .putInt("quantityThreshold", threshold)
                    .apply();

            Toast.makeText(this, "Settings Saved", Toast.LENGTH_SHORT).show();

            // Request SMS permission if needed
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.SEND_SMS}, 1);
            }
        });

    }

    // Sets up the Toolbar menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    // Menu clicks on SMSNotifications page
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // Inventory list clicked, navigate to this page
        if (id == R.id.inventory_list) {
            Intent invenIntent = new Intent(this, InventoryGrid.class);
            startActivity(invenIntent);
            return true;
        }
        // Already on notification settings page, do nothing
        else if (id == R.id.notification_settings) {
            // Already on SMS page
            return true;
        }
        else if (id == R.id.account_settings) {
            Intent invenIntent = new Intent(this, AccountSettingsActivity.class);
            startActivity(invenIntent);
            return true;
        }
        // Logout clicked, return to login screen
        else if (id == R.id.sign_out) {
            Intent logoutIntent = new Intent(this, MainActivity.class);
            logoutIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(logoutIntent);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
