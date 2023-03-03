package tcp_file_service._SERVER;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Objects;

public class Server {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
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
            String[] strArr;

            switch (clientCommand) {
                case 'U': { // upload (RECEIVE FILE FROM CLIENT)
                    // server debug statement
                    System.out.println("Initial message: " + clientMessage);

                    payload = clientMessage.substring(1);

                    // server debug statement
                    System.out.println("File to receive from client: " + payload);

                    downloadFile(payload, serveChannel);

                    file = new File("file_dir/" + payload);

                    // server debug statement
                    System.out.println("Does file exist?: " + file.exists() + " | " + file.getName().equals(payload) +
                            " | " + file.length() + " | " + payload);

                    result = file.exists();

                    // server debug statement
                    System.out.println("Result: " + result);

                    if (result) {
                        msg = ByteBuffer.wrap("S".getBytes());
                    } else {
                        msg = ByteBuffer.wrap("F".getBytes());
                    }
                    serveChannel.write(msg);

                    serveChannel.close();
                    break;
                }
                case 'D': { // download (SEND FILE TO CLIENT)
                    payload = clientMessage.substring(2); // start at index 2 bc client sends "," before file name

                    // server debug statement
                    System.out.println("File to send to client: " + payload);

                    File fileToDownload = new File("file_dir/" + payload);
                    result = (fileToDownload).exists();

                    // server debug statement
                    System.out.println("File exists?: " + result);

                    // download file to client
                    if (fileToDownload.length() != 0) {
                        // initialize variables
                        BufferedReader bufferedReader = new BufferedReader(new FileReader(fileToDownload));

                        try {
                            String line = bufferedReader.readLine();

                            while (line != null) {
                                ByteBuffer lineToSend = ByteBuffer.wrap((line + "\n").getBytes());

                                // server debug
                                System.out.println(line);
//                                System.out.println(lineToSend);

                                serveChannel.write(lineToSend);
                                // read next line
                                line = bufferedReader.readLine();
                            }

                            bufferedReader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        serveChannel.close();
                        break;
                    }
                }
                case 'L': { // list
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
                }
                case 'R': { // rename
                    payload = clientMessage.substring(1);// Rtest.txt -> test.txt,smile.txt

                    // server debug statement
                    System.out.println("File names received from client: " + payload);

                    // separates file name
                    strArr = payload.split(",");// test.txt,smile.txt -> ["test.txt","smile.txt"]
                    String fileName = strArr[0];     // "test.txt"
                    String newName = strArr[1];      // "smile.txt"

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

    private static String listDirectory(File directory) {
        String[] listedDirectory = directory.list();
        String list = "";
        for (int i = 0; i < Objects.requireNonNull(listedDirectory).length; i++) {
            list += listedDirectory[i];
            if (new File("data" + "/" + listedDirectory[i]).isDirectory()) {
                list += "/\n";
            } else {
                list += "\n";
            }
        }
        return list;
    }

    private static void downloadFile(String message, SocketChannel serveChannel) throws IOException {
        ByteBuffer replyBuffer = ByteBuffer.allocate(1024);

        //Get file
        FileOutputStream outputStream = new FileOutputStream("file_dir/" + message);

        //read from the TCP channel and write to the file
        int contentRead;
        byte[] fileContent = new byte[1024];

        while((contentRead = serveChannel.read(replyBuffer)) !=-1) {
            replyBuffer.flip();
            replyBuffer.get(fileContent, 0, contentRead);
            outputStream.write(fileContent, 0, contentRead);
            replyBuffer.clear();
            System.out.println("File content: " + fileContent);
        }
    }
}