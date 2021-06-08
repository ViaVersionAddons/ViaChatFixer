package fr.mrmicky.viachatfixer.common;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.protocol.remapper.PacketRemapper;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_11to1_10.Protocol1_11To1_10;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ChatHandler {

    private final Set<UUID> unknownPlayers = new HashSet<>();

    private final ViaChatFixerPlatform platform;

    private boolean enabled = false;

    public ChatHandler(ViaChatFixerPlatform platform) {
        this.platform = platform;
    }

    public void init() {
        if (Via.getAPI().getServerVersion().lowestSupportedVersion() >= ProtocolVersion.v1_11.getVersion()) {
            this.platform.getLoggerAdapter().warn("This plugin is not required on 1.11+ servers, you can just remove it.");
            return;
        }

        Protocol<?, ?, ?, ?> protocol = Via.getManager().getProtocolManager().getProtocol(Protocol1_11To1_10.class);

        if (protocol == null) {
            throw new IllegalStateException("Protocol 1_11To1_10 not found");
        }

        protocol.registerServerbound(State.PLAY, 0x02, 0x02, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.STRING); // 0 - Message
                handler(wrapper -> {
                    // 100 character limit on older servers
                    String message = wrapper.get(Type.STRING, 0);

                    if (message.length() <= 100) {
                        return;
                    }

                    wrapper.set(Type.STRING, 0, message.substring(0, 100));

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
                this.platform.getLoggerAdapter().warn("Unknown connection for player with UUID " + uuid);
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
