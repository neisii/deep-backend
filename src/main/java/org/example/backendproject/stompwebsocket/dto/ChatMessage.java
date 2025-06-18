package org.example.backendproject.stompwebsocket.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    
    private String message;
    private String roomId;
    
    private String from; // 보낸 사람
    private String to; // 받은 사람
}
