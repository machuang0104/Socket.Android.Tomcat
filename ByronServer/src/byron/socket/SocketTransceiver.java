package byron.socket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Socket发送数据，新线程监听Socket接收到的数据
 *
 * @author Machuang
 * @since 2017-6-28
 */
public abstract class SocketTransceiver implements Runnable {

    protected Socket socket;
    protected InetAddress addr;
    protected DataInputStream in;
    protected DataOutputStream out;
    private boolean runFlag;

    /**
     * @param socket 已经建立连接的socket
     */
    public SocketTransceiver(Socket socket) {
        this.socket = socket;
        this.addr = socket.getInetAddress();
    }

    /**
     * 获取连接到的Socket地址
     *
     * @return InetAddress对象
     */
    public InetAddress getInetAddress() {
        return addr;
    }

    /**
     * 开启Socket收发
     * 若开启失败，断开连接并回调{@code onDisconnect()}
     */
    public void start() {
        runFlag = true;
        new Thread(this).start();
    }

    /**
     * 断开连接(主动)，回调{@code onDisconnect()}
     */
    public void stop() {
        runFlag = false;
        try {
            socket.shutdownInput();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @return true：发送成功
     */
    public boolean send(String s) {
        if (out != null) {
            try {
                out.writeUTF(s);
                out.flush();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 监听Socket接收的数据(新线程中运行)
     */
    @Override
    public void run() {
        try {
            in = new DataInputStream(this.socket.getInputStream());
            out = new DataOutputStream(this.socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
            runFlag = false;
        }
        while (runFlag) {
            try {
                final String s = in.readUTF();
                this.onReceive(addr, s);
            } catch (IOException e) {
                // 连接被断开(被动)
                runFlag = false;
            }
        }
        try {// 断开连接
            in.close();
            out.close();
            socket.close();
            in = null;
            out = null;
            socket = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.onDisconnect(addr);
    }

    /**
     * 接收到数据，新线程中执行
     */
    public abstract void onReceive(InetAddress address, String receiverStr);

    /**
     * 连接断开,新线程中执行的
     */
    public abstract void onDisconnect(InetAddress addr);
}
