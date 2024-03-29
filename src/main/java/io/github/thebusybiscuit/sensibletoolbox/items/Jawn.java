package io.github.thebusybiscuit.sensibletoolbox.items;

import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBItem;
import io.github.thebusybiscuit.sensibletoolbox.items.components.EnergizedIronIngot;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;

import me.mrCookieSlime.Slimefun.api.BlockStorage;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.scheduler.BukkitRunnable;

public class Jawn extends BaseSTBItem {

    public Jawn() {
        super();
    }

    public Jawn(ConfigurationSection conf) {
        super(conf);
    }

    @Override
    public Material getMaterial() {
        return Material.BONE;
    }

    @Override
    public String getItemName() {
        return "Jawn";
    }

    @Override
    public String[] getLore() {
        return new String[]{"Use on placed head items", "to instantly pick up. Works", "for any type of head.", "L-Click: " + ChatColor.WHITE + "Break and Collect"};
    }

    @Override
    public boolean hasGlow() {
        return true;
    }

    @Override
    public Recipe getMainRecipe() {
        EnergizedIronIngot ei = new EnergizedIronIngot();
        registerCustomIngredients(ei);
        ShapedRecipe recipe = new ShapedRecipe(getKey(), toItemStack());
        recipe.shape(" I ", " C ", "CRC");
        recipe.setIngredient('I', ei.getMaterial());
        recipe.setIngredient('C', Material.COPPER_INGOT);
        recipe.setIngredient('R', Material.REDSTONE);
        return recipe;
    }


    @Override
    public void onInteractItem(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            event.setCancelled(true);
        }

        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();
            Player player = event.getPlayer();

            if (block != null && (block.getType() == Material.PLAYER_HEAD || block.getType() == Material.PLAYER_WALL_HEAD)) {
                if (Slimefun.getProtectionManager().hasPermission(player, block, Interaction.BREAK_BLOCK)) {
                    event.setCancelled(true);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            BlockBreakEvent e = new BlockBreakEvent(block, player);
                            Bukkit.getPluginManager().callEvent(e);
                            BlockStorage.clearBlockInfo(block);
                            block.setType(Material.AIR);
                        }
                    }.runTaskLater(getProviderPlugin(), 2);

                }
            }
        }
    }
}


