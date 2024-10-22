import java.io.BufferedWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.InputMismatchException;
import java.util.Scanner;

public class ProgramaPrincipal {
    static Connection conexion;
    static Scanner sc = new Scanner(System.in);
    public static void ejecutar(Connection conexion_inicializada, BufferedWriter log_writer) throws SQLException {
        conexion = conexion_inicializada; // Guardar aquí para facilitar su acceso.

        boolean ejecucion = true;
        int opcion;
        do {
            System.out.print("""
                    PokeTienda de Peluches
                    
                    1 - Mostrar todos los peluches.
                    2 - Mostrar clientes.
                    3 - Obtener carrito compra.
                    4 - Dar de alta un cliente.
                    5 - Dar VIP.
                    
                    0 - Salir.
                    >""" + " ");
            try {
                opcion = sc.nextInt();
                sc.nextLine();

                switch (opcion) {
                    case 1 -> mostrarPeluches(log_writer);
                    case 2 -> mostrarClientes(log_writer);
                    case 3 -> {
                        System.out.println("Introduzca el NIF del cliente a buscar:");
                        String NIF = sc.nextLine();

                        if (comprobarNIF(NIF)) {
                            obtenerCarritoCompra(log_writer, NIF);
                        } else {
                            System.out.println("El NIF no tiene un formato correcto.");
                        }
                    }
                    case 4 -> {
                        System.out.println("Introduzca el NIF del cliente a dar de alta:");
                        String NIF = sc.nextLine();

                        if (comprobarNIF(NIF)) {
                            System.out.println("Introduzca el nombre:");
                            String nombre = sc.nextLine();

                            System.out.println("Introduzca el primer apellido:");
                            String apellido1 = sc.nextLine();

                            System.out.println("Introduzca el segundo apellido:");
                            String apellido2 = sc.nextLine();

                            System.out.println("Introduzca la dirección del cliente:");
                            String direccion = sc.nextLine();

                            darDeAlta(log_writer, NIF, nombre, apellido1, apellido2, direccion);
                        } else {
                            System.out.println("El NIF no tiene un formato correcto.");
                        }
                    }
                    case 5 -> {
                        System.out.println("Introduzca el NIF del cliente a dar estado VIP:");
                        String NIF = sc.nextLine();

                        if (comprobarNIF(NIF)) {
                            darVIP(log_writer, NIF);
                        } else {
                            System.out.println("El NIF no tiene un formato correcto.");
                        }
                    }

                    case 0 -> ejecucion = false;

                    default -> System.out.println("Opción incorrecta.");
                }
            } catch (InputMismatchException _) {}
            esperar_enter();
        } while (ejecucion);
    }
    private static void darVIP(BufferedWriter log_writer, String NIF) throws SQLException {
        ResultSet resultados = Inicializador.lanzar_consulta("select iEstadoVIP from pokeplushes.clientes WHERE cNIF = '" + NIF + "';", log_writer, true);

        if (resultados == null) {
            System.out.println("No se ha podido obtener los datos de la base de datos.");
            Inicializador.escribe_log(log_writer, Inicializador.log_error, "No se ha podido obtener los datos.");
        } else {
            if (resultados.next()) {
                if (!resultados.getBoolean(1)) {
                    Inicializador.lanzar_consulta(
                            "call pokeplushes.darVIP('" + NIF + "');", log_writer, false);

                    System.out.println("Se ha dado VIP al cliente.");
                } else {
                    System.out.println("El cliente ya tiene VIP.");
                }
            } else {
                System.out.println("No hay ningún cliente con ese NIF.");
            }
        }
    }

    private static void darDeAlta(BufferedWriter log_writer, String NIF, String nombre,
                                  String apellido1, String apellido2, String direccion) {
       Inicializador.lanzar_consulta(
                "call pokeplushes.insertarNuevoCliente(" +
               String.format("'%s', '%s', '%s', '%s', '%s', 0);", NIF, nombre, apellido1, apellido2, direccion), log_writer, false);

        System.out.println("Cliente dado de alta.");
    }

