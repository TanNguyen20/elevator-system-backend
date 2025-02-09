package elevator.system.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import elevator.system.service.intf.WebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class WebSocketServiceImpl implements WebSocketService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void registerSession(String sessionId, WebSocketSession session) {
        sessions.put(sessionId, session);
        log.info("✅ Registered WebSocket session: {}", sessionId);
    }

    @Override
    public void removeSession(String sessionId) {
        sessions.remove(sessionId);
        log.info("❌ Removed WebSocket session: {}", sessionId);
    }

    @Override
    public void sendMessageToClient(String sessionId, Object messageObject) {
        WebSocketSession session = sessions.get(sessionId);
        if (session != null && session.isOpen()) {
            try {
                String jsonMessage = objectMapper.writeValueAsString(messageObject);
                session.sendMessage(new TextMessage(jsonMessage));
                log.info("📤 Sent JSON message to {}: {}", sessionId, jsonMessage);
            } catch (IOException e) {
                log.error("❌ Error sending WebSocket message to {}: {}", sessionId, e.getMessage());
            }
        } else {
            log.warn("⚠️ WebSocket session {} is not available", sessionId);
        }
    }

    @Override
    public void sendMessageToAllClients(Object messageObject) {
        String jsonMessage;
        try {
            jsonMessage = objectMapper.writeValueAsString(messageObject);
        } catch (IOException e) {
            log.error("❌ Error converting message to JSON: {}", e.getMessage());
            return;
        }

        for (WebSocketSession session : sessions.values()) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(jsonMessage));
                    log.info("📤 Sent JSON message to {}: {}", session.getId(), jsonMessage);
                } catch (IOException e) {
                    log.error("❌ Error sending WebSocket message to {}: {}", session.getId(), e.getMessage());
                }
            }
        }
    }
}