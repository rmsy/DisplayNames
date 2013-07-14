package com.github.rmsy.displaynames;

import com.google.common.base.Preconditions;
import com.sk89q.bukkit.util.BukkitCommandsManager;
import com.sk89q.bukkit.util.CommandsManagerRegistration;
import com.sk89q.minecraft.util.commands.*;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.logging.Level;

public class DisplayNamesPlugin extends JavaPlugin {

    /**
     * The commands manager.
     */
    @Nonnull
    private CommandsManager commands;
    /**
     * The command registration.
     */
    @Nonnull
    private CommandsManagerRegistration commandsRegistration;
    /**
     * The metrics instance.
     */
    private Metrics metrics;

    @Command(
            aliases = {"dn"},
            desc = "Sets the display name of the sender (or the target).",
            help = "Execute the command with any intended color codes (&b, etc.)",
            max = 2,
            min = 1,
            usage = "<name> [target]"
    )
    @Console
    @CommandPermissions({"displaynames.edit.own", "displaynames.edit.others"})
    public static void displayNameCommand(@Nonnull final CommandContext arguments, @Nonnull final CommandSender sender) throws CommandException {
        if (Preconditions.checkNotNull(arguments, "arguments").argsLength() == 2) {
            if (Preconditions.checkNotNull(sender, "sender").hasPermission("displaynames.edit.others")) {
                String targetName = arguments.getString(1);
                Player target = Bukkit.getPlayer(targetName);
                if (target != null) {
                    target.setDisplayName(ChatColor.translateAlternateColorCodes('&', arguments.getString(0)));
                    target.setOverheadName(ChatColor.translateAlternateColorCodes('&', arguments.getString(0)));
                    sender.sendMessage(ChatColor.YELLOW + "Display name of " + target.getName() + " set to " + ChatColor.RESET + target.getDisplayName());
                } else {
                    throw new CommandException("Player " + targetName + " not found.");
                }
            } else {
                throw new CommandPermissionsException();
            }
        } else {
            if (Preconditions.checkNotNull(sender, "sender").hasPermission("displaynames.edit.own")) {
                if (sender instanceof Player) {
                    ((Player) sender).setOverheadName(ChatColor.translateAlternateColorCodes('&', arguments.getString(0)));
                    ((Player) sender).setDisplayName(ChatColor.translateAlternateColorCodes('&', arguments.getString(0)));
                    sender.sendMessage(ChatColor.YELLOW + "Display name set to " + ChatColor.RESET + ((Player) sender).getDisplayName());
                } else {
                    throw new CommandUsageException("You must specify a target.", "/dn <name> <target>");
                }
            } else {
                throw new CommandPermissionsException();
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String commandLabel, String[] args) {
        try {
            this.commands.execute(cmd.getName(), args, sender, sender);
        } catch (CommandPermissionsException e) {
            sender.sendMessage(ChatColor.RED + "You don't have permission.");
        } catch (MissingNestedCommandException e) {
            sender.sendMessage(ChatColor.RED + e.getUsage());
        } catch (CommandUsageException e) {
            sender.sendMessage(ChatColor.RED + e.getMessage());
            sender.sendMessage(ChatColor.RED + "Usage: " + e.getUsage());
        } catch (WrappedCommandException e) {
            sender.sendMessage(ChatColor.RED + "An unknown error has occurred. Please notify an administrator.");
            e.printStackTrace();
        } catch (CommandException e) {
            sender.sendMessage(ChatColor.RED + e.getMessage());
        }

        return true;
    }

    @Override
    public void onDisable() {
        this.commandsRegistration.unregisterCommands();
        this.commands = null;
        this.commandsRegistration = null;
        this.metrics = null;
    }

    @Override
    public void onEnable() {
        try {
            this.metrics = new Metrics(this);
            this.metrics.start();
            this.getLogger().log(Level.INFO, "Metrics enabled.");
        } catch (IOException exception) {
            this.getLogger().log(Level.WARNING, "An unknown error occurred. Metrics were not started.");
        }

        this.commands = new BukkitCommandsManager();
        this.commandsRegistration = new CommandsManagerRegistration(this, this.commands);
        this.commandsRegistration.register(DisplayNamesPlugin.class);
    }
}
