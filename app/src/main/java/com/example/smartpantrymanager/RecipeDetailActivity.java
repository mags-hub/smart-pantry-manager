package com.example.smartpantrymanager;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/** Screen 4: Full ingredient list and method for one recipe. */
public class RecipeDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        long id = getIntent().getLongExtra("recipe_id", -1);
        Recipe recipe = new DatabaseHelper(this).getRecipe(id);
        if (recipe == null) { finish(); return; }

        setTitle(recipe.getName());
        ((TextView) findViewById(R.id.tvDetailName)).setText(recipe.getName());

        StringBuilder sb = new StringBuilder();
        for (RecipeIngredient ing : recipe.getIngredients()) sb.append("\u2022 ").append(ing).append("\n");
        ((TextView) findViewById(R.id.tvDetailIngredients)).setText(sb.toString().trim());
        ((TextView) findViewById(R.id.tvDetailSteps)).setText(recipe.getSteps());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
