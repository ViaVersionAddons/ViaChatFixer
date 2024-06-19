package fr.mrmicky.viachatfixer;

import com.viaversion.viaversion.api.connection.StoredObject;
import com.viaversion.viaversion.api.connection.UserConnection;

public class ChatTracker extends StoredObject {

    private String lastMessage;
    private long lastMessageTime;

    public ChatTracker(UserConnection user) {
        super(user);
    }

    public String getLastMessage() {
        return this.lastMessage;
    }

    public boolean isValid(int time) {
        if (this.lastMessage == null) {
            return false;
        }

        return (System.currentTimeMillis() - this.lastMessageTime) < time;
    }

    public void updateLastMessage(String message) {
        this.lastMessage = message;
        this.lastMessageTime = System.currentTimeMillis();
    }

    public void reset() {
        this.lastMessage = null;
        this.lastMessageTime = 0;
    }
}
