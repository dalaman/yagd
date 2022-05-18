import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class WebSocketClient {
    private WebSocket webSocket;

    public void sendText(String text) {
        webSocket.sendText(text, true);
    }

    public void closeConnection() {
        webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "close_connection");
    }

    public WebSocketClient(Consumer<String> onMessage) throws InterruptedException, ExecutionException {
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

        // 接続開始
        CompletableFuture<WebSocket> comp = wsb.buildAsync(URI.create("ws://localhost:8080/ws"), listener);

        // 接続完了
        WebSocket ws = comp.get();

        this.webSocket = ws;
    }
}