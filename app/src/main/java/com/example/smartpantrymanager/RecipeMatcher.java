package com.example.smartpantrymanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * CORE BUSINESS LOGIC - the strict-matching rule.
 *
 * A recipe is suggested ONLY IF every one of its ingredients is in the pantry
 * in at least the required quantity. One missing/insufficient ingredient = excluded.
 *
 * Robustness: names are normalised (case, whitespace, simple plurals) and
 * quantities are converted to a base unit (g, ml or count) before comparing.
 */
public class RecipeMatcher {

    private static final String MASS = "MASS", VOLUME = "VOLUME", COUNT = "COUNT";

    /** Returns only the recipes that can be made right now. */
    public static List<Recipe> getSuggested(List<Recipe> recipes, List<PantryItem> pantry) {
        Map<String, Double> totals = buildPantryTotals(pantry);
        List<Recipe> result = new ArrayList<>();
        for (Recipe r : recipes) {
            if (canMake(r, totals)) result.add(r);
        }
        return result;
    }

    private static boolean canMake(Recipe recipe, Map<String, Double> totals) {
        for (RecipeIngredient need : recipe.getIngredients()) {
            String key = key(need.getName(), need.getUnit());
            Double have = totals.get(key);
            double required = toBase(need.getUnit(), need.getQuantity());
            if (have == null || have + 0.0001 < required) {
                return false; // missing or not enough -> strict rule fails
            }
        }
        return true;
    }

    /** Sums pantry quantities per (normalised name + unit dimension). */
    private static Map<String, Double> buildPantryTotals(List<PantryItem> pantry) {
        Map<String, Double> totals = new HashMap<>();
        for (PantryItem p : pantry) {
            String k = key(p.getName(), p.getUnit());
            double base = toBase(p.getUnit(), p.getQuantity());
            Double current = totals.get(k);
            totals.put(k, (current == null ? 0 : current) + base);
        }
        return totals;
    }

    private static String key(String name, String unit) {
        return normalizeName(name) + "|" + dimension(unit);
    }

    /** "Tomatoes " -> "tomato", "Eggs" -> "egg". Applied to BOTH sides so it is consistent. */
    public static String normalizeName(String raw) {
        String s = raw.toLowerCase(Locale.ROOT).trim().replaceAll("\\s+", " ");
        if (s.endsWith("ies") && s.length() > 4) return s.substring(0, s.length() - 3) + "y";
        if (s.endsWith("oes")) return s.substring(0, s.length() - 2);
        if (s.endsWith("ss")) return s;
        if (s.endsWith("s") && s.length() > 3) return s.substring(0, s.length() - 1);
        return s;
    }

    private static String normalizeUnit(String u) {
        String s = u.toLowerCase(Locale.ROOT).trim();
        switch (s) {
            case "g": case "gram": case "grams": return "g";
            case "kg": case "kilogram": case "kilograms": return "kg";
            case "ml": case "millilitre": case "milliliter": return "ml";
            case "l": case "litre": case "liter": case "litres": case "liters": return "l";
            case "tsp": case "teaspoon": case "teaspoons": return "tsp";
            case "tbsp": case "tablespoon": case "tablespoons": return "tbsp";
            case "cup": case "cups": return "cup";
            default: return "pcs"; // pcs, piece, whole, can, etc.
        }
    }

    private static String dimension(String unit) {
        switch (normalizeUnit(unit)) {
            case "g": case "kg": return MASS;
            case "ml": case "l": case "tsp": case "tbsp": case "cup": return VOLUME;
            default: return COUNT;
        }
    }

    /** Converts to g / ml / count so different units of the same type can be compared. */
    private static double toBase(String unit, double qty) {
        switch (normalizeUnit(unit)) {
            case "kg": return qty * 1000;
            case "l": return qty * 1000;
            case "tsp": return qty * 5;
            case "tbsp": return qty * 15;
            case "cup": return qty * 240;
            default: return qty; // g, ml, pcs
        }
    }
}
