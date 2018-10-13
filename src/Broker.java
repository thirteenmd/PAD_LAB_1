import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Broker implements Runnable {
    private static Map<Socket, ObjectOutputStream> allOutStreams = new ConcurrentHashMap();
    private static Map<Socket, ObjectOutputStream> allReceiverOutStreams = new ConcurrentHashMap();
    private Socket socket;
    private BufferedReader bufferedReader;
    private static BlockingQueue<JsonObject> blockingQueue = new ArrayBlockingQueue(40);
    private JsonObject jsonObject;

    public Broker(Socket socket) throws IOException {
        this.socket = socket;
        allOutStreams.put(socket, new ObjectOutputStream(socket.getOutputStream()));
    }

    public void sendAndSerialize(Map<Socket, ObjectOutputStream> allReceiverOutStreams) throws IOException, InterruptedException {
        ExecutorService service = Executors.newSingleThreadExecutor();
        service.execute(() -> {
            Iterator var1 = allReceiverOutStreams.entrySet().iterator();

            while(var1.hasNext()) {
                Entry<Socket, ObjectOutputStream> entry = (Entry)var1.next();
                Iterator var3 = blockingQueue.iterator();

                while(var3.hasNext()) {
                    JsonObject strObj = (JsonObject)var3.next();

                    try {
                        ((ObjectOutputStream)entry.getValue()).writeObject(strObj.toString());
                        System.out.println("Sending to: " + ((Socket)entry.getKey()).getRemoteSocketAddress());
                    } catch (IOException var6) {
                        var6.printStackTrace();
                    }
                }
            }

        });
        service.shutdown();
        service.awaitTermination(1000L, TimeUnit.MILLISECONDS);
    }

    public void run() {
        String message = "";

        try {
            this.bufferedReader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            System.out.println("User connected: " + this.socket.getRemoteSocketAddress());

            while((message = this.bufferedReader.readLine()) != null) {
                this.createJSON(message);
                this.ifSubscribe(message);
                this.checkForEmptyAndSendAsync(message);
            }
        } catch (InterruptedException | IOException var3) {
            var3.printStackTrace();
        }

    }

    public void ifSubscribe(String message) {
        if (Objects.equals(message, "subscribe")) {
            Iterator var2 = allOutStreams.entrySet().iterator();

            while(var2.hasNext()) {
                Entry<Socket, ObjectOutputStream> entry = (Entry)var2.next();
                if (this.socket.getRemoteSocketAddress().equals(((Socket)entry.getKey()).getRemoteSocketAddress())) {
                    allReceiverOutStreams.put(entry.getKey(), entry.getValue());
                }
            }

            blockingQueue.remove(this.jsonObject);
        }

    }

    public void createJSON(String message) {
        this.jsonObject = new JsonObject();
        this.jsonObject.addProperty("Message:", message);
        this.jsonObject.addProperty("Sender:", this.socket.getRemoteSocketAddress().toString());
        blockingQueue.add(this.jsonObject);
    }

    public void checkForEmptyAndSendAsync(String message) throws IOException, InterruptedException {
        if (!allReceiverOutStreams.isEmpty() && !Objects.equals(message, "subscribe")) {
            this.sendAndSerialize(allReceiverOutStreams);
            blockingQueue.remove(this.jsonObject);
        }

    }
}
