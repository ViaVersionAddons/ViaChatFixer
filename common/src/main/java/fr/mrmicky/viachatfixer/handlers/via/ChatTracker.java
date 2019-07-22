package fr.mrmicky.viachatfixer.handlers.via;

import us.myles.ViaVersion.api.data.StoredObject;
import us.myles.ViaVersion.api.data.UserConnection;

public class ChatTracker extends StoredObject {

    private String lastMessage;
    private long lastMessageTime;

    public ChatTracker(UserConnection user) {
        super(user);
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public boolean isValid(int time) {
        return (System.currentTimeMillis() - lastMessageTime) < time;
    }

    public void updateLastMessage(String message) {
        lastMessage = message;
        lastMessageTime = System.currentTimeMillis();
    }

    public void reset() {
        lastMessage = null;
        lastMessageTime = 0;
    }
}
