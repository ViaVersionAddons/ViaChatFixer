package fr.mrmicky.viachatfixer.bukkit;

import fr.mrmicky.viachatfixer.common.ChatHandler;
import fr.mrmicky.viachatfixer.common.ViaChatFixerPlatform;
import fr.mrmicky.viachatfixer.common.logger.JavaLoggerAdapter;
import fr.mrmicky.viachatfixer.common.logger.LoggerAdapter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class ViaChatFixerBukkit extends JavaPlugin implements Listener, ViaChatFixerPlatform {

    private LoggerAdapter logger;
    private ChatHandler chatHandler;

    @Override
    public void onLoad() {
        this.logger = new JavaLoggerAdapter(getLogger());
    }

    @Override
    public void onEnable() {
        try {
            Class.forName("com.viaversion.viaversion.api.ViaManager");
        } catch (ClassNotFoundException e) {
            this.logger.error("You need to install ViaVersion v5.0.0 or higher to use this version of ViaChatFixer.");
            this.logger.error("If you can't update ViaVersion, you can use an older ViaChatFixer versions.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.chatHandler = new ChatHandler(this);

        // Only load when ViaVersion is loaded
        getServer().getScheduler().runTask(this, () -> {
            try {
                this.chatHandler.init();

                getServer().getPluginManager().registerEvents(this, this);
            } catch (Exception e) {
                this.logger.error("An error occurred during initialization", e);
                getServer().getPluginManager().disablePlugin(this);
            }
        });
    }

    @Override
    public LoggerAdapter getLoggerAdapter() {
        return this.logger;
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
