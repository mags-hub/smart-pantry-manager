package com.example.smartpantrymanager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

/** Screen 1: Pantry list (READ), entry point for Add (CREATE), Edit (UPDATE) and Delete. */
public class PantryActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private RecyclerView recyclerView;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantry);
        setTitle("My Pantry");

        db = new DatabaseHelper(this);
        recyclerView = findViewById(R.id.recyclerPantry);
        tvEmpty = findViewById(R.id.tvEmpty);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        FloatingActionButton fab = findViewById(R.id.fabAdd);
        fab.setOnClickListener(v -> startActivity(new Intent(this, AddEditItemActivity.class)));

        NavHelper.setup(this, R.id.nav_pantry);
    }

    // Reload every time the screen returns to the foreground so the list is always fresh.
    @Override
    protected void onResume() {
        super.onResume();
        loadItems();
    }

    private void loadItems() {
        List<PantryItem> items = db.getAllItems();
        tvEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        boolean highlight = prefs.getBoolean("highlight_expiring", true);

        recyclerView.setAdapter(new PantryAdapter(items, highlight, new PantryAdapter.Listener() {
            @Override
            public void onEdit(PantryItem item) {
                Intent i = new Intent(PantryActivity.this, AddEditItemActivity.class);
                i.putExtra("item_id", item.getId()); // pass data with an Intent extra
                startActivity(i);
            }

            @Override
            public void onDelete(final PantryItem item) {
                new AlertDialog.Builder(PantryActivity.this)
                        .setTitle("Delete item")
                        .setMessage("Remove " + item.getName() + " from your pantry?")
                        .setPositiveButton("Delete", (d, w) -> {
                            db.deleteItem(item.getId());
                            Toast.makeText(PantryActivity.this, "Item deleted", Toast.LENGTH_SHORT).show();
                            loadItems();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        }));
    }
}
