import java.io.*;
import java.net.*;
import java.util.*;
import java.util.function.Consumer;

public class BackendProcess {
    public static final int SOCKET_PORT = 8081;
    // protected final Socket socket;
    // private final BufferedReader in;
    // private final PrintWriter out;
    private static String TextToServer;
    private static String TextToClient;
    private static ContentReceiver listener;

    public static void setTextToServer(String text) {
        TextToServer = text;
    }

    public void setTextToClient(String text) {
        TextToClient = text;
    }

    private static void logging(String context) {
        System.out.println("[BackendProcess] " + context);
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            BackendProcess.logging(
                "\nErr\nUsage: java BackendProcess [websocket_port] [socket_url] [socket_port]");
            System.exit(1);
        }
        final String webSocketPort = args[0];
        final String socketUrl = args[1];
        int socketPort;
        try {
            socketPort = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            logging("illegal number was given as socket_port. use default port 8081");
            socketPort = SOCKET_PORT;
        }

        System.out.println(webSocketPort);

        WebSocketClient webSocketClient = new WebSocketClient(webSocketPort, (text) -> {
            System.out.println("got message from js server: " + text);
            listener.sendMessageToServer(text);
        });

        webSocketClient.sendText("{\"INFO\":\"BackendProcess connected\"}");

        // InetAddress address = InetAddress.getByName("localhost");
        InetAddress address = InetAddress.getByName(socketUrl);
        Socket socket = new Socket(address, socketPort);

        listener = new ContentReceiver(socket, (text) -> { webSocketClient.sendText(text); });
        listener.start();
    }
}

// Session <===> BackendProcess (for receive)
class ContentReceiver extends Thread {
    Socket socket;
    BufferedReader in;
    PrintWriter out;
    Consumer<String> onMessage;

    public ContentReceiver(Socket socket, Consumer<String> onMessage) throws IOException {
        this.socket = socket;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(
            new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
        this.onMessage = onMessage;
    }

    public void sendMessageToServer(String message) {
        this.out.println(message);
    }

    @Override
    public void run() {
        try {
            while (true) {
                String newText = in.readLine();
                if (newText.equals("END"))
                    break;
                System.out.println("new message from server: " + newText);
                onMessage.accept(newText);
            }
        } catch (Exception e) {
            System.out.println("an error occurred.");
            e.printStackTrace();
        } finally {
            System.out.println("closing connection " + socket);
            try {
                socket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
