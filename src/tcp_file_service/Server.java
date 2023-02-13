package tcp_file_service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class Server {
    public static void main(String[] args) throws IOException {
        if(args.length != 1){
            System.out.println("Usage: tcp_file_service.Server <ServerPort>");
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
        System.out.println("Message from client: " + clientMessage);
        buffer.rewind();

        //read bytes from the buffer and write to the TCP channel
        serveChannel.write(buffer);
        serveChannel.close();
    }
}