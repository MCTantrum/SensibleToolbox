package io.github.thebusybiscuit.sensibletoolbox.items;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

public class GoldCombineHoe extends CombineHoe {

    public GoldCombineHoe() {
        super();
    }

    public GoldCombineHoe(ConfigurationSection conf) {
        super(conf);
    }

    @Override
    public Material getMaterial() {
        return Material.GOLDEN_HOE;
    }

    @Override
    public String getItemName() {
        return "Gold Combine Hoe";
    }

    @Override
    public Recipe getMainRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(getKey(), toItemStack());
        WoodCombineHoe hoe = new WoodCombineHoe();
        registerCustomIngredients(hoe);
        recipe.shape("SSS", "HCW", "SSS");
        recipe.setIngredient('S', Material.STRING);
        recipe.setIngredient('H', Material.GOLDEN_HOE);
        recipe.setIngredient('C', hoe.getMaterial());
        recipe.setIngredient('W', Material.GOLDEN_SWORD);
        return recipe;
    }
}
