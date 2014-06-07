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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.World;

import autosaveworld.core.logging.MessageLogger;
import autosaveworld.threads.purge.WorldEditRegeneration;
import autosaveworld.threads.purge.bynames.ActivePlayersList;
import autosaveworld.utils.SchedulerUtils;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class WGPurge {

    public void doWGPurgeTask(ActivePlayersList pacheck, final boolean regenrg, boolean noregenoverlap) {

        MessageLogger.debug("WG purge started");

        final WorldGuardPlugin wg = (WorldGuardPlugin) Bukkit.getPluginManager().getPlugin("WorldGuard");

        int deletedrg = 0;

        for (final World w: Bukkit.getWorlds()) {
            MessageLogger.debug("Checking WG protections in world " + w.getName());
            final RegionManager m = wg.getRegionManager(w);

            // searching for inactive players in regions
            final HashSet<ProtectedRegion> regions = new HashSet<ProtectedRegion>(m.getRegions().values());
            for (final ProtectedRegion rg: regions) {
                MessageLogger.debug("Checking region " + rg.getId());
                final Set<String> owners = rg.getOwners().getPlayers();
                final Set<String> members = rg.getMembers().getPlayers();
                int inactive = 0;
                for (final String checkPlayer: owners) {
                    if (!pacheck.isActiveNCS(checkPlayer)) {
                        MessageLogger.debug(checkPlayer + " is inactive");
                        inactive++;
                    }
                }
                for (final String checkPlayer: members) {
                    if (!pacheck.isActiveNCS(checkPlayer)) {
                        MessageLogger.debug(checkPlayer + " is inactive");
                        inactive++;
                    }
                }
                // check region for remove (ignore regions without owners and
                // members)
                if (rg.hasMembersOrOwners() && inactive == owners.size() + members.size()) {
                    MessageLogger.debug("No active owners and members for region " + rg.getId() + ". Purging region");
                    if (regenrg) {
                        // regen and delete region
                        purgeRG(m, w, rg, regenrg, noregenoverlap);
                    } else {
                        // add region to delete batch
                        rgtodel.add(rg.getId());
                        // delete regions if maximum batch size reached
                        if (rgtodel.size() == 40) {
                            flushBatch(m);
                        }
                    }
                    deletedrg += 1;
                }
            }
            if (!regenrg) {
                // delete the rest of the regions in batch
                flushBatch(m);
            }
        }

        MessageLogger.debug("WG purge finished, deleted " + deletedrg + " inactive regions");
    }

    private void purgeRG(final RegionManager m, final World w, final ProtectedRegion rg, final boolean regenrg,
            final boolean noregenoverlap) {
        final Runnable rgregen = new Runnable() {
            BlockVector minpoint = rg.getMinimumPoint();
            BlockVector maxpoint = rg.getMaximumPoint();

            @Override
            public void run() {
                try {
                    if (!(noregenoverlap && m.getApplicableRegions(rg).size() > 1)) {
                        MessageLogger.debug("Regenerating region " + rg.getId());
                        WorldEditRegeneration.regenerateRegion(w, minpoint, maxpoint);
                    }
                    MessageLogger.debug("Deleting region " + rg.getId());
                    m.removeRegion(rg.getId());
                    m.save();
                } catch (final Exception e) {}
            }
        };
        SchedulerUtils.callSyncTaskAndWait(rgregen);
    }

    private final ArrayList<String> rgtodel = new ArrayList<String>(70);

    private void flushBatch(final RegionManager m) {
        // delete regions
        final Runnable deleteregions = new Runnable() {
            @Override
            public void run() {
                for (final String regionid: rgtodel) {
                    MessageLogger.debug("Deleting region " + regionid);
                    m.removeRegion(regionid);
                }
                try {
                    m.save();
                } catch (final Exception e) {}
                rgtodel.clear();
            }
        };
        SchedulerUtils.callSyncTaskAndWait(deleteregions);
    }

}
