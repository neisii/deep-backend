package org.example.backendproject.stompwebsocket.controller;

import lombok.RequiredArgsConstructor;
import org.example.backendproject.stompwebsocket.dto.ChatMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatController {

//    @MessageMapping({"chat.sendMessage"})
//    @SendTo("/topic/public")
//    public ChatMessage sendMessage(ChatMessage message) {
//        return message;
//    }

    // 서버가 클라이언트에게 수도으로 메시지를 보낼 수 있도록 하는 클래스
    private final SimpMessagingTemplate template;


    // 동적 방 생성
    @MessageMapping({"chat.sendMessage"})
    public void sendmessage(ChatMessage message) {
        if (message.getTo() != null && !message.getTo().isEmpty()) {
            template.convertAndSendToUser(message.getTo(), "/queue/private", message);
        } else {
            template.convertAndSend("/topic/" + message.getRoomId(), message);
        }

    }
}
