package com.mcdragonmasters.tryhardplugin;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.executors.CommandArguments;
import org.bukkit.command.CommandSender;

public class ReloadCommand {
    public ReloadCommand() {
        new CommandAPICommand("tryhardplugin")
                .withPermission("tryhardplugin")
                .withSubcommand(
                        new CommandAPICommand("reload")
                                .executes(this::execute)
                ).register();
    }
    private void execute(CommandSender sender, CommandArguments args) {
        TryhardPlugin.reload();
        sender.sendMessage("[TryhardPlugin] Reloading...");
    }
}
