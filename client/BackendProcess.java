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


@ServerEndpoint("/helloendpoint")
public class BackendProcess{
    public static final int PORT = 8080;
    private final InetAddress address;
    private final Socket socket;
    private final ContentReceiver listener;
    private final WebSocket websocket;
    
    BackendProcess() throws IOException{
        this.address = InetAddress.getByName("localhost");
        this.socket = new Socket(address, PORT);

        this.listener = new ContentReceiver(socket);
        listener.start();

        this.websocket = new WebSocket();
    }

    public void sendToServer(String message) {
        listener.sendMessageToServer(message);
    }

    public void sendToClient(String message){
        websocket.sendMessageToClient(message);
    }

    public static void main(String[] args) throws IOException {
        BackendProcess backend = new BackendProcess();
        Scanner sc = new Scanner(System.in);
        String message = "";

        try{
            while (true) {
                System.out.println("type your message. to finish, type END");
                message = sc.next();
                backend.sendToServer(message);
    
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


class WebSocket{
    // record session
    static Session currentClient = null;

    public WebSocket() {
        super();
    }

    //BackendProcess <===> Frontend(Client)
    //                 |               
    //              WebSocket
    //Connection Open
    @OnOpen
    public void onOpen(Session client, EndpointConfig ec) {
        currentClient = client;
    }

    //Receive Message from Client ===> Send Message to server
    @OnMessage
    public void receiveMessagefromClient(String msg) throws IOException {
        BackendProcess.sendToServer(msg);
    }

    public void sendMessageToClient(String msg){
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
    
    public ContentReceiver(Socket socket) throws IOException {
        this.socket = socket;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
    }

    public void sendMessageToServer(String message){
        out.println(message);
    }

    @Override
    public void run() {
        try {
            while(true) {
                //Receive Message from Server ===> Send Massage to Client
                String newText = in.readLine();
                if (newText.equals("END")) break;
                System.out.println("new message from server: " + newText);
                BackendProcess.sendToClient(newText);
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

