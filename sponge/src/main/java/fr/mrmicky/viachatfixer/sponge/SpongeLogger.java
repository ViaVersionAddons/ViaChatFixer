package fr.mrmicky.viachatfixer.sponge;

import com.google.inject.Inject;
import fr.mrmicky.viachatfixer.common.logger.LoggerAdapter;
import org.slf4j.Logger;

public class SpongeLogger implements LoggerAdapter {

    private final Logger logger;

    @Inject
    public SpongeLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void info(String message) {
        this.logger.info(message);
    }

    @Override
    public void warn(String message) {
        this.logger.warn(message);
    }

    @Override
    public void warn(String message, Throwable throwable) {
        this.logger.warn(message, throwable);
    }

    @Override
    public void error(String message) {
        this.logger.error(message);
    }

    @Override
    public void error(String message, Throwable throwable) {
        this.logger.error(message, throwable);
    }
}
