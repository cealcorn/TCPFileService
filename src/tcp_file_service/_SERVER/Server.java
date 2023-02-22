package tcp_file_service._SERVER;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

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
            File file;
            boolean result;
            ByteBuffer msg;

            switch (clientCommand) {
                case 'U': // upload
                case 'D': // download
                case 'L': {
                    File directory = new File("file_dir/");
                    result = directory.exists();

                    if (result) {
                        // server debug statement
                        System.out.println(listDirectory(directory));

                        // send "S", send list
                        msg = ByteBuffer.wrap(("S" + listDirectory(directory)).getBytes());
                        serveChannel.write(msg);
                    } else {
                        msg = ByteBuffer.wrap("F".getBytes());
                        serveChannel.write(msg);
                    }

                    serveChannel.close();
                    break;
                } // list
                case 'R': { // rename
                    payload = clientMessage.substring(1);               // Rtest.txt -> test.txt,smile.txt

                    // server debug statement
                    System.out.println("File names received from client: " + payload);

                    // separates file name
                    String[] strArr = payload.split(",");                  // test.txt,smile.txt -> ["test.txt","smile.txt"]
                    String fileName = strArr[0];                                // "test.txt"
                    String newName = strArr[1];                                 // "smile.txt"

                    // server debug statement
                    System.out.println("Original file name: " + fileName);
                    System.out.println("Name to be renamed to: " + newName);

                    file = new File("file_dir/" + fileName);
                    File rename = new File("file_dir/" + newName);
                    result = file.renameTo(rename);

                    // server debug statement
                    System.out.println("Rename success: " + result);
                    System.out.println("New file name:" + file);

                    if (result) {
                        msg = ByteBuffer.wrap("S".getBytes());
                    } else {
                        msg = ByteBuffer.wrap("F".getBytes());
                    }
                    serveChannel.write(msg);
                    serveChannel.close();
                    break;
                }

                case 'T': { // delete
                    payload = clientMessage.substring(1);
                    file = new File("file_dir/" + payload);

                    // server debug statement
                    System.out.println("Name of file: " + file.getName());

                    result = file.delete();

                    // server debug statement
                    System.out.println("File was successfully deleted: " + result);

                    // sends client appropriate response
                    if (result == true) {
                        msg = ByteBuffer.wrap("S".getBytes());
                    } else {
                        msg = ByteBuffer.wrap("F".getBytes());
                    }
                    serveChannel.write(msg);
                    serveChannel.close();
                    break;
                }

            }
        }


    }

    private static String listDirectory(File directory){
        String[] listedDirectory = directory.list();
        String list = "";
        for (int i = 0; i < Objects.requireNonNull(listedDirectory).length; i++ ) {
            list += listedDirectory[i];
            if(new File("data" + "/" + listedDirectory[i]).isDirectory()){
                list += "/\n";
            } else {
                list += "\n";
            }
        }
        return list;
    }
}
