package fr.mrmicky.viachatfixer.sponge;

import com.google.inject.Inject;
import fr.mrmicky.viachatfixer.common.ChatHandler;
import fr.mrmicky.viachatfixer.common.ViaChatFixerPlatform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;

import java.lang.reflect.Field;

@Plugin(
        id = "viachatfixer",
        name = "ViaChatFixer",
        version = ViaChatFixerVersion.VERSION,
        dependencies = @Dependency(id = "viaversion"),
        description = "Allow 1.11+ players to have longer chat messages on 1.8-1.10 servers with ViaVersion",
        authors = "MrMicky"
)
public final class ViaChatFixerSponge implements ViaChatFixerPlatform {

    private final SpongeLogger logger;

    private ChatHandler chatHandler;
    private Field chatRawMessageField;
    private boolean chatRawMessageFieldInitialized;

    @Inject
    public ViaChatFixerSponge(SpongeLogger logger) {
        this.logger = logger;
    }

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        try {
            Class.forName("com.viaversion.viaversion.api.ViaManager");
        } catch (ClassNotFoundException e) {
            this.logger.error("You need to install ViaVersion v5.0.0 or higher to use this version of ViaChatFixer.");
            this.logger.error("If you can't update ViaVersion, you can use an older ViaChatFixer version.");
            return;
        }

        this.chatHandler = new ChatHandler(this);

        try {
            this.chatHandler.init();
        } catch (Exception e) {
            this.logger.error("An error occurred during initialization", e);
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onChat(MessageChannelEvent.Chat event, @Root Player player) {
        String message = this.chatHandler.handle(player.getUniqueId());

        if (message == null) {
            return;
        }

        Text text = Text.of(message);

        event.getFormatter().setBody(text);

        setChatEventRawMessage(event, text);
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onCommand(SendCommandEvent event, @Root Player player) {
        String command = this.chatHandler.handle(player.getUniqueId());

        if (command == null) {
            return;
        }

        command = command.trim();

        if (command.startsWith("/")) {
            command = command.substring(1);
        }

        String[] args = command.split(" ", 2);

        event.setCommand(args[0]);
        event.setArguments(args.length > 1 ? args[1] : "");
    }

    @Override
    public SpongeLogger getLoggerAdapter() {
        return this.logger;
    }

    private void setChatEventRawMessage(MessageChannelEvent.Chat event, Text rawMessage) {
        try {
            if (!this.chatRawMessageFieldInitialized) {
                this.chatRawMessageFieldInitialized = true;

                Field field = event.getClass().getDeclaredField("rawMessage");
                field.setAccessible(true);

                this.chatRawMessageField = field;
            }

            if (this.chatRawMessageField != null) {
                this.chatRawMessageField.set(event, rawMessage);
            }
        } catch (ReflectiveOperationException e) {
            this.logger.warn("Unable to find rawMessage field in " + event.getClass().getName(), e);

            this.chatRawMessageField = null;
        }
    }
}
