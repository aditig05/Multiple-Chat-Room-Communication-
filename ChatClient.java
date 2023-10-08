import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ChatClient {
    private static final int SERVER_TCP_PORT = 12345;
    private static final int SERVER_UDP_PORT = 54321;
    private static final String SERVER_IP = "127.0.0.1"; // Replace with the server's IP address

    private Socket tcpSocket;
    private DatagramSocket udpSocket;

    public ChatClient() {
        try {
            // TCP Socket for connecting to the server
            tcpSocket = new Socket(SERVER_IP, SERVER_TCP_PORT);
            System.out.println("Connected to the server via TCP.");

            // UDP Socket for receiving messages from the server
            udpSocket = new DatagramSocket(SERVER_UDP_PORT);
            System.out.println("Listening for UDP messages from the server.");

            // Start a separate thread for receiving UDP messages
            Thread udpReceiverThread = new Thread(new UDPReceiver());
            udpReceiverThread.start();

            // Input stream for user input
            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
            String message;

            // Loop to send messages to the server via TCP
            while (true) {
                System.out.print("Enter your message (or 'exit' to quit): ");
                message = userInput.readLine();
                if ("exit".equalsIgnoreCase(message)) {
                    break;
                }

                // Send the message to the server over TCP
                PrintWriter out = new PrintWriter(tcpSocket.getOutputStream(), true);
                out.println(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Inner class for receiving UDP messages from the server
    private class UDPReceiver implements Runnable {
        @Override
        public void run() {
            try {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                while (true) {
                    udpSocket.receive(packet);
                    String receivedMessage = new String(packet.getData(), 0, packet.getLength());
                    System.out.println("Received from server: " + receivedMessage);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        ChatClient client = new ChatClient();
    }
}
