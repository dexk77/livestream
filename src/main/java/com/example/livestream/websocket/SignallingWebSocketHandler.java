package com.example.livestream.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;


@Component
public class SignallingWebSocketHandler extends TextWebSocketHandler {

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // Handle new WebSocket connection

        System.out.println("WebSocket Connection Established Successfully:" + session.getId());
        session.sendMessage(new TextMessage("echo:Connection Successfull from backened"));

    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // Handle incoming text messages
        System.out.println("Received message: " + message.getPayload() + " from session: " + session.getId());

        // Echo the message back to the client
        session.sendMessage(new TextMessage("Echo: " + message.getPayload()));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // Handle connection closed
        System.out.println("WebSocket Connection Closed: " + session.getId());
    }

}
