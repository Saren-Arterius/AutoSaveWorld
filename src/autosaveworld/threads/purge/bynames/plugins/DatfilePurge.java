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

import org.bukkit.Bukkit;

import autosaveworld.core.logging.MessageLogger;
import autosaveworld.threads.purge.bynames.ActivePlayersList;

public class DatfilePurge {

    public void doDelPlayerDatFileTask(ActivePlayersList pacheck) {

        MessageLogger.debug("Playre .dat file purge started");

        int deleted = 0;
        final String worldfoldername = Bukkit.getWorlds().get(0).getWorldFolder().getAbsolutePath();
        final File playersdatfolder = new File(worldfoldername + File.separator + "players" + File.separator);
        for (final File playerfile: playersdatfolder.listFiles()) {
            if (playerfile.getName().endsWith(".dat")) {
                final String playername = playerfile.getName().substring(0, playerfile.getName().length() - 4);
                if (!pacheck.isActiveCS(playername)) {
                    MessageLogger.debug(playername + " is inactive. Removing dat file");
                    playerfile.delete();
                    deleted += 1;
                }
            }
        }

        MessageLogger.debug("Player .dat purge finished, deleted " + deleted + " player .dat files");
    }

}
