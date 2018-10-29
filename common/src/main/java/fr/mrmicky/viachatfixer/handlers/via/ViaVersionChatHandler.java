package fr.mrmicky.viachatfixer.handlers.via;

import fr.mrmicky.viachatfixer.ViaChatFixerPlatform;
import fr.mrmicky.viachatfixer.handlers.ChatHandler;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.protocol.ProtocolRegistry;
import us.myles.ViaVersion.api.remapper.PacketHandler;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion.protocols.protocol1_11to1_10.Protocol1_11To1_10;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;

/**
 * @author MrMicky
 */
public class ViaVersionChatHandler implements ChatHandler {

    private ViaChatFixerPlatform platform;

    public ViaVersionChatHandler(ViaChatFixerPlatform platform) {
        this.platform = platform;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void init() throws Exception {
        Field registryMapField = ProtocolRegistry.class.getDeclaredField("registryMap");
        registryMapField.setAccessible(true);

        Map<Integer, Map<Integer, Protocol>> registryMap = (Map<Integer, Map<Integer, Protocol>>) registryMapField.get(null);

        Protocol protocol = null;
        for (Map<Integer, Protocol> protocolMap : registryMap.values()) {
            for (Protocol prot : protocolMap.values()) {
                if (prot instanceof Protocol1_11To1_10) {
                    protocol = prot;
                    break;
                }
            }
        }

        if (protocol == null) {
            throw new IllegalStateException("Protocol 1_11To1_10 not found");
        }

        protocol.registerIncoming(State.PLAY, 0x02, 0x02, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.STRING); // 0 - Message
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        String msg = wrapper.get(Type.STRING, 0);

                        if (msg.length() > 100) {
                            wrapper.set(Type.STRING, 0, msg.substring(0, 100));

                            UserConnection connection = wrapper.user();
                            ChatTracker chatTracker = connection.get(ChatTracker.class);

                            if (chatTracker == null) {
                                connection.put(chatTracker = new ChatTracker(connection));
                            }

                            // don't allow messages longer than 256 characters
                            if (msg.length() > 256) {
                                msg = msg.substring(0, 256);
                            }

                            chatTracker.setLastMessage(msg);
                            chatTracker.updateLastMessageTime();
                        }
                    }
                });
            }
        });
    }

    @Override
    public String handle(UUID uuid) {
        UserConnection connection = Via.getManager().getConnection(uuid);

        if (connection == null) {
            platform.getLogger().warning("Unknown connection for player with UUID " + uuid);
            return null;
        }

        ChatTracker chatTracker = connection.get(ChatTracker.class);

        if (chatTracker != null && chatTracker.getLastMessage() != null) {
            if (!chatTracker.isValid(100)){
                chatTracker.reset();
                return null;
            }

            String message = chatTracker.getLastMessage();
            chatTracker.reset();
            return message;
        }

        return null;
    }
}
