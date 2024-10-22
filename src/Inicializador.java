import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Inicializador {
    final static String logs = System.getProperty("user.dir") + "\\logs\\";
    final static String log_info = "I";
    final static String log_error = "E";

    static Connection conexion;

    public static void main(String[] args){
        Date fecha_actual = new Date();
        DateFormat fecha_hora = new SimpleDateFormat("yyyy_MM_dd-HH_mm_ss");
        File fichero_log = new File(logs + "log-" + fecha_hora.format(fecha_actual) + ".log");
        BufferedWriter log_writer = null;

        try {
            log_writer = new BufferedWriter(new FileWriter(fichero_log));
        } catch (IOException e) {
            System.out.println("Error en la creación del fichero log: " + e);
        }

        escribe_log(log_writer, log_info, "¡Comienza ejecución!");

        // Conectar con la base de datos.
        conectar_base_de_datos(log_writer);

        // En caso de algún error, no empezar el programa.
        if (conexion != null) {
            try {
                ProgramaPrincipal.ejecutar(conexion, log_writer);
            } catch (SQLException e) {
                System.out.println("Error de ejecución: " + e);
                escribe_log(log_writer, log_info, "Error de ejecución: " + e);
            }
        }

        escribe_log(log_writer, log_info, "Ejecución terminada correctamente.");
    }

    public static ResultSet lanzar_consulta(String consulta, BufferedWriter log_writer, boolean devuelve) {
        Statement sentencia;
        ResultSet resultado = null;
        escribe_log(log_writer, log_info, "Lanzando consulta: " + consulta);

        try {
            sentencia = conexion.createStatement();
            if (devuelve) {
                resultado = sentencia.executeQuery(consulta);
            } else {
                sentencia.execute(consulta);
            }

            escribe_log(log_writer, log_info, "Consulta ejecutada");
        } catch (SQLException e) {
            System.out.println("SQLException: " + e);
            System.out.println("SQLState: " + e.getSQLState());
            System.out.println("VendorError: " + e.getErrorCode());
            escribe_log(log_writer, log_error, "SQLException: " + e);
            escribe_log(log_writer, log_error, "SQLState: " + e.getSQLState());
            escribe_log(log_writer, log_error, "VendorError: " + e.getErrorCode());
        }

        return resultado;
    }

    private static void conectar_base_de_datos(BufferedWriter log_writer) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            escribe_log(log_writer, log_info, "Driver MySQL cargado");

            conexion = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306", "desarrollador", "root");
            escribe_log(log_writer, log_info, "Conexión establecida");
        } catch (SQLException | ClassNotFoundException e) {
            System.out.println("Error conectando: " + e);
            escribe_log(log_writer, log_error, "Error conectando: " + e);
        }
    }

    public static void escribe_log(BufferedWriter log_writer, String tipo, String mensaje) {
        // Si no se ha podido cargar el fichero log, no escribir nada.
        if (log_writer == null) return;

        Date fecha_actual = new Date();
        DateFormat fecha_hora = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
        try {
            log_writer.write(fecha_hora.format(fecha_actual) + " - " + tipo + " - " + mensaje);
            log_writer.newLine();
            log_writer.flush();
        } catch (IOException e) {
            System.out.println("Error de IO en el fichero de log: " + e);
        } catch (Exception e) {
            System.out.println("Error escribiendo en el fichero log: " + e);
        }
    }
}
