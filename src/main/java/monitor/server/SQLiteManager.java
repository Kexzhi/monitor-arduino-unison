package monitor.server;

import monitor.common.SensorData;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Maneja todas las operaciones con la base de datos SQLite (monitorBD.db).
 */
public class SQLiteManager {

    private static final String DB_URL = "jdbc:sqlite:monitorBD.db";

    public SQLiteManager() {
        createTableIfNotExists();
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    private void createTableIfNotExists() {
        String sql = """
                CREATE TABLE IF NOT EXISTS datos_sensor (
                  id INTEGER PRIMARY KEY AUTOINCREMENT,
                  x INTEGER NOT NULL,
                  y INTEGER NOT NULL,
                  z INTEGER NOT NULL,
                  fecha_de_captura TEXT NOT NULL,
                  hora_de_captura  TEXT NOT NULL
                )
                """;

        try (Connection conn = connect();
             Statement st = conn.createStatement()) {
            st.execute(sql);
            System.out.println("[BD] Tabla datos_sensor lista.");
        } catch (SQLException e) {
            System.err.println("[BD] Error creando tabla: " + e.getMessage());
        }
    }

    public void insertData(SensorData data) {
        String sql = "INSERT INTO datos_sensor(x, y, z, fecha_de_captura, hora_de_captura) " +
                "VALUES(?,?,?,?,?)";
        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, data.getX());
            ps.setInt(2, data.getY());
            ps.setInt(3, data.getZ());
            ps.setString(4, data.getFechaSql());
            ps.setString(5, data.getHoraSql());
            ps.executeUpdate();

        } catch (SQLException e) {
            System.err.println("[BD] Error insertando datos: " + e.getMessage());
        }
    }

    public List<SensorData> queryByRange(String fechaDesde, String fechaHasta,
                                         String horaDesde, String horaHasta) {
        String sql = """
            SELECT x, y, z, fecha_de_captura, hora_de_captura
            FROM datos_sensor
            WHERE fecha_de_captura BETWEEN ? AND ?
              AND hora_de_captura BETWEEN ? AND ?
            ORDER BY fecha_de_captura, hora_de_captura
            """;

        List<SensorData> list = new ArrayList<>();

        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, fechaDesde);
            ps.setString(2, fechaHasta);
            ps.setString(3, horaDesde);
            ps.setString(4, horaHasta);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int x = rs.getInt("x");
                int y = rs.getInt("y");
                int z = rs.getInt("z");
                String fecha = rs.getString("fecha_de_captura");
                String hora = rs.getString("hora_de_captura");

                list.add(new SensorData(
                        x, y, z,
                        java.time.LocalDate.parse(fecha),
                        java.time.LocalTime.parse(hora)
                ));
            }
        } catch (SQLException e) {
            System.err.println("[BD] Error consultando datos: " + e.getMessage());
        }
        return list;
    }
}
