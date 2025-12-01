package monitor.client;

import monitor.common.CryptoUtils;
import monitor.common.MessageUtils;
import monitor.common.SensorData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Cliente de sockets.
 * El servidor se arranca desde AppMain, aquí solo nos conectamos.
 */
public class SocketClient {

    private final String host;
    private final int port;

    private BufferedReader reader;
    private PrintWriter writer;
    private Socket socket;

    public SocketClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    // ---------- conexión base ----------

    private void open() throws IOException {
        socket = new Socket(host, port);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new PrintWriter(socket.getOutputStream(), true);
    }

    private void close() {
        try { if (writer != null) writer.close(); } catch (Exception ignored) {}
        try { if (reader != null) reader.close(); } catch (Exception ignored) {}
        try { if (socket != null) socket.close(); } catch (Exception ignored) {}
    }

    // ---------- INSERT (Monitor) ----------

    public void sendInsertAsync(SensorData data) {
        new Thread(() -> sendInsert(data), "InsertSender").start();
    }

    public void sendInsert(SensorData data) {
        String plain = MessageUtils.buildInsertMessage(data);
        String encrypted;
        try {
            encrypted = CryptoUtils.encrypt(plain);
        } catch (RuntimeException e) {
            System.err.println("[CLIENTE] Error cifrando INSERT: " + e.getMessage());
            return;
        }

        try {
            open();
            writer.println(encrypted);

            String responseEnc = reader.readLine();
            if (responseEnc != null) {
                String response = CryptoUtils.decrypt(responseEnc);
                System.out.println("[CLIENTE] Respuesta INSERT: " + response);
            } else {
                System.err.println("[CLIENTE] Respuesta INSERT nula del servidor.");
            }
        } catch (ConnectException e) {
            System.err.println("[CLIENTE] No se pudo conectar al servidor para INSERT: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("[CLIENTE] Error IO en INSERT: " + e.getMessage());
        } finally {
            close();
        }
    }

    // ---------- QUERY (Histórico) ----------

    public List<SensorData> query(LocalDate fDesde, LocalDate fHasta,
                                  LocalTime hDesde, LocalTime hHasta) {

        String plain = MessageUtils.buildQueryMessage(fDesde, fHasta, hDesde, hHasta);
        String encrypted;
        try {
            encrypted = CryptoUtils.encrypt(plain);
        } catch (RuntimeException e) {
            System.err.println("[CLIENTE] Error cifrando QUERY: " + e.getMessage());
            return List.of();
        }

        try {
            open();
            writer.println(encrypted);

            String responseEnc = reader.readLine();
            if (responseEnc == null) {
                System.err.println("[CLIENTE] Respuesta QUERY nula del servidor.");
                return List.of();
            }

            String response = CryptoUtils.decrypt(responseEnc);

            if (!response.startsWith("OK;")) {
                System.err.println("[CLIENTE] Respuesta inesperada QUERY: " + response);
                return List.of();
            }

            String dataPart = response.substring(3); // quita "OK;"
            return MessageUtils.parseQueryResponseData(dataPart);

        } catch (ConnectException e) {
            System.err.println("[CLIENTE] No se pudo conectar al servidor para QUERY: " + e.getMessage());
            return List.of();
        } catch (IOException e) {
            System.err.println("[CLIENTE] Error IO en QUERY: " + e.getMessage());
            return List.of();
        } finally {
            close();
        }
    }
}
