package monitor.server;

import monitor.common.CryptoUtils;
import monitor.common.SensorData;

import java.io.*;
import java.net.Socket;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Atiende a un cliente conectado al servidor en su propio hilo.
 */
public class ClientHandler implements Runnable {

    private final Socket socket;
    private final SQLiteManager db;

    public ClientHandler(Socket socket, SQLiteManager db) {
        this.socket = socket;
        this.db = db;
    }

    @Override
    public void run() {
        String clientInfo = socket.getInetAddress() + ":" + socket.getPort();
        System.out.println("[SERVIDOR] Nuevo cliente conectado: " + clientInfo);

        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            String encryptedLine;
            while ((encryptedLine = in.readLine()) != null) {
                String msg = CryptoUtils.decrypt(encryptedLine);
                System.out.println("[SERVIDOR] Mensaje recibido (descifrado): " + msg);

                if (msg.startsWith("INSERT;")) {
                    handleInsert(msg);
                    String resp = CryptoUtils.encrypt("OK;INSERT");
                    out.println(resp);
                    System.out.println("[SERVIDOR] Registro guardado en BD.");

                } else if (msg.startsWith("QUERY;")) {
                    String dataStr = handleQuery(msg);
                    String resp = CryptoUtils.encrypt("OK;" + dataStr);
                    out.println(resp);
                    System.out.println("[SERVIDOR] Datos enviados al cliente.");

                } else if (msg.equals("BYE")) {
                    System.out.println("[SERVIDOR] Cliente solicitó cierre: " + clientInfo);
                    break;

                } else {
                    String resp = CryptoUtils.encrypt("ERROR;Comando no reconocido");
                    out.println(resp);
                }
            }
        } catch (IOException e) {
            System.err.println("[SERVIDOR] Error de E/S con el cliente: " + e.getMessage());
        } finally {
            try { socket.close(); } catch (IOException ignored) {}
            System.out.println("[SERVIDOR] Conexión cerrada con " + clientInfo);
        }
    }

    private void handleInsert(String msg) {
        // INSERT;x=10;y=20;z=30;fecha=2025-11-22;hora=09:30:01
        String[] parts = msg.split(";");
        int x = 0, y = 0, z = 0;
        String fecha = null, hora = null;

        for (int i = 1; i < parts.length; i++) {
            String[] kv = parts[i].split("=");
            if (kv.length != 2) continue;
            String key = kv[0];
            String value = kv[1];

            switch (key) {
                case "x" -> x = Integer.parseInt(value);
                case "y" -> y = Integer.parseInt(value);
                case "z" -> z = Integer.parseInt(value);
                case "fecha" -> fecha = value;
                case "hora" -> hora = value;
            }
        }

        if (fecha == null || hora == null) {
            SensorData data = SensorData.now(x, y, z);
            db.insertData(data);
        } else {
            SensorData data = new SensorData(
                    x, y, z,
                    LocalDate.parse(fecha),
                    LocalTime.parse(hora)
            );
            db.insertData(data);
        }
    }

    private String handleQuery(String msg) {
        // QUERY;fecha_desde=...;fecha_hasta=...;hora_desde=...;hora_hasta=...
        String fechaDesde = "2000-01-01";
        String fechaHasta = "2100-12-31";
        String horaDesde = "00:00:00";
        String horaHasta = "23:59:59";

        String[] parts = msg.split(";");
        for (int i = 1; i < parts.length; i++) {
            String[] kv = parts[i].split("=");
            if (kv.length != 2) continue;
            String key = kv[0];
            String value = kv[1];

            switch (key) {
                case "fecha_desde" -> fechaDesde = value;
                case "fecha_hasta" -> fechaHasta = value;
                case "hora_desde" -> horaDesde = value;
                case "hora_hasta" -> horaHasta = value;
            }
        }

        List<SensorData> list = db.queryByRange(fechaDesde, fechaHasta, horaDesde, horaHasta);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            SensorData d = list.get(i);
            sb.append(d.getX()).append(",")
                    .append(d.getY()).append(",")
                    .append(d.getZ()).append(",")
                    .append(d.getFechaSql()).append(",")
                    .append(d.getHoraSql());
            if (i < list.size() - 1) sb.append("|");
        }
        return sb.toString();
    }
}
