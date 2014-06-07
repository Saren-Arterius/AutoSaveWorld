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

package autosaveworld.modules.pluginmanager;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import autosaveworld.core.AutoSaveWorld;
import autosaveworld.core.logging.MessageLogger;

public class PluginManager {

    private final AutoSaveWorld plugin;

    public PluginManager(AutoSaveWorld plugin) {
        this.plugin = plugin;
        if (ManagementFactory.getRuntimeMXBean().getInputArguments().contains("-XX:+DisableExplicitGC")) {
            MessageLogger.warn("Your JVM is configured to ignore GC calls, plugin manager may not work as expected");
        }
    }

    private final InternalUtils iutils = new InternalUtils();

    public void handlePluginManagerCommand(CommandSender sender, String command, String pluginname) {
        if (command.equalsIgnoreCase("load")) {
            loadPlugin(sender, pluginname);
        } else if (command.equalsIgnoreCase("unload")) {
            unloadPlugin(sender, pluginname);
        } else if (command.equalsIgnoreCase("reload")) {
            reloadPlugin(sender, pluginname);
        } else {
            MessageLogger.sendMessage(sender, "Invalid plugin manager command");
        }
    }

    private void unloadPlugin(CommandSender sender, String pluginname) {
        // find plugin
        final Plugin pmplugin = findPlugin(pluginname);
        // ignore if plugin is not loaded
        if (pmplugin == null) {
            MessageLogger.sendMessage(sender, "Plugin with this name not found");
            return;
        }
        // now unload plugin
        try {
            iutils.unloadPlugin(pmplugin);
            MessageLogger.sendMessage(sender, "Plugin unloaded");
        } catch (final Exception e) {
            e.printStackTrace();
            MessageLogger.sendMessage(sender, "Some error occured while unloading plugin");
        }
    }

    private void loadPlugin(CommandSender sender, String pluginname) {
        // ignore if plugin is already loaded
        if (isPluginAlreadyLoaded(pluginname)) {
            MessageLogger.sendMessage(sender, "Plugin is already loaded");
            return;
        }
        // find plugin file
        final File pmpluginfile = findPluginFile(pluginname);
        // ignore if we can't find plugin file
        if (!pmpluginfile.exists()) {
            MessageLogger.sendMessage(sender, "File with this plugin name not found");
            return;
        }
        // now load plugin
        try {
            iutils.loadPlugin(pmpluginfile);
            MessageLogger.sendMessage(sender, "Plugin loaded");
        } catch (final Exception e) {
            e.printStackTrace();
            MessageLogger.sendMessage(sender, "Some error occured while loading plugin");
        }
    }

    private void reloadPlugin(CommandSender sender, String pluginname) {
        // find plugin
        final Plugin pmplugin = findPlugin(pluginname);
        // find plugin file
        final File pmpluginfile = findPluginFile(pluginname);
        // ignore if plugin is not loaded
        if (pmplugin == null) {
            MessageLogger.sendMessage(sender, "Plugin with this name not found");
            return;
        }
        // ignore if we can't find plugin file
        if (!pmpluginfile.exists()) {
            MessageLogger.sendMessage(sender, "File with this plugin name not found");
            return;
        }
        // now reload plugin
        try {
            iutils.unloadPlugin(pmplugin);
            iutils.loadPlugin(pmpluginfile);
            MessageLogger.sendMessage(sender, "Plugin reloaded");
        } catch (final Exception e) {
            e.printStackTrace();
            MessageLogger.sendMessage(sender, "Some error occured while reloading plugin");
        }
    }

    private Plugin findPlugin(String pluginname) {
        for (final Plugin plugin: Bukkit.getPluginManager().getPlugins()) {
            if (plugin.getName().equalsIgnoreCase(pluginname)) {
                return plugin;
            }
        }
        return Bukkit.getPluginManager().getPlugin(pluginname);
    }

    private File findPluginFile(String pluginname) {
        for (final File pluginfile: plugin.getDataFolder().getParentFile().listFiles()) {
            try {
                if (pluginfile.getName().endsWith(".jar")) {
                    final JarFile jarFile = new JarFile(pluginfile);
                    final JarEntry je = jarFile.getJarEntry("plugin.yml");
                    if (je != null) {
                        final FileConfiguration plugininfo = YamlConfiguration.loadConfiguration(jarFile
                                .getInputStream(je));
                        final String jarpluginName = plugininfo.getString("name");
                        if (pluginname.equalsIgnoreCase(jarpluginName)
                                || pluginname.equalsIgnoreCase(jarpluginName.replace(" ", "_"))) {
                            jarFile.close();
                            return pluginfile;
                        }
                    }
                    jarFile.close();
                }
            } catch (final Exception e) {}
        }
        return new File(plugin.getDataFolder().getParent(), pluginname + ".jar");
    }

    private boolean isPluginAlreadyLoaded(String pluginname) {
        for (final Plugin plugin: Bukkit.getPluginManager().getPlugins()) {
            if (plugin.getName().equalsIgnoreCase(pluginname)) {
                return true;
            }
        }
        return false;
    }

}
