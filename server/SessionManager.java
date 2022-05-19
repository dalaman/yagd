import java.io.*;
import java.net.*;
import java.security.NoSuchAlgorithmException;
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

    private static ArrayList<Session> sessionList = new ArrayList<>();
    private static String model = "";

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

    public static void updateText(String newText) {
        SessionManager.model = newText;
        SessionManager.notifyChangesToAllSession();
    }

    // HACK: make clean
    private static void notifyChangesToAllSession() {
        // TODO 各SessionにModelが変わったことを通知する。
        for (Session session : sessionList) {
            try {
                session.sendMessageToClient(SessionManager.model);
            } catch (UnsupportedEncodingException e) {
                SessionManager.logging("Err: " + e);
            } catch (IOException e) {
                SessionManager.logging("Err: " + e);
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
                Session newSession =
                    new Session(socket, /* id= */ SessionManager.sessionList.size());
                sessionList.add(newSession);
            }
        } finally {
            serverSocket.close();
        }
    }
}
