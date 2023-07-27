import java.net.*;
import java.io.*;
import java.util.*;
import java.nio.file.*;

public class ClientHandler implements Runnable{
    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String clientUsername;

    public ClientHandler(Socket socket) {
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.clientUsername = bufferedReader.readLine();
            clientHandlers.add(this);
            //broadcastMessage("SERVER: " + clientUsername + " has entered the chat!");
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }
    private void handleFileUpload() throws IOException {
        bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        broadcastMessage("enter the path of the file that you want to upload");
        String sourceFilePath =  bufferedReader.readLine();
        while(true) {
            try {
                if(sourceFilePath.equals("EXIT")){
                    broadcastMessage("exiting upload enter a new command");
                    break;
                }
                Path sourcePath = Paths.get(sourceFilePath);
                if (Files.exists(sourcePath) && Files.isRegularFile(sourcePath)) {
                    Path destinationFilePath = Server.repositoryPath.resolve(sourcePath.getFileName());
                    Files.copy(sourcePath, destinationFilePath, StandardCopyOption.REPLACE_EXISTING);
                    broadcastMessage("File was successfully uploaded");
                    break;
                } else {
                    broadcastMessage("source file does not exists enter a new source file or enter EXIT to enter a different command");
                    sourceFilePath = bufferedReader.readLine();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void sendFileList() throws IOException {
        File[] files = Server.repositoryPath.toFile().listFiles();
        if(files.length > 0){
            for (File file: files) {
                if(file.isFile()){
                    broadcastMessage(file.getName());
                }
            }
        }else{
            broadcastMessage("there are no files in the repository");
        }
    }

    private void handleFileDownload() throws IOException {
        boolean exit = false;
        bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        broadcastMessage("enter the file that you want to download the available files in the repository are:");
        sendFileList();
        String sourceFile =  bufferedReader.readLine();
        while(!exit) {
            try {
                if(sourceFile.equals("EXIT")){
                    broadcastMessage("exiting download enter a new command");
                    break;
                }
                Path sourcePath = Paths.get(String.valueOf(Server.repositoryPath), sourceFile);
                if (Files.exists(sourcePath) && Files.isRegularFile(sourcePath)) {
                    broadcastMessage("Enter a directory you want the file to be uploaded to");
                    while(true){
                        String directoryPath = bufferedReader.readLine();
                        Path directory = Paths.get(directoryPath);
                        if(directoryPath.equals("EXIT")){
                            exit = true;
                            break;
                        }
                        if(Files.exists(directory)){
                            Path destinationPath = directory.resolve(sourcePath.getFileName());
                            Files.copy(sourcePath,destinationPath,StandardCopyOption.REPLACE_EXISTING);
                            broadcastMessage("File was successfully downloaded");
                            exit = true;
                            break;
                        }else{
                            broadcastMessage("directory doesn't exist reenter the directory name or enter EXIT to quit command");
                        }
                    }
                    break;
                } else {
                    broadcastMessage("source file does not exists enter a new source file or enter EXIT to enter a different command");
                    sourceFile = bufferedReader.readLine();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        try {
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String inputLine;
            while ((inputLine = bufferedReader.readLine()) != null) {
                inputLine.trim();
                  String[] words = inputLine.split(" ");
                if (words[0].equals("UPLOAD")) {
                    handleFileUpload();
                } else if (words[0].equals("LIST")) {
                    sendFileList();
                } else if (words[0].equals("DOWNLOAD")) {
                    handleFileDownload();
                }else{
                    broadcastMessage("not a valid command please start your message with either UPLOAD, LIST, or DOWNLOAD");
                }
            }

            closeEverything(socket, bufferedReader, bufferedWriter);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
     public void removeClientHandler() {
        clientHandlers.remove(this);
        //broadcastMessage("SERVER: " + clientUsername + " left the chat");
    }
    public void broadcastMessage(String messageToSend) {
        for (ClientHandler clientHandler : clientHandlers) {
            try {
                if (clientHandler.clientUsername.equals(clientUsername)) {
                    clientHandler.bufferedWriter.write(messageToSend);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                }
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }
    }
    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        removeClientHandler();
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
}

