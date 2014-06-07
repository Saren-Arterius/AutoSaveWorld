/**
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301,
 * USA.
 * 
 */

package autosaveworld.threads.worldregen;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import autosaveworld.config.AutoSaveWorldConfig;
import autosaveworld.config.AutoSaveWorldConfigMSG;
import autosaveworld.core.AutoSaveWorld;
import autosaveworld.core.GlobalConstants;
import autosaveworld.core.logging.MessageLogger;
import autosaveworld.threads.restart.RestartWaiter;
import autosaveworld.threads.worldregen.factions.FactionsCopy;
import autosaveworld.threads.worldregen.griefprevention.GPCopy;
import autosaveworld.threads.worldregen.towny.TownyCopy;
import autosaveworld.threads.worldregen.wg.WorldGuardCopy;
import autosaveworld.utils.SchedulerUtils;

public class WorldRegenCopyThread extends Thread {

    private AutoSaveWorld                plugin = null;
    private final AutoSaveWorldConfig    config;
    private final AutoSaveWorldConfigMSG configmsg;

    public WorldRegenCopyThread(AutoSaveWorld plugin, AutoSaveWorldConfig config, AutoSaveWorldConfigMSG configmsg) {
        this.plugin = plugin;
        this.config = config;
        this.configmsg = configmsg;
    }

    // Allows for the thread to naturally exit if value is false
    public void stopThread() {
        run = false;
    }

    public void startworldregen(String worldname) {
        worldtoregen = worldname;
        doregen = true;
    }

    private String           worldtoregen = "";
    private volatile boolean run          = true;
    private boolean          doregen      = false;

    @Override
    public void run() {
        MessageLogger.debug("WorldRegenThread Started");
        Thread.currentThread().setName("AutoSaveWorld WorldRegenCopyThread");

        while (run) {
            if (doregen) {
                try {
                    doWorldRegen();
                } catch (final Exception e) {
                    e.printStackTrace();
                }
                doregen = false;
                run = false;
            }
            try {
                Thread.sleep(1000);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
        }

        MessageLogger.debug("Graceful quit of WorldRegenThread");
    }

    private void doWorldRegen() throws Exception {

        final World wtoregen = Bukkit.getWorld(worldtoregen);

        // kick all player and deny them from join
        final AntiJoinListener ajl = new AntiJoinListener(configmsg);
        Bukkit.getPluginManager().registerEvents(ajl, plugin);
        SchedulerUtils.callSyncTaskAndWait(new Runnable() {
            @Override
            public void run() {
                for (final Player p: Bukkit.getOnlinePlayers()) {
                    MessageLogger.kickPlayer(p, configmsg.messageWorldRegenKick);
                }
            }
        });

        MessageLogger.debug("Saving buildings");

        // save WorldGuard buildings
        if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null && config.worldregensavewg) {
            new WorldGuardCopy(worldtoregen).copyAllToSchematics();
        }

        // save Factions homes
        if (Bukkit.getPluginManager().getPlugin("Factions") != null && config.worldregensavefactions) {
            new FactionsCopy(worldtoregen).copyAllToSchematics();
        }

        // save GriefPrevention claims
        if (Bukkit.getPluginManager().getPlugin("GriefPrevention") != null && config.worldregensavegp) {
            new GPCopy(worldtoregen).copyAllToSchematics();
        }

        // save Towny towns
        if (Bukkit.getPluginManager().getPlugin("Towny") != null && config.worldregensavetowny) {
            new TownyCopy(worldtoregen).copyAllToSchematics();
        }

        MessageLogger.debug("Saving finished");

        if (config.worldregenremoveseeddata) {
            MessageLogger.debug("Removing seed data");
            new File(wtoregen.getWorldFolder(), "level.dat").delete();
            new File(wtoregen.getWorldFolder(), "level.dat_old").delete();
            new File(wtoregen.getWorldFolder(), "uid.dat").delete();
            MessageLogger.debug("Removing finished");
        }

        // Save worldname file
        final FileConfiguration cfg = new YamlConfiguration();
        cfg.set("wname", worldtoregen);
        cfg.save(new File(GlobalConstants.getWorldnameFile()));

        MessageLogger.debug("Deleting map and restarting server");
        // Add hook that will delete world folder, signal that restart should
        // wait, and schedule restart restart
        final WorldRegenJVMshutdownhook wrsh = new WorldRegenJVMshutdownhook(wtoregen.getWorldFolder()
                .getCanonicalPath());
        Runtime.getRuntime().addShutdownHook(wrsh);
        RestartWaiter.incrementWait();
        plugin.autorestartThread.startrestart(true);

    }

    private SchematicOperations schemops = null;

    public SchematicOperations getSchematicOperations() {
        if (schemops == null) {
            schemops = new SchematicOperations();
        }
        return schemops;
    }

}
