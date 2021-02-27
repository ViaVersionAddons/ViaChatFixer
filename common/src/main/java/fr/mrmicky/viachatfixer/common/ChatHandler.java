package fr.mrmicky.viachatfixer.common;

import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.protocol.ProtocolRegistry;
import us.myles.ViaVersion.api.protocol.ProtocolVersion;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion.protocols.protocol1_11to1_10.Protocol1_11To1_10;
import us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2.ServerboundPackets1_9_3;

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
        if (ProtocolRegistry.SERVER_PROTOCOL >= ProtocolVersion.v1_11.getId()) {
            this.platform.getLoggerAdapter().warn("This plugin is not required on 1.11+ servers, you can just remove it.");
            return;
        }

        //noinspection unchecked
        Protocol<?, ?, ?, ServerboundPackets1_9_3> protocol = ProtocolRegistry.getProtocol(Protocol1_11To1_10.class);

        if (protocol == null) {
            throw new IllegalStateException("Protocol 1_11To1_10 not found");
        }

        protocol.registerIncoming(State.PLAY, 0x02, 0x02, new PacketRemapper() {
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
        UserConnection connection = Via.getManager().getConnection(uuid);

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
