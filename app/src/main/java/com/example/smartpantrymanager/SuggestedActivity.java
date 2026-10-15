package com.example.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/** Screen 3: Shows only recipes that pass the strict-matching rule. */
public class SuggestedActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested);
        setTitle("Suggested Recipes");
        recyclerView = findViewById(R.id.recyclerRecipes);
        tvEmpty = findViewById(R.id.tvEmptyRecipes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        NavHelper.setup(this, R.id.nav_suggested);
    }

    @Override
    protected void onResume() {
        super.onResume();
        DatabaseHelper db = new DatabaseHelper(this);
        List<Recipe> suggested = RecipeMatcher.getSuggested(db.getAllRecipes(), db.getAllItems());

        // Feedback when nothing matches (required by the brief)
        tvEmpty.setVisibility(suggested.isEmpty() ? View.VISIBLE : View.GONE);

        recyclerView.setAdapter(new RecipeAdapter(suggested, recipe -> {
            Intent i = new Intent(this, RecipeDetailActivity.class);
            i.putExtra("recipe_id", recipe.getId());
            startActivity(i);
        }));
    }
}
