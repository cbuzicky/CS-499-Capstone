package com.zybooks.c499_buzicky_cheryl;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class InventoryTrackingAdapter extends RecyclerView.Adapter<InventoryTrackingAdapter.ViewHolder> {

        private Context context;
        private ArrayList<InventoryTracking> list;

        public InventoryTrackingAdapter(Context context, ArrayList<InventoryTracking> list) {
            this.context = context;
            this.list = list;
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {

            TextView name, sku, change, oldValue, newValue, date;

            public ViewHolder(View itemView) {
                super(itemView);

                name = itemView.findViewById(R.id.tracking_name);
                sku = itemView.findViewById(R.id.tracking_sku);
                change = itemView.findViewById(R.id.tracking_change);
                oldValue = itemView.findViewById(R.id.tracking_old_value);
                newValue = itemView.findViewById(R.id.tracking_new_value);
                date = itemView.findViewById(R.id.tracking_date);
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context)
                    .inflate(R.layout.inventory_tracking_items, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

            InventoryTracking item = list.get(position);

            holder.name.setText(item.getName());
            holder.sku.setText(item.getSku());
            holder.change.setText(item.getChangeType());
            holder.oldValue.setText(item.getOldValue());
            holder.newValue.setText(item.getNewValue());
            holder.date.setText(item.getTimestamp());
        }

        @Override
        public int getItemCount() {
            return list.size();
        }
}

