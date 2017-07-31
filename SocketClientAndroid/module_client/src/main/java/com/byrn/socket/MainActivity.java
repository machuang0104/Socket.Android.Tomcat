package com.byrn.socket;


import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private Button mConnectBtn;
    private TextView mDataReceivedView;
    private EditText mSocketIpEt, mSocketPortEt, mSocketDataEt;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mConnectBtn = (Button) this.findViewById(R.id.socket_connect);
        mSocketIpEt = (EditText) this.findViewById(R.id.socket_ip_et);
        mSocketPortEt = (EditText) this.findViewById(R.id.socket_port_et);
        mSocketDataEt = (EditText) this.findViewById(R.id.socket_data_et);
        mDataReceivedView = (TextView) this.findViewById(R.id.data_received);

        mDataReceivedView.setOnClickListener(mOnClickListener);
        mConnectBtn.setOnClickListener(mOnClickListener);
        findViewById(R.id.socket_send).setOnClickListener(mOnClickListener);
        refreshView(false);
//        mSocketIpEt.setText("218.145.64.217");
        mSocketIpEt.setText("218.145.64.217");
        mSocketPortEt.setText("31260/machuang");
        mSocketDataEt.setText("Machuang_Byron");
    }

    private void refreshView(final boolean isConnected) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                mSocketPortEt.setEnabled(!isConnected);
                mSocketIpEt.setEnabled(!isConnected);
                mConnectBtn.setText(isConnected ? R.string.socket_status_disconnect : R.string.socket_status_connected);
            }
        });
    }

    OnClickListener mOnClickListener = v -> {
        switch (v.getId()) {
            case R.id.socket_connect:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        switchConnect();
                    }
                }).start();
                break;
            case R.id.socket_send:
                sendData();
                break;
            case R.id.data_received:
                mDataReceivedView.setText("");
                break;
        }
    };

    private void switchConnect() {
        if (mTcpClient.isConnected()) {
            mTcpClient.disconnect();
        } else {
            String hostIP = mSocketIpEt.getText().toString();
            int port = Integer.parseInt(mSocketPortEt.getText().toString());
            mTcpClient.connect(hostIP, port);
        }
    }

    private void sendData() {
        try {
            String data = mSocketDataEt.getText().toString();
            if (mTcpClient.isConnected()){
                mTcpClient.getTransceiver().send(data);
            }else {
                Log.e("tag_event","SendData : Tcp Disconnected!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Handler handler = new Handler(Looper.getMainLooper());
    private TcpClient mTcpClient = new TcpClient() {

        @Override
        public void onConnect(SocketTransceiver transceiver) {
            refreshView(true);
        }

        @Override
        public void onDisconnect(SocketTransceiver transceiver) {
            refreshView(false);
        }

        @Override
        public void onConnectFailed() {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Log.d(EVENT, "Connect Error");
                }
            });
        }

        @Override
        public void onReceive(SocketTransceiver transceiver, final String s) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    mDataReceivedView.append(s);
                }
            });
        }
    };

    @Override
    public void onStop() {
        mTcpClient.disconnect();
        super.onStop();
    }

    private static final String EVENT = "tag_event";
}