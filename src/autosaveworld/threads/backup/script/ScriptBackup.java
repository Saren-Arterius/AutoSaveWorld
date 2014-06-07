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

package autosaveworld.threads.backup.script;

import java.io.File;

import autosaveworld.config.AutoSaveWorldConfig;
import autosaveworld.core.logging.MessageLogger;

public class ScriptBackup {

    private final AutoSaveWorldConfig config;

    public ScriptBackup(AutoSaveWorldConfig config) {
        this.config = config;
    }

    public void performBackup() {
        for (final String scriptpath: config.scriptbackupscriptpaths) {
            final File scriptfile = new File(scriptpath);
            if (!scriptpath.isEmpty() && scriptfile.exists()) {
                MessageLogger.debug("Executing script " + scriptfile.getAbsolutePath());
                final Process p;
                final ProcessBuilder pb = new ProcessBuilder();
                pb.command(scriptfile.getAbsolutePath());
                pb.inheritIO();
                try {
                    p = pb.start();
                    p.waitFor();
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            } else {
                MessageLogger.debug("Script path is invalid");
            }
        }
    }

}
