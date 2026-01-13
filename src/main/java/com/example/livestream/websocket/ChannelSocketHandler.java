package com.example.livestream.websocket;

import ch.qos.logback.core.testUtil.TeeOutputStream;
import com.example.livestream.Model.WsMessage;
import com.example.livestream.Model.WsMessageType;
import com.example.livestream.Service.ChannelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JsonParseException;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


@Component
public class ChannelSocketHandler extends TextWebSocketHandler{

    @Autowired
    private ChannelService channelService;
    private final ObjectMapper objectMapper=new ObjectMapper();

    private final Map<Integer, Set<WebSocketSession>> channelsessions=new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {

        System.out.println("Connection Established");
        session.sendMessage(new TextMessage("Echo from ChannelSocketHandler : Connection Successfull"));

    }
    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {

        WsMessage  wsmessage = null;
        try{
            wsmessage=objectMapper.readValue((String)message.getPayload(),WsMessage.class);


        }catch (Exception e){

            sendError(session,-1,null,"Invalid message type");
            return;
        }

        if(wsmessage==null){

            sendError(session,-1,null,"Message Type Missing");
            return;
        }
        int channelId= wsmessage.getChannelId();
        String userId= wsmessage.getUserId();


        switch (wsmessage.getType()){

            case JOIN -> HandleJoin( session,channelId,userId);
            case LEAVE -> handleLeave(session,channelId,userId);
            case START_STREAM -> CheckStreamActive(session,channelId, userId);
            case END_STREAM -> channelService.StopStream(channelId);
            case STATE_UPDATE -> broadCastState(channelId);
            default->sendError(session,channelId,userId,"Unsupported Message Type");


        }


    }

    private boolean CheckStreamActive(WebSocketSession session,int channelId,String userId) throws IOException{

       boolean check= channelService.startStream(channelId,userId);

       if(!check){
           session.sendMessage(new TextMessage("From ChannelSsocketHandler:Already Active User"));
       }

       broadCastState(channelId);
       return check;
    }

    private void HandleJoin(WebSocketSession session,int channelId,String userId) throws IOException {
        Boolean joined=channelService.addViewer(channelId);

        if(!joined){
            sendError(session,channelId,userId,"channel Full");
        }

        channelsessions.computeIfAbsent(channelId,k->ConcurrentHashMap.newKeySet()).add(session); // *

        broadCastState(channelId);
    }
    private void handleLeave(WebSocketSession session,int ChannelId,String userId)  throws IOException{

        boolean userleft=channelService.deleteViewer(ChannelId);

        Set<WebSocketSession> sessions=channelsessions.get(ChannelId);
        if(sessions!=null){
            sessions.remove(session);
        }
        broadCastState(ChannelId);
    }

    private void broadCastState(int channelId) throws IOException {

        Set<WebSocketSession> session=channelsessions.get(channelId);

        System.out.println(session);
        if( session==null || session.isEmpty() ){

            System.out.println("There are no Current Availabler sessions to show");
            return ;
        }
        Map<Object,Object> state=channelService.getstate(channelId);
        WsMessage message=new WsMessage(WsMessageType.STATE_UPDATE,channelId,null,state);

        for(WebSocketSession s:session){

            if(s.isOpen()){

                System.out.println("In Sprinboot:Console"+new TextMessage(objectMapper.writeValueAsString(message)));

                s.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
            }
        }




    }

    private void sendError(WebSocketSession session,int channelId,String userId,String ErrorMessage) throws IOException{

        WsMessage wsMessage=new WsMessage(WsMessageType.ERROR,channelId,userId,ErrorMessage);

         session.sendMessage(new TextMessage(objectMapper.writeValueAsString(wsMessage)));
        System.out.println("sendError Executed");
    }


    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception{

        for(Set<WebSocketSession>  s:channelsessions.values()){

            s.remove(session);
        }
        System.out.println("Channel Webssocket closed "+session.getId());

    }

    @Override
    public boolean supportsPartialMessages() {
        return false;


    }
}
