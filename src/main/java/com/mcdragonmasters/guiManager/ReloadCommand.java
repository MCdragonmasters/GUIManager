package com.mcdragonmasters.guiManager;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.executors.CommandArguments;
import org.bukkit.command.CommandSender;

public class ReloadCommand {
    public ReloadCommand() {
        new CommandAPICommand("guimanager")
                .withPermission("guimanager")
                .withSubcommand(
                        new CommandAPICommand("reload")
                                .executes(this::execute)
                ).register();
    }
    private void execute(CommandSender sender, CommandArguments args) {
        GUIManager.reload();
        sender.sendMessage("[GUIManager] Reloading...");
    }
}
