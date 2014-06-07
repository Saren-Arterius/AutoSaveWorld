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

package autosaveworld.core;

import java.io.File;

public class GlobalConstants {

    public static void init(AutoSaveWorld plugin) {
        GlobalConstants.pluginfolder = plugin.getDataFolder().getPath() + File.separator;
    }

    // main
    private static String pluginfolder;

    public static String getPluginFolder() {
        return GlobalConstants.pluginfolder;
    }

    // config
    private static String configfile = "config.yml";

    public static String getConfigPath() {
        return GlobalConstants.getPluginFolder() + GlobalConstants.configfile;
    }

    private static String configmsgfile = "configmsg.yml";

    public static String getConfigMSGPath() {
        return GlobalConstants.getPluginFolder() + GlobalConstants.configmsgfile;
    }

    // counter preserve files
    private static String backuppreserverfile = "backupintervalpreserve.yml";

    public static String getBackupIntervalPreservePath() {
        return GlobalConstants.getPluginFolder() + GlobalConstants.backuppreserverfile;
    }

    // worldregen
    private static String worldregentempfolder      = "WorldRegenTemp" + File.separator;
    private static String worldnamefile             = "wname.yml";
    private static String wgtempfolder              = "WG" + File.separator;
    private static String factionstempfolder        = "Factions" + File.separator;
    private static String griefpreventiontempfolder = "GP" + File.separator;
    private static String townytempfolder           = "Towny" + File.separator;

    public static String getWorldRegenTempFolder() {
        return GlobalConstants.getPluginFolder() + GlobalConstants.worldregentempfolder;
    }

    public static String getWorldnameFile() {
        return GlobalConstants.getWorldRegenTempFolder() + GlobalConstants.worldnamefile;
    }

    public static String getWGTempFolder() {
        return GlobalConstants.getWorldRegenTempFolder() + GlobalConstants.wgtempfolder;
    }

    public static String getFactionsTempFolder() {
        return GlobalConstants.getWorldRegenTempFolder() + GlobalConstants.factionstempfolder;
    }

    public static String getGPTempFolder() {
        return GlobalConstants.getWorldRegenTempFolder() + GlobalConstants.griefpreventiontempfolder;
    }

    public static String getTownyTempFolder() {
        return GlobalConstants.getWorldRegenTempFolder() + GlobalConstants.townytempfolder;
    }

}
