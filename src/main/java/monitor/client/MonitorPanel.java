package monitor.client;

import com.fazecast.jSerialComm.SerialPort;
import monitor.common.SensorData;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Vista Monitor: muestra una gráfica en tiempo real.
 * - Si el puerto serial funciona, se puede leer de Arduino.
 * - Si jSerialComm falla o no hay puertos, entra en modo simulación.
 */
public class MonitorPanel extends JPanel {

    private static final String OPCION_SIMULACION = "Simulación (sin Arduino)";

    private final MainFrame parent;
    private final SocketClient socketClient;

    // Dataset para la gráfica
    private final XYSeries serieX = new XYSeries("X");
    private final XYSeries serieY = new XYSeries("Y");
    private final XYSeries serieZ = new XYSeries("Z");
    private final AtomicLong sampleIndex = new AtomicLong(0);

    // Componentes UI
    private final JComboBox<String> comboPuertos;
    private final JLabel lblEstado;

    // Simulación
    private final SimulatedArduino simulador;

    // Arduino real
    private SerialPort puertoArduino;
    private Thread hiloSerial;

    // Estado general
    private volatile boolean running = false;

    public MonitorPanel(MainFrame parent, SocketClient socketClient) {
        this.parent = parent;
        this.socketClient = socketClient;
        this.simulador = new SimulatedArduino(this::procesarLineaArduino);

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // ---------- PANEL SUPERIOR ----------
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(new Color(0xE3F2FD));
        top.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0x00529e)));

        // Lado izquierdo: puerto
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT));
        left.setOpaque(false);
        JLabel lblPuerto = new JLabel("Puerto COM:");
        lblPuerto.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        comboPuertos = new JComboBox<>();
        comboPuertos.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cargarPuertos();
        left.add(lblPuerto);
        left.add(comboPuertos);

        // Lado derecho: botón Inicio
        JButton btnInicio = new JButton("Inicio");
        btnInicio.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnInicio.addActionListener(e -> parent.showCard(MainFrame.CARD_INICIO));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        right.setOpaque(false);
        right.add(btnInicio);

        top.add(left, BorderLayout.WEST);
        top.add(right, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);

        // ---------- GRÁFICA ----------
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(serieX);
        dataset.addSeries(serieY);
        dataset.addSeries(serieZ);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Gráfica en tiempo real",
                "Muestra",
                "Valor",
                dataset
        );
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setBorder(BorderFactory.createLineBorder(new Color(0x00529e), 2, true));
        add(chartPanel, BorderLayout.CENTER);

        // ---------- CONTROLES INFERIORES ----------
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottom.setBackground(new Color(0xE3F2FD));

        JButton btnIniciar = new JButton("Iniciar");
        JButton btnDetener = new JButton("Detener");
        btnDetener.setEnabled(false);

        btnIniciar.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btnDetener.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        lblEstado = new JLabel("Seleccione el puerto y presione Iniciar.");
        lblEstado.setFont(new Font("Segoe UI", Font.ITALIC, 12));

        btnIniciar.addActionListener(e -> {
            if (!running) {
                running = true;
                limpiarSeries();
                decidirModoYArrancar();
                btnIniciar.setEnabled(false);
                btnDetener.setEnabled(true);
            }
        });

        btnDetener.addActionListener(e -> {
            detenerTodo();
            btnIniciar.setEnabled(true);
            btnDetener.setEnabled(false);
        });

        bottom.add(btnIniciar);
        bottom.add(btnDetener);
        bottom.add(lblEstado);

        add(bottom, BorderLayout.SOUTH);
    }

    /**
     * Llena el combo con los puertos disponibles + opción de simulación.
     * Si jSerialComm falla (como en tu caso), cae en el catch y solo deja simulación.
     */
    private void cargarPuertos() {
        comboPuertos.removeAllItems();

        try {
            SerialPort[] ports = SerialPort.getCommPorts();

            if (ports.length == 0) {
                comboPuertos.addItem(OPCION_SIMULACION);
                return;
            }

            for (SerialPort p : ports) {
                String name = p.getSystemPortName();
                String desc = p.getDescriptivePortName();
                comboPuertos.addItem(name + " - " + desc);
            }
            comboPuertos.addItem(OPCION_SIMULACION);

        } catch (Throwable t) {
            System.err.println("[MONITOR] No se pudo inicializar jSerialComm. " +
                    "Se usará modo simulación. Detalle: " + t.getClass().getSimpleName() +
                    " - " + t.getMessage());

            comboPuertos.addItem(OPCION_SIMULACION);
        }
    }

    /**
     * Decide si se usa Arduino real o simulación.
     */
    private void decidirModoYArrancar() {
        String seleccion = (String) comboPuertos.getSelectedItem();

        if (seleccion == null || seleccion.equals(OPCION_SIMULACION)) {
            iniciarSimulacion("Modo simulación: sin Arduino seleccionado.");
            return;
        }

        String portName = seleccion.split(" - ")[0]; // se queda con "COM3", etc.
        SerialPort port = SerialPort.getCommPort(portName);
        port.setBaudRate(9600);
        port.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 2000, 0);

        if (!port.openPort()) {
            iniciarSimulacion("No se pudo abrir " + portName +
                    ". Iniciando simulación automática.");
            return;
        }

        this.puertoArduino = port;
        lblEstado.setText("Leyendo datos del Arduino en " + portName + ".");

        hiloSerial = new Thread(() -> leerDesdeArduino(port), "ArduinoSerialReader");
        hiloSerial.start();
    }

    /**
     * Lectura en bucle desde el puerto serial del Arduino.
     */
    private void leerDesdeArduino(SerialPort port) {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(port.getInputStream()))) {

            String line;
            while (running && (line = br.readLine()) != null) {
                procesarLineaArduino(line);
            }
        } catch (Exception e) {
            System.err.println("[MONITOR] Error leyendo del Arduino: " + e.getMessage());
            SwingUtilities.invokeLater(() ->
                    lblEstado.setText("Error leyendo del Arduino. Puede usar simulación."));
        } finally {
            if (port != null && port.isOpen()) {
                port.closePort();
            }
        }
    }

    // --------- SIMULACIÓN ---------

    private void iniciarSimulacion(String mensaje) {
        lblEstado.setText(mensaje);
        simulador.start();
    }

    private void detenerTodo() {
        running = false;

        simulador.stop();

        if (hiloSerial != null && hiloSerial.isAlive()) {
            hiloSerial.interrupt();
        }
        if (puertoArduino != null && puertoArduino.isOpen()) {
            puertoArduino.closePort();
        }
        lblEstado.setText("Lectura detenida.");
    }

    // --------- LÓGICA COMÚN ---------

    private void limpiarSeries() {
        serieX.clear();
        serieY.clear();
        serieZ.clear();
        sampleIndex.set(0);
    }

    /**
     * Recibe una línea "x:10,y:20,z:30" (real o simulada),
     * actualiza la gráfica y envía la lectura al servidor.
     */
    private void procesarLineaArduino(String dataString) {
        try {
            int x = 0, y = 0, z = 0;

            String[] partes = dataString.split(",");
            for (String parte : partes) {
                String[] kv = parte.split(":");
                if (kv.length != 2) continue;
                String key = kv[0].trim();
                int value = Integer.parseInt(kv[1].trim());
                switch (key) {
                    case "x" -> x = value;
                    case "y" -> y = value;
                    case "z" -> z = value;
                }
            }

            long idx = sampleIndex.incrementAndGet();
            serieX.add(idx, x);
            serieY.add(idx, y);
            serieZ.add(idx, z);

            SensorData data = SensorData.now(x, y, z);
            socketClient.sendInsertAsync(data);

            final int fx = x;
            final int fy = y;
            final int fz = z;
            final String horaTexto = LocalDateTime.now()
                    .toLocalTime()
                    .withNano(0)
                    .toString();

            SwingUtilities.invokeLater(() ->
                    lblEstado.setText("Última muestra: X=" + fx +
                            " Y=" + fy + " Z=" + fz +
                            " (" + horaTexto + ")")
            );

        } catch (Exception ex) {
            System.err.println("[MONITOR] Error procesando línea: "
                    + dataString + " -> " + ex.getMessage());
        }
    }
}
