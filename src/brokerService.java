import java.net.ServerSocket;
import java.net.Socket;

public class brokerService {
    private static final int PORT = 4444;
    ServerSocket serverSocket;

    public brokerService() {
    }

    public static void main(String[] args) {
        (new brokerService()).connection();
    }

    public void connection() {
        try {
            this.serverSocket = new ServerSocket(4444);
            System.out.println("Service ready for receiving connections!");

            while(true) {
                Socket socket = this.serverSocket.accept();
                (new Thread(new Broker(socket))).start();
            }
        } catch (Exception var2) {
            System.out.println(var2);
        }
    }
}
