import java.io.*;
import java.net.*;
import java.util.*;
import java.util.function.Consumer;

public class BackendProcess{
    public static final int PORT = 8081;
    //protected final Socket socket;
    //private final BufferedReader in;
    //private final PrintWriter out;
    private static String TextToServer;
    private static String TextToClient;
    private static ContentReceiver listener;

    public static void setTextToServer(String text){
        TextToServer = text;
    }

    public void setTextToClient(String text){
        TextToClient = text;
    }

    public static void main(String[] args) throws IOException {
        WebSocketClient webSocketClient = new WebSocketClient((text) -> {
            System.out.println("got message from js server: " + text);
            listener.sendMessageToServer(text);
        });

        webSocketClient.sendText("setsuzoku kanryo");

        InetAddress address = InetAddress.getByName("localhost");
        Socket socket = new Socket(address, PORT);

        listener = new ContentReceiver(socket, (text) -> {
            webSocketClient.sendText(text);
        });
        listener.start();
    }
}
    
//Session <===> BackendProcess (受信用)
class ContentReceiver extends Thread {
    Socket socket;
    BufferedReader in;
    PrintWriter out;
    Consumer<String> onMessage;

    public ContentReceiver(Socket socket, Consumer<String> onMessage) throws IOException {
        this.socket = socket;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
        this.onMessage = onMessage;
    }

    public void sendMessageToServer(String message) {
        this.out.println(message);
    }

    @Override
    public void run() {
        try {
            while(true) {
                String newText = in.readLine();
                if (newText.equals("END")) break;
                System.out.println("new message from server: " + newText);
                onMessage.accept(newText);
            }
        } catch(Exception e) {
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
