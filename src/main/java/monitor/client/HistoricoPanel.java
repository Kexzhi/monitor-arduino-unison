package monitor.client;

import monitor.common.SensorData;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Vista Histórico:
 * - Filtros de fecha/hora
 * - Botón Consultar
 * - Botones rápidos: último minuto, últimos 5 min, todo el día
 * - Botón Inicio
 */
public class HistoricoPanel extends JPanel {

    private final SocketClient socketClient;
    private final MainFrame parent;

    private final JTextField txtFechaDesde;
    private final JTextField txtFechaHasta;
    private final JTextField txtHoraDesde;
    private final JTextField txtHoraHasta;
    private final JLabel lblEstado;

    private final XYSeries serieX = new XYSeries("X");
    private final XYSeries serieY = new XYSeries("Y");
    private final XYSeries serieZ = new XYSeries("Z");
    private final XYSeriesCollection dataset = new XYSeriesCollection();

    public HistoricoPanel(MainFrame parent, SocketClient socketClient) {
        this.parent = parent;
        this.socketClient = socketClient;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // --- Panel de filtros arriba ---
        JPanel filtros = new JPanel(new GridBagLayout());
        filtros.setBackground(new Color(0xFFF8E1)); // dorado suave
        filtros.setBorder(BorderFactory.createTitledBorder("Filtros de búsqueda"));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.fill = GridBagConstraints.HORIZONTAL;

        txtFechaDesde = new JTextField(10);
        txtFechaHasta = new JTextField(10);
        txtHoraDesde = new JTextField(8);
        txtHoraHasta = new JTextField(8);

        LocalDate hoy = LocalDate.now();
        txtFechaDesde.setText(hoy.toString());
        txtFechaHasta.setText(hoy.toString());
        txtHoraDesde.setText("00:00:00");
        txtHoraHasta.setText("23:59:59");

        int row = 0;
        c.gridy = row++;
        c.gridx = 0; filtros.add(new JLabel("Fecha desde (YYYY-MM-DD):"), c);
        c.gridx = 1; filtros.add(txtFechaDesde, c);
        c.gridx = 2; filtros.add(new JLabel("Fecha hasta:"), c);
        c.gridx = 3; filtros.add(txtFechaHasta, c);

        c.gridy = row++;
        c.gridx = 0; filtros.add(new JLabel("Hora desde (HH:MM:SS):"), c);
        c.gridx = 1; filtros.add(txtHoraDesde, c);
        c.gridx = 2; filtros.add(new JLabel("Hora hasta:"), c);
        c.gridx = 3; filtros.add(txtHoraHasta, c);

        // Botones rápidos
        JButton btnUltimoMin = new JButton("Último minuto");
        JButton btnUltimos5 = new JButton("Últimos 5 min");
        JButton btnTodoDia  = new JButton("Todo el día");

        btnUltimoMin.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnUltimos5.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnTodoDia.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        btnUltimoMin.addActionListener(e -> aplicarUltimosMinutos(1));
        btnUltimos5.addActionListener(e -> aplicarUltimosMinutos(5));
        btnTodoDia.addActionListener(e -> aplicarTodoDia());

        JPanel pnlRapidos = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlRapidos.setOpaque(false);
        pnlRapidos.add(new JLabel("Rangos rápidos:"));
        pnlRapidos.add(btnUltimoMin);
        pnlRapidos.add(btnUltimos5);
        pnlRapidos.add(btnTodoDia);

        c.gridy = row++;
        c.gridx = 0;
        c.gridwidth = 4;
        filtros.add(pnlRapidos, c);

        // Botón consultar + botón Inicio
        JButton btnConsultar = new JButton("Consultar");
        btnConsultar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnConsultar.setBackground(new Color(0xF8BB00));
        btnConsultar.setFocusPainted(false);
        btnConsultar.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1, true));

        JButton btnInicio = new JButton("Inicio");
        btnInicio.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnInicio.addActionListener(e -> parent.showCard(MainFrame.CARD_INICIO));

        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlBotones.setOpaque(false);
        pnlBotones.add(btnInicio);
        pnlBotones.add(btnConsultar);

        c.gridy = row;
        c.gridx = 0;
        c.gridwidth = 4;
        filtros.add(pnlBotones, c);

        add(filtros, BorderLayout.NORTH);

        // --- Dataset y gráfica ---
        dataset.addSeries(serieX);
        dataset.addSeries(serieY);
        dataset.addSeries(serieZ);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Gráfica con los datos consultados",
                "Muestra",
                "Valor",
                dataset
        );

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setBorder(BorderFactory.createLineBorder(new Color(0xD99E30), 2, true));
        add(chartPanel, BorderLayout.CENTER);

        // --- Barra de estado ---
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottom.setBackground(new Color(0xFFF3E0));
        lblEstado = new JLabel("Listo para consultar datos.");
        lblEstado.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        bottom.add(lblEstado);
        add(bottom, BorderLayout.SOUTH);

        // Acción del botón Consultar
        btnConsultar.addActionListener(e -> consultarEnSegundoPlano());
    }

    // Rangos rápidos
    private void aplicarUltimosMinutos(int minutos) {
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime antes = ahora.minusMinutes(minutos);

        txtFechaDesde.setText(antes.toLocalDate().toString());
        txtFechaHasta.setText(ahora.toLocalDate().toString());
        txtHoraDesde.setText(antes.toLocalTime().withNano(0).toString());
        txtHoraHasta.setText(ahora.toLocalTime().withNano(0).toString());
    }

    private void aplicarTodoDia() {
        LocalDate hoy = LocalDate.now();
        txtFechaDesde.setText(hoy.toString());
        txtFechaHasta.setText(hoy.toString());
        txtHoraDesde.setText("00:00:00");
        txtHoraHasta.setText("23:59:59");
    }

    private void consultarEnSegundoPlano() {
        lblEstado.setText("Cargando datos desde la base de datos...");
        serieX.clear(); serieY.clear(); serieZ.clear();

        new Thread(() -> {
            try {
                LocalDate fDesde = LocalDate.parse(txtFechaDesde.getText().trim());
                LocalDate fHasta = LocalDate.parse(txtFechaHasta.getText().trim());
                LocalTime hDesde = LocalTime.parse(txtHoraDesde.getText().trim());
                LocalTime hHasta = LocalTime.parse(txtHoraHasta.getText().trim());

                List<SensorData> datos = socketClient.query(fDesde, fHasta, hDesde, hHasta);

                SwingUtilities.invokeLater(() -> actualizarGrafica(datos));

            } catch (Exception ex) {
                // Si algo truena (incluye error de cifrado), se muestra bonito
                SwingUtilities.invokeLater(() ->
                        lblEstado.setText("Error en los filtros o la consulta: "
                                + ex.getMessage()));
            }
        }, "ConsultaHistorico").start();
    }

    private void actualizarGrafica(List<SensorData> datos) {
        serieX.clear(); serieY.clear(); serieZ.clear();

        int index = 0;
        for (SensorData d : datos) {
            serieX.add(index, d.getX());
            serieY.add(index, d.getY());
            serieZ.add(index, d.getZ());
            index++;
        }

        lblEstado.setText("Se cargaron " + datos.size() + " registros.");
    }
}
