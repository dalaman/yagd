import java.io.IOException;
import java.util.Set;

import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

/**
 * Websocket Endpoint implementation class HelloEndPoint
 */

@ServerEndpoint("/helloendpoint")
public class HelloEndPoint {

    // record session
    Session currentSession = null;

    public HelloEndPoint() {
        super();
    }

    /*
     * connection open
     */
    @OnOpen
    public void onOpen(Session session, EndpointConfig ec) {
        this.currentSession = session;
    }

    /*
     * receive massage
     */
    @OnMessage
    public void receiveMessage(String msg) throws IOException {
        // send massage to client
        Set<Session> sessions = currentSession.getOpenSessions();
        for (Session session : sessions) {
            try {
                session.getBasicRemote().sendText(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /*
     * close
     */
    @OnClose
    public void onClose(Session session, CloseReason reason) {
        // ...
    }

    /*
     * Error
     */
    @OnError
    public void onError(Throwable t) {
        // ...
    }
    
}
