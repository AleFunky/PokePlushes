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

                Inicializador.escribe_log(log_writer, Inicializador.log_info, "Opción elegida: " + opcion);

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
            } catch (InputMismatchException e) {
                sc.nextLine(); // Hace falta luego de que el nextInt() falle.
                System.out.println("Opción incorrecta.");
            }
            esperar_enter();
        } while (ejecucion);
    }

    private static void darVIP(BufferedWriter log_writer, String NIF) throws SQLException {
        Inicializador.escribe_log(log_writer, Inicializador.log_info, "Ejecutando darVip.");

        ResultSet resultados = Inicializador.lanzar_consulta("select iEstadoVIP from clientes WHERE cNIF = '" + NIF + "';", log_writer, true);

        if (resultados == null) {
            System.out.println("No se ha podido obtener los datos de la base de datos.");
            Inicializador.escribe_log(log_writer, Inicializador.log_error, "No se ha podido obtener los datos.");
        } else {
            if (resultados.next()) {
                if (!resultados.getBoolean(1)) {
                    Inicializador.lanzar_consulta(
                            "call darVIP('" + NIF + "');", log_writer, false);

                    System.out.println("Se ha dado VIP al cliente.");
                    Inicializador.escribe_log(log_writer, Inicializador.log_info, "Se ha dado VIP al cliente con NIF " + NIF + ".");
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
        Inicializador.escribe_log(log_writer, Inicializador.log_info, "Ejecutando darDeAlta.");

        Inicializador.lanzar_consulta(
                "call insertarNuevoCliente(" +
               String.format("'%s', '%s', '%s', '%s', '%s', 0);", NIF, nombre, apellido1, apellido2, direccion), log_writer, false);

        Inicializador.escribe_log(log_writer, Inicializador.log_info, "Dado de alta a un nuevo cliente con NIF " + NIF + ".");
        System.out.println("Cliente dado de alta.");
    }

    private static void mostrarPeluches(BufferedWriter log_writer) throws SQLException {
        Inicializador.escribe_log(log_writer, Inicializador.log_info, "Ejecutando mostrarPeluches.");
        ResultSet resultados = Inicializador.lanzar_consulta("select * from productos", log_writer, true);

        if (resultados == null) {
            System.out.println("No se ha podido obtener los datos de la base de datos.");
            Inicializador.escribe_log(log_writer, Inicializador.log_error, "No se ha podido obtener los datos.");
        } else {
            Inicializador.escribe_log(log_writer, Inicializador.log_info, "Accedido a la lista de peluches.");
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
        Inicializador.escribe_log(log_writer, Inicializador.log_info, "Ejecutando mostrarClientes.");

        ResultSet resultados = Inicializador.lanzar_consulta(
                "select cNIF, concat(cNombre, ' ', cApellido1, ' ', cApellido2), cDireccion, iEstadoVIP " +
                "from clientes", log_writer, true);

        if (resultados == null) {
            System.out.println("No se ha podido obtener los datos de la base de datos.");
            Inicializador.escribe_log(log_writer, Inicializador.log_error, "No se ha podido obtener los datos.");
        } else {
            Inicializador.escribe_log(log_writer, Inicializador.log_info, "Accedido a la lista de clientes.");
            if (resultados.next()) {
                do {
                    String NIF = "NIF: " + resultados.getString(1);
                    String nombre = resultados.getString(2);
                    String direccion = resultados.getString(3);
                    boolean VIP = (resultados.getInt(4) == 1);

                    System.out.println(NIF + " - " + (VIP ? "(VIP) " : "") +
                            nombre + " con dirección: " + direccion + ".");
                } while (resultados.next());
            } else {
                System.out.println("Sin clientes.");
            }
        }
    }

    private static void obtenerCarritoCompra(BufferedWriter log_writer, String NIF) throws SQLException {
        Inicializador.escribe_log(log_writer, Inicializador.log_info, "Ejecutando obtenerCarritoCompra.");

        ResultSet resultados = Inicializador.lanzar_consulta(
                "select concat(cNombre, ' ', cApellido1, ' ', cApellido2), cEspeciePokemon, cTipo, dPrecio, iCantidad from carrito_compra \n" +
                        "join clientes on clientes.cNIF = carrito_compra.cNIF\n" +
                        "join productos on productos.iCodigoProducto = carrito_compra.iCodigoProducto\n" +
                        "where carrito_compra.cNIF = '" + NIF + "';"
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
                Inicializador.escribe_log(log_writer, Inicializador.log_info,
                        "Accedido al carrito de compra de " + NIF + "."
                );
            } else {
                resultados = Inicializador.lanzar_consulta(
                        "select concat(cNombre, ' ', cApellido1, ' ', cApellido2) from clientes where clientes.cNIF = '" + NIF + "';"
                        , log_writer, true);

                if (resultados.next()) {
                    System.out.println("Carrito de " + resultados.getString(1) + ":");
                    System.out.println("> Sin productos en el carrito de la compra.");
                    Inicializador.escribe_log(log_writer, Inicializador.log_info,
                            "Accedido al carrito de compra de " + NIF + "."
                    );
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
        // Este RegEx prueba si la cadena contiene 8 números y una letra al final.
        return NIF.matches("^\\d{8}[A-Z]$");
    }
}
