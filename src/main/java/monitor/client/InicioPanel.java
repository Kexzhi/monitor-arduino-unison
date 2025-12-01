package monitor.client;

import javax.swing.*;
import java.awt.*;

/**
 * Vista Inicio:
 * - Logo de la UNISON (desde resources)
 * - Título de la aplicación
 * - Nombre del autor
 * - Botones Monitor / Histórico
 */
public class InicioPanel extends JPanel {

    public InicioPanel(MainFrame parent) {
        setLayout(new BorderLayout());
        setBackground(new Color(0xE6F0FA)); // tono suave azulado

        // Panel central con info
        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("SISTEMA DE MONITOREO DE SENSORES", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel author = new JLabel("Autor: ZAID MONTANO MARTINEZ", SwingConstants.CENTER);
        author.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        author.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Logo desde resources
        JLabel logoLabel = new JLabel();
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoLabel.setPreferredSize(new Dimension(180, 180));
        logoLabel.setMaximumSize(new Dimension(180, 180));

        try {
            // Asegúrate que el archivo esté en src/main/resources/escudo-unison-logo.png
            java.net.URL imgUrl = InicioPanel.class.getResource("/escudo-unison-logo.png");
            if (imgUrl != null) {
                ImageIcon icon = new ImageIcon(imgUrl);
                Image img = icon.getImage().getScaledInstance(180, 180, Image.SCALE_SMOOTH);
                logoLabel.setIcon(new ImageIcon(img));
            } else {
                logoLabel.setText("LOGO UNISON");
                logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
                logoLabel.setOpaque(true);
                logoLabel.setBackground(new Color(0x00529e));
                logoLabel.setForeground(Color.WHITE);
            }
        } catch (Exception e) {
            logoLabel.setText("LOGO UNISON");
            logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
            logoLabel.setOpaque(true);
            logoLabel.setBackground(new Color(0x00529e));
            logoLabel.setForeground(Color.WHITE);
        }

        center.add(Box.createVerticalStrut(20));
        center.add(title);
        center.add(Box.createVerticalStrut(10));
        center.add(author);
        center.add(Box.createVerticalStrut(20));
        center.add(logoLabel);
        center.add(Box.createVerticalStrut(30));

        add(center, BorderLayout.CENTER);

        // Panel de botones inferiores
        JPanel bottom = new JPanel();
        bottom.setOpaque(false);

        JButton btnMonitor = createColoredButton("Monitor", new Color(0x00529e));   // azul
        JButton btnHistorico = createColoredButton("Histórico", new Color(0xF8BB00)); // dorado

        btnMonitor.addActionListener(e -> parent.showCard(MainFrame.CARD_MONITOR));
        btnHistorico.addActionListener(e -> parent.showCard(MainFrame.CARD_HISTORICO));

        bottom.add(btnMonitor);
        bottom.add(btnHistorico);

        add(bottom, BorderLayout.SOUTH);
    }

    private JButton createColoredButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setBackground(color);
        btn.setForeground(Color.BLACK);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1, true));
        return btn;
    }
}
