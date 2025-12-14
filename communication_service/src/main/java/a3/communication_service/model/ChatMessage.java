package a3.communication_service.model;

public class ChatMessage {
    private String senderId;
    private String senderName;
    private String content;
    private boolean admin;
    private String recipientId;

    public ChatMessage() {}

    public ChatMessage(String senderId, String senderName, String content, boolean admin, String recipientId) {
        this.senderId = senderId;
        this.senderName = senderName;
        this.content = content;
        this.admin = admin;
        this.recipientId = recipientId;
    }


    public String getSenderId() {
        return senderId;
    }
    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }
    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }

    public boolean isAdmin() {
        return admin;
    }
    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public String getRecipientId() {
        return recipientId;
    }
    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }
}