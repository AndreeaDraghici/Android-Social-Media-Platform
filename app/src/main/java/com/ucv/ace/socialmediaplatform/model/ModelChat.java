package com.ucv.ace.socialmediaplatform.model;

/**
 * This class represents a chat message model.
 * It contains the sender, receiver, message, whether the message has been seen or not, and the timestamp of the message.
 */
public class ModelChat {

    private String sender;
    private String receiver;
    private String message;
    private boolean isSeen;

    private String timestamp;

    private String type;

    /**
     * Constructor for the ModelChat class.
     * @param sender The sender of the message.
     * @param receiver The receiver of the message.
     * @param message The message.
     * @param isSeen Whether the message has been seen or not.
     * @param timestamp The timestamp of the message.
     * @param type The type of the message.
     */

    public ModelChat(String sender, String receiver, String message, boolean isSeen, String timestamp, String type) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.isSeen = isSeen;
        this.timestamp = timestamp;
        this.type = type;
    }

    public ModelChat() {
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSeen() {
        return isSeen;
    }

    public void setSeen(boolean seen) {
        this.isSeen = seen;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
