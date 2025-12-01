package monitor.common;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Encapsula el formato de los mensajes que viajan por el Socket.
 *
 * Formatos:
 *   INSERT;x=10;y=20;z=30;fecha=2025-11-22;hora=09:30:01
 *   QUERY;fecha_desde=...;fecha_hasta=...;hora_desde=...;hora_hasta=...
 *
 * Respuesta de QUERY:
 *   OK;10,20,30,2025-11-22,09:30:01|5,60,12,2025-11-22,09:30:02|...
 */
public class MessageUtils {

    public static String buildInsertMessage(SensorData data) {
        return "INSERT;" +
                "x=" + data.getX() + ";" +
                "y=" + data.getY() + ";" +
                "z=" + data.getZ() + ";" +
                "fecha=" + data.getFechaSql() + ";" +
                "hora=" + data.getHoraSql();
    }

    public static String buildQueryMessage(LocalDate fechaDesde,
                                           LocalDate fechaHasta,
                                           LocalTime horaDesde,
                                           LocalTime horaHasta) {
        return "QUERY;" +
                "fecha_desde=" + fechaDesde + ";" +
                "fecha_hasta=" + fechaHasta + ";" +
                "hora_desde=" + horaDesde + ";" +
                "hora_hasta=" + horaHasta;
    }

    /**
     * Parsea la parte de datos de la respuesta:
     * 10,20,30,2025-11-22,09:30:01|5,60,12,2025-11-22,09:30:02
     */
    public static List<SensorData> parseQueryResponseData(String dataPart) {
        List<SensorData> list = new ArrayList<>();
        if (dataPart == null || dataPart.isBlank()) return list;

        String[] registros = dataPart.split("\\|");
        for (String reg : registros) {
            String[] campos = reg.split(",");
            if (campos.length != 5) continue;

            int x = Integer.parseInt(campos[0].trim());
            int y = Integer.parseInt(campos[1].trim());
            int z = Integer.parseInt(campos[2].trim());
            LocalDate fecha = LocalDate.parse(campos[3].trim());
            LocalTime hora = LocalTime.parse(campos[4].trim());

            list.add(new SensorData(x, y, z, fecha, hora));
        }
        return list;
    }
}
