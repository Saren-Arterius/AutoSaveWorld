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

package autosaveworld.threads.purge.bynames;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;

import autosaveworld.config.AutoSaveWorldConfig;
import autosaveworld.core.logging.MessageLogger;

public class ActivePlayersList {

    private final AutoSaveWorldConfig config;

    public ActivePlayersList(AutoSaveWorldConfig config) {
        this.config = config;
    }

    private final HashSet<String> plactivencs = new HashSet<String>();
    private final HashSet<String> plactivecs  = new HashSet<String>();

    @SuppressWarnings("deprecation")
    public void gatherActivePlayersList(long awaytime) {
        try {
            // fill lists
            // due to bukkit fucks up itself when we have two player files with
            // different case (test.dat and Test.dat), i had to write this...
            final Server server = Bukkit.getServer();
            final Class<?> craftofflineplayer = Bukkit.getOfflinePlayer("fakeautopurgeplayer").getClass();
            final Constructor<?> ctor = craftofflineplayer.getDeclaredConstructor(server.getClass(), String.class);
            ctor.setAccessible(true);
            final File playersdir = new File(Bukkit.getWorlds().get(0).getWorldFolder(), "players");
            for (final String file: playersdir.list()) {
                if (file.endsWith(".dat")) {
                    final String nickname = file.substring(0, file.length() - 4);
                    MessageLogger.debug("Checking player " + nickname);
                    final OfflinePlayer offplayer = (OfflinePlayer) ctor.newInstance(server, nickname);
                    if (System.currentTimeMillis() - offplayer.getLastPlayed() < awaytime) {
                        MessageLogger.debug("Adding player " + nickname + " to active list");
                        plactivecs.add(offplayer.getName());
                        plactivencs.add(offplayer.getName().toLowerCase());
                    }
                }
            }
            for (final String ignorednick: config.purgeIgnoredNicks) {
                plactivecs.add(ignorednick);
                plactivencs.add(ignorednick.toLowerCase());
            }
        } catch (final Exception e) {
            throw new RuntimeException("Failed to gather active players list");
        }
    }

    public int getActivePlayersCount() {
        return plactivecs.size();
    }

    public boolean isActiveNCS(String playername) {
        return plactivencs.contains(playername.toLowerCase());
    }

    public boolean isActiveCS(String playername) {
        return plactivecs.contains(playername);
    }

}
