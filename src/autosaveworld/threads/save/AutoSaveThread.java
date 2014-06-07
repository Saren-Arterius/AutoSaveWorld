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

package autosaveworld.threads.save;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.World;

import autosaveworld.config.AutoSaveWorldConfig;
import autosaveworld.config.AutoSaveWorldConfigMSG;
import autosaveworld.core.AutoSaveWorld;
import autosaveworld.core.logging.MessageLogger;
import autosaveworld.utils.SchedulerUtils;

public class AutoSaveThread extends Thread {

    private AutoSaveWorld                plugin = null;
    private final AutoSaveWorldConfig    config;
    private final AutoSaveWorldConfigMSG configmsg;

    public AutoSaveThread(AutoSaveWorld plugin, AutoSaveWorldConfig config, AutoSaveWorldConfigMSG configmsg) {
        this.plugin = plugin;
        this.config = config;
        this.configmsg = configmsg;
    }

    public void stopThread() {
        run = false;
    }

    public void startsave() {
        command = true;
    }

    private volatile boolean run     = true;
    private boolean          command = false;

    @Override
    public void run() {

        MessageLogger.debug("AutoSaveThread Started");
        Thread.currentThread().setName("AutoSaveWorld AutoSaveThread");

        while (run) {
            // sleep
            for (int i = 0; i < config.saveInterval; i++) {
                if (!run || command) {
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (final InterruptedException e) {}
            }

            // save
            if (run && (config.saveEnabled || command)) {
                command = false;
                plugin.lock.lock();
                try {
                    performSave();
                } catch (final Exception e) {
                    e.printStackTrace();
                }
                plugin.lock.unlock();
            }
        }

        MessageLogger.debug("Graceful quit of AutoSaveThread");

    }

    public void performSaveNow() {
        MessageLogger.broadcast(configmsg.messageSaveBroadcastPre, config.saveBroadcast);

        MessageLogger.debug("Saving players");
        plugin.getServer().savePlayers();
        MessageLogger.debug("Saved Players");
        MessageLogger.debug("Saving worlds");
        for (final World w: plugin.getServer().getWorlds()) {
            saveWorld(w);
        }
        MessageLogger.debug("Saved Worlds");

        MessageLogger.broadcast(configmsg.messageSaveBroadcastPost, config.saveBroadcast);
    }

    public void performSave() {
        MessageLogger.broadcast(configmsg.messageSaveBroadcastPre, config.saveBroadcast);
        // Save the players
        MessageLogger.debug("Saving players");
        if (run) {
            SchedulerUtils.callSyncTaskAndWait(new Runnable() {
                @Override
                public void run() {
                    plugin.getServer().savePlayers();
                }
            });
        }
        MessageLogger.debug("Saved Players");
        // Save the worlds
        MessageLogger.debug("Saving worlds");
        for (final World world: plugin.getServer().getWorlds()) {
            if (run) {
                SchedulerUtils.callSyncTaskAndWait(new Runnable() {
                    @Override
                    public void run() {
                        saveWorld(world);
                    }
                });
            }
        }
        MessageLogger.debug("Saved Worlds");
        MessageLogger.broadcast(configmsg.messageSaveBroadcastPost, config.saveBroadcast);
    }

    private void saveWorld(World world) {
        if (!world.isAutoSave()) {
            return;
        }
        // structures are saved only for main world so we use this workaround
        // only for main world
        if (config.donotsavestructures && Bukkit.getWorlds().get(0).getName().equalsIgnoreCase(world.getName())) {
            saveWorldDoNoSaveStructureInfo(world);
        } else {
            saveWorldNormal(world);
        }
    }

    private void saveWorldNormal(World world) {
        world.save();
    }

    private void saveWorldDoNoSaveStructureInfo(World world) {
        try {
            // get worldserver and dataManager
            final Field worldField = world.getClass().getDeclaredField("world");
            worldField.setAccessible(true);
            final Object worldserver = worldField.get(world);
            final Field dataManagerField = worldserver.getClass().getSuperclass().getDeclaredField("dataManager");
            dataManagerField.setAccessible(true);
            final Object dataManager = dataManagerField.get(worldserver);
            // invoke check session
            final Method checkSessionMethod = dataManager.getClass().getSuperclass().getDeclaredMethod("checkSession");
            checkSessionMethod.setAccessible(true);
            checkSessionMethod.invoke(dataManager);
            // invoke saveWorldData
            final Field worldDataField = worldserver.getClass().getSuperclass().getDeclaredField("worldData");
            worldDataField.setAccessible(true);
            final Object worldData = worldDataField.get(worldserver);
            boolean methodfound = false;
            for (final Method method: dataManager.getClass().getSuperclass().getDeclaredMethods()) {
                if (method.getName().equals("saveWorldData") && method.getParameterTypes().length == 2) {
                    method.setAccessible(true);
                    method.invoke(dataManager, worldData, null);
                    methodfound = true;
                    break;
                }
            }
            if (!methodfound) {
                throw new RuntimeException("Cannot find method saveWorldData");
            }
            // invoke saveChunks
            methodfound = false;
            final Field chunkProviderField = worldserver.getClass().getSuperclass().getDeclaredField("chunkProvider");
            chunkProviderField.setAccessible(true);
            final Object chunkProvider = chunkProviderField.get(worldserver);
            for (final Method method: chunkProvider.getClass().getDeclaredMethods()) {
                if (method.getName().equals("saveChunks") && method.getParameterTypes().length == 2) {
                    method.setAccessible(true);
                    method.invoke(chunkProvider, true, null);
                    methodfound = true;
                    break;
                }
            }
            if (!methodfound) {
                throw new RuntimeException("Cannot find method saveChunks");
            }
        } catch (final Exception e) {
            e.printStackTrace();
            // failed to save using reflections, save world normal
            MessageLogger.debug("failed to workaround stucture saving, saving world using normal methods");
            saveWorldNormal(world);
        }
    }

}
