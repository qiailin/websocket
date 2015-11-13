package unuuu.com.websocket;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.phoenixframework.channels.Envelope;
import org.phoenixframework.channels.IErrorCallback;
import org.phoenixframework.channels.IMessageCallback;
import org.phoenixframework.channels.Socket;

import java.io.IOException;

import de.tavendo.autobahn.WebSocketConnection;

public class MainActivity extends AppCompatActivity {
    private static final String WEB_SOCKET_URL = "ws://192.168.137.179:4000/socket/websocket";
    private static final String TAG = "MainActivity";

    private final WebSocketConnection mConnection = new WebSocketConnection();
    private Socket mSocket;
    private org.phoenixframework.channels.Channel mChannel;
    private EditText mEditText;
    private Button mButton;
    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.mEditText = (EditText)findViewById(R.id.activity_main_frame_001);
        this.mButton = (Button)findViewById(R.id.activity_main_frame_002);
        this.mTextView = (TextView)findViewById(R.id.activity_main_frame_003);

        this.mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = mEditText.getText().toString();

                if (!text.isEmpty()){
                    try {
                        ObjectNode node = new ObjectNode(JsonNodeFactory.instance)
                            .put("user", "Android")
                            .put("body", text);
                        mChannel.push("send_message", node);
                        mEditText.setText("");
                    } catch (IOException e){

                    }
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        try {
            mSocket = new Socket(WEB_SOCKET_URL);
            mSocket.connect();
            mChannel = mSocket.chan("rooms:lobby", null);
            mChannel.join()
                    .receive("ignore", new IMessageCallback() {
                        @Override
                        public void onMessage(Envelope envelope) {
                            Log.d(TAG, "auth error");
                            Log.d(TAG, envelope.toString());
                        }
                    })
                    .receive("ok", new IMessageCallback() {
                        @Override
                        public void onMessage(Envelope envelope) {
                            Log.d(TAG, "join ok");
                            Log.d(TAG, envelope.toString());
                        }
                    });

            mChannel.on("receive_message", new IMessageCallback() {
                @Override
                public void onMessage(Envelope envelope) {
                    Log.d(TAG, "receive_message");
                    Log.d(TAG, envelope.toString());

                    if (!envelope.getPayload().has("message")) {
                        return;
                    }

                    final String message = envelope.getPayload().get("message").asText();

                    Log.d(TAG, "received message: " + message);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String text = mTextView.getText().toString();
                            text = message + '\n' + text;
                            mTextView.setText(text);
                        }
                    });

                }
            });

            mChannel.onClose(new IMessageCallback() {
                @Override
                public void onMessage(Envelope envelope) {
                    Log.d(TAG, "Channel Closed");
                }
            });

            mChannel.onError(new IErrorCallback() {
                @Override
                public void onError(String reason) {
                    Log.d(TAG, reason);
                }
            });

        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }
    }

    @Override
    public void onPause() {
        if (this.mConnection.isConnected()) {
            this.mConnection.disconnect();
        }

        super.onPause();
    }
}
