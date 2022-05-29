import java.io.*;
import java.net.*;
import java.util.ArrayList;

// SessionManager
//     * main server
//
//     * watch all connection trials
//
//     * own session instance list
//
//     * if connected, create new session in new thread & append to sessionList
//
//     * if one session got changed => notify this change for all sessions

public class SessionManager {
    public static final int PORT = 8081; // FIXME: port num

    private static int clientCount = 0;
    private static ArrayList<Session> sessionList = new ArrayList<>();
    private static IModel model = new IModel();

    private static void logging(String context) {
        System.out.println("[SessionManager] " + context);
    }

    public static void deleteSession(Session removeTarget) {
        boolean removed = sessionList.remove(removeTarget);
        if (removed) {
            SessionManager.logging("successfully deleted session: " + removeTarget);
        } else {
            SessionManager.logging("failed to remove session from the session list");
        }
    }

    synchronized public static void updateModel(String newText, int id) {
        if (MessageParser.extractMessageTypeFromJson(newText) == MessageType.TEXT) {
            model.updateTEXT(newText);
            SessionManager.notifyChangesToAllSession(new String(model.TEXT));
        } else if (MessageParser.extractMessageTypeFromJson(newText) == MessageType.CHAT) {
            SessionManager.notifyChangesToAllSession(newText);
        } else if (MessageParser.extractMessageTypeFromJson(newText) == MessageType.CURSOR) {
            model.updateCURSOR(newText, id);
            SessionManager.notifyChangesToAllSession(newText);
        } else if (MessageParser.extractMessageTypeFromJson(newText) == MessageType.EXIT) {
            // ...
        } else if (MessageParser.extractMessageTypeFromJson(newText) == MessageType.ERROR) {
            // ...
        }
    }

    // For all Sessions: Send Changes
    private static void notifyChangesToAllSession(String message) {
        for (Session session : sessionList) {
            session.sendMessageToClient(message);
        }
    }

    // For new Session: Send Model
    private static void sendModelToNewSession(String[] model, Session newSession) {
        for (int i = 0; i < model.length; i++) {
            newSession.sendMessageToClient(model[i]);
        }
    }

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);

        SessionManager.logging("Server launched at " + serverSocket);

        try {
            // TODO:
            // ayasii
            while (true) {
                Socket socket = serverSocket.accept();
                SessionManager.logging("Connection accepted: " + socket);
                final int clientCountNow = clientCount++;
                Session newSession = new Session(socket, /* id= */ clientCountNow, (newData) -> {
                    // add these lines after initializing newSession.
                    //
                    // if (MessageParser.extractMessageTypeFromJson(newData) == MessageType.EXIT) {
                    //     deleteSession(newSession);
                    // }
                    SessionManager.updateModel(newData, clientCountNow);
                    SessionManager.notifyChangesToAllSession(newData);
                });

                newSession.onReceiveMessage = (newData) -> {
                    if (MessageParser.extractMessageTypeFromJson(newData) == MessageType.EXIT) {
                        deleteSession(newSession);
                    }
                    SessionManager.updateModel(newData, clientCountNow);
                    SessionManager.notifyChangesToAllSession(newData);
                };

                sessionList.add(newSession);
                SessionManager.sendModelToNewSession(model.outputModel(), newSession); // Send Model
            }
        } finally {
            serverSocket.close();
        }
    }
}
