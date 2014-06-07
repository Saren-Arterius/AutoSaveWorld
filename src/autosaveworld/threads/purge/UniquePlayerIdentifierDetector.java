package autosaveworld.threads.purge;

import java.util.UUID;

import org.bukkit.Bukkit;

public class UniquePlayerIdentifierDetector {

    public static UniquePlayerIdentifierType getUniquePlayerIdentifierType() {
        try {
            final Class<?> craftserver = Bukkit.getServer().getClass();
            final Class<?> craftofflineplayer = Bukkit.getOfflinePlayer(UUID.randomUUID()).getClass();
            craftofflineplayer.getDeclaredConstructor(craftserver,
                    net.minecraft.util.com.mojang.authlib.GameProfile.class);
            return UniquePlayerIdentifierType.UUID;
        } catch (Exception | NoSuchMethodError e) {}
        return UniquePlayerIdentifierType.NAME;
    }

    public static enum UniquePlayerIdentifierType {
        NAME,
        UUID;
    }

}
