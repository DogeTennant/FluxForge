package me.dogetennant.fluxforge.utils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import java.util.Iterator;

public class SmeltingUtil {

    public static ItemStack getSmeltingResult(ItemStack input) {
        if (input == null || input.getType() == Material.AIR) return null;

        Iterator<Recipe> recipes = Bukkit.recipeIterator();
        while (recipes.hasNext()) {
            Recipe recipe = recipes.next();
            if (recipe instanceof FurnaceRecipe furnaceRecipe) {
                if (furnaceRecipe.getInputChoice().test(input)) {
                    return furnaceRecipe.getResult().clone();
                }
            }
        }
        return null;
    }
}