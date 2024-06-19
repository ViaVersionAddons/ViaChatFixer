package fr.mrmicky.viachatfixer;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public final class ViaChatFixer extends JavaPlugin implements Listener {

    private ChatHandler chatHandler;

    @Override
    public void onEnable() {
        try {
            Class.forName("com.viaversion.viaversion.api.type.Types");
        } catch (ClassNotFoundException e) {
            getLogger().severe("You need to install ViaVersion v5.0.0 or higher to use this version of ViaChatFixer.");
            getLogger().severe("If you can't update ViaVersion, you can use an older ViaChatFixer versions.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.chatHandler = new ChatHandler(this);

        // Only load when ViaVersion is loaded
        getServer().getScheduler().runTask(this, () -> {
            try {
                if (!this.chatHandler.init()) {
                    getServer().getPluginManager().disablePlugin(this);
                    return;
                }

                getServer().getPluginManager().registerEvents(this, this);
            } catch (Exception e) {
                getLogger().log(Level.SEVERE, "An error occurred during initialization", e);
                getServer().getPluginManager().disablePlugin(this);
            }
        });
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent e) {
        String message = this.chatHandler.handle(e.getPlayer().getUniqueId());

        if (message != null) {
            e.setMessage(message);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerCommand(PlayerCommandPreprocessEvent e) {
        String message = this.chatHandler.handle(e.getPlayer().getUniqueId());

        if (message != null) {
            e.setMessage(message);
        }
    }
}
