package fr.mrmicky.viachatfixer.handlers.via;

import us.myles.ViaVersion.api.data.StoredObject;
import us.myles.ViaVersion.api.data.UserConnection;

/**
 * @author MrMicky
 */
public class ChatTracker extends StoredObject {

    private String lastMessage;

    public ChatTracker(UserConnection user) {
        super(user);
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }
}
