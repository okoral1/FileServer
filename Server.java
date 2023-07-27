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
    public static String repositoryName = "FilesRepository";
    public static String currentDir= System.getProperty("user.dir");
    public static Path repositoryPath = Paths.get(currentDir,repositoryName);

    public Server(ServerSocket serverSocket){
        this.serverSocket = serverSocket;
    }
    public void startServer(){
        try{
            while(!serverSocket.isClosed()){
                Socket socket = serverSocket.accept();
                System.out.println("A new client connected!");
                ClientHandler clientHandler = new ClientHandler(socket);
                Thread thread = new Thread((clientHandler));
                thread.start();

            }
        }catch(IOException e){

        }
    }

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
        ServerSocket serverSocket = new ServerSocket(1234);
        Server server = new Server(serverSocket);
        Files.createDirectories(repositoryPath);
        server.startServer();


    }
}