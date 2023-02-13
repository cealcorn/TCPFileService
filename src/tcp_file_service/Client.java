package tcp_file_service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.out.println("Usage: tcp_file_service.Client <ServerIP> <ServerPort>");
            return;
        }
        String serverIP = args[0];
        int serverPort = Integer.parseInt(args[1]);
        SocketChannel channel = SocketChannel.open();
        channel.connect(new InetSocketAddress(serverIP, serverPort));

        Scanner keyboard = new Scanner(System.in);
        String message = keyboard.nextLine();

        // Client enters command
//        char command;
//        Scanner keyboard = new Scanner(System.in);
//        System.out.println(
//                """
//                        Enter a command:
//                        U - Upload
//                        D - Download
//                        L - List
//                        R - Rename
//                        M - Move
//                        T - Delete
//                        H - Help
//                        Q - Quit
//                """
//        );
//        command = keyboard.nextLine().toUpperCase().charAt(0);



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
        System.out.println("Reply message from server: " + replyMessage);
        channel.close();
    }
}