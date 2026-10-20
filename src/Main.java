import servicios.GestionClientes;
import servicios.GestionFacturas;
import servicios.GestionMesas;
import servicios.GestionMenu;
import servicios.GestionPedidos;
import modelo.Cliente;
import modelo.Factura;
import modelo.Mesa;
import modelo.Pedido;
import modelo.Plato;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class Main {

    private static final Scanner teclado = new Scanner(System.in);
    private static final GestionMesas gestionMesas = new GestionMesas();
    private static final GestionMenu gestionMenu = new GestionMenu();
    private static final GestionPedidos gestionPedidos = new GestionPedidos();
    private static final GestionFacturas gestionFacturas = new GestionFacturas();
    private static final GestionClientes gestionClientes = new GestionClientes();

    public static void main(String[] args) {
        int opcion;

        do {
            System.out.println("\n===== SISTEMA DE GESTIÓN DE RESTAURANTE =====");
            System.out.println("1. Gestión de mesas");
            System.out.println("2. Gestión del menú");
            System.out.println("3. Gestión de pedidos");
            System.out.println("4. Facturación");
            System.out.println("5. Gestión de clientes");
            System.out.println("6. Consultas y reportes");
            System.out.println("0. Salir");

            System.out.print("Seleccione una opción: ");
            opcion = leerEntero();

            switch (opcion) {
                case 1 -> menuGestionMesas();
                case 2 -> menuGestionMenu();
                case 3 -> menuGestionPedidos();
                case 4 -> menuFacturacion();
                case 5 -> menuGestionClientes();
                case 6 -> menuConsultas();
                case 0 -> System.out.println("Saliendo del sistema...");
                default -> System.out.println("Opción inválida. Intente de nuevo.");
            }

        } while (opcion != 0);

        cerrarRecursos();
        teclado.close();
    }

    private static void menuGestionMesas() {
        int opcion;
        do {
            System.out.println("\n--- GESTIÓN DE MESAS ---");
            System.out.println("1. Listar todas las mesas");
            System.out.println("2. Listar mesas disponibles");
            System.out.println("3. Listar mesas ocupadas");
            System.out.println("4. Registrar nueva mesa");
            System.out.println("5. Cambiar estado de mesa");
            System.out.println("0. Volver");

            System.out.print("Opción: ");
            opcion = leerEntero();

            switch (opcion) {
                case 1 -> listarTodasMesas();
                case 2 -> listarMesasDisponibles();
                case 3 -> listarMesasOcupadas();
                case 4 -> registrarMesa();
                case 5 -> cambiarEstadoMesa();
                case 0 -> System.out.println("Volviendo al menú principal...");
                default -> System.out.println("Opción inválida.");
            }
        } while (opcion != 0);
    }

    private static void listarTodasMesas() {
        List<Mesa> mesas = gestionMesas.listarTodasLasMesas();
        if (mesas.isEmpty()) {
            System.out.println("No hay mesas registradas.");
        } else {
            System.out.printf("%-5s %-10s %-12s %-15s%n", "ID", "NÚMERO", "CAPACIDAD", "ESTADO");
            System.out.println("------------------------------------------------");
            for (Mesa m : mesas) {
                System.out.printf("%-5d %-10d %-12d %-15s%n",
                        m.getIdMesa(), m.getNumero(), m.getCapacidad(), m.getEstado());
            }
        }
    }

    private static void listarMesasDisponibles() {
        System.out.print("Capacidad mínima (0 para todas): ");
        int cap = leerEntero();
        List<Mesa> mesas = gestionMesas.listarMesasDisponibles(cap);
        if (mesas.isEmpty()) {
            System.out.println("No hay mesas disponibles con esa capacidad.");
        } else {
            System.out.printf("%-5s %-10s %-12s %-15s%n", "ID", "NÚMERO", "CAPACIDAD", "UBICACIÓN");
            System.out.println("------------------------------------------------");
            for (Mesa m : mesas) {
                System.out.printf("%-5d %-10d %-12d %-15s%n",
                        m.getIdMesa(), m.getNumero(), m.getCapacidad(), m.getUbicacion());
            }
        }
    }

    private static void listarMesasOcupadas() {
        List<Mesa> mesas = gestionMesas.listarMesasOcupadas();
        if (mesas.isEmpty()) {
            System.out.println("No hay mesas ocupadas.");
        } else {
            System.out.printf("%-5s %-10s %-12s %-15s%n", "ID", "NÚMERO", "CAPACIDAD", "ESTADO");
            System.out.println("------------------------------------------------");
            for (Mesa m : mesas) {
                System.out.printf("%-5d %-10d %-12d %-15s%n",
                        m.getIdMesa(), m.getNumero(), m.getCapacidad(), m.getEstado());
            }
        }
    }

    private static void registrarMesa() {
        System.out.print("Número de mesa: ");
        int numero = leerEntero();
        System.out.print("Capacidad: ");
        int capacidad = leerEntero();
        System.out.print("Ubicación: ");
        teclado.nextLine(); // consume newline
        String ubicacion = teclado.nextLine();

        Mesa mesa = new Mesa(numero, capacidad, "FREE");
        mesa.setUbicacion(ubicacion);

        if (gestionMesas.registrarMesa(mesa)) {
            System.out.println("Mesa registrada exitosamente.");
        } else {
            System.out.println("Error al registrar la mesa.");
        }
    }

    private static void cambiarEstadoMesa() {
        System.out.print("ID de mesa: ");
        int idMesa = leerEntero();
        System.out.print("Nuevo estado (FREE/OCCUPIED/RESERVED/MAINTENANCE): ");
        String estado = teclado.next().toUpperCase();

        if (gestionMesas.cambiarEstadoMesa(idMesa, estado)) {
            System.out.println("Estado de mesa actualizado.");
        } else {
            System.out.println("Error al cambiar estado (validación fallida).");
        }
    }

    private static void menuGestionMenu() {
        int opcion;
        do {
            System.out.println("\n--- GESTIÓN DEL MENÚ ---");
            System.out.println("1. Listar platos disponibles");
            System.out.println("2. Listar platos por categoría");
            System.out.println("3. Listar categorías");
            System.out.println("4. Registrar nuevo plato");
            System.out.println("5. Cambiar disponibilidad de plato");
            System.out.println("0. Volver");

            System.out.print("Opción: ");
            opcion = leerEntero();

            switch (opcion) {
                case 1 -> listarPlatosDisponibles();
                case 2 -> listarPlatosPorCategoria();
                case 3 -> listarCategorias();
                case 4 -> registrarPlato();
                case 5 -> cambiarDisponibilidadPlato();
                case 0 -> System.out.println("Volviendo al menú principal...");
                default -> System.out.println("Opción inválida.");
            }
        } while (opcion != 0);
    }

    private static void listarPlatosDisponibles() {
        List<Plato> platos = gestionMenu.listarPlatosDisponibles();
        if (platos.isEmpty()) {
            System.out.println("No hay platos disponibles.");
        } else {
            System.out.printf("%-5s %-25s %-10s %-10s %-15s%n", "ID", "NOMBRE", "PRECIO", "TIEMPO", "CATEGORÍA");
            System.out.println("------------------------------------------------------------------");
            for (Plato p : platos) {
                System.out.printf("%-5d %-25s $%-9.2f %-10d %-15s%n",
                        p.getIdPlato(), p.getNombre(), p.getPrecio(), p.getTiempoPreparacion(),
                        p.getNombreCategoria() != null ? p.getNombreCategoria() : "N/A");
            }
        }
    }

    private static void listarPlatosPorCategoria() {
        listarCategorias();
        System.out.print("ID de categoría: ");
        int idCat = leerEntero();
        List<Plato> platos = gestionMenu.listarPlatosPorCategoria(idCat);
        if (platos.isEmpty()) {
            System.out.println("No hay platos en esta categoría.");
        } else {
            for (Plato p : platos) {
                System.out.printf("  %d. %s - $%.2f (%d min)%n",
                        p.getIdPlato(), p.getNombre(), p.getPrecio(), p.getTiempoPreparacion());
            }
        }
    }

    private static void listarCategorias() {
        List<modelo.CategoriaPlato> cats = gestionMenu.listarCategorias();
        System.out.println("\nCATEGORÍAS:");
        for (modelo.CategoriaPlato c : cats) {
            System.out.printf("  %d. %s%n", c.getIdCategoria(), c.getNombre());
        }
    }

    private static void registrarPlato() {
        listarCategorias();
        System.out.print("ID de categoría: ");
        int idCat = leerEntero();
        System.out.print("Nombre del plato: ");
        teclado.nextLine();
        String nombre = teclado.nextLine();
        System.out.print("Descripción: ");
        String desc = teclado.nextLine();
        System.out.print("Precio: ");
        double precio = teclado.nextDouble();
        System.out.print("Tiempo preparación (min): ");
        int tiempo = leerEntero();

        Plato plato = new Plato(idCat, nombre, desc, precio, tiempo);
        if (gestionMenu.registrarPlato(plato)) {
            System.out.println("Plato registrado exitosamente.");
        } else {
            System.out.println("Error al registrar el plato.");
        }
    }

    private static void cambiarDisponibilidadPlato() {
        System.out.print("ID del plato: ");
        int idPlato = leerEntero();
        System.out.print("¿Disponible? (true/false): ");
        boolean disp = teclado.nextBoolean();

        if (gestionMenu.cambiarDisponibilidadPlato(idPlato, disp)) {
            System.out.println("Disponibilidad actualizada.");
        } else {
            System.out.println("Error al actualizar disponibilidad.");
        }
    }

    private static void menuGestionPedidos() {
        int opcion;
        do {
            System.out.println("\n--- GESTIÓN DE PEDIDOS ---");
            System.out.println("1. Crear nuevo pedido");
            System.out.println("2. Agregar plato a pedido");
            System.out.println("3. Ver pedidos en cocina");
            System.out.println("4. Ver pedidos por mesa");
            System.out.println("5. Cambiar estado de pedido");
            System.out.println("6. Cancelar pedido");
            System.out.println("0. Volver");

            System.out.print("Opción: ");
            opcion = leerEntero();

            switch (opcion) {
                case 1 -> crearPedido();
                case 2 -> agregarPlatoAPedido();
                case 3 -> verPedidosCocina();
                case 4 -> verPedidosPorMesa();
                case 5 -> cambiarEstadoPedido();
                case 6 -> cancelarPedido();
                case 0 -> System.out.println("Volviendo al menú principal...");
                default -> System.out.println("Opción inválida.");
            }
        } while (opcion != 0);
    }

    private static void crearPedido() {
        listarMesasDisponibles();
        System.out.print("ID de mesa: ");
        int idMesa = leerEntero();

        System.out.print("ID de cliente (0 si no hay): ");
        int idCliente = leerEntero();
        if (idCliente == 0) idCliente = -1;

        System.out.print("Observaciones: ");
        teclado.nextLine();
        String obs = teclado.nextLine();

        Optional<Integer> idPedido = gestionPedidos.crearPedido(idMesa, idCliente > 0 ? idCliente : 0, obs);
        if (idPedido.isPresent()) {
            System.out.println("Pedido creado con ID: " + idPedido.get());
        } else {
            System.out.println("Error al crear pedido (mesa no disponible).");
        }
    }

    private static void agregarPlatoAPedido() {
        System.out.print("ID del pedido: ");
        int idPedido = leerEntero();

        Optional<Pedido> pedidoOpt = gestionPedidos.buscarPedidoPorId(idPedido);
        if (pedidoOpt.isEmpty()) {
            System.out.println("Pedido no encontrado.");
            return;
        }

        Pedido pedido = pedidoOpt.get();
        if (pedido.getEstado() != Pedido.Estado.PENDIENTE && pedido.getEstado() != Pedido.Estado.EN_PREPARACION) {
            System.out.println("No se puede modificar un pedido en estado: " + pedido.getEstado());
            return;
        }

        listarPlatosDisponibles();
        System.out.print("ID del plato: ");
        int idPlato = leerEntero();
        System.out.print("Cantidad: ");
        int cantidad = leerEntero();

        if (gestionPedidos.agregarPlatoAPedido(idPedido, idPlato, cantidad)) {
            System.out.println("Plato agregado al pedido.");
        } else {
            System.out.println("Error al agregar plato (no disponible o pedido en estado inválido).");
        }
    }

    private static void verPedidosCocina() {
        List<Pedido> pedidos = gestionPedidos.listarPedidosCocina();
        if (pedidos.isEmpty()) {
            System.out.println("No hay pedidos en cocina.");
        } else {
            for (Pedido p : pedidos) {
                System.out.printf("\nPedido #%d - Mesa %s - %s%n",
                        p.getIdPedido(), p.getNumeroMesa(), p.getEstado());
                System.out.println("  Items:");
                for (modelo.DetallePedido d : p.getDetalles()) {
                    System.out.printf("    %dx %s - $%.2f%n",
                            d.getCantidad(), d.getNombrePlato(), d.getSubtotal());
                }
                System.out.printf("  Total: $%.2f%n", p.getTotal());
            }
        }
    }

    private static void verPedidosPorMesa() {
        System.out.print("ID de mesa: ");
        int idMesa = leerEntero();
        List<Pedido> pedidos = gestionPedidos.listarPedidosPorMesa(idMesa);
        if (pedidos.isEmpty()) {
            System.out.println("No hay pedidos para esta mesa.");
        } else {
            for (Pedido p : pedidos) {
                System.out.printf("Pedido #%d - %s - $%.2f%n",
                        p.getIdPedido(), p.getEstado(), p.getTotal());
            }
        }
    }

    private static void cambiarEstadoPedido() {
        System.out.print("ID del pedido: ");
        int idPedido = leerEntero();
        System.out.println("Estados: PENDIENTE, EN_PREPARACION, LISTO, ENTREGADO, CANCELADO, FACTURADO");
        System.out.print("Nuevo estado: ");
        String estadoStr = teclado.next().toUpperCase();

        try {
            Pedido.Estado estado = Pedido.Estado.valueOf(estadoStr);
            if (gestionPedidos.actualizarEstadoPedido(idPedido, estado)) {
                System.out.println("Estado actualizado.");
            } else {
                System.out.println("Error al actualizar estado.");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Estado inválido.");
        }
    }

    private static void cancelarPedido() {
        System.out.print("ID del pedido a cancelar: ");
        int idPedido = leerEntero();

        if (gestionPedidos.cancelarPedido(idPedido)) {
            System.out.println("Pedido cancelado y mesa liberada.");
        } else {
            System.out.println("Error al cancelar pedido.");
        }
    }

    private static void menuFacturacion() {
        int opcion;
        do {
            System.out.println("\n--- FACTURACIÓN ---");
            System.out.println("1. Generar factura para pedido");
            System.out.println("2. Registrar pago de factura");
            System.out.println("3. Ver facturas pendientes");
            System.out.println("4. Buscar factura por número");
            System.out.println("5. Anular factura");
            System.out.println("0. Volver");

            System.out.print("Opción: ");
            opcion = leerEntero();

            switch (opcion) {
                case 1 -> generarFactura();
                case 2 -> registrarPagoFactura();
                case 3 -> verFacturasPendientes();
                case 4 -> buscarFacturaPorNumero();
                case 5 -> anularFactura();
                case 0 -> System.out.println("Volviendo al menú principal...");
                default -> System.out.println("Opción inválida.");
            }
        } while (opcion != 0);
    }

    private static void generarFactura() {
        System.out.print("ID del pedido: ");
        int idPedido = leerEntero();

        Optional<Pedido> pedidoOpt = gestionPedidos.buscarPedidoPorId(idPedido);
        if (pedidoOpt.isEmpty()) {
            System.out.println("Pedido no encontrado.");
            return;
        }

        Pedido pedido = pedidoOpt.get();
        if (pedido.getEstado() == Pedido.Estado.FACTURADO) {
            System.out.println("Este pedido ya tiene factura generada.");
            return;
        }

        System.out.println("Métodos de pago: EFECTIVO, TARJETA_CREDITO, TARJETA_DEBITO, TRANSFERENCIA, QR, MIXTO");
        System.out.print("Método de pago: ");
        String metodoStr = teclado.next().toUpperCase();

        try {
            Factura.MetodoPago metodo = Factura.MetodoPago.valueOf(metodoStr);

            // Generate invoice number (simple format)
            String numFactura = "INV-" + System.currentTimeMillis();

            Optional<Integer> idFactura = gestionFacturas.generarFactura(idPedido, metodo, numFactura);
            if (idFactura.isPresent()) {
                System.out.println("Factura generada con ID: " + idFactura.get());
                System.out.println("Número de factura: " + numFactura);

                // If cash, it's already paid
                if (metodo == Factura.MetodoPago.EFECTIVO) {
                    System.out.println("Factura pagada en efectivo.");
                }
            } else {
                System.out.println("Error al generar factura (pedido no listo o ya facturado).");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Método de pago inválido.");
        }
    }

    private static void registrarPagoFactura() {
        System.out.print("ID de factura: ");
        int idFactura = leerEntero();

        Optional<Factura> facturaOpt = gestionFacturas.buscarFacturaPorId(idFactura);
        if (facturaOpt.isEmpty()) {
            System.out.println("Factura no encontrada.");
            return;
        }

        Factura factura = facturaOpt.get();
        if (factura.getEstadoPago() == Factura.EstadoPago.PAGADO) {
            System.out.println("Esta factura ya está pagada completamente.");
            return;
        }

        System.out.printf("Total factura: $%.2f | Pendiente: $%.2f%n",
                factura.getTotal(), factura.getTotal() - getPagado(factura));
        System.out.print("Monto a pagar: ");
        double monto = teclado.nextDouble();

        System.out.println("Método: EFECTIVO, TARJETA_CREDITO, TARJETA_DEBITO, TRANSFERENCIA, QR");
        System.out.print("Método de pago: ");
        String metodoStr = teclado.next().toUpperCase();

        System.out.print("Referencia (opcional): ");
        teclado.nextLine();
        String ref = teclado.nextLine();

        try {
            Factura.MetodoPago metodo = Factura.MetodoPago.valueOf(metodoStr);
            if (gestionFacturas.registrarPago(idFactura, monto, metodo, ref)) {
                System.out.println("Pago registrado exitosamente.");
            } else {
                System.out.println("Error al registrar pago.");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Método de pago inválido.");
        }
    }

    private static double getPagado(Factura f) {
        // Would need a separate query to get sum of payments
        return f.getEstadoPago() == Factura.EstadoPago.PAGADO ? f.getTotal() : 0;
    }

    private static void verFacturasPendientes() {
        List<Factura> facturas = gestionFacturas.listarFacturasPendientes();
        if (facturas.isEmpty()) {
            System.out.println("No hay facturas pendientes.");
        } else {
            System.out.printf("%-5s %-15s %-12s %-10s %-10s%n", "ID", "NÚMERO", "TOTAL", "ESTADO", "MESA");
            System.out.println("----------------------------------------------------------");
            for (Factura f : facturas) {
                System.out.printf("%-5d %-15s $%-11.2f %-10s %-10s%n",
                        f.getIdFactura(), f.getNumeroFactura(), f.getTotal(),
                        f.getEstadoPago(), f.getNumeroMesa() != null ? f.getNumeroMesa() : "N/A");
            }
        }
    }

    private static void buscarFacturaPorNumero() {
        System.out.print("Número de factura: ");
        String num = teclado.next();
        Optional<Factura> facturaOpt = gestionFacturas.buscarFacturaPorNumero(num);
        if (facturaOpt.isPresent()) {
            Factura f = facturaOpt.get();
            System.out.println(f);
        } else {
            System.out.println("Factura no encontrada.");
        }
    }

    private static void anularFactura() {
        System.out.print("ID de factura a anular: ");
        int idFactura = leerEntero();

        if (gestionFacturas.anularFactura(idFactura)) {
            System.out.println("Factura anulada.");
        } else {
            System.out.println("Error al anular factura.");
        }
    }

    private static void menuGestionClientes() {
        int opcion;
        do {
            System.out.println("\n--- GESTIÓN DE CLIENTES ---");
            System.out.println("1. Listar todos los clientes");
            System.out.println("2. Buscar cliente por email");
            System.out.println("3. Registrar nuevo cliente");
            System.out.println("4. Actualizar cliente");
            System.out.println("0. Volver");

            System.out.print("Opción: ");
            opcion = leerEntero();

            switch (opcion) {
                case 1 -> listarClientes();
                case 2 -> buscarClientePorEmail();
                case 3 -> registrarCliente();
                case 4 -> actualizarCliente();
                case 0 -> System.out.println("Volviendo al menú principal...");
                default -> System.out.println("Opción inválida.");
            }
        } while (opcion != 0);
    }

    private static void listarClientes() {
        List<Cliente> clientes = gestionClientes.listarTodosLosClientes();
        if (clientes.isEmpty()) {
            System.out.println("No hay clientes registrados.");
        } else {
            System.out.printf("%-5s %-25s %-15s %-30s%n", "ID", "NOMBRE", "TELÉFONO", "EMAIL");
            System.out.println("------------------------------------------------------------------------");
            for (Cliente c : clientes) {
                System.out.printf("%-5d %-25s %-15s %-30s%n",
                        c.getIdCliente(), c.getNombre(), c.getTelefono(), c.getEmail());
            }
        }
    }

    private static void buscarClientePorEmail() {
        System.out.print("Email: ");
        String email = teclado.next();
        Optional<Cliente> clienteOpt = gestionClientes.buscarClientePorEmail(email);
        if (clienteOpt.isPresent()) {
            System.out.println(clienteOpt.get());
        } else {
            System.out.println("Cliente no encontrado.");
        }
    }

    private static void registrarCliente() {
        System.out.print("Nombre: ");
        teclado.nextLine();
        String nombre = teclado.nextLine();
        System.out.print("Teléfono: ");
        String telefono = teclado.next();
        System.out.print("Email: ");
        String email = teclado.next();
        System.out.print("Dirección: ");
        teclado.nextLine();
        String direccion = teclado.nextLine();

        Cliente cliente = new Cliente(nombre, telefono, email, direccion);
        if (gestionClientes.registrarCliente(cliente)) {
            System.out.println("Cliente registrado con ID: " + cliente.getIdCliente());
        } else {
            System.out.println("Error al registrar cliente (¿email duplicado?).");
        }
    }

    private static void actualizarCliente() {
        System.out.print("ID del cliente: ");
        int id = leerEntero();
        Optional<Cliente> clienteOpt = gestionClientes.buscarClientePorId(id);
        if (clienteOpt.isEmpty()) {
            System.out.println("Cliente no encontrado.");
            return;
        }

        Cliente c = clienteOpt.get();
        System.out.print("Nuevo nombre (" + c.getNombre() + "): ");
        teclado.nextLine();
        String nombre = teclado.nextLine();
        if (!nombre.isEmpty()) c.setNombre(nombre);

        System.out.print("Nuevo teléfono (" + c.getTelefono() + "): ");
        String tel = teclado.nextLine();
        if (!tel.isEmpty()) c.setTelefono(tel);

        System.out.print("Nuevo email (" + c.getEmail() + "): ");
        String email = teclado.nextLine();
        if (!email.isEmpty()) c.setEmail(email);

        System.out.print("Nueva dirección (" + c.getDireccion() + "): ");
        String dir = teclado.nextLine();
        if (!dir.isEmpty()) c.setDireccion(dir);

        if (gestionClientes.actualizarCliente(c)) {
            System.out.println("Cliente actualizado.");
        } else {
            System.out.println("Error al actualizar cliente.");
        }
    }

    private static void menuConsultas() {
        System.out.println("\n--- CONSULTAS Y REPORTES ---");
        System.out.println("1. Ventas del día (requiere BD con datos)");
        System.out.println("2. Pedidos por cliente");
        System.out.println("3. Historial de facturas");
        System.out.println("0. Volver");
        System.out.print("Opción: ");
        int op = leerEntero();

        switch (op) {
            case 2 -> {
                System.out.print("ID de cliente: ");
                int idC = leerEntero();
                List<Pedido> pedidos = gestionPedidos.listarPedidosPorCliente(idC);
                for (Pedido p : pedidos) {
                    System.out.printf("Pedido #%d - %s - $%.2f%n", p.getIdPedido(), p.getEstado(), p.getTotal());
                }
            }
            case 3 -> {
                List<Factura> facturas = gestionFacturas.listarFacturasPorEstado(Factura.EstadoPago.PAGADO);
                System.out.println("Facturas pagadas: " + facturas.size());
                double total = facturas.stream().mapToDouble(Factura::getTotal).sum();
                System.out.printf("Total ventas: $%.2f%n", total);
            }
        }
    }

    private static int leerEntero() {
        while (!teclado.hasNextInt()) {
            System.out.print("Entrada inválida. Ingrese un número: ");
            teclado.next();
        }
        return teclado.nextInt();
    }

    private static void cerrarRecursos() {
        gestionMesas.cerrarConexion();
        gestionMenu.cerrarConexion();
        gestionPedidos.cerrarConexion();
        gestionFacturas.cerrarConexion();
        gestionClientes.cerrarConexion();
    }
}