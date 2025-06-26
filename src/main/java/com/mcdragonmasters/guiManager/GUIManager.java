package com.mcdragonmasters.guiManager;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkit;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import dev.jorel.commandapi.CommandAPICommand;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

@SuppressWarnings("UnstableApiUsage")
public class GUIManager extends JavaPlugin {

    public static FileConfiguration config;
    public static Logger LOGGER;
    public static GUIManager INSTANCE;
    @Getter
    private static Chat vaultChat;

    @Override
    public void onLoad() {
        var cmdAPIConfig = new CommandAPIBukkitConfig(this);
        if (!getConfig().getBoolean("reloadDatapacks")) {
            cmdAPIConfig.skipReloadDatapacks(true);
        }
        CommandAPI.onLoad(cmdAPIConfig);
    }

    @Override
    public void onEnable() {
        INSTANCE = this;
        LOGGER = getLogger();
        Bukkit.getScheduler().runTask(this, GUIManager::reload);
        if (!setupVaultChat() ) {
            getLogger().severe("Disabled due to no Vault dependency found!");
            getServer().getPluginManager().disablePlugin(this);
        }
        new ReloadCommand();
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onInvClick(InventoryClickEvent e) {
                if (!(e.getInventory().getHolder(false) instanceof CustomInventory)) return;
                e.setCancelled(true);
                var item = e.getCurrentItem();
                if (item==null) return;
                var pdc = item.getPersistentDataContainer();
                String clickOpenInv = pdc.get(CustomInventory.getPdcKey(), PersistentDataType.STRING);
                if (clickOpenInv!=null) {
                    CustomInventory customInv = CustomInventory.getCustomInventories().get(clickOpenInv);
                    if (customInv==null) {
                        LOGGER.severe("Unable to get inventory '"+clickOpenInv+"'");
                        return;
                    }
                    e.getWhoClicked().openInventory(customInv.getInventory(e.getWhoClicked()));
                }
            }
        }, INSTANCE);
    }

    public static void reload() {
        CustomInventory.getCommandsList().clear();
        CustomInventory.getCustomInventories().clear();
        INSTANCE.saveDefaultConfig();
        INSTANCE.reloadConfig();
        config = INSTANCE.getConfig();
        var section = config.getConfigurationSection("inventories");
        if (section == null) return;
        for (String key : section.getKeys(false)) {
            String confPrefix = "inventories."+key;
            String command = getString(confPrefix+".command");
            if (command!=null) {
                CustomInventory.getCommandsList().add(command);
                for (String commandName : CustomInventory.getCommandsList()) {
                    CommandAPIBukkit.unregister(commandName, false, false);
                    CommandAPIBukkit.unregister(commandName, false, true);
                }
                String commandPerm = getString(confPrefix+".commandPermission");
                CommandAPI.unregister(command);
                var cmd = new CommandAPICommand(command)
                        .executesPlayer((p, args) -> {
                            p.openInventory(CustomInventory.getCustomInventories().get(key).getInventory());
                        });
                if (commandPerm!=null) cmd.withPermission(commandPerm);
                cmd.register();
            }
            Component title = Utils.miniMessage(getString(confPrefix+".title"));
            var customInv = new CustomInventory(key, title, getInt(confPrefix+".rows"));
            var itemsSection = config.getConfigurationSection(confPrefix+".items");
            if (itemsSection==null) return;
            for (String itemKey : itemsSection.getKeys(false)) {
                String itemPath = confPrefix+".items."+itemKey;
                String itemType = getString(itemPath+".type");
                int slot = getInt(itemPath+".slot");
                var material = Material.getMaterial(itemType);
                var stack = new ItemStack(material!=null?material:Material.DIRT);
                var loreBuilder = ItemLore.lore();
                for (String loreLine : config.getStringList(itemPath+".lore")) {
                    loreBuilder.addLine(Utils.miniMessage(loreLine));
                }
                stack.setData(DataComponentTypes.LORE, loreBuilder.build());
                stack.setData(DataComponentTypes.CUSTOM_NAME, Utils.miniMessage(getString(itemPath+".name")));
                String clickOpenInv = getString(itemPath+".clickOpenInv");
                String rankRequired = getString(itemPath+".rankRequired");
                stack.editPersistentDataContainer(pdc -> {
                    if (clickOpenInv!=null) pdc.set(CustomInventory.getPdcKey(), PersistentDataType.STRING, clickOpenInv);
                    if (rankRequired!=null)
                        pdc.set(CustomInventory.getPdcRankRequiredKey(), PersistentDataType.STRING, rankRequired);
                });
                customInv.setSlot(slot, stack);
            }
        }
    }

    private boolean setupVaultChat() {
        RegisteredServiceProvider<Chat> rsp = getServer().getServicesManager().getRegistration(Chat.class);
        if (rsp != null) {
            vaultChat = rsp.getProvider();
        }
        return vaultChat != null;
    }



    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static String getString(String s) {
        return config.getString(s);
    }
    public static int getInt(String s) {
        return config.getInt(s);
    }
}
