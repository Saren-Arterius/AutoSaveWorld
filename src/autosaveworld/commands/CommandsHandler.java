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

package autosaveworld.commands;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import autosaveworld.config.AutoSaveWorldConfig;
import autosaveworld.config.AutoSaveWorldConfigMSG;
import autosaveworld.config.LocaleChanger;
import autosaveworld.core.AutoSaveWorld;
import autosaveworld.core.logging.MessageLogger;
import autosaveworld.utils.StringUtils;

public class CommandsHandler implements CommandExecutor {

    private AutoSaveWorld                plugin = null;
    private final AutoSaveWorldConfig    config;
    private final AutoSaveWorldConfigMSG configmsg;
    private final LocaleChanger          localeChanger;

    public CommandsHandler(AutoSaveWorld plugin, AutoSaveWorldConfig config, AutoSaveWorldConfigMSG configmsg,
            LocaleChanger localeChanger) {
        this.plugin = plugin;
        this.config = config;
        this.configmsg = configmsg;
        this.localeChanger = localeChanger;
    };

    private final PermissionCheck permCheck = new PermissionCheck();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {

        final String commandName = command.getName().toLowerCase();

        // check permissions
        if (!permCheck.isAllowed(sender, commandName, args, config.commandonlyfromconsole)) {
            MessageLogger.sendMessage(sender, configmsg.messageInsufficientPermissions);
            return true;
        }

        // now handle commands
        if (commandName.equalsIgnoreCase("autosave")) {
            // "autosave" command handler
            plugin.saveThread.startsave();
            return true;
        } else if (commandName.equalsIgnoreCase("autobackup")) {
            // "autobackup" command handler
            plugin.backupThread.startbackup();
            return true;
        } else if (commandName.equalsIgnoreCase("autopurge")) {
            // "autopurge" command handler
            plugin.purgeThread.startpurge();
            return true;
        } else if (commandName.equalsIgnoreCase("autosaveworld")) {
            // "autosaveworld" command handler
            if (args.length == 1 && args[0].equalsIgnoreCase("help")) {
                // help
                MessageLogger.sendMessage(sender, "&f/asw save&7 - &3Saves all worlds and players");
                MessageLogger.sendMessage(sender, "&f/save&7 - &3Same as /asw save");
                MessageLogger
                        .sendMessage(sender,
                                "&f/asw backup&7 - &3Backups worlds defined in config.yml (* - all worlds) and plugins (if enabled in config)");
                MessageLogger.sendMessage(sender, "&f/backup&7 - &3Same as /asw backup");
                MessageLogger.sendMessage(sender, "&f/asw purge&7 - &3Purges plugins info from inactive players");
                MessageLogger.sendMessage(sender, "&f/purge&7 - &3Same as /asw purge");
                MessageLogger.sendMessage(sender, "&f/asw restart&7 - &3Restarts server");
                MessageLogger.sendMessage(sender, "&f/asw forcerestart&7 - &3Restarts server without countdown");
                MessageLogger.sendMessage(sender, "&f/asw regenworld {world}&7 - &3Regenerates world");
                MessageLogger.sendMessage(sender, "&f/asw pmanager load {pluginname}&7 - &3Loads plugin {pluginname}");
                MessageLogger.sendMessage(sender,
                        "&f/asw pmanager unload {pluginname}&7 - &3Unloads plugin {pluginname}");
                MessageLogger
                        .sendMessage(sender,
                                "&f/asw pmanager reload {pluginname}&7 - &3Reloads(unloads and then loads) plugin {pluginname}");
                MessageLogger.sendMessage(sender,
                        "&f/asw process start {processname} {command line}&7 - &3Starts process using {command line}");
                MessageLogger.sendMessage(sender, "&f/asw process stop {processname}&7 - &3Stops process");
                MessageLogger
                        .sendMessage(sender,
                                "&f/asw process output {processname}&7 - &3Prints latest process output from output and error streams");
                MessageLogger.sendMessage(sender,
                        "&f/asw process input {processname} {input}&7 - &3Sends a line to process input stream");
                MessageLogger.sendMessage(sender, "&f/asw process list&7 - &3Shows registered processes");
                MessageLogger.sendMessage(sender, "&f/asw serverstatus&7 - &3Shows cpu, memory, HDD usage");
                MessageLogger.sendMessage(sender, "&f/asw forcegc&7 - &3Forces garbage collection");
                MessageLogger.sendMessage(sender, "&f/asw reload&7 - &3Reload all configs)");
                MessageLogger.sendMessage(sender, "&f/asw reloadconfig&7 - &3Reload plugin config (config.yml)");
                MessageLogger.sendMessage(sender, "&f/asw reloadmsg&7 - &3Reload message config (configmsg.yml)");
                MessageLogger.sendMessage(sender, "&f/asw locale available&7 - &3Show available messages locales");
                MessageLogger.sendMessage(sender,
                        "&f/asw locale load {locale}&7 - &3Set meesages locale to one of the available locales");
                MessageLogger.sendMessage(sender, "&f/asw version&7 - &3Shows plugin version");
                return true;
            } else if (args.length >= 2 && args[0].equalsIgnoreCase("process")) {
                String processname = null;
                if (args.length > 2) {
                    processname = args[2];
                }
                String[] processargs = null;
                if (args.length > 3) {
                    processargs = Arrays.copyOfRange(args, 3, args.length);
                }
                plugin.processmanager.handleProcessManagerCommand(sender, args[1], processname, processargs);
                return true;
            } else if (args.length >= 3 && args[0].equalsIgnoreCase("pmanager")) {
                final String[] nameArray = Arrays.copyOfRange(args, 2, args.length);
                plugin.pluginmanager.handlePluginManagerCommand(sender, args[1], StringUtils.join(nameArray, " "));
                return true;
            } else if (args.length == 1 && args[0].equalsIgnoreCase("forcegc")) {
                final List<String> arguments = ManagementFactory.getRuntimeMXBean().getInputArguments();
                if (arguments.contains("-XX:+DisableExplicitGC")) {
                    MessageLogger.sendMessage(sender, "&4Your JVM is configured to ignore GC calls, can't force gc");
                    return true;
                }
                MessageLogger.sendMessage(sender, "&9Forcing GC");
                System.gc();
                System.gc();
                MessageLogger.sendMessage(sender, "&9GC finished");
                return true;
            } else if (args.length == 1 && args[0].equalsIgnoreCase("serverstatus")) {
                final DecimalFormat df = new DecimalFormat("0.00");
                // processor (if available)
                try {
                    final com.sun.management.OperatingSystemMXBean systemBean = (com.sun.management.OperatingSystemMXBean) java.lang.management.ManagementFactory
                            .getOperatingSystemMXBean();
                    final double cpuusage = systemBean.getProcessCpuLoad() * 100;
                    if (cpuusage > 0) {
                        sender.sendMessage(ChatColor.GOLD + "Cpu usage: " + ChatColor.RED + df.format(cpuusage) + "%");
                    } else {
                        sender.sendMessage(ChatColor.GOLD + "Cpu usage: " + ChatColor.RED + "not available");
                    }
                } catch (final Exception e) {}
                // memory
                final Runtime runtime = Runtime.getRuntime();
                final long maxmemmb = runtime.maxMemory() / 1024 / 1024;
                final long freememmb = (runtime.maxMemory() - (runtime.totalMemory() - runtime.freeMemory())) / 1024 / 1024;
                sender.sendMessage(ChatColor.GOLD + "Memory usage: " + ChatColor.RED
                        + df.format((maxmemmb - freememmb) * 100 / maxmemmb) + "% " + ChatColor.DARK_AQUA + "("
                        + ChatColor.DARK_GREEN + (maxmemmb - freememmb) + "/" + maxmemmb + " MB" + ChatColor.DARK_AQUA
                        + ")" + ChatColor.RESET);
                // hard drive
                final File file = new File(".");
                final long maxspacegb = file.getTotalSpace() / 1024 / 1024 / 1024;
                final long freespacegb = file.getFreeSpace() / 1024 / 1024 / 1024;
                sender.sendMessage(ChatColor.GOLD + "Disk usage: " + ChatColor.RED
                        + df.format((maxspacegb - freespacegb) * 100 / maxspacegb) + "% " + ChatColor.DARK_AQUA + "("
                        + ChatColor.DARK_GREEN + (maxspacegb - freespacegb) + "/" + maxspacegb + " GB"
                        + ChatColor.DARK_AQUA + ")" + ChatColor.RESET);
                return true;
            } else if (args.length == 1 && args[0].equalsIgnoreCase("save")) {
                // save
                plugin.saveThread.startsave();
                return true;
            } else if (args.length == 1 && args[0].equalsIgnoreCase("backup")) {
                // backup
                plugin.backupThread.startbackup();
                return true;
            } else if (args.length == 1 && args[0].equalsIgnoreCase("purge")) {
                // purge
                plugin.purgeThread.startpurge();
                return true;
            } else if (args.length == 1 && args[0].equalsIgnoreCase("restart")) {
                // restart
                plugin.autorestartThread.startrestart(false);
                return true;
            } else if (args.length == 1 && args[0].equalsIgnoreCase("forcerestart")) {
                // restrat without countdown
                plugin.autorestartThread.startrestart(true);
                return true;
            } else if ((args.length == 2 && args[0].equalsIgnoreCase("regenworld"))) {
                // regen world
                if (Bukkit.getPluginManager().getPlugin("WorldEdit") == null) {
                    MessageLogger.sendMessage(sender, "You need WorldEdit installed to do that");
                    return true;
                }
                if (Bukkit.getWorld(args[1]) == null) {
                    MessageLogger.sendMessage(sender, "This world doesn't exist");
                    return true;
                }
                plugin.worldregencopyThread.startworldregen(args[1]);
                return true;
            } else if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                // reload
                config.load();
                configmsg.loadmsg();
                MessageLogger.sendMessage(sender, "All configurations reloaded");
                return true;
            } else if (args.length == 1 && args[0].equalsIgnoreCase("reloadconfig")) {
                // reload config
                config.load();
                MessageLogger.sendMessage(sender, "Main configuration reloaded");
                return true;
            } else if (args.length == 1 && args[0].equalsIgnoreCase("reloadmsg")) {
                // reload messages
                configmsg.loadmsg();
                MessageLogger.sendMessage(sender, "Messages file reloaded");
                return true;
            } else if (args.length == 1 && args[0].equalsIgnoreCase("version")) {
                // version
                MessageLogger.sendMessage(sender, plugin.getDescription().getName() + " "
                        + plugin.getDescription().getVersion());
                return true;
            } else if ((args.length >= 1 && args[0].equalsIgnoreCase("locale"))) {
                // locale loader
                if (args.length == 2 && args[1].equalsIgnoreCase("available")) {
                    MessageLogger.sendMessage(sender, "Available locales: " + localeChanger.getAvailableLocales());
                    return true;
                } else if (args.length == 2 && args[1].equalsIgnoreCase("load")) {
                    MessageLogger
                            .sendMessage(sender,
                                    "You should specify a locale to load (get available locales using /asw locale available command)");
                    return true;
                } else if (args.length == 3 && args[1].equalsIgnoreCase("load")) {
                    if (localeChanger.getAvailableLocales().contains(args[2])) {
                        MessageLogger.sendMessage(sender, "Loading locale " + args[2]);
                        localeChanger.loadLocale(args[2]);
                        MessageLogger.sendMessage(sender, "Loaded locale " + args[2]);
                        return true;
                    } else {
                        MessageLogger.sendMessage(sender, "Locale " + args[2] + " is not available");
                        return true;
                    }
                }
            }
            return false;
        }
        return false;
    }

}
