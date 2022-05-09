import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ClientTester extends Thread {
    public static final int PORT = 8080;

    public static void main(String[] args) throws IOException {
        InetAddress address = InetAddress.getByName("localhost");
        Socket socket = new Socket(address, PORT);

        ContentReceiver contentReceiver = new ContentReceiver(socket);
        Thread listener = new Thread(contentReceiver);
        listener.start();

        Scanner sc = new Scanner(System.in);
        String message = "";

        while (true) {
            System.out.println("type your message. to finish, type END");
            message = sc.next();
            contentReceiver.sendMessage(message);

            if (message.equals("END")) {
                break;
            }
        }

        sc.close();
    }
}

class ContentReceiver implements Runnable {
    Socket socket;
    BufferedReader in;
    PrintWriter out;

    public ContentReceiver(Socket socket) throws IOException {
        this.socket = socket;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(
            new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
    }

    public void sendMessage(String message) {
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
