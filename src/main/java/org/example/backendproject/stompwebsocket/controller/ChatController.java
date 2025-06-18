package org.example.backendproject.stompwebsocket.controller;

import org.example.backendproject.stompwebsocket.dto.ChatMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    @MessageMapping({"chat.sendMessage"})
    @SendTo("/topic/public")
    public ChatMessage sendMessage(ChatMessage message) {

        return message;
    }
}
