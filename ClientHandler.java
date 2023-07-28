import java.net.*;
import java.io.*;
import java.util.*;
import java.nio.file.*;

// ClientHandler class that implements the Runnable interface
public class ClientHandler implements Runnable {
    // ArrayList to store all instances of ClientHandler (connected clients)
    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String clientUsername;

    // Constructor to initialize the ClientHandler for a specific client
    public ClientHandler(Socket socket) {
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.clientUsername = bufferedReader.readLine();
            clientHandlers.add(this);
            //broadcastMessage("SERVER: " + clientUsername + " has entered the chat!");
        } catch (IOException e) {
            // Handle IO exceptions during initialization
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    // Method to handle the file upload from the client
    private void handleFileUpload() throws IOException {
        // Read the path of the file to be uploaded from the client
        bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        broadcastMessage("Enter the path of the file that you want to upload:");
        String sourceFilePath = bufferedReader.readLine();

        while (true) {
            try {
                if (sourceFilePath.equals("EXIT")) {
                    broadcastMessage("Exiting upload. Enter a new command.");
                    break;
                }

                // Check if the source file exists and is a regular file
                Path sourcePath = Paths.get(sourceFilePath);
                if (Files.exists(sourcePath) && Files.isRegularFile(sourcePath)) {
                    // Determine the destination path in the repository
                    Path destinationFilePath = Server.repositoryPath.resolve(sourcePath.getFileName());

                    // Perform the file upload by copying the source file to the destination
                    Files.copy(sourcePath, destinationFilePath, StandardCopyOption.REPLACE_EXISTING);
                    broadcastMessage("File was successfully uploaded.");
                    break;
                } else {
                    broadcastMessage("Source file does not exist. Enter a new source file or enter EXIT to enter a different command.");
                    sourceFilePath = bufferedReader.readLine();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Method to send the list of files in the repository to the client
    private void sendFileList() throws IOException {
        File[] files = Server.repositoryPath.toFile().listFiles();
        if (files.length > 0) {
            for (File file : files) {
                if (file.isFile()) {
                    broadcastMessage(file.getName());
                }
            }
        } else {
            broadcastMessage("There are no files in the repository.");
        }
    }

    // Method to handle the file download from the client
    private void handleFileDownload() throws IOException {
        boolean exit = false;
        bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        broadcastMessage("Enter the file that you want to download. The available files in the repository are:");
        sendFileList();
        String sourceFile = bufferedReader.readLine();

        while (!exit) {
            try {
                if (sourceFile.equals("EXIT")) {
                    broadcastMessage("Exiting download. Enter a new command.");
                    break;
                }

                // Check if the source file exists and is a regular file
                Path sourcePath = Paths.get(String.valueOf(Server.repositoryPath), sourceFile);
                if (Files.exists(sourcePath) && Files.isRegularFile(sourcePath)) {
                    broadcastMessage("Enter a directory where you want the file to be uploaded:");

                    while (true) {
                        String directoryPath = bufferedReader.readLine();
                        Path directory = Paths.get(directoryPath);
                        if (directoryPath.equals("EXIT")) {
                            exit = true;
                            break;
                        }
                        if (Files.exists(directory)) {
                            // Determine the destination path for the downloaded file
                            Path destinationPath = directory.resolve(sourcePath.getFileName());

                            // Perform the file download by copying the source file to the destination
                            Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
                            broadcastMessage("File was successfully downloaded.");
                            exit = true;
                            break;
                        } else {
                            broadcastMessage("Directory doesn't exist. Re-enter the directory name or enter EXIT to quit command.");
                        }
                    }
                    break;
                } else {
                    broadcastMessage("Source file does not exist. Enter a new source file or enter EXIT to enter a different command.");
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
            // Create input and output streams for communication with the client
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String inputLine;
            while ((inputLine = bufferedReader.readLine()) != null) {
                // Trim the input line to remove leading/trailing spaces
                inputLine.trim();

                // Split the input line into words
                String[] words = inputLine.split(" ");

                // Check the first word of the input to determine the command
                if (words[0].equals("UPLOAD")) {
                    handleFileUpload();
                } else if (words[0].equals("LIST")) {
                    sendFileList();
                } else if (words[0].equals("DOWNLOAD")) {
                    handleFileDownload();
                } else {
                    broadcastMessage("Not a valid command. Please start your message with either UPLOAD, LIST, or DOWNLOAD.");
                }
            }

            // Close the client's resources when the communication ends
            closeEverything(socket, bufferedReader, bufferedWriter);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to remove the ClientHandler from the list of connected clients
    public void removeClientHandler() {
        clientHandlers.remove(this);
        //broadcastMessage("SERVER: " + clientUsername + " left the chat");
    }

    // Method to broadcast a message to all clients
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

    // Method to close the resources for a client
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
