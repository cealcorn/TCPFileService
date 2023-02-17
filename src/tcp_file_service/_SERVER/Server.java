package tcp_file_service._SERVER;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class Server {
    public static void main(String[] args) throws IOException {
        if(args.length != 1){
            System.out.println("Usage: tcp_file_service._SERVER.Server <ServerPort>");
            return;
        }
        int serverPort = Integer.parseInt(args[0]);
        ServerSocketChannel listenChannel = ServerSocketChannel.open();
        listenChannel.socket().bind(new InetSocketAddress(serverPort));
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
                File file = new File(payload);
                // TODO: create directory for files
                // TODO: follow file path name for file to delete
                // TODO: does file exist? if y, delete and send back 'S' if n, send back 'F'
        }

        //read bytes from the buffer and write to the TCP channel
        serveChannel.write(buffer);
        serveChannel.close();
    }
}