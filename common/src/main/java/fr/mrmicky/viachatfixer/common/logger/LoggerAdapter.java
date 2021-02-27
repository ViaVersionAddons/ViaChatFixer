package fr.mrmicky.viachatfixer.common.logger;

public interface LoggerAdapter {

    void info(String message);

    void warn(String message);

    void warn(String message, Throwable throwable);

    void error(String message);

    void error(String message, Throwable throwable);
}
