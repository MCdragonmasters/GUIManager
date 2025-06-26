package com.mcdragonmasters.guiManager;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static com.mcdragonmasters.guiManager.GUIManager.INSTANCE;

public class CustomInventory implements InventoryHolder {
    @Getter
    private static final HashMap<String, CustomInventory> customInventories = new HashMap<>();
    @Getter
    private static final List<String> commandsList = new ArrayList<>();
    private final Inventory inv;
    private final Component title;
    @Getter
    private static final NamespacedKey pdcKey = new NamespacedKey(INSTANCE, "clickOpenInv");
    @Getter
    private static final NamespacedKey pdcRankRequiredKey = new NamespacedKey(INSTANCE, "rankRequired");
    public CustomInventory(String key, Component title, int rows) {
        inv = Bukkit.createInventory(this, rows*9, title);
        this.title = title;
        customInventories.put(key, this);
    }

    public void setSlot(int slot, ItemStack stack) {
        this.inv.setItem(slot, stack);
    }

    public Inventory getInventory(HumanEntity humanEntity) {
        Inventory inventory = cloneInventory(this.inv, this.title);
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack==null) continue;

            var rankRequired = stack.getPersistentDataContainer().get(CustomInventory.getPdcRankRequiredKey(), PersistentDataType.STRING);
            List<String> groups = Arrays.stream(GUIManager.getVaultChat().getPlayerGroups((Player) humanEntity)).toList();
            if (rankRequired!=null && !groups.contains(rankRequired)) {
                inventory.setItem(i, new ItemStack(Material.AIR));
            }
        }
        return inventory;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inv;
    }
    private static Inventory cloneInventory(Inventory originalInventory, Component title) {
        Inventory clonedInventory = Bukkit.createInventory(originalInventory.getHolder(false), originalInventory.getSize(), title);
        clonedInventory.setContents(originalInventory.getContents());
        return clonedInventory;
    }
}
