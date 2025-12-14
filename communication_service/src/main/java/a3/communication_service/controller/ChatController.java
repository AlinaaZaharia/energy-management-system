package a3.communication_service.controller;

import a3.communication_service.model.ChatMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import a3.communication_service.service.AIService;

@Controller
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);
    private final SimpMessagingTemplate messagingTemplate;
    private final AIService aiService;

    public ChatController(SimpMessagingTemplate messagingTemplate, AIService aiService) {
        this.messagingTemplate = messagingTemplate;
        this.aiService = aiService;
    }

    @MessageMapping("/chat")
    public void processMessage(@Payload ChatMessage chatMessage) {
        log.info("Chat msg from {}: {}", chatMessage.getSenderName(), chatMessage.getContent());
        messagingTemplate.convertAndSend("/topic/admin/messages", chatMessage);
        String autoResponse = checkRules(chatMessage.getContent());

        if (autoResponse != null) {
            try { Thread.sleep(500); } catch (InterruptedException e) {}
            respondToUser(chatMessage.getSenderId(), "Auto-Bot", autoResponse);
        } else {
            String aiResponse = aiService.getAIResponse(chatMessage.getContent());
            respondToUser(chatMessage.getSenderId(), "AI-Assistant", aiResponse);
        }
    }

    @MessageMapping("/chat/admin")
    public void processAdminMessage(@Payload ChatMessage chatMessage) {
        if (chatMessage.getRecipientId() != null) {
            respondToUser(chatMessage.getRecipientId(), "Admin", chatMessage.getContent());
        }
    }

    @MessageMapping("/chat/typing")
    public void typing(@Payload ChatMessage chatMessage) {
        if (!chatMessage.isAdmin()) {
            messagingTemplate.convertAndSend("/topic/admin/typing", chatMessage);
        }
    }

    private void respondToUser(String userId, String senderName, String text) {
        ChatMessage response = new ChatMessage();
        response.setSenderId("SYSTEM");
        response.setSenderName(senderName);
        response.setContent(text);
        response.setAdmin(true);

        messagingTemplate.convertAndSend("/topic/messages/" + userId, response);
    }

    private String checkRules(String message) {
        if (message == null) return null;
        String msg = message.toLowerCase();

        if (msg.contains("hello") || msg.contains("hi") || msg.contains("salut"))
            return "Hello! How can I help you with your energy monitoring?";

        if (msg.contains("bill") || msg.contains("cost") || msg.contains("price"))
            return "Billing is calculated based on hourly consumption. Contact accounting@energy.com.";

        if (msg.contains("consumption") || msg.contains("usage"))
            return "You can see your consumption charts in the 'My Devices' page.";

        if (msg.contains("device") && (msg.contains("add") || msg.contains("new")))
            return "Only administrators can add new devices to your account.";

        if (msg.contains("alert") || msg.contains("notification") || msg.contains("red"))
            return "Red alerts appear when your hourly consumption exceeds the maximum limit set for the device.";

        if (msg.contains("password") || msg.contains("login"))
            return "For password resets, please use the 'Forgot Password' link on the login page.";

        if (msg.contains("limit") || msg.contains("max"))
            return "The maximum hourly limit is configured by the admin to prevent grid overload.";

        if (msg.contains("sensor") || msg.contains("meter"))
            return "Make sure your smart meter is connected to Wi-Fi to sync data.";

        if (msg.contains("contract"))
            return "Your contract ID is available in your profile settings.";

        if (msg.contains("thank") || msg.contains("bye"))
            return "You're welcome! Have a great day!";

        return null;
    }
}