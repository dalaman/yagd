import java.io.*;
import java.net.*;
import java.util.*;

import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;


public class BackendProcess{
    public static final int PORT = 8080;
    //protected final Socket socket;
    //private final BufferedReader in;
    //private final PrintWriter out;
    private static String TextToServer;
    private static String TextToClient;

    public static void setTextToServer(String text){
        TextToServer = text;
    }

    public void setTextToClient(String text){
        TextToClient = text;
    }

    public static void main(String[] args) throws IOException {
        WebSocket websocket = new WebSocket();

        Text text = new Text();

        InetAddress address = InetAddress.getByName("localhost");
        Socket socket = new Socket(address, PORT);

        ContentReceiver listener = new ContentReceiver(socket);
        listener.start();

        Scanner sc = new Scanner(System.in);
        String message = "";

        try{
            while (true) {
                System.out.println("type your message. to finish, type END");
                message = sc.next();
                setTextFromClient(message);
                listener.sendMessageToServer(message);
    
                if (message.equals("END")) {
                    break;
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            sc.close();
        }
    }
}
    

class Text{
    private String Text;

    void setText(String Text){
        this.Text = Text;
        listener.sendMessageToClient(Text);
    }

    String getText(){
        return Text;
    }
}

//BackendProcess <===> Frontend(Client)
//                 |               
//              WebSocket
@ServerEndpoint("/helloendpoint")
class WebSocket {

    // record session
    Session currentClient = null;

    public WebSocket() {
        super();
    }

    //Connection Open
    @OnOpen
    public void onOpen(Session client, EndpointConfig ec) {
        this.currentClient = client;
    }

    //Receive Message
    @OnMessage
    public void receiveMessagefromClient(String msg) throws IOException {
        
        
        // send message to client
        Set<Session> clients = currentClient.getOpenSessions();
        for (Session client : clients) {
            try {
                client.getBasicRemote().sendText(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void sendMessageToClient(String msg){
        try {
            currentClient.getBasicRemote().sendText(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //close
    @OnClose
    public void onClose(Session session, CloseReason reason) {
        // ...
    }

    //error
    @OnError
    public void onError(Throwable t) {
        // ...
    }
    
}

//Session <===> BackendProcess (受信用)
class ContentReceiver extends Thread {
    Socket socket;
    BufferedReader in;
    PrintWriter out;
    private Text text;

    public ContentReceiver(Socket socket) throws IOException {
        this.socket = socket;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
    }

    public void sendMessageToServer(String message) {
        this.out.println(message);
    }

    void shareText(Text text){
        this.text = text;
    }

    @Override
    public void run() {
        try {
            while(true) {
                String newText = in.readLine();
                if (newText.equals("END")) break;
                System.out.println("new message from server: " + newText);
                text.setText(newText);
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

