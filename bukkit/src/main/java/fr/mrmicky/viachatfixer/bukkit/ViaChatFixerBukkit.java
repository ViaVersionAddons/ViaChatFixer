package fr.mrmicky.viachatfixer.bukkit;

import fr.mrmicky.viachatfixer.ViaChatFixerPlatform;
import fr.mrmicky.viachatfixer.handlers.ChatHandler;
import fr.mrmicky.viachatfixer.handlers.via.ViaVersionChatHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

/**
 * @author MrMicky
 */
public final class ViaChatFixerBukkit extends JavaPlugin implements ViaChatFixerPlatform, Listener {

    private ChatHandler chatHandler;

    @Override
    public void onEnable() {
        if (getServer().getPluginManager().getPlugin("ViaVersion") == null) {
            // TODO support if ViaVersion is not installed, for example if ViaVersion is on the proxy
            getLogger().severe("ViaVersion is not installed");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        chatHandler = new ViaVersionChatHandler(this);

        // Only load when ViaVersion is loaded
        getServer().getScheduler().runTask(this, () -> {
            try {
                chatHandler.init();
            } catch (Exception e) {
                getLogger().log(Level.SEVERE, "An error occurred during init", e);
                getServer().getPluginManager().disablePlugin(this);
                return;
            }

            getServer().getPluginManager().registerEvents(this, this);
        });
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent e) {
        String message = chatHandler.handle(e.getPlayer().getUniqueId());

        if (message != null) {
            e.setMessage(message);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerCommand(PlayerCommandPreprocessEvent e) {
        String message = chatHandler.handle(e.getPlayer().getUniqueId());

        if (message != null) {
            e.setMessage(message);
        }
    }
}
