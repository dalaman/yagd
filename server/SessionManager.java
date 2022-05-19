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
    }

    // HACK: make clean
    private static void notifyChangesToAllSession(String newText) {
        // TODO 各SessionにModelが変わったことを通知する。
        for (Session session : sessionList) {
            session.sendMessageToClient(newText);
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
                    new Session(socket, /* id= */ clientCount++, (newData) -> {
                        SessionManager.updateText(newData);
                        SessionManager.notifyChangesToAllSession(newData);
                    });
                sessionList.add(newSession);
            }
        } finally {
            serverSocket.close();
        }
    }
}
