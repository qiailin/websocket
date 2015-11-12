package unuuu.com.websocket;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketConnectionHandler;
import de.tavendo.autobahn.WebSocketException;

public class MainActivity extends AppCompatActivity {
    private static final String WEB_SOCKET_URL = "ws://websocket-java.herokuapp.com";
    private static final String TAG = "MainActivity";

    private WebSocketConnection mConnection = new WebSocketConnection();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public void onResume() {
        super.onResume();

        try {
            this.mConnection.connect(WEB_SOCKET_URL, new WebSocketConnectionHandler() {
                @Override
                public void onOpen() {
                    Log.d(TAG, "Status: Connected to " + WEB_SOCKET_URL);
                }

                @Override
                public void onTextMessage(String payload) {
                    Log.d(TAG, "Got echo: " + payload);
                }

                @Override
                public void onClose(int code, String reason) {
                    Log.d(TAG, "Connection lost: " + reason);
                }
            });
        } catch (WebSocketException e) {
            Log.d(TAG, e.toString());
        }
    }

    @Override
    public void onPause() {
        this.mConnection.disconnect();

        super.onPause();
    }
}
