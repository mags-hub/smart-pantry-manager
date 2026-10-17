package com.example.smartpantrymanager;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.Locale;

/** Screen 2: Add or edit one pantry item, with input validation. */
public class AddEditItemActivity extends AppCompatActivity {

    private static final String[] UNITS = {"g", "kg", "ml", "l", "tsp", "tbsp", "cup", "pcs"};

    private EditText etName, etQty, etExpiry;
    private Spinner spUnit;
    private DatabaseHelper db;
    private long itemId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit);

        db = new DatabaseHelper(this);
        etName = findViewById(R.id.etName);
        etQty = findViewById(R.id.etQuantity);
        etExpiry = findViewById(R.id.etExpiry);
        spUnit = findViewById(R.id.spUnit);
        Button btnSave = findViewById(R.id.btnSave);

        spUnit.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, UNITS));

        // Edit mode if an item id was passed in the Intent
        itemId = getIntent().getLongExtra("item_id", -1);
        if (itemId != -1) {
            setTitle("Edit Ingredient");
            PantryItem item = db.getItem(itemId);
            if (item != null) {
                etName.setText(item.getName());
                etQty.setText(String.valueOf(item.getQuantity()));
                etExpiry.setText(item.getExpiry());
                for (int i = 0; i < UNITS.length; i++) {
                    if (UNITS[i].equals(item.getUnit())) spUnit.setSelection(i);
                }
            }
        } else {
            setTitle("Add Ingredient");
        }

        // Date picker for the optional expiry date
        etExpiry.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(this, (view, y, m, d) ->
                    etExpiry.setText(String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d)),
                    c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        btnSave.setOnClickListener(v -> save());
    }

    private void save() {
        String name = etName.getText().toString().trim();
        name = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
        String qtyText = etQty.getText().toString().trim();
        String unit = spUnit.getSelectedItem().toString();
        String expiry = etExpiry.getText().toString().trim();

        // ---- Input validation ----
        if (name.isEmpty()) {
            etName.setError("Ingredient name is required");
            return;
        }
        if (!name.matches("[A-Za-z ]+")) {
            etName.setError("Use letters only");
            return;
        }
        double qty;
        try {
            qty = Double.parseDouble(qtyText);
        } catch (NumberFormatException e) {
            etQty.setError("Enter a valid number");
            return;
        }
        if (qty <= 0) {
            etQty.setError("Quantity must be greater than 0");
            return;
        }

        if (itemId == -1) {
            db.insertItem(name, qty, unit, expiry);
            Toast.makeText(this, "Ingredient added", Toast.LENGTH_SHORT).show();
        } else {
            db.updateItem(itemId, name, qty, unit, expiry);
            Toast.makeText(this, "Ingredient updated", Toast.LENGTH_SHORT).show();
        }
        finish(); // back to PantryActivity, whose onResume reloads the list
    }
}
