package com.example.smartpantrymanager;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/** Custom RecyclerView adapter that binds PantryItem objects to item_pantry.xml rows. */
public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.ViewHolder> {

    public interface Listener {
        void onEdit(PantryItem item);
        void onDelete(PantryItem item);
    }

    private final List<PantryItem> items;
    private final Listener listener;
    private final boolean highlightExpiring;

    public PantryAdapter(List<PantryItem> items, boolean highlightExpiring, Listener listener) {
        this.items = items;
        this.highlightExpiring = highlightExpiring;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pantry, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        final PantryItem item = items.get(position);
        h.name.setText(item.getName());
        String q = (item.getQuantity() == Math.floor(item.getQuantity()))
                ? String.valueOf((long) item.getQuantity()) : String.valueOf(item.getQuantity());
        h.qty.setText(q + " " + item.getUnit());

        String expiry = item.getExpiry();
        if (expiry == null || expiry.isEmpty()) {
            h.expiry.setText("No expiry date");
            h.expiry.setTextColor(Color.GRAY);
        } else {
            h.expiry.setText("Expires: " + expiry);
            h.expiry.setTextColor(highlightExpiring && expiresSoon(expiry) ? Color.RED : Color.GRAY);
        }

        h.itemView.setOnClickListener(v -> listener.onEdit(item));
        h.delete.setOnClickListener(v -> listener.onDelete(item));
    }

    /** True if the date is within the next 3 days (or already past). */
    private boolean expiresSoon(String expiry) {
        try {
            Date d = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(expiry);
            long days = TimeUnit.MILLISECONDS.toDays(d.getTime() - System.currentTimeMillis());
            return days <= 3;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, qty, expiry;
        ImageButton delete;

        ViewHolder(View v) {
            super(v);
            name = v.findViewById(R.id.tvItemName);
            qty = v.findViewById(R.id.tvItemQty);
            expiry = v.findViewById(R.id.tvItemExpiry);
            delete = v.findViewById(R.id.btnDelete);
        }
    }
}
