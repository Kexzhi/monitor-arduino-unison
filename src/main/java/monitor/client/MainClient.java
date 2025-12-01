package monitor.client;

import monitor.AppMain;

/**
 * Punto de entrada alternativo: si en IntelliJ ejecutas "MainClient",
 * en realidad se delega a AppMain para que SIEMPRE inicie
 * servidor + ventana con un solo run.
 */
public class MainClient {

    public static void main(String[] args) {
        AppMain.main(args);
    }
}
