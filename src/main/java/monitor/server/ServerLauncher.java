package monitor.server;

/**
 * Se encarga de iniciar el servidor en segundo plano
 * y asegurarse de que solo se arranque una vez.
 */
public class ServerLauncher {

    private static volatile boolean started = false;

    public static synchronized void startServerInBackground() {
        if (started) return;

        Thread serverThread = new Thread(() -> {
            try {
                ServerMain.startServer();
            } catch (Exception e) {
                System.err.println("[APP] Error en servidor: " + e.getMessage());
                e.printStackTrace();
            }
        }, "ServerThread");

        serverThread.setDaemon(true);  // muere cuando se cierre la app
        serverThread.start();
        started = true;

        System.out.println("[APP] Servidor iniciado en segundo plano.");
    }
}
