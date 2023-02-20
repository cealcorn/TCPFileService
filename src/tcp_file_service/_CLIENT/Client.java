package tcp_file_service._CLIENT;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) throws IOException {
        Scanner keyboard = new Scanner(System.in);

        System.out.println("Enter IP address of Server: ");
        String serverIP = keyboard.nextLine();
        System.out.println("Enter port number of Server: ");
        int serverPortNumber = Integer.parseInt(keyboard.nextLine());

        // Client enters command
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
            command = keyboard.nextLine().toUpperCase().charAt(0);

            switch (command) {
                case 'U' -> {}
                case 'D' -> {}
                case 'L' -> {}
                case 'R' -> {
                    System.out.println("Enter file to be renamed: ");
                    String filetoRename = "R" + keyboard.nextLine();
                    status = sendCommand(filetoRename, serverIP, serverPortNumber).toUpperCase();
                    checkStatus(status);
                }
                case 'T' -> {
                    System.out.println("Enter file name: ");
                    String fileToDelete = "T" + keyboard.nextLine();
                    status = sendCommand(fileToDelete, serverIP, serverPortNumber).toUpperCase();
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

        //Be sure to call shutdownOutput when done sending
        channel.shutdownOutput();
        replyBuffer.flip();
        byte[] b = new byte[bytesRead];

        //read bytes from the buffer and convert them to byte array
        replyBuffer.get(b);
        String replyMessage = new String(b);
        channel.close();
        return replyMessage;
    }

    private static void checkStatus(String status){
        if(status.equals("S")){
            System.out.println("Operation Successful");
        } else {
            System.out.println("Operation Failed");
        }
    }
}