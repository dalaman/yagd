import java.io.*;
import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Base64.Encoder;

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
    public final int id;
    private byte firstByte; // HACK: adhoc

    public Session(Socket socket, Integer id) throws IOException {
        this.socket = socket;
        this.is = this.socket.getInputStream();
        BufferedReader in = new BufferedReader(new InputStreamReader(this.is));
        this.os = this.socket.getOutputStream();

        // TODO: use id; e.g. cursor, name...
        this.id = id;

        this.handShake(in, this.os);
        this.start();
    }

    private void logging(String context) {
        System.out.println("[Session " + id + "] " + context);
    }

    public void sendMessageToClient(String message)
        throws IOException, UnsupportedEncodingException {
        byte[] sendHead = new byte[2];
        sendHead[0] = this.firstByte;
        sendHead[1] = (byte) message.getBytes("UTF-8").length;
        this.os.write(sendHead);
        this.os.write(message.getBytes("UTF-8"));
    }

    // make websocket protcol (TCP => http)
    public void handShake(BufferedReader in, OutputStream os) {
        String header = "";
        String key = "";
        try {
            while (!(header = in.readLine()).equals("")) {
                System.out.println(header);
                String[] spLine = header.split(":");
                if (spLine[0].equals("Sec-WebSocket-Key")) {
                    key = spLine[1].trim();
                }
            }
            key += "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
            byte[] keyUtf8 = key.getBytes("UTF-8");
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] keySha1 = md.digest(keyUtf8);
            Encoder encoder = Base64.getEncoder();
            byte[] keyBase64 = encoder.encode(keySha1);
            String keyNext = new String(keyBase64);
            byte[] response = ("HTTP/1.1 101 Switching Protocols\r\n"
                + "Connection: Upgrade\r\n"
                + "Upgrade: websocket\r\n"
                + "Sec-WebSocket-Accept: " + keyNext + "\r\n\r\n")
                                  .getBytes("UTF-8");
            this.os.write(response);
        } catch (IOException e) {
            this.logging("Err: " + e);
        } catch (NoSuchAlgorithmException e) {
            this.logging("Err: " + e);
        }
    }

    // 読み込み待ちするスレッド
    @Override
    public void run() {
        this.logging("New session established : " + this.socket);
        try {
            while (true) {
                byte[] buff = new byte[1024];
                int lineData = this.is.read(buff);
                for (int i = 0; i < lineData - 6; i++) {
                    buff[i + 6] = (byte) (buff[i % 4 + 2] ^ buff[i + 6]);
                }
                String line = new String(buff, 6, lineData - 6, "UTF-8");
                byte[] sendHead = new byte[2];
                sendHead[0] = buff[0];

                this.firstByte = buff[0]; // HACK: adhoc

                sendHead[1] = (byte) line.getBytes("UTF-8").length;

                if (line.equals("END"))
                    break;

                this.logging("From:\t" + this.socket);
                this.logging("Ctx:\t" + line + "\n");
                SessionManager.updateText(line);
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