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
    public static final int PORT = 8080; // FIXME: port num

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

    public static void updateModel(String newText, int id) {
        if(MessageParser.extractMessageTypeFromJson(newText) == MessageType.TEXT){
            model.updateTEXT(newText);
            SessionManager.notifyChangesToAllSession(new String(model.TEXT));
        }else if(MessageParser.extractMessageTypeFromJson(newText) == MessageType.CHAT){
            SessionManager.notifyChangesToAllSession(newText);
        }else if(MessageParser.extractMessageTypeFromJson(newText) == MessageType.CURSOR){
            model.updateCURSOR(newText,id);
            SessionManager.notifyChangesToAllSession(newText);
        }else if(MessageParser.extractMessageTypeFromJson(newText) == MessageType.ERROR){
            // ...
        }
    }


    // For all Sessions: Send Changes
    private static void notifyChangesToAllSession(String message) {
        for (Session session : sessionList) {
            try {
                session.sendMessageToClient(message);
            } catch (UnsupportedEncodingException e) {
                SessionManager.logging("Err: " + e);
            } catch (IOException e) {
                SessionManager.logging("Err: " + e);
            }
        }
    }


    //For new Session: Send Model
    private static void sendModelToNewSession(String[] model, Session newSession){
        for(int i=0; i<model.length; i++){
            try{
                newSession.sendMessageToClient(model[i]);
            } catch(UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);

        SessionManager.logging("Server launched at " + serverSocket);

        try {
            // TODO:
            // ここ怪しい。Ctrl-Cとかで強制終了した時にちゃんとServerSocket.close()が呼ばれるかとか知らずに書いてる。
            while (true) {
                Socket socket = serverSocket.accept();
                SessionManager.logging("Connection accepted: " + socket);
                Session newSession = new Session(socket, /* ID */ SessionManager.sessionList.size());
                sessionList.add(newSession);
                SessionManager.sendModelToNewSession(model.outputModel(), newSession); // Send Model
            }
        } finally {
            serverSocket.close();
        } 
    }
}