    private static void mostrarPeluches(BufferedWriter log_writer) throws SQLException {
        ResultSet resultados = Inicializador.lanzar_consulta("select * from pokeplushes.productos", log_writer, true);

        if (resultados == null) {
            System.out.println("No se ha podido obtener los datos de la base de datos.");
            Inicializador.escribe_log(log_writer, Inicializador.log_error, "No se ha podido obtener los datos.");
        } else {
            if (resultados.next()) {
                do {
                    String id = "ID" + resultados.getString(1);
                    String especie = resultados.getString(2);
                    String tipo = resultados.getString(3);
                    double altura = resultados.getDouble(4);
                    double precio = resultados.getDouble(5);
                    int stock = resultados.getInt(6);

                    int alturaEnCentimetros = (int) (altura * 100);

                    System.out.println(id + " - " + especie + " (Tipo: " + tipo + "). " +
                            "Altura: " + alturaEnCentimetros + " cm. " + precio + "€. (" + stock + " restantes)");
                } while (resultados.next());
            } else {
                System.out.println("Sin productos.");
            }
        }
    }

    private static void mostrarClientes(BufferedWriter log_writer) throws SQLException {
        ResultSet resultados = Inicializador.lanzar_consulta(
                "select cNIF, concat(cNombre, ' ', cApellido1, ' ', cApellido2), cDireccion, iEstadoVIP " +
                "from pokeplushes.clientes", log_writer, true);

        if (resultados == null) {
            System.out.println("No se ha podido obtener los datos de la base de datos.");
            Inicializador.escribe_log(log_writer, Inicializador.log_error, "No se ha podido obtener los datos.");
        } else {
            if (resultados.next()) {
                do {
                    String nif = "NIF: " + resultados.getString(1);
                    String nombre = resultados.getString(2);
                    String direccion = resultados.getString(3);
                    boolean VIP = (resultados.getInt(4) == 1);

                    System.out.println(nif + " - " + (VIP ? "(VIP) " : "") +  nombre + " con dirección: " + direccion + ".");
                } while (resultados.next());
            } else {
                System.out.println("Sin clientes.");
            }
        }
    }

    private static void obtenerCarritoCompra(BufferedWriter log_writer, String NIF) throws SQLException {
        ResultSet resultados = Inicializador.lanzar_consulta(
                "select concat(cNombre, ' ', cApellido1, ' ', cApellido2), cEspeciePokemon, cTipo, dPrecio, iCantidad from pokeplushes.carrito_compra \n" +
                        "join pokeplushes.clientes on pokeplushes.clientes.cNIF = pokeplushes.carrito_compra.cNIF\n" +
                        "join pokeplushes.productos on pokeplushes.productos.iCodigoProducto = pokeplushes.carrito_compra.iCodigoProducto\n" +
                        "where pokeplushes.carrito_compra.cNIF = '" + NIF + "';"
                , log_writer, true);

        if (resultados == null) {
            System.out.println("No se ha podido obtener los datos de la base de datos.");
            Inicializador.escribe_log(log_writer, Inicializador.log_error, "No se ha podido obtener los datos.");
        } else {
            if (resultados.next()) {
                double precioTotal = 0;
                System.out.println("Carrito de " + resultados.getString(1) + ":");
                do {
                    String especie = resultados.getString(2);
                    String tipo    = resultados.getString(3);
                    double precio  = resultados.getDouble(4);
                    int cantidad   = resultados.getInt(5);

                    System.out.println(String.format("> %.2f € x%s ", precio, cantidad) + especie + " (Tipo: " + tipo + ")");
                    precioTotal += precio * cantidad;
                } while (resultados.next());
                System.out.println("Total: " + String.format("%.2f", precioTotal) + " €.");
            } else {
                resultados = Inicializador.lanzar_consulta(
                        "select * from pokeplushes.clientes where pokeplushes.clientes.cNIF = '" + NIF + "';"
                        , log_writer, true);

                if (resultados.next()) {
                    System.out.println("Sin productos en el carrito de la compra.");
                } else {
                    System.out.println("No existe ningún cliente con ese NIF.");
                }

            }
        }
    }

    private static void esperar_enter() {
        System.out.println("\nPresione Intro para continuar.");
        sc.nextLine();
    }

    private static boolean comprobarNIF(String NIF) {
        return NIF.matches("^\\d{8}[A-Z]$");
    }
}
