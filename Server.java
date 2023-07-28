import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

// Server class
class Server {
    private ServerSocket serverSocket;
    //creates a path to a FilesRepository which will be located inside the user's current directory
    public static String repositoryName = "FilesRepository";
    public static String currentDir= System.getProperty("user.dir");
    public static Path repositoryPath = Paths.get(currentDir,repositoryName);

    // Constructor to initialize the server with the provided ServerSocket
    public Server(ServerSocket serverSocket){
        this.serverSocket = serverSocket;
    }

    // Method to start the server and accept client connections
    public void startServer(){
        try{
            while(!serverSocket.isClosed()){
                Socket socket = serverSocket.accept();
                System.out.println("A new client connected!");

                // Create a new ClientHandler for each connected client
                ClientHandler clientHandler = new ClientHandler(socket);

                // Start a new thread to handle the client's requests
                Thread thread = new Thread((clientHandler));
                thread.start();
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    // Method to close the server socket
    public void closeServerSocket(){
        try{
            if(serverSocket != null){
                serverSocket.close();
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException{
        // Create a new server socket on port 1234
        ServerSocket serverSocket = new ServerSocket(1234);

        // Create a new instance of the Server class
        Server server = new Server(serverSocket);

        // Create the repository directory if it doesn't exist
        Files.createDirectories(repositoryPath);

        // Start the server to accept client connections
        server.startServer();
    }
}
