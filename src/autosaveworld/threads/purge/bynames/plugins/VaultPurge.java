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

package autosaveworld.threads.purge.bynames.plugins;

import java.io.File;
import java.util.ArrayList;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
import org.bukkit.World;

import autosaveworld.core.logging.MessageLogger;
import autosaveworld.threads.purge.bynames.ActivePlayersList;
import autosaveworld.utils.SchedulerUtils;

public class VaultPurge {

    private final ArrayList<String> playerstopurgeperms = new ArrayList<String>(70);

    public void doPermissionsPurgeTask(ActivePlayersList pacheck) {

        MessageLogger.debug("Player permissions purge started");

        final Permission permission = Bukkit.getServicesManager().getRegistration(Permission.class).getProvider();

        int deleted = 0;

        final String worldfoldername = Bukkit.getWorlds().get(0).getWorldFolder().getAbsolutePath();
        final File playersdatfolder = new File(worldfoldername + File.separator + "players" + File.separator);
        for (final String playerfile: playersdatfolder.list()) {
            if (playerfile.endsWith(".dat")) {
                final String playername = playerfile.substring(0, playerfile.length() - 4);
                if (!pacheck.isActiveCS(playername)) {
                    // add player to delete batch
                    playerstopurgeperms.add(playername);
                    // delete permissions if maximum batch size reached
                    if (playerstopurgeperms.size() == 40) {
                        flushPermsBatch(permission);
                    }
                    deleted += 1;
                }
            }
        }
        // flush the rest of the batch
        flushPermsBatch(permission);

        MessageLogger.debug("Player permissions purge finished, deleted " + deleted + " players permissions");
    }

    private void flushPermsBatch(final Permission permission) {
        // delete permissions
        final Runnable deleteperms = new Runnable() {
            @Override
            public void run() {
                for (final String playername: playerstopurgeperms) {
                    MessageLogger.debug(playername + " is inactive. Removing permissions");
                    // remove all player groups
                    for (final String group: permission.getGroups()) {
                        permission.playerRemoveGroup((String) null, playername, group);
                        for (final World world: Bukkit.getWorlds()) {
                            permission.playerRemoveGroup(world, playername, group);
                        }
                    }
                }
                playerstopurgeperms.clear();
            }
        };
        SchedulerUtils.callSyncTaskAndWait(deleteperms);
    }

    private final ArrayList<String> playerstopurgeecon = new ArrayList<String>(70);

    public void doEconomyPurgeTask(ActivePlayersList pacheck) {

        MessageLogger.debug("Player economy purge started");

        final Economy economy = Bukkit.getServicesManager().getRegistration(Economy.class).getProvider();

        int deleted = 0;

        final String worldfoldername = Bukkit.getWorlds().get(0).getWorldFolder().getAbsolutePath();
        final File playersdatfolder = new File(worldfoldername + File.separator + "players" + File.separator);
        for (final String playerfile: playersdatfolder.list()) {
            if (playerfile.endsWith(".dat")) {
                final String playername = playerfile.substring(0, playerfile.length() - 4);
                if (economy.hasAccount(playername) && !pacheck.isActiveCS(playername)) {
                    // add player to delete batch
                    playerstopurgeecon.add(playername);
                    // delete economy if maximum batch size reached
                    if (playerstopurgeecon.size() == 40) {
                        flushEconomyBatch(economy);
                    }
                    deleted += 1;
                }
            }
        }
        // flush the rest of the batch
        flushEconomyBatch(economy);

        MessageLogger.debug("Player economy purge finished, deleted " + deleted + " players economy accounts");
    }

    private void flushEconomyBatch(final Economy economy) {
        // delete economy accounts
        final Runnable deleteeconomy = new Runnable() {
            @Override
            public void run() {
                for (final String playername: playerstopurgeecon) {
                    MessageLogger.debug(playername + " is inactive. Removing economy account");
                    // set player balance to 0
                    economy.withdrawPlayer(playername, economy.getBalance(playername));
                }
                playerstopurgeecon.clear();
            }
        };
        SchedulerUtils.callSyncTaskAndWait(deleteeconomy);
    }

}
