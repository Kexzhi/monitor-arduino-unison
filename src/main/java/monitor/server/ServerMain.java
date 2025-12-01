package monitor.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Programa Servidor.
 * Encargado de escuchar a los clientes, recibir lecturas y atender consultas.
 */
public class ServerMain {

    // Puerto de comunicación (debe coincidir con el Cliente)
    public static final int PORT = 5050;

    // Se puede ejecutar solo si el profe quiere ver solo el servidor
    public static void main(String[] args) {
        startServer();
    }

    public static void startServer() {
        SQLiteManager db = new SQLiteManager();

        System.out.println("=== SERVIDOR DE MONITOREO ===");
        System.out.println("Puerto: " + PORT);
        System.out.println("Esperando clientes...\n");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("[SERVIDOR] Nuevo cliente conectado: "
                        + clientSocket.getInetAddress() + ":" + clientSocket.getPort());

                ClientHandler handler = new ClientHandler(clientSocket, db);
                new Thread(handler,
                        "ClientHandler-" + clientSocket.getPort()).start();
            }
        } catch (IOException e) {
            System.err.println("[SERVIDOR] Error en el servidor: " + e.getMessage());
        }
    }
}
