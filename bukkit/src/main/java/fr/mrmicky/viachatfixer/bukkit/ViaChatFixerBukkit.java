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

    private final ChatHandler chatHandler = new ChatHandler(this);

    private LoggerAdapter logger;

    @Override
    public void onLoad() {
        this.logger = new JavaLoggerAdapter(getLogger());
    }

    @Override
    public void onEnable() {
        if (getServer().getPluginManager().getPlugin("ViaVersion") == null) {
            this.logger.error("You need to install ViaVersion to use ViaChatFixer");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

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
