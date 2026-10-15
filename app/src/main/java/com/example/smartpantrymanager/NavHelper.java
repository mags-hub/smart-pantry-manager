package com.example.smartpantrymanager;

import android.app.Activity;
import android.content.Intent;

import com.google.android.material.bottomnavigation.BottomNavigationView;

/** Wires the bottom navigation bar and uses Intents to switch screens. */
public class NavHelper {
    public static void setup(final Activity activity, final int selectedId) {
        BottomNavigationView nav = activity.findViewById(R.id.bottom_nav);
        nav.setSelectedItemId(selectedId);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == selectedId) return true;
            Class<?> target;
            if (id == R.id.nav_pantry) target = PantryActivity.class;
            else if (id == R.id.nav_suggested) target = SuggestedActivity.class;
            else target = SettingsActivity.class;
            activity.startActivity(new Intent(activity, target));
            activity.overridePendingTransition(0, 0);
            activity.finish();
            return true;
        });
    }
}
