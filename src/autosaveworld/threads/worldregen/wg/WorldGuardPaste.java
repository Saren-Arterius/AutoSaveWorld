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

package autosaveworld.threads.worldregen.wg;

import java.util.Arrays;
import java.util.LinkedList;

import org.bukkit.Bukkit;
import org.bukkit.World;

import autosaveworld.core.GlobalConstants;
import autosaveworld.core.logging.MessageLogger;
import autosaveworld.threads.worldregen.SchematicData.SchematicToLoad;
import autosaveworld.threads.worldregen.SchematicOperations;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class WorldGuardPaste {

    private final World wtopaste;

    public WorldGuardPaste(String worldtopasteto) {
        wtopaste = Bukkit.getWorld(worldtopasteto);
    }

    public void pasteAllFromSchematics() {
        MessageLogger.debug("Pasting WG regions from schematics");

        final WorldGuardPlugin wg = (WorldGuardPlugin) Bukkit.getPluginManager().getPlugin("WorldGuard");

        final String schemfolder = GlobalConstants.getWGTempFolder();
        final RegionManager m = wg.getRegionManager(wtopaste);
        // paste all regions
        for (final ProtectedRegion rg: m.getRegions().values()) {
            // ignore global region
            if (rg.getId().equalsIgnoreCase("__global__")) {
                continue;
            }
            // paste
            MessageLogger.debug("Pasting WG region " + rg.getId() + " from schematic");
            final SchematicToLoad schematicdata = new SchematicToLoad(schemfolder + rg.getId());
            SchematicOperations.pasteFromSchematic(wtopaste,
                    new LinkedList<SchematicToLoad>(Arrays.asList(schematicdata)));
            MessageLogger.debug("Pasted WG region " + rg.getId() + " from schematic");
        }
    }

}
