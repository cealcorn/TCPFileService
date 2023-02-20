package tcp_file_service._SERVER;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Server {
    public static void main(String[] args) throws IOException {
        if(args.length != 1){
            System.out.println("Usage: tcp_file_service._SERVER.Server <ServerPort>");
            return;
        }
        int serverPort = Integer.parseInt(args[0]);
        ServerSocketChannel listenChannel = ServerSocketChannel.open();
        listenChannel.socket().bind(new InetSocketAddress(serverPort));
        while (true) {
            SocketChannel serveChannel = listenChannel.accept();
            ByteBuffer buffer = ByteBuffer.allocate(1024);

            //read from the TCP channel and write to the buffer
            int bytesRead = serveChannel.read(buffer);
            buffer.flip();
            byte[] b = new byte[bytesRead];

            //read bytes from the buffer and convert them to string
            buffer.get(b);
            String clientMessage = new String(b);
            char clientCommand = clientMessage.charAt(0);
            System.out.println("Message from client: " + clientMessage);
            buffer.rewind();

            String payload;

            switch (clientCommand) {
                case 'U': // upload
                case 'D': // download
                case 'L': // list
                case 'R': // rename
                case 'T': // delete
                    payload = clientMessage.substring(1);
                    File file = new File("file_dir/" + payload);

                    // server debug statement
                    System.out.println("Name of file: " + file.getName());

                    boolean result = file.delete();

                    // server debug statement
                    System.out.println("File was successfully deleted: " + result);

                    // sends client appropriate response
                    ByteBuffer msg;
                    if (result == true) {
                        msg = ByteBuffer.wrap("S".getBytes());
                    } else {
                        msg = ByteBuffer.wrap("F".getBytes());
                    }
                    serveChannel.write(msg);
                    serveChannel.close();
            }
        }


    }
}