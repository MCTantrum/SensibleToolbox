package io.github.thebusybiscuit.sensibletoolbox.items;

import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBItem;
import io.github.thebusybiscuit.sensibletoolbox.items.components.EnergizedIronIngot;
import io.github.thebusybiscuit.sensibletoolbox.items.components.IntegratedCircuit;
import io.github.thebusybiscuit.sensibletoolbox.items.energycells.EnergyCell;
import io.github.thebusybiscuit.sensibletoolbox.utils.STBUtil;
import io.github.thebusybiscuit.sensibletoolbox.utils.UnicodeSymbol;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpongeBlob extends EnergyCell {

    private final long cooldownTimeSeconds = 1;
    int range = 7;
    boolean playSound = false;

    @ParametersAreNonnullByDefault
    public SpongeBlob() {
        super();
    }

    public SpongeBlob(ConfigurationSection conf) {
        super(conf);
    }

    @Override
    public int getMaxCharge() {
        return 8000;
    }

    @Override
    public int getChargeRate() {
        return 100;
    }

    @Override
    public Color getCellColor() {
        return null;
    }

    @Override
    public Material getMaterial() {
        return Material.HEART_OF_THE_SEA;
    }

    @Override
    public String getItemName() {
        return "Sponge Blob";
    }

    @Override
    public String[] getLore() {
        return new String[]{"Absorbs water like a sponge, and", "occasionally eats plants.", "R-Click: " + ChatColor.WHITE + "Absorb Water", "L-Click: " + ChatColor.WHITE + "Place Water"};
    }

    @Override
    public boolean hasGlow() {
        return true;
    }

    @Override
    public Recipe getMainRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(getKey(), toItemStack());
        AdvancedMoistureChecker amc = new AdvancedMoistureChecker();
        registerCustomIngredients(amc);
        recipe.shape("SWS", "MHM", "WSW");
        recipe.setIngredient('M', amc.getMaterial());
        recipe.setIngredient('S', Material.SPONGE);
        recipe.setIngredient('W', Material.WET_SPONGE);
        recipe.setIngredient('H', Material.HEART_OF_THE_SEA);
        return recipe;
    }


    public static List<Location> getPossibleLocations(@Nonnull Player playerLocation, int range) {
        final List<Location> circleBlocks = new ArrayList<>();

        double bx = playerLocation.getLocation().getX();
        double by = playerLocation.getLocation().getY();
        double bz = playerLocation.getLocation().getZ();

        for (int x = (int) Math.round(bx) - range; x <= bx + range; x++) {
            for (int y = (int) Math.round(by) - range; y <= by + range; y++) {
                for (int z = (int) Math.round(bz) - range; z <= bz + range; z++) {
                    final double distance = ((bx - x) * (bx - x) + ((bz - z) * (bz - z)) + ((by - y) * (by - y)));
                    if (distance < range * range) {
                        final Location l = new Location(playerLocation.getWorld(), x, y, z);
                        circleBlocks.add(l);
                    }
                }
            }
        }
        return circleBlocks;
    }

    @Override
    public void onInteractItem(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }


        if (getCharge() <= 0) {
            event.setCancelled(true);
            STBUtil.complain(player, UnicodeSymbol.ELECTRICITY.toUnicode() + " Sponge Blob out of charge!");
            return;
        }

        if (!player.hasCooldown(Material.HEART_OF_THE_SEA)) {

            if (event.useItemInHand().equals(Event.Result.DENY)) {
                return;
            }

            if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
                event.setCancelled(true);
                if (getCharge() <= 999) {
                    STBUtil.complain(player, UnicodeSymbol.ELECTRICITY.toUnicode() + " Not enough charge!");
                } else {
                    handleLeftClick(event, player);
                }
            } else if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);
                handleRightClick(event, player);
            }

        }
    }

    private void handleLeftClick(PlayerInteractEvent event, Player player) {
        if (event.getClickedBlock() != null && getCharge() >= 1000 && Slimefun.getProtectionManager()
            .hasPermission(player, event.getClickedBlock(), Interaction.BREAK_BLOCK)) {

            Block clickedBlock = event.getClickedBlock();
            Block targetBlock = clickedBlock.getRelative(event.getBlockFace());

            if (targetBlock.getType() == Material.AIR) {
                targetBlock.setType(Material.WATER);
                setCharge(getCharge() - 1000);
                player.playSound(player.getLocation(), Sound.ITEM_BUCKET_EMPTY, SoundCategory.PLAYERS, 1F, 2F);
                player.getInventory().setItemInMainHand(this.toItemStack());
                player.setCooldown(Material.HEART_OF_THE_SEA, (int) (cooldownTimeSeconds * 15));
            }
        }
    }

    private void handleRightClick(PlayerInteractEvent event, Player player) {
        for (Location possibleLocation : getPossibleLocations(player, range)) {
            Block checkBlock = possibleLocation.getBlock();

            if (checkBlock != null && getCharge() >= 1) {
                if (!Slimefun.getProtectionManager().hasPermission(player, checkBlock, Interaction.BREAK_BLOCK)) {
                    event.setCancelled(true);
                    return;
                }

                if (checkBlock.getType() == Material.WATER || checkBlock.getType() == Material.SEAGRASS
                    || checkBlock.getType() == Material.TALL_SEAGRASS || checkBlock.getType() == Material.KELP_PLANT) {
                    checkBlock.setType(Material.AIR);
                    setCharge(getCharge() - 1);
                }

                if (checkBlock.getBlockData() instanceof Waterlogged wl) {
                    wl.setWaterlogged(false);
                    checkBlock.setBlockData(wl);
                    setCharge(getCharge() - 1);
                }
            }
        }
        player.playSound(player.getLocation(), Sound.ITEM_BUCKET_FILL, SoundCategory.PLAYERS, 1F, 2F);
        player.getInventory().setItemInMainHand(this.toItemStack());
        player.setCooldown(Material.HEART_OF_THE_SEA, (int) (cooldownTimeSeconds * 15));
    }
}






