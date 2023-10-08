import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class ChatServer {
    private static final int TCP_PORT = 12345;
    private static final int UDP_PORT = 54321;

    private List<ClientHandler> clients;
    private DatagramSocket udpSocket;

    public ChatServer() {
        clients = new ArrayList<>();
    }

    public void start() {
        try {
            // TCP Server Socket
            ServerSocket tcpServerSocket = new ServerSocket(TCP_PORT);
            System.out.println("TCP Server started on port " + TCP_PORT);

            // UDP Socket
            udpSocket = new DatagramSocket(UDP_PORT);
            System.out.println("UDP Server started on port " + UDP_PORT);

            while (true) {
                // Accept new TCP clients
                Socket tcpClientSocket = tcpServerSocket.accept();
                System.out.println("New TCP client connected: " + tcpClientSocket);

                // Create a new thread to handle the TCP client
                ClientHandler clientHandler = new ClientHandler(tcpClientSocket);
                clients.add(clientHandler);
                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Broadcast a message to all connected clients using UDP
    public void broadcastMessage(String message) {
        try {
            byte[] data = message.getBytes();
            InetAddress broadcastAddress = InetAddress.getByName("255.255.255.255");
            DatagramPacket packet = new DatagramPacket(data, data.length, broadcastAddress, UDP_PORT);
            udpSocket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ChatServer server = new ChatServer();
        server.start();
    }

    // Inner class to handle individual TCP clients
    private class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;

        public ClientHandler(Socket socket) {
            this.socket = socket;
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                String clientMessage;
                while ((clientMessage = in.readLine()) != null) {
                    // Broadcast the received message to all clients using UDP
                    broadcastMessage(clientMessage);
                    System.out.println("Received message: " + clientMessage);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                    clients.remove(this);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
