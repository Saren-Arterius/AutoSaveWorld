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

package autosaveworld.threads.worldregen.factions;

import java.io.File;
import java.util.LinkedList;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import autosaveworld.core.GlobalConstants;
import autosaveworld.core.logging.MessageLogger;
import autosaveworld.threads.worldregen.SchematicData.SchematicToSave;
import autosaveworld.threads.worldregen.SchematicOperations;

import com.massivecraft.factions.entity.BoardColls;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.FactionColls;
import com.massivecraft.mcore.ps.PS;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.bukkit.BukkitUtil;

public class FactionsCopy {

    private final World wtoregen;

    public FactionsCopy(String worldtoregen) {
        wtoregen = Bukkit.getWorld(worldtoregen);
    }

    public void copyAllToSchematics() {
        MessageLogger.debug("Saving factions lands to schematics");

        new File(GlobalConstants.getFactionsTempFolder()).mkdirs();

        for (final Faction f: FactionColls.get().getForWorld(wtoregen.getName()).getAll()) {
            final Set<PS> chunks = BoardColls.get().getChunks(f);
            // ignore factions with no claimed land
            if (chunks.size() != 0) {
                MessageLogger.debug("Saving faction land " + f.getName() + " to schematic");
                // create temp folder for faction
                new File(GlobalConstants.getFactionsTempFolder() + f.getName()).mkdirs();
                // save all chunks
                final LinkedList<SchematicToSave> schematics = new LinkedList<SchematicToSave>();
                for (final PS ps: chunks) {
                    if (ps.getWorld().equalsIgnoreCase(wtoregen.getName())) {
                        // get coords
                        final int xcoord = ps.getChunkX();
                        final int zcoord = ps.getChunkZ();
                        final Vector bvmin = BukkitUtil.toVector(new Location(wtoregen, xcoord * 16, 0, zcoord * 16));
                        final Vector bvmax = BukkitUtil.toVector(new Location(wtoregen, xcoord * 16 + 15, wtoregen
                                .getMaxHeight(), zcoord * 16 + 15));
                        // add to save list
                        final SchematicToSave schematicdata = new SchematicToSave(
                                GlobalConstants.getFactionsTempFolder() + f.getName() + File.separator + "X" + xcoord
                                        + "Z" + zcoord, bvmin, bvmax);
                        schematics.add(schematicdata);
                    }
                }
                SchematicOperations.saveToSchematic(wtoregen, schematics);
                MessageLogger.debug("Faction land " + f.getName() + " saved");
            }
        }
    }

}
