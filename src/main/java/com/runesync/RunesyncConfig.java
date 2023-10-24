package com.runesync;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("RuneSync")
public interface RunesyncConfig extends Config {
    @ConfigItem(keyName = "trackLoot", name = "Track Loot", description = "Whether or not to send unique loot drops to the server for tracking.")
    default boolean trackLoot() {
        return true;
    }
}
