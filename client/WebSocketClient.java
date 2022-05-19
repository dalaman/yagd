import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class WebSocketClient {
    
    /**
     * インスタンス作り方、メッセージ受信時の処理の書き方：
     * コンストラクタにメッセージが来た時の処理を表す関数を渡す。引数は文字列1つで戻り値はvoid。プロBでやってる高階関数みたいな感じ。
     * 
     * 例：
     * WebSocketClient hoge = new WebSocketClient( (text) -> {
     *     // ここに処理を書く。textには受け取ったメッセージが入ってる。
     *     System.out.println(text); //この場合、フロントからメッセージを受け取るたびにその内容を標準出力にプリントすることになる。
     * } );
     * 
     * 
     * メッセージ送り方：
     * sendText()関数を呼び出すだけ。
     * 
     * 例：
     * hoge.sendText("aaaaaaaaaaaaaaaaaaaaa"); // aaaaaaaaaaaaaaaaaaaaaaaがフロントに送られる。
     */

    private WebSocket webSocket;

    public void sendText(String text) {
        webSocket.sendText(text, true);
    }

    public void closeConnection() {
        webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "close_connection");
    }

    public WebSocketClient(Consumer<String> onMessage) {
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
        // エラー握りつぶしまくってるので良くない
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