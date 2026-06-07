package com.zybooks.c499_buzicky_cheryl;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.SearchView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class InventoryGrid extends AppCompatActivity {

    RecyclerView recyclerView;
    InventoryAdaptor.InventoryAdapter adapter;
    ArrayList<InventoryItem> items;
    InventoryDatabase dbHelper;
    SearchView searchView;

    ImageView nameSortIcon;
    ImageView skuSortIcon;
    ImageView qtySortIcon;

    private final Set<Integer> smsSentItems = new HashSet<>();

    private String selectedImageUri;

    private ImageView currentDialogImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_grid);

        dbHelper = new InventoryDatabase(this);

        // Creates new list of items, returns all items from search bar
        items = new ArrayList<>();
        items.addAll(dbHelper.getAllItems());

        // Connects recyclerview and search bar from XML
        recyclerView = findViewById(R.id.inventory_grid);
        searchView = findViewById(R.id.searchView);

        // Connects sort arrows from XML
        nameSortIcon = findViewById(R.id.name_sort_icon);
        skuSortIcon = findViewById(R.id.sku_sort_icon);
        qtySortIcon = findViewById(R.id.qty_sort_icon);

        // Opens recyclerview, arranges items vertically in the grid as they are added
        recyclerView = findViewById(R.id.inventory_grid);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Creates adapter, attaches to recyclerview to display rows
        adapter = new InventoryAdaptor.InventoryAdapter(items, dbHelper);
        recyclerView.setAdapter(adapter);

        // Initialize sorting
        setupSorting();

        // Toolbar setup
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Add button allows user to add new items, calls add_item method
        Button addBtn = findViewById(R.id.add_item);

        // Load items when app opens from database
        loadItems();

        // Reads text entered into the search bar, filters inventory list based on text
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                return true;
            }
        });

        // When the Add Item button is clicked, default values are shown for each of the text boxes
        addBtn.setOnClickListener(v -> openEditDialog(null, true));

    }
    // Attaches a popup sorting menu to the arrow header icon.
    // Executes ascending or descending sort logic to allow items to be sorted for all 3 columns
    // Three arrows, one for each column, are all clickable (Name, SKU, Qty)
    private void attachSort(View view, Runnable asc, Runnable desc) {

        view.setOnClickListener(v -> {

            PopupMenu popup = new PopupMenu(this, v);
            popup.getMenuInflater().inflate(R.menu.sort_menu, popup.getMenu());

            popup.setOnMenuItemClickListener(item -> {

                if (item.getItemId() == R.id.sort_asc) {
                    asc.run();
                } else if (item.getItemId() == R.id.sort_desc) {
                    desc.run();
                }

                adapter.notifyDataSetChanged();
                return true;
            });

            popup.show();
        });
    }

    // Loads the items that were previously added in the app to the Inventory grid page
    // Ensures page reloads after new items are added while app is in use
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
    // Default page is Inventory grid page
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        // Already on inventory grid page, do nothing
        if (id == R.id.inventory_list) {
            return true;
        }
        // Notification settings clicked, navigate to this page
        else if (id == R.id.notification_settings) {
            Intent notifIntent = new Intent(this, SMSNotificationsActivity.class);
            startActivity(notifIntent);
            return true;
        }
        // Account settings clicked, navigate to this page
        else if (id == R.id.account_settings) {
            Intent accountIntent = new Intent(this, AccountSettingsActivity.class);
            startActivity(accountIntent);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1001 &&
                resultCode == RESULT_OK &&
                data != null) {

            Uri uri = data.getData();

            if (uri != null) {

                // Validate image type (JPEG or PNG only)
                String type = getContentResolver().getType(uri);

                if (type == null ||
                        !(type.equals("image/jpeg") ||
                                type.equals("image/png"))) {

                    Toast.makeText(this,
                            "Image type must be JPEG or PNG",
                            Toast.LENGTH_SHORT).show();

                    return;
                }

                try {

                    // Persist permission so image remains after app restart
                    getContentResolver().takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                    );

                    // Save URI as string to the database
                    selectedImageUri = uri.toString();

                    // Show preview immediately in dialog box when editing an item
                    if (currentDialogImage != null) {
                        currentDialogImage.setImageURI(uri);
                    }

                } catch (Exception e) {

                    // Fallback placeholder if image cannot be loaded or no image has been added
                    if (currentDialogImage != null) {
                        currentDialogImage.setImageResource(
                                R.drawable.image_placeholder
                        );
                    }
                }
            }
        }
    }

    // Creates the new item dialog box when a new item is added
    void openEditDialog(InventoryItem item, boolean isNewItem) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.edit_item);
        selectedImageUri = null;

        // Edit text fields for item name, SKU and quantity
        EditText nameEdit = dialog.findViewById(R.id.edit_name);
        EditText skuEdit = dialog.findViewById(R.id.edit_sku);
        EditText qtyEdit = dialog.findViewById(R.id.edit_quantity);
        // Buttons to save or cancel the item being added
        Button saveBtn = dialog.findViewById(R.id.button_save);
        Button cancelBtn = dialog.findViewById(R.id.button_cancel);

        // Add images by clicking the Change Image button
        currentDialogImage = dialog.findViewById(R.id.image_item);
        ImageView itemImage = currentDialogImage;

        // Button to change the images in the dialog box to a new image
        Button changeImageBtn = dialog.findViewById(R.id.button_image);

        if (item != null) {
            nameEdit.setText(item.getName());
            skuEdit.setText(item.getSku());
            qtyEdit.setText(String.valueOf(item.getQuantity()));

            // Load existing image if you have it in your model
            try {

                if (item.getImageUrl() != null &&
                        !item.getImageUrl().isEmpty()) {

                    itemImage.setImageURI(Uri.parse(item.getImageUrl()));

                } else {

                    itemImage.setImageResource(R.drawable.image_placeholder);
                }

            } catch (Exception e) {

                itemImage.setImageResource(R.drawable.image_placeholder);
            }
        }


        changeImageBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivityForResult(intent, 1001);
        });

        // If saved and validated, items are added to the inventory grid and saved to the database
        saveBtn.setOnClickListener(v -> {

            String newName = nameEdit.getText().toString().trim();
            String newSku = skuEdit.getText().toString().trim();
            String qtyStr = qtyEdit.getText().toString().trim();

            // Validate that item name can only contain letters
            if (!newName.matches("[a-zA-Z ]+")) {
                nameEdit.setError("Letters only");
                return;}

            // Validate that item SKU can have letters and numbers
            if (!newSku.matches("[a-zA-Z0-9]+")) {
                skuEdit.setError("Letters and numbers only");
                return;}

            // Validate that item quantity can only have numbers
            if (!qtyStr.matches("\\d{1,6}")) {
                qtyEdit.setError("Numbers only, max 6 digits");
                return;}

            // Change quantity to an integer to use in the SMS Notifications
            int newQty = Integer.parseInt(qtyStr);

            // Save the image selected, otherwise image can be blank if none is added
            String imageUrl = selectedImageUri != null
                    ? selectedImageUri
                    : (item != null ? item.getImageUrl() : null);

            // New item added to the database
            if (isNewItem) {
                dbHelper.insertItem(newName, newSku, newQty, imageUrl);
            }
            // Item updated in the database
            else {
                dbHelper.updateItem(item.getId(), newName, newSku, newQty, imageUrl);
            }

            // Refresh recyclerview and items on the screen
            loadItems();
            dialog.dismiss();

            // Get threshold from preferences, default threshold is set to zero.
            int threshold = getSharedPreferences("sms_prefs", MODE_PRIVATE)
                    .getInt("quantity_threshold", 0);

            // Only send SMS if this new quantity is below the set threshold
            if (newQty < threshold) {
                InventoryAdaptor.SMSNotifications.sendSMS(this,
                        "5554", "Low inventory alert: " + newName + " is below threshold (" + threshold + ")");
            }

        });

        // New item cancelled, return to grid
        cancelBtn.setOnClickListener(v -> dialog.dismiss());

        // Start/open dialog box
        dialog.show();

        // dialog box settings when opened
        dialog.getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    // Checks all inventory items against the user-defined quantity threshold
    // and sends an SMS alert when an item falls below the threshold.
    // Ensures each item triggers only one alert until the quantity goes above the threshold.
    void checkThresholdSMS() {
        {
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

    // Sets up sorting for the Inventory List by Name, SKU, or Quantity.
    // Sorts the adapter's filtered list in ascending or descending order
    // by comparing item values using a lambda expression to sort the list
    private void setupSorting() {

        attachSort(nameSortIcon,() -> Collections.sort(adapter.getFilteredList(),
                        (a, b) -> a.getName().compareToIgnoreCase(b.getName())),
                () -> Collections.sort(adapter.getFilteredList(),
                        (a, b) -> b.getName().compareToIgnoreCase(a.getName()))
        );

        attachSort(skuSortIcon,() -> Collections.sort(adapter.getFilteredList(),
                        (a, b) -> a.getSku().compareToIgnoreCase(b.getSku())),
                () -> Collections.sort(adapter.getFilteredList(),
                        (a, b) -> b.getSku().compareToIgnoreCase(a.getSku()))
        );

        attachSort(qtySortIcon,() -> Collections.sort(adapter.getFilteredList(),
                        (a, b) -> Integer.compare(a.getQuantity(), b.getQuantity())),
                () -> Collections.sort(adapter.getFilteredList(),
                        (a, b) -> Integer.compare(b.getQuantity(), a.getQuantity()))
        );
    }
}
