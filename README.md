# Smart Pantry Manager

Smart Pantry Manager is a Java Android app that helps reduce food waste. You record the ingredients you already have at home, and the app suggests only the recipes you can cook right now, with no shopping trip needed.

## The strict-matching rule

A recipe is suggested only if every ingredient it needs is in your pantry, in at least the required quantity. A recipe missing even one ingredient is left out. The matching copes with simple real-world differences: names are compared ignoring case and plural endings ("tomato" matches "tomatoes"), and quantities are converted to a common unit before comparing (kg and g, l and ml, tbsp, tsp and cups).

## Features

- Add, edit and delete pantry items (name, quantity, unit, optional expiry date) with input validation
- Pantry list screen using a RecyclerView with a custom adapter
- 18 pre-loaded recipes, seeded on first run
- Suggested Recipes screen showing only recipes that pass the strict-matching rule, with a message when none match
- Recipe detail screen with the full ingredient list and method
- Settings screen with a toggle that highlights items expiring within 3 days
- Bottom navigation bar between Pantry, Recipes and Settings

## Database: SQLite

The app uses SQLite through `SQLiteOpenHelper`. Data is stored on the device and is still there after the app is closed and reopened.

Why I chose it:  I selected SQLiteOpenHelper because it allowed my app to work entirely offline without the need to set up or maintain a complex external backend. I found it incredibly simple to handle initial database creation and updates using its built-in lifecycle methods. However, writing raw SQL queries manually and managing database versioning by hand became quite difficult as my schema grew.
## Setup and run

1. Install Android Studio and clone this repository, or download it as a ZIP.
2. Open the project folder in Android Studio and wait for the Gradle sync to finish.
3. Connect an Android phone with USB debugging turned on, or create an emulator (minimum SDK is API 26).
4. Press the green Run button.

No API keys or special permissions are needed.

## Project structure

- `DatabaseHelper.java`: creates the SQLite tables, seeds the recipes and provides the pantry CRUD methods
- `RecipeMatcher.java`: the strict-matching logic
- `PantryActivity`, `AddEditItemActivity`, `SuggestedActivity`, `RecipeDetailActivity`, `SettingsActivity`: the five screens
- `PantryAdapter`, `RecipeAdapter`: RecyclerView adapters
- `NavHelper.java`: wires up the bottom navigation using Intents

## Known limitations

- Mass units (g, kg) and volume units (ml, l, tsp, tbsp, cup) can't be converted into each other, so "1 g" of salt does not satisfy a recipe asking for "1 tsp".
- Recipes are seeded only when the database is first created.

## Out of scope

The app does not use Google Maps, any mapping SDK or location services.

## Credits

Icon Credit: Image sourced from Vecteezy.com under the Vecteezy Free License.