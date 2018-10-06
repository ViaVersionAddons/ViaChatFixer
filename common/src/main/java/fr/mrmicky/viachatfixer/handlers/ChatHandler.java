package fr.mrmicky.viachatfixer.handlers;

import java.util.UUID;

/**
 * @author MrMicky
 */
public interface ChatHandler {

    void init() throws Exception;

    String handle(UUID uuid);
}
