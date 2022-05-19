import java.io.*;
import java.net.*;
import java.util.function.Consumer;

// Session
//     * connected to client
//
//     * if received, send ctx to SessionManager
//
//     * SessionManager <===> Session <===> Client
//                        |             |
//                   TCP(Socket)    http(WebSocket)

class Session extends Thread {
    protected Socket socket;
    protected PrintWriter out;
    protected BufferedReader in;
    final Consumer<String> onReceiveMessage; // ラムダ式でメッセージ受け取り時に実行するメソッドを渡す。
    public final int id;

    public Session(Socket socket, int id, Consumer<String> onReceiveMessage) throws IOException {
        this.socket = socket;
        this.out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.id = id;
        this.onReceiveMessage = onReceiveMessage;

        this.start();
    }

    public void sendMessageToClient(String message) {
        out.println(message);
    }

    public void closeSession() {
        System.out.println("closing connection. id: " + this.id);
        try {
            socket.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    // 読み込み待ちするスレッド
    @Override
    public void run() {
        System.out.println("Established new session: " + this.socket);
        try {
            while(true) {
                String newText = in.readLine();
                if (newText.equals("END")) break;

                System.out.println("received new message on " + socket);
                System.out.println("content: " + newText);
                onReceiveMessage.accept(newText);
            }
        } catch(Exception e) {
            System.out.println("an error occurred.");
            e.printStackTrace();
        } finally {
            System.out.println("closing connection " + socket);
            try {
                SessionManager.deleteSession(this);
                socket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                // FIXME tekitou error handling
                e.printStackTrace();
            }
        }
    }
}