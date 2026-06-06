package com.zybooks.cs360_buzicky_cheryl;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.telephony.SmsManager;
import android.view.*;
import android.widget.*;

import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
public class InventoryAdaptor {

    // Connects database to recyclerview
    public static class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.ViewHolder> {

        List<InventoryItem> items;
        InventoryDatabase dbHelper;

        public InventoryAdapter(List<InventoryItem> items, InventoryDatabase dbHelper) {
            this.items = items;
            this.dbHelper = dbHelper;
        }
        // Links data in database to recyclerview, creates the rows
        public static class ViewHolder extends RecyclerView.ViewHolder {
            TextView name, sku, qty;
            Button deleteBtn;

            public ViewHolder(View view) {
                super(view);
                name = view.findViewById(R.id.text_item);
                sku = view.findViewById(R.id.text_sku);
                qty = view.findViewById(R.id.text_quantity);
                deleteBtn = view.findViewById(R.id.button_delete);
            }
        }

        // Creates a new row if items are added
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.inventory_items, parent, false);
            return new ViewHolder(view);
        }

        // Displays data from database in the rows of inventory list
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {

            InventoryItem item = items.get(position);

            holder.name.setText(item.getName());
            holder.sku.setText(item.getSku());
            holder.qty.setText(String.valueOf(item.getQuantity()));

            holder.itemView.setOnClickListener(v -> {
                InventoryItem clickedItem = items.get(holder.getAdapterPosition());
                ((InventoryGrid) v.getContext()).openEditDialog(clickedItem, false);
            });

            // Deletes item from the database when red delete button is clicked
            holder.deleteBtn.setOnClickListener(v -> {
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                db.delete("items", "id=?",
                        new String[]{String.valueOf(item.getId())});

                items.remove(position);
                notifyDataSetChanged();
            });
        }
        // Record of number of rows in recycler view for display
        @Override
        public int getItemCount() {
            return items.size();
        }
    }
    // Checks permissions from notification settings, sends SMS on low inventory
    public static class SMSNotifications {

        public static void sendSMS(Context context, String phone, String message) {

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
