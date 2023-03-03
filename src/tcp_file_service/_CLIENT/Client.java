package tcp_file_service._CLIENT;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.out.println("Usage: tcp_file_service.TCPFileService <ServerIP> <ServerPort>");
            return;
        }
        String serverIP = args[0];
        int serverPort = Integer.parseInt(args[1]);

        char command;
        String status;

        do {
            System.out.println(
                    """
                                    Enter a command:
                                    U - Upload
                                    D - Download
                                    L - List
                                    R - Rename
                                    T - Delete
                                    Q - Quit
                            """
            );
            Scanner keyboard = new Scanner(System.in);
            command = keyboard.nextLine().toUpperCase().charAt(0);

            switch (command) {
                case 'U' -> { //Upload to server
                    System.out.println("Enter file to be uploaded: ");
                    String fileToUpload = "U" + keyboard.nextLine();
                    status = uploadFile(fileToUpload, serverIP, serverPort).toUpperCase();
                    checkStatus(status);

                }
                case 'D' -> { //Download from server
                    System.out.println("Enter file to be downloaded: ");
                    String fileToDownload = "D" + keyboard.nextLine();
                    status = downloadFile(fileToDownload, serverIP, serverPort).toUpperCase();
                    checkStatus(status);
                }
                case 'L' -> { //List
                    String listOfFiles = "L";
                    status = sendCommand(listOfFiles, serverIP, serverPort);
                    checkStatus(String.valueOf(status.charAt(0)));
                    System.out.println("List of Available Files: \n" + status.substring(1));
                }
                case 'R' -> { //Rename
                    System.out.println("Enter file to be renamed: ");
                    String oldFileName = keyboard.nextLine();
                    System.out.println("Enter new file name: ");
                    String newFileName = keyboard.nextLine();
                    String fileToRename = "R" + oldFileName + "," + newFileName;
                    status = sendCommand(fileToRename, serverIP, serverPort).toUpperCase();
                    checkStatus(status);
                }
                case 'T' -> { //Delete
                    System.out.println("Enter file name: ");
                    String fileToDelete = "T" + keyboard.nextLine();
                    status = sendCommand(fileToDelete, serverIP, serverPort).toUpperCase();
                    checkStatus(status);
                }
            }

        } while (command != 'Q');
    }

    private static String sendCommand(String message, String serverIP, int serverPortNumber) throws IOException {
        SocketChannel channel = SocketChannel.open();
        channel.connect(new InetSocketAddress(serverIP, serverPortNumber));

        ByteBuffer requestBuffer = ByteBuffer.wrap(message.getBytes());
        channel.write(requestBuffer);
        ByteBuffer replyBuffer = ByteBuffer.allocate(1024);

        //read from the TCP channel and write to the buffer
        int bytesRead = channel.read(replyBuffer);

        //shutdown output
        channel.shutdownOutput();
        replyBuffer.flip();
        byte[] b = new byte[bytesRead];

        //read bytes from the buffer and convert them to byte array
        replyBuffer.get(b);
        String replyMessage = new String(b);
        channel.close();
        return replyMessage;
    }

    private static String downloadFile(String message, String serverIP, int serverPortNumber) throws IOException {
        SocketChannel channel = SocketChannel.open();
        channel.connect(new InetSocketAddress(serverIP, serverPortNumber));

        ByteBuffer requestBuffer = ByteBuffer.wrap(message.getBytes());
        channel.write(requestBuffer);
        ByteBuffer replyBuffer = ByteBuffer.allocate(1024);

        //get file
        FileOutputStream outputStream = new FileOutputStream("client_file_dir/" + message.substring(1));

        //read from the TCP channel and write to the buffer
        int bytesRead = channel.read(replyBuffer);
        replyBuffer.flip();

        //read bytes from the buffer and convert them to byte array
        byte[] statusByte = new byte[bytesRead];
        replyBuffer.get(statusByte);
        String replyMessage = new String(statusByte);

        //clear replyBuffer
        replyBuffer.clear();

        //read from the TCP channel and write to the file
        //send contents separately
        int contentRead;
        byte[] fileContent = new byte[1024];
        while((contentRead = channel.read(replyBuffer)) !=-1) {
            replyBuffer.flip();
            replyBuffer.get(fileContent, 0, contentRead);
            outputStream.write(fileContent, 0, contentRead);
            replyBuffer.clear();
        }
        //shutdown output
        channel.shutdownOutput();
        channel.close();
        return replyMessage;
    }

    private static String uploadFile(String message, String serverIP, int serverPortNumber) throws IOException {
        SocketChannel channel = SocketChannel.open();
        channel.connect(new InetSocketAddress(serverIP, serverPortNumber));

        ByteBuffer requestBuffer = ByteBuffer.wrap(message.getBytes());
        channel.write(requestBuffer);

        //get file
        File file = new File("client_file_dir/" + message.substring(1));

        //send file contents separately
        if(file.length() != 0 && file.exists()){
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            try {
                String line = bufferedReader.readLine();
                while (line != null) {
                    ByteBuffer lineToSend = ByteBuffer.wrap((line + "\n").getBytes());
                    channel.write(lineToSend);
                    // read next line
                    line = bufferedReader.readLine();
                }
                bufferedReader.close();

                //shutdown output
                channel.shutdownOutput();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        ByteBuffer replyBuffer = ByteBuffer.allocate(1024);

        //read from the TCP channel and write to the buffer
        int bytesRead = channel.read(replyBuffer);
        replyBuffer.flip();
        byte[] b = new byte[bytesRead];

        //read bytes from the buffer and convert them to byte array
        replyBuffer.get(b);
        String replyMessage = new String(b);

        // client debug statement
        System.out.println("Reply message: " + replyMessage);

        channel.close();
        return replyMessage;
    }

    private static void checkStatus(String status){// Check if command was successful or failed
        if(status.equals("S")){
            System.out.println("Operation Successful");
        } else {
            System.out.println("Operation Failed");
        }
    }
}