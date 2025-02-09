package elevator.system.service.intf;

import org.springframework.web.socket.WebSocketSession;

public interface WebSocketService {

    void registerSession(String sessionId, WebSocketSession session);

    void removeSession(String sessionId);

    void sendMessageToClient(String sessionId, Object messageObject);

    void sendMessageToAllClients(Object messageObject);
}