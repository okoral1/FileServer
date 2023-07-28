import java.io.*;
import java.net.*;
import java.util.*;

// Client class
class Client {
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String username;

    // Constructor to initialize the client with the socket and username
    public Client(Socket socket, String username) {
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.username = username;
        } catch (IOException e) {
            // Handle IO exceptions during initialization
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    // Method to send messages to the server
    public void sendMessage() {
        try {
            // Send the username to the server
            bufferedWriter.write(username);
            bufferedWriter.newLine();
            bufferedWriter.flush();

            // Read input from the user and send messages to the server until disconnected
            Scanner sc = new Scanner(System.in);
            while (socket.isConnected()) {
                String messageToSend = sc.nextLine();
                bufferedWriter.write(messageToSend);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }

        } catch (IOException e) {
            // Handle any IO errors that might occur during communication
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    // Method to listen for messages from the server in a separate thread
    public void listenForMessage(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String msgFromServer;
                    while ((msgFromServer = bufferedReader.readLine()) != null) {
                        // Print the messages received from the server
                        System.out.println(msgFromServer);
                    }
                } catch (IOException e) {
                    // Handle any IO errors that might occur during communication
                    e.printStackTrace();
                } finally {
                    // Close client resources when the communication ends
                    closeEverything(socket, bufferedReader, bufferedWriter);
                }
            }
        }).start();
    }

    // Method to close the client resources
    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Main method to run the client application
    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter your username: ");
        String username = sc.nextLine();
        Socket socket = new Socket("localhost", 1234);
        Client client = new Client(socket, username);

        // Start listening for messages from the server in a separate thread
        client.listenForMessage();

        // Send messages to the server
        client.sendMessage();
    }
}
