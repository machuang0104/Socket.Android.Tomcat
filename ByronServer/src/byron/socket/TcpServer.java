package byron.socket;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public abstract class TcpServer implements Runnable {

    private int monitorPort;
    private boolean isRunning;
    private List<SocketTransceiver> clients = new ArrayList<SocketTransceiver>();

    /**
     * 监听的端口
     */
    public TcpServer(int port) {
        this.monitorPort = port;
    }

    public void start() {
        isRunning = true;
        new Thread(this).start();
    }

    public void stop() {
        isRunning = false;
    }

    /**
     * 监听端口，接受客户端连接(新线程中运行)
     */
    @Override
    public void run() {
        try {
            final ServerSocket server = new ServerSocket(monitorPort);
            while (isRunning) {
                try {
                    final Socket socket = server.accept();
                    startClient(socket);
                } catch (IOException e) {
                    e.printStackTrace();
                    onConnectFailed();
                }
            }
            try {
                for (SocketTransceiver client : clients) {
                    // 停止服务器，断开与每个客户端的连接
                    client.stop();
                }
                clients.clear();
                server.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.onServerStop();
    }

    /**
     * 启动客户端收发
     */
    private void startClient(final Socket socket) {
        SocketTransceiver client = new SocketTransceiver(socket) {

            @Override
            public void onReceive(InetAddress address, String receiverSTr) {
                TcpServer.this.onReceive(this, receiverSTr);
            }

            @Override
            public void onDisconnect(InetAddress addr) {
                clients.remove(this);
                TcpServer.this.onDisconnect(this);
            }
        };
        client.start();
        clients.add(client);
        this.onConnect(client);
    }

    /**
     * 建立连接
     * （新线程中回调）
     */
    public abstract void onConnect(SocketTransceiver client);

    /**
     * 连接建立失败
     * （新线程中回调）
     */
    public abstract void onConnectFailed();

    /**
     * 客户端收到字符串
     * （新线程中回调）
     */
    public abstract void onReceive(SocketTransceiver client, String s);

    /**
     * 客户端：连接断开
     * （新线程中回调）
     */
    public abstract void onDisconnect(SocketTransceiver client);

    /**
     * 新线程中回调
     */
    public abstract void onServerStop();
}
