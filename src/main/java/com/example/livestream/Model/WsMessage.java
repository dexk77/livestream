package com.example.livestream.Model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class WsMessage {

    private WsMessageType type;
    private int channelId;
    private String userId;
    private Object payload;

public WsMessage(){}
}
