import java.io.*;
import java.net.*;
/*
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Base64.Encoder;
*/

// Session
//     * connected to client
//
//     * if received, send ctx to SessionManager
//
//     * SessionManager <===> Session <===> Client
//                        |             |
//                   TCP(Socket)    http(WebSocket)

class Session extends Thread {
    protected final Socket socket;
    private final InputStream is;
    private final OutputStream os;
    private final BufferedReader in;
    public final int id;
    //private byte firstByte; // HACK: adhoc

    public Session(Socket socket, Integer id) throws IOException {
        this.socket = socket;
        this.is = this.socket.getInputStream();
        this.in = new BufferedReader(new InputStreamReader(this.is));
        this.os = this.socket.getOutputStream();

        // TODO: use id; e.g. cursor, name...
        this.id = id;

        //this.handShake(in, this.os);
        this.start();
    }

    private void logging(String context) {
        System.out.println("[Session " + id + "] " + context);
    }

    public void sendMessageToClient(String message)
        throws IOException, UnsupportedEncodingException {
        this.os.write(message.getBytes("UTF-8"));
    }

    // 読み込み待ちするスレッド
    @Override
    public void run() {
        this.logging("New session established : " + this.socket);
        try {
            while (true) {
                String line = in.readLine();
                if (line.equals("END"))
                    break;

                this.logging("From:\t" + this.socket);
                this.logging("Ctx:\t" + line + "\n");
                SessionManager.updateModel(line, this.id);
            }
        } catch (Exception e) {
            this.logging("Err: " + e);
            e.printStackTrace();
        } finally {
            this.logging(this.socket + "closing...");
            try {
                SessionManager.deleteSession(this);
                this.socket.close();
            } catch (IOException e) {
                this.logging("Err: " + e);
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}