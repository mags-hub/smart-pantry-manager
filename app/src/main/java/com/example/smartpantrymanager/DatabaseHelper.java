package com.example.smartpantrymanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLite database (SQLiteOpenHelper). Holds three tables:
 *  - pantry            : the user's ingredients (full CRUD)
 *  - recipes           : seeded on first run
 *  - recipe_ingredients: required ingredients per recipe (seeded)
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "pantry.db";
    private static final int DB_VERSION = 2;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE pantry (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, quantity REAL NOT NULL, unit TEXT NOT NULL, expiry TEXT)");
        db.execSQL("CREATE TABLE recipes (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, steps TEXT NOT NULL)");
        db.execSQL("CREATE TABLE recipe_ingredients (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "recipe_id INTEGER NOT NULL, name TEXT NOT NULL, quantity REAL NOT NULL, unit TEXT NOT NULL, " +
                "FOREIGN KEY(recipe_id) REFERENCES recipes(id))");
        seedRecipes(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS recipe_ingredients");
        db.execSQL("DROP TABLE IF EXISTS recipes");
        db.execSQL("DROP TABLE IF EXISTS pantry");
        onCreate(db);
    }

    // ---------------------------------------------------------------- PANTRY CRUD

    /** CREATE */
    public long insertItem(String name, double qty, String unit, String expiry) {
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("quantity", qty);
        cv.put("unit", unit);
        cv.put("expiry", expiry);
        return getWritableDatabase().insert("pantry", null, cv);
    }

    /** READ (all) */
    public List<PantryItem> getAllItems() {
        List<PantryItem> list = new ArrayList<>();
        Cursor c = getReadableDatabase().query("pantry", null, null, null, null, null, "name COLLATE NOCASE");
        while (c.moveToNext()) list.add(fromCursor(c));
        c.close();
        return list;
    }

    /** READ (one) */
    public PantryItem getItem(long id) {
        Cursor c = getReadableDatabase().query("pantry", null, "id=?", new String[]{String.valueOf(id)},
                null, null, null);
        PantryItem item = c.moveToFirst() ? fromCursor(c) : null;
        c.close();
        return item;
    }

    /** UPDATE */
    public int updateItem(long id, String name, double qty, String unit, String expiry) {
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("quantity", qty);
        cv.put("unit", unit);
        cv.put("expiry", expiry);
        return getWritableDatabase().update("pantry", cv, "id=?", new String[]{String.valueOf(id)});
    }

    /** DELETE */
    public int deleteItem(long id) {
        return getWritableDatabase().delete("pantry", "id=?", new String[]{String.valueOf(id)});
    }

    private PantryItem fromCursor(Cursor c) {
        return new PantryItem(c.getLong(c.getColumnIndexOrThrow("id")),
                c.getString(c.getColumnIndexOrThrow("name")),
                c.getDouble(c.getColumnIndexOrThrow("quantity")),
                c.getString(c.getColumnIndexOrThrow("unit")),
                c.getString(c.getColumnIndexOrThrow("expiry")));
    }

    // -------------------------------------------------------------------- RECIPES

    public List<Recipe> getAllRecipes() {
        List<Recipe> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query("recipes", null, null, null, null, null, "name");
        while (c.moveToNext()) {
            Recipe r = new Recipe(c.getLong(0), c.getString(1), c.getString(2));
            loadIngredients(db, r);
            list.add(r);
        }
        c.close();
        return list;
    }

    public Recipe getRecipe(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query("recipes", null, "id=?", new String[]{String.valueOf(id)}, null, null, null);
        Recipe r = null;
        if (c.moveToFirst()) {
            r = new Recipe(c.getLong(0), c.getString(1), c.getString(2));
            loadIngredients(db, r);
        }
        c.close();
        return r;
    }

    private void loadIngredients(SQLiteDatabase db, Recipe r) {
        Cursor c = db.query("recipe_ingredients", null, "recipe_id=?",
                new String[]{String.valueOf(r.getId())}, null, null, null);
        while (c.moveToNext()) {
            r.getIngredients().add(new RecipeIngredient(
                    c.getString(c.getColumnIndexOrThrow("name")),
                    c.getDouble(c.getColumnIndexOrThrow("quantity")),
                    c.getString(c.getColumnIndexOrThrow("unit"))));
        }
        c.close();
    }

    // ----------------------------------------------------------------------- SEED

    /** Ingredient format: "name|quantity|unit". */
    private void addRecipe(SQLiteDatabase db, String name, String steps, String... ingredients) {
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("steps", steps);
        long recipeId = db.insert("recipes", null, cv);
        for (String s : ingredients) {
            String[] p = s.split("\\|");
            ContentValues iv = new ContentValues();
            iv.put("recipe_id", recipeId);
            iv.put("name", p[0]);
            iv.put("quantity", Double.parseDouble(p[1]));
            iv.put("unit", p[2]);
            db.insert("recipe_ingredients", null, iv);
        }
    }

    private void seedRecipes(SQLiteDatabase db) {
        addRecipe(db, "Scrambled Eggs",
                "1. Crack the eggs into a bowl and whisk with the salt.\n2. Melt the butter in a pan on low heat.\n3. Pour in the eggs and stir gently until just set.\n4. Serve immediately.",
                "eggs|3|pcs", "butter|10|g", "salt|1|tsp");
        addRecipe(db, "Tomato Pasta",
                "1. Boil the pasta in salted water until tender.\n2. Chop the tomatoes and garlic.\n3. Fry the garlic in olive oil, add tomatoes and simmer 10 minutes.\n4. Toss the drained pasta in the sauce.",
                "pasta|200|g", "tomatoes|3|pcs", "garlic|2|pcs", "olive oil|2|tbsp", "salt|1|tsp");
        addRecipe(db, "Cheese Omelette",
                "1. Whisk the eggs.\n2. Melt butter in a pan and pour in the eggs.\n3. When almost set, add the cheese and fold in half.\n4. Cook one more minute and serve.",
                "eggs|3|pcs", "cheese|50|g", "butter|10|g");
        addRecipe(db, "Pancakes",
                "1. Mix flour, sugar, milk and eggs into a smooth batter.\n2. Heat a greased pan.\n3. Pour in a small ladle of batter and cook until bubbles form.\n4. Flip and cook the other side.",
                "flour|200|g", "milk|300|ml", "eggs|2|pcs", "sugar|2|tbsp");
        addRecipe(db, "Egg Fried Rice",
                "1. Cook the rice and let it cool.\n2. Fry the chopped onion in oil.\n3. Push aside, scramble the eggs in the pan.\n4. Add rice and soy sauce and stir-fry for 3 minutes.",
                "rice|200|g", "eggs|2|pcs", "onion|1|pcs", "soy sauce|2|tbsp", "oil|2|tbsp");
        addRecipe(db, "Garlic Bread",
                "1. Mash the butter with crushed garlic.\n2. Spread on the bread slices.\n3. Bake at 200C for 8 minutes until golden.",
                "bread|4|pcs", "butter|50|g", "garlic|2|pcs");
        addRecipe(db, "Mashed Potatoes",
                "1. Peel, chop and boil the potatoes for 15 minutes.\n2. Drain and mash.\n3. Stir in butter, warm milk and salt until smooth.",
                "potatoes|500|g", "butter|30|g", "milk|100|ml", "salt|1|tsp");
        addRecipe(db, "Chicken Stir-Fry",
                "1. Slice the chicken, onion and carrots.\n2. Stir-fry the chicken in oil until cooked.\n3. Add vegetables and cook 5 minutes.\n4. Add soy sauce and toss.",
                "chicken|300|g", "onion|1|pcs", "carrots|2|pcs", "soy sauce|2|tbsp", "oil|1|tbsp");
        addRecipe(db, "Pap and Spinach",
                "1. Add boiling water to a pot.\n2. Add 150 g (about 1 cup) of mealie meal and leave it to boil.\n3. Heat the cooking oil in a pan on the stove.\n4. Add the spinach and salt and stir until it is well cooked.\n5. Serve the pap with the spinach.",
                "mealie meal|150|g", "cooking oil|2|tbsp", "salt|1|tsp", "spinach|200|g");

        addRecipe(db, "Cheese Toastie",
                "1. Butter the outside of the bread slices.\n2. Place cheese between them.\n3. Toast in a pan for 3 minutes per side.",
                "bread|2|pcs", "cheese|60|g", "butter|10|g");
        addRecipe(db, "Coleslaw Salad",
                "1. Shred the cabbage and grate the carrots.\n2. Add the cabbage, carrots and mayonnaise to a bowl.\n3. Mix everything together until well combined.\n4. Serve.",
                "cabbage|200|g", "mayonnaise|3|tbsp", "carrots|2|pcs");
        addRecipe(db, "Banana Smoothie",
                "1. Peel the bananas.\n2. Blend with milk and honey until smooth.\n3. Serve cold.",
                "bananas|2|pcs", "milk|250|ml", "honey|1|tbsp");
        addRecipe(db, "Egg Noodles",
                "1. Boil the noodles and drain.\n2. Scramble the eggs in hot oil.\n3. Add noodles and soy sauce and toss for 2 minutes.",
                "noodles|200|g", "eggs|2|pcs", "soy sauce|2|tbsp", "oil|1|tbsp");
        addRecipe(db, "Creamy Porridge",
                "1. Combine oats and milk in a pot.\n2. Cook on medium heat for 5 minutes, stirring.\n3. Sweeten with sugar and serve.",
                "oats|80|g", "milk|300|ml", "sugar|1|tbsp");
        addRecipe(db, "Scones",
                "1. Add the flour, baking powder, sugar, salt and cooking oil to a bowl.\n2. Stir all the ingredients together.\n3. Add some lukewarm water a little at a time, keeping the dough strong and firm.\n4. Shape the dough into rounds and bake until golden brown.",
                "flour|250|g", "baking powder|2|tsp", "sugar|1|tbsp", "salt|1|tsp", "cooking oil|2|tbsp");

        addRecipe(db, "French Toast",
                "1. Whisk eggs, milk and sugar.\n2. Dip the bread slices in the mixture.\n3. Fry in a pan until golden on both sides.",
                "bread|4|pcs", "eggs|2|pcs", "milk|100|ml", "sugar|1|tbsp");
        addRecipe(db, "Loaded Baked Potatoes",
                "1. Prick the potatoes and bake at 200C for 60 minutes.\n2. Split open and add butter, salt and grated cheese.\n3. Return to the oven for 5 minutes.",
                "potatoes|4|pcs", "butter|30|g", "salt|1|tsp", "cheese|50|g");
        addRecipe(db, "Malva Pudding",
                "1. Mix the flour, sugar, egg, butter and apricot jam into a smooth sponge batter.\n2. Pour the batter into a baking dish and bake in the oven until golden and cooked through.\n3. While it bakes, heat the cream, butter and sugar on the stove to make the sauce.\n4. Pour the hot sauce over the pudding as soon as it comes out of the oven, so the sponge soaks it up.\n5. Serve warm.",
                "flour|250|g", "sugar|200|g", "eggs|1|pcs", "butter|100|g", "apricot jam|1|tbsp", "cream|250|ml");
    }
}
