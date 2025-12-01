package monitor.client;

import monitor.server.ServerMain;

import javax.swing.*;
import java.awt.*;

/**
 * Ventana principal que contiene las tres vistas:
 * Inicio, Monitor e Histórico (usando CardLayout).
 */
public class MainFrame extends JFrame {

    public static final String CARD_INICIO = "INICIO";
    public static final String CARD_MONITOR = "MONITOR";
    public static final String CARD_HISTORICO = "HISTORICO";

    private final CardLayout cardLayout;
    private final JPanel cards;

    private final SocketClient socketClient;

    public MainFrame() {
        super("Sistema de Monitoreo UNISON");

        // Cliente de sockets hacia el servidor local
        this.socketClient = new SocketClient("127.0.0.1", ServerMain.PORT);

        this.cardLayout = new CardLayout();
        this.cards = new JPanel(cardLayout);

        InicioPanel inicio = new InicioPanel(this);
        MonitorPanel monitor = new MonitorPanel(this, socketClient);
        HistoricoPanel historico = new HistoricoPanel(this, socketClient);

        cards.add(inicio, CARD_INICIO);
        cards.add(monitor, CARD_MONITOR);
        cards.add(historico, CARD_HISTORICO);

        setContentPane(cards);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
    }

    public void showCard(String cardName) {
        cardLayout.show(cards, cardName);
    }
}
