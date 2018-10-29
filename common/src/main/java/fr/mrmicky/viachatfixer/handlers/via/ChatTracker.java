package fr.mrmicky.viachatfixer.handlers.via;

import us.myles.ViaVersion.api.data.StoredObject;
import us.myles.ViaVersion.api.data.UserConnection;

/**
 * @author MrMicky
 */
public class ChatTracker extends StoredObject {

    private String lastMessage;
    private long lastMessageTime;

    public ChatTracker(UserConnection user) {
        super(user);
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public boolean isValid(int time) {
        return (System.currentTimeMillis() - lastMessageTime) < time;
    }

    public void updateLastMessageTime() {
        lastMessageTime = System.currentTimeMillis();
    }

    public void reset() {
        lastMessage = null;
        lastMessageTime = 0;
    }
}
