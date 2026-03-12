package me.nethma.bookdiary.utils;

/**
 * Represents a single in-app notification entry stored locally.
 */
public class NotificationItem {

    public enum Type {
        RECOMMENDATION,
        REMINDER,
        QUOTE,
        UPDATE
    }

    private final String id;
    private final Type   type;
    private final String title;
    private final String message;
    private final long   timestamp;
    private boolean      read;

    public NotificationItem(String id, Type type, String title, String message,
                            long timestamp, boolean read) {
        this.id        = id;
        this.type      = type;
        this.title     = title;
        this.message   = message;
        this.timestamp = timestamp;
        this.read      = read;
    }

    public String  getId()        { return id; }
    public Type    getType()      { return type; }
    public String  getTitle()     { return title; }
    public String  getMessage()   { return message; }
    public long    getTimestamp() { return timestamp; }
    public boolean isRead()       { return read; }
    public void    setRead(boolean read) { this.read = read; }
}

