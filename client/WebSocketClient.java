import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

// Usage @ https://github.com/dalaman/yagd/wiki/client-WebSocketClient.java
public class WebSocketClient {
    private WebSocket webSocket;
    private final String webSocketPort;

    public void sendText(String text) {
        webSocket.sendText(text, true);
    }

    public void closeConnection() {
        webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "close_connection");
    }

    public WebSocketClient(String webSocketPort, Consumer<String> onMessage) {
        this.webSocketPort = webSocketPort;

        HttpClient client = HttpClient.newHttpClient();
        WebSocket.Builder wsb = client.newWebSocketBuilder();

        WebSocket.Listener listener = new WebSocket.Listener() {
            @Override
            public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                System.out.println("onText invoked. new data:");
                System.out.println(data);
                System.out.println("");

                onMessage.accept(data.toString());

                webSocket.request(1);
                return null;
            }
        };

        // connection start
        CompletableFuture<WebSocket> comp =
            wsb.buildAsync(URI.create("ws://localhost:" + webSocketPort + "/ws"), listener);

        // connection done
        // TODO: error handling
        try {
            WebSocket ws;
            ws = comp.get();

            this.webSocket = ws;
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
