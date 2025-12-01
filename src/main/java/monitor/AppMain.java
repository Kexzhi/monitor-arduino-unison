package monitor;

import monitor.client.MainFrame;
import monitor.server.ServerLauncher;

import javax.swing.*;

/**
 * Punto de entrada principal:
 * - Inicia el servidor como parte de la misma aplicación.
 * - Muestra la interfaz gráfica del cliente.
 */
public class AppMain {

    public static void main(String[] args) {

        // 1) Iniciar servidor integrado en segundo plano
        ServerLauncher.startServerInBackground();

        // 2) Mostrar la ventana principal del cliente
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
