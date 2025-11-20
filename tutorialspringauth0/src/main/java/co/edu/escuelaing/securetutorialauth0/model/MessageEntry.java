package co.edu.escuelaing.securetutorialauth0.model;

public class MessageEntry {
    private String message;
    private String clientIp;
    private String timestamp;

    public MessageEntry() {}

    public MessageEntry(String message, String clientIp, String timestamp) {
        this.message = message;
        this.clientIp = clientIp;
        this.timestamp = timestamp;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getClientIp() { return clientIp; }
    public void setClientIp(String clientIp) { this.clientIp = clientIp; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}