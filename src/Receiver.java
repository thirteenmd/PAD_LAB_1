import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class Receiver {
    private PrintWriter printWriter;
    private Scanner keyboardScanner;
    private ObjectInputStream objectInputStream;
    private Socket socket;
    private Gson gson;
    private JsonElement jsonElement;
    private JsonObject jsonObject;

    public Receiver() {
    }

    public static void main(String[] args) throws IOException {
        try {
            (new Receiver()).getMessages();
        } catch (ClassNotFoundException var2) {
            var2.printStackTrace();
        }

    }

    public void getMessages() throws IOException, ClassNotFoundException {
        String messageOutput = "";
        this.socket = new Socket("localhost", 4444);
        System.out.println("Receiver: " + this.socket.getLocalSocketAddress());
        this.keyboardScanner = new Scanner(System.in);

        try {
            this.printWriter = new PrintWriter(this.socket.getOutputStream(), true);
            messageOutput = this.keyboardScanner.nextLine();
            this.printWriter.println(messageOutput);
            this.printWriter.flush();
            this.objectInputStream = new ObjectInputStream(this.socket.getInputStream());

            while (true) {
                ExecutorService service = Executors.newSingleThreadExecutor();
                Future<JsonObject> future = service.submit(new Callable<JsonObject>() {
                    public JsonObject call() throws Exception {
                        return Receiver.this.deserializeStringObject();
                    }
                });
                service.shutdown();
                service.awaitTermination(1000L, TimeUnit.MILLISECONDS);
                System.out.println("\nFrom: " + ((JsonObject) future.get()).get("Sender:") + "\nMessage: " + ((JsonObject) future.get()).get("Message:"));
            }
        } catch (Exception var4) {
            var4.printStackTrace();
        }
    }

    public JsonObject deserializeStringObject() throws IOException, ClassNotFoundException {
        this.gson = new Gson();
        this.jsonElement = (JsonElement) this.gson.fromJson(this.objectInputStream.readObject().toString(), JsonElement.class);
        this.jsonObject = this.jsonElement.getAsJsonObject();
        return this.jsonObject;
    }
}
