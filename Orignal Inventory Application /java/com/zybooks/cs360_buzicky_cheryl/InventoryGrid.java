package com.zybooks.cs360_buzicky_cheryl;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class InventoryGrid extends AppCompatActivity {

    RecyclerView recyclerView;
    InventoryAdaptor.InventoryAdapter adapter;
    ArrayList<InventoryItem> items;
    InventoryDatabase dbHelper;

    private Set<Integer> smsSentItems = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_grid);

        dbHelper = new InventoryDatabase(this);

        // Toolbar setup
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Opens recyclerview, arranges items vertically in the grid as they are added
        recyclerView = findViewById(R.id.inventory_grid);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Button addBtn = findViewById(R.id.add_item);

        loadItems();

        // When the Add Item button is clicked
        addBtn.setOnClickListener(v -> {
            // Create a default item first
            InventoryItem newItem = new InventoryItem(-1, "New Item", "000", 0);
            openEditDialog(newItem, true); // true = it’s a new item
        });


    }

    // Loads the items that were previously added in the app
    private void loadItems() {
        items = new ArrayList<>();
        items.addAll(dbHelper.getAllItems()); // Make sure this exists in your DB helper
        adapter = new InventoryAdaptor.InventoryAdapter(items, dbHelper);
        recyclerView.setAdapter(adapter);

        checkThresholdSMS();
    }

    // Sets up the Toolbar menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    // Handles Menu Clicks to other Pages
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        // Already on inventory grid page, do nothing
        if (id == R.id.inventory_list) {
            return true;
        }
        // Notification settings clocked, navigate to this page
        else if (id == R.id.notification_settings) {
            Intent notifIntent = new Intent(this, SMSNotificationsActivity.class);
            startActivity(notifIntent);
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
        // Default
        return super.onOptionsItemSelected(item);
    }

    // Creates the new item dialog box when a new item is added
    void openEditDialog(InventoryItem item, boolean isNewItem) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.edit_item);

        // Edit text fields for item name, SKU and quantity
        EditText nameEdit = dialog.findViewById(R.id.edit_name);
        EditText skuEdit = dialog.findViewById(R.id.edit_sku);
        EditText qtyEdit = dialog.findViewById(R.id.edit_quantity);
        // Buttons to save or cancel the item being added
        Button saveBtn = dialog.findViewById(R.id.button_save);
        Button cancelBtn = dialog.findViewById(R.id.button_cancel);

        if (item != null) {
            nameEdit.setText(item.getName());
            skuEdit.setText(item.getSku());
            qtyEdit.setText(String.valueOf(item.getQuantity()));
        }

        // If saved, items are added to the inventory grid and saved to the database
        saveBtn.setOnClickListener(v -> {
            String newName = nameEdit.getText().toString();
            String newSku = skuEdit.getText().toString();
            int newQty = qtyEdit.getText().toString().isEmpty() ? 0 : Integer.parseInt(qtyEdit.getText().toString());

            // New item added
            if (isNewItem) {
                dbHelper.insertItem(newName, newSku, newQty);
            }
            // Item updated
            else {
                dbHelper.updateItem(item.getId(), newName, newSku, newQty);
            }

            // Refresh recyclerview and items on the screen
            loadItems();
            dialog.dismiss();

            // Get threshold from preferences
            int threshold = getSharedPreferences("sms_prefs", MODE_PRIVATE)
                    .getInt("quantity_threshold", 0);

            // Only send SMS if below threshold
            if (newQty < threshold) {
                InventoryAdaptor.SMSNotifications.sendSMS(this,
                        "5554", "Low inventory alert: " + newName + " is below threshold (" + threshold + ")");
            }

        });

        // New item cancelled, return to grid
        cancelBtn.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
    // Check the item threshold and send a text message if it falls below quantity in notification settings
    private void checkThresholdSMS() {
        int threshold = getSharedPreferences("NotificationPrefs", MODE_PRIVATE)
                .getInt("quantityThreshold", 0);

        for (InventoryItem item : items) {
            // Send SMS only if below threshold and message has not been sent for this item
            if (item.getQuantity() < threshold && !smsSentItems.contains(item.getId())) {
                InventoryAdaptor.SMSNotifications.sendSMS(this, "5554", "Low inventory: " + item.getName() + " Quantity: " + item.getQuantity()
                );
                smsSentItems.add(item.getId());
            }
            // Reset SMS settings if item is restocked above threshold
            if (item.getQuantity() >= threshold && smsSentItems.contains(item.getId())) {
                smsSentItems.remove(item.getId());

            }

        }
    }

}
