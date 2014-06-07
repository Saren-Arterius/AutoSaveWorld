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

package autosaveworld.threads.backup.localfs;

import java.text.SimpleDateFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.World;

import autosaveworld.config.AutoSaveWorldConfig;
import autosaveworld.core.AutoSaveWorld;

public class LocalFSBackup {

    private final AutoSaveWorld       plugin;
    private final AutoSaveWorldConfig config;

    public LocalFSBackup(AutoSaveWorld plugin, AutoSaveWorldConfig config) {
        this.plugin = plugin;
        this.config = config;
    }

    public void performBackup() {
        for (final String extpath: config.lfsextfolders) {

            // init backup operations class
            final LFSBackupOperations bo = new LFSBackupOperations(plugin, config.lfsbackupzip, extpath,
                    config.lfsbackupexcludefolders);

            // create executor
            final ExecutorService backupService = Executors.newFixedThreadPool(Math.max(Runtime.getRuntime()
                    .availableProcessors() - 1, 1));

            // create timestamp
            final String backuptimestamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(System
                    .currentTimeMillis());

            // start world backup
            for (final World world: Bukkit.getWorlds()) {
                // check if we need to backup this world
                if ((config.lfsbackupWorldsList).contains("*") || config.lfsbackupWorldsList.contains(world.getName())) {
                    // backup world
                    bo.startWorldBackup(backupService, world, config.lfsMaxNumberOfWorldsBackups, backuptimestamp);
                }
            }

            // start plugins backup
            if (config.lfsbackuppluginsfolder) {
                bo.startPluginsBackup(backupService, config.lfsMaxNumberOfPluginsBackups, backuptimestamp);
            }

            // wait for executor to finish (let's hope that the backup will
            // finish in max 2 days)
            backupService.shutdown();
            try {
                backupService.awaitTermination(48, TimeUnit.HOURS);
            } catch (final InterruptedException e) {}

        }
    }

}
