import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Sender {
    private Socket socket = null;
    private BufferedReader bufferedReaderFromRemote = null;
    private PrintWriter printWriter = null;

    public Sender() {
    }

    public static void main(String[] args) throws IOException {
        try {
            Sender sender = new Sender();
            sender.sendMessages();
        } catch (Exception var2) {
            var2.printStackTrace();
        }

    }

    public void sendMessages() throws IOException {
        this.socket = new Socket("localhost", 4444);
        System.out.println("Sender: " + this.socket.getLocalSocketAddress());

        while(true) {
            System.out.println("Type your msg: ");
            Scanner scanner = new Scanner(System.in);

            this.printWriter = new PrintWriter(this.socket.getOutputStream(), true);
            this.bufferedReaderFromRemote = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));

            this.printWriter.println(scanner.nextLine());
        }
    }
}
