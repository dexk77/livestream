package com.example.livestream.config;


import com.example.livestream.websocket.SignallingWebSocketHandler;
import com.example.livestream.websocket.ChannelSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {


    private final SignallingWebSocketHandler handler;
    private final ChannelSocketHandler channelhandler;

    public WebSocketConfig(SignallingWebSocketHandler handler, ChannelSocketHandler channelhandler) {
        this.handler = handler;
        this.channelhandler=channelhandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {

        registry
                .addHandler(handler,"/ws/signalling")
                        .setAllowedOrigins("*");

        registry.addHandler(channelhandler,"ws/channel")
                .setAllowedOrigins("*");
    }
}
