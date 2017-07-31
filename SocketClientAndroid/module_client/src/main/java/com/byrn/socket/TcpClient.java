package com.byrn.socket;

import java.net.InetAddress;
import java.net.Socket;

/**
 * TCP Socket客户端
 *
 * @author machuang
 * @since 2017-06-20
 */
public abstract class TcpClient implements Runnable {

    private int port;
    private String hostIP;
    private boolean connect = false;
    private SocketTransceiver transceiver;

    /**
     * @param hostIP 服务器地址
     * @param port   端口
     */
    public void connect(String hostIP, int port) {
        this.hostIP = hostIP;
        this.port = port;
        new Thread(this).start();
    }

    @Override
    public void run() {
        try {
            Socket socket = new Socket(hostIP, port);
            transceiver = new SocketTransceiver(socket) {

                @Override
                public void onReceive(InetAddress addr, String s) {
                    TcpClient.this.onReceive(this, s);
                }

                @Override
                public void onDisconnect(InetAddress addr) {
                    connect = false;
                    TcpClient.this.onDisconnect(this);
                }
            };
            transceiver.start();
            connect = true;
            this.onConnect(transceiver);
        } catch (Exception e) {
            e.printStackTrace();
            this.onConnectFailed();
        }
    }

    /**
     * 连接断开，回调{@code onDisconnect()}
     */
    public void disconnect() {
        if (transceiver != null) {
            transceiver.stop();
            transceiver = null;
        }
    }

    /**
     * @return 当前处于连接状态，则返回true
     */
    public boolean isConnected() {
        return connect;
    }

    /**
     * 获取Socket收发器
     *
     * @return 未连接则返回null
     */
    public SocketTransceiver getTransceiver() {
        return isConnected() ? transceiver : null;
    }

    /**
     * 连接建立
     *
     * @param transceiver SocketTransceiver对象
     */
    public abstract void onConnect(SocketTransceiver transceiver);

    /**
     * 连接建立失败
     */
    public abstract void onConnectFailed();

    /**
     * 接收到数据
     * 注意：此回调是在新线程中执行的

     * @param transceiver SocketTransceiver对象
     * @param s           字符串
     */
    public abstract void onReceive(SocketTransceiver transceiver, String s);

    /**
     * 连接断开
     * 注意：此回调是在新线程中执行的
     *
     * @param transceiver SocketTransceiver对象
     */
    public abstract void onDisconnect(SocketTransceiver transceiver);
}
