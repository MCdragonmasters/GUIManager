package com.mcdragonmasters.tryhardplugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class Utils {
    public static Component miniMessage(String s) {
        return MiniMessage.miniMessage().deserialize(s);
    }
}
