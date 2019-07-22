package fr.mrmicky.viachatfixer.handlers;

import java.util.UUID;

public interface ChatHandler {

    void init() throws Exception;

    String handle(UUID uuid);
}
