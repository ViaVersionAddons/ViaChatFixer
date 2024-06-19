package fr.mrmicky.viachatfixer;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_10to1_11.Protocol1_10To1_11;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ChatHandler {

    private final Set<UUID> unknownPlayers = new HashSet<>();

    private final ViaChatFixer plugin;

    private boolean enabled = false;

    public ChatHandler(ViaChatFixer plugin) {
        this.plugin = plugin;
    }

    public boolean init() {
        ProtocolVersion lowestVersion = Via.getAPI().getServerVersion().lowestSupportedProtocolVersion();

        if (lowestVersion.getVersion() >= ProtocolVersion.v1_11.getVersion()) {
            this.plugin.getLogger().warning("This plugin is not required on 1.11+ servers, you can just remove it.");
            return false;
        }

        Protocol<?, ?, ?, ?> protocol = Via.getManager().getProtocolManager().getProtocol(Protocol1_10To1_11.class);

        if (protocol == null) {
            throw new IllegalStateException("Protocol1_10To1_11 not found");
        }

        protocol.registerServerbound(State.PLAY, 0x02, 0x02, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.STRING); // 0 - Message
                handler(wrapper -> {
                    // 100 characters limit on older servers
                    String message = wrapper.get(Types.STRING, 0);

                    if (message.length() <= 100) {
                        return;
                    }

                    wrapper.set(Types.STRING, 0, message.substring(0, 100));

                    UserConnection connection = wrapper.user();
                    ChatTracker chatTracker = connection.get(ChatTracker.class);

                    if (chatTracker == null) {
                        chatTracker = new ChatTracker(connection);
                        connection.put(chatTracker);
                    }

                    // don't allow messages longer than 256 characters
                    if (message.length() > 256) {
                        message = message.substring(0, 256);
                    }

                    chatTracker.updateLastMessage(message);
                });
            }
        }, true);

        this.enabled = true;

        return true;
    }

    public String handle(UUID uuid) {
        if (!this.enabled) {
            return null;
        }

        ChatTracker chatTracker = getChatTracker(uuid);

        if (chatTracker == null) {
            return null;
        }

        String message = chatTracker.getLastMessage();

        chatTracker.reset();

        return message;
    }

    private ChatTracker getChatTracker(UUID uuid) {
        UserConnection connection = Via.getManager().getConnectionManager().getConnectedClient(uuid);

        if (connection == null) {
            if (this.unknownPlayers.add(uuid)) {
                this.plugin.getLogger().warning("Unknown connection for player with UUID " + uuid);
            }

            return null;
        }

        ChatTracker chatTracker = connection.get(ChatTracker.class);

        if (chatTracker != null && !chatTracker.isValid(100)) {
            chatTracker.reset();
            return null;
        }

        return chatTracker;
    }
}
