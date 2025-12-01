package monitor.common;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Representa una lectura del sensor (x, y, z) con fecha y hora de captura.
 */
public class SensorData {

    private final int x;
    private final int y;
    private final int z;
    private final LocalDate fecha;
    private final LocalTime hora;

    public SensorData(int x, int y, int z, LocalDate fecha, LocalTime hora) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.fecha = fecha;
        this.hora = hora;
    }

    public static SensorData now(int x, int y, int z) {
        LocalDateTime now = LocalDateTime.now();
        return new SensorData(x, y, z, now.toLocalDate(), now.toLocalTime());
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }
    public LocalDate getFecha() { return fecha; }
    public LocalTime getHora() { return hora; }

    public String getFechaSql() {
        return fecha.toString(); // "YYYY-MM-DD"
    }

    public String getHoraSql() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm:ss");
        return hora.format(fmt);
    }

    @Override
    public String toString() {
        return "SensorData{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", fecha=" + fecha +
                ", hora=" + hora +
                '}';
    }
}
