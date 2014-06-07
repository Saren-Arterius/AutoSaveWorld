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

package autosaveworld.threads.worldregen.griefprevention;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedList;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.ClaimArray;
import me.ryanhamshire.GriefPrevention.DataStore;
import me.ryanhamshire.GriefPrevention.GriefPrevention;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import autosaveworld.core.GlobalConstants;
import autosaveworld.core.logging.MessageLogger;
import autosaveworld.threads.worldregen.SchematicData.SchematicToSave;
import autosaveworld.threads.worldregen.SchematicOperations;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.bukkit.BukkitUtil;

public class GPCopy {

    private final World wtoregen;

    public GPCopy(String worldtoregen) {
        wtoregen = Bukkit.getWorld(worldtoregen);
    }

    public void copyAllToSchematics() {
        MessageLogger.debug("Saving griefprevention regions to schematics");

        new File(GlobalConstants.getGPTempFolder()).mkdirs();

        final GriefPrevention gp = (GriefPrevention) Bukkit.getPluginManager().getPlugin("GriefPrevention");

        // get database
        ClaimArray ca = null;
        try {
            final Field fld = DataStore.class.getDeclaredField("claims");
            fld.setAccessible(true);
            final Object o = fld.get(gp.dataStore);
            ca = (ClaimArray) o;
        } catch (final Exception e) {
            e.printStackTrace();
            MessageLogger.warn("Failed to access GriefPrevntion database. GP save cancelled");
            return;
        }

        // save all claims
        for (int i = 0; i < ca.size(); i++) {
            final Claim claim = ca.get(i);
            // get coords
            final double xmin = claim.getLesserBoundaryCorner().getX();
            final double zmin = claim.getLesserBoundaryCorner().getZ();
            final double xmax = claim.getGreaterBoundaryCorner().getX();
            final double zmax = claim.getGreaterBoundaryCorner().getZ();
            final Vector bvmin = BukkitUtil.toVector(new Location(wtoregen, xmin, 0, zmin));
            final Vector bvmax = BukkitUtil.toVector(new Location(wtoregen, xmax, wtoregen.getMaxHeight(), zmax));
            // save
            MessageLogger.debug("Saving GP Region " + claim.getID() + " to schematic");
            final SchematicToSave schematicdata = new SchematicToSave(GlobalConstants.getGPTempFolder()
                    + claim.getID().toString(), bvmin, bvmax);
            SchematicOperations
                    .saveToSchematic(wtoregen, new LinkedList<SchematicToSave>(Arrays.asList(schematicdata)));
            MessageLogger.debug("GP Region " + claim.getID() + " saved");
        }
    }

}
