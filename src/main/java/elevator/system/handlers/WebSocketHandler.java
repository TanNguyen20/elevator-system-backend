package elevator.system.handlers;

import elevator.system.service.intf.WebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;


@Slf4j
@Component
public class WebSocketHandler extends TextWebSocketHandler {

    private final WebSocketService webSocketService;

    @Autowired
    public WebSocketHandler(WebSocketService webSocketService) {
        this.webSocketService = webSocketService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        webSocketService.registerSession(session.getId(), session);
        log.info("✅ WebSocket connected: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        log.info("📩 Received message: " + message.getPayload());

        // Send a JSON response
        webSocketService.sendMessageToClient(
            session.getId(),
            message.getPayload()
        );
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        webSocketService.removeSession(session.getId());
        log.info("❌ WebSocket disconnected: " + session.getId());
    }
}