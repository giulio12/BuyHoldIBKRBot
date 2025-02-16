package ch.ibkrbot;

public interface Observer {
    void notify(String notificationName, Object data);
}
