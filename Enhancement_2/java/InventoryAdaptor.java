package com.zybooks.c499_buzicky_cheryl;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.telephony.SmsManager;
import android.view.*;
import android.widget.*;

import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
public class InventoryAdaptor {

    // Connects database to recyclerview
    public static class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.ViewHolder> {

        List<InventoryItem> items;

        List<InventoryItem> filteredList;
        InventoryDatabase dbHelper;

        public InventoryAdapter(List<InventoryItem> items, InventoryDatabase dbHelper) {
            this.items = items;
            this.dbHelper = dbHelper;

            this.filteredList = new ArrayList<>(items);
        }

        public void filter(String text) {

            filteredList.clear();

            if (text == null || text.trim().isEmpty()) {
                filteredList.addAll(items);
            } else {
                String query = text.toLowerCase();

                for (InventoryItem item : items) {
                    if (item.getName().toLowerCase().contains(query) ||
                            item.getSku().toLowerCase().contains(query)) {

                        filteredList.add(item);
                    }
                }
            }
            notifyDataSetChanged();
        }
        // Links data in database to recyclerview, creates the rows
        public static class ViewHolder extends RecyclerView.ViewHolder {
            TextView name, sku, qty;

            ImageView itemImage;
            ImageButton deleteBtn;

            // ViewHolder initializes all UI components for a single RecyclerView item view per row
            public ViewHolder(View view) {
                super(view);
                name = view.findViewById(R.id.text_item);
                sku = view.findViewById(R.id.text_sku);
                qty = view.findViewById(R.id.text_quantity);
                itemImage = view.findViewById(R.id.image_item);
                deleteBtn = view.findViewById(R.id.button_delete);
            }
        }

        // Creates a new row on the Inventory List page if items are added
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.inventory_items, parent, false);
            return new ViewHolder(view);
        }

        // Displays data from database in the rows of inventory list
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {

            InventoryItem item = filteredList.get(holder.getBindingAdapterPosition());

            holder.name.setText(item.getName());
            holder.sku.setText(item.getSku());
            holder.qty.setText(String.valueOf(item.getQuantity()));


            if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {

                try {
                    holder.itemImage.setImageURI(Uri.parse(item.getImageUrl()));
                } catch (Exception e) {
                    holder.itemImage.setImageResource(R.drawable.image_placeholder);
                }

            } else {
                holder.itemImage.setImageResource(R.drawable.image_placeholder);
            }

            holder.itemView.setOnClickListener(v -> {
                InventoryItem clickedItem = items.get(holder.getBindingAdapterPosition());
                ((InventoryGrid) v.getContext()).openEditDialog(clickedItem, false);
            });

            //Deletes item from the database when red delete button is clicked
            //Alert Dialog box opens first, asking if the user is certain they want to delete the item

            holder.deleteBtn.setOnClickListener(v -> {

                int pos = holder.getBindingAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return;

                InventoryItem itemToDelete = filteredList.get(pos);

                new AlertDialog.Builder(v.getContext())
                        .setTitle("Delete Item")
                        .setMessage("Are you sure you want to delete this item?")
                        .setPositiveButton("Delete", (dialog, which) -> {

                            SQLiteDatabase db = dbHelper.getWritableDatabase();
                            db.delete(
                                    "items",
                                    "id=?",
                                    new String[]{String.valueOf(itemToDelete.getId())}
                            );

                            // remove from BOTH lists (important!)
                            items.remove(itemToDelete);
                            filteredList.remove(pos);

                            notifyItemRemoved(pos);
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                        .show();
            });
        }
        // Record of number of rows in recycler view for display
        @Override
        public int getItemCount() {
            return filteredList.size();
        }

        public List<InventoryItem> getFilteredList() {
            return filteredList;
        }
    }
    // Checks permissions from notification settings, sends SMS on low inventory
        public static class SMSNotifications {

            public static void sendSMS(Context context, String phone, String message) {
            // User needs to provide permission to get text messages before they can be sent
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS)
                    != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(context, "SMS permission not granted", Toast.LENGTH_SHORT).show();
                return;
            }

            // Gets default SMS settings, sends low inventory message to phone
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage("5554", null, message, null, null);

            Toast.makeText(context, "Low inventory Message Sent", Toast.LENGTH_SHORT).show();
        }
    }




}
