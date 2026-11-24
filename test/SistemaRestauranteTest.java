import dao.ConexionDB;
import modelo.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SistemaRestauranteTest {

    @Test
    void testHerenciaYPolimorfismoEntidadBase() {
        Mesa mesa = new Mesa(1, 1, 4, "FREE", "Main Hall");
        Plato plato = new Plato(1, 2, "Bandeja Paisa", "Traditional dish", 35000.0, true, 25);
        Cliente cliente = new Cliente(1, "Juan Perez", "3101234567", "juan@email.com", "Bogota");
        CategoriaPlato categoria = new CategoriaPlato(1, "Main Courses", "Traditional specialties", 3);
        Pedido pedido = new Pedido(1, 1, "No onion");
        pedido.setIdPedido(1);
        DetallePedido detalle = new DetallePedido(1, 1, 2, 2, 17500.0, "");
        Factura factura = new Factura(1, 35000.0, 6650.0, 41650.0, Factura.MetodoPago.EFECTIVO, "INV-001");
        factura.setIdFactura(1);

        List<EntidadBase> entidades = List.of(mesa, plato, cliente, categoria, pedido, detalle, factura);

        for (EntidadBase entidad : entidades) {
            assertTrue(entidad.esValido(), "Entity should be valid: " + entidad.getClass().getSimpleName());
            assertEquals(1, entidad.getId());
            assertNotNull(entidad.obtenerResumen());
            assertFalse(entidad.obtenerResumen().isBlank());
            assertNotNull(entidad.getFechaCreacion());
        }
    }

    @Test
    void testComposicionYCalculoDePedido() {
        Pedido pedido = new Pedido(3, 1, "No onion");
        pedido.setIdPedido(10);

        DetallePedido d1 = new DetallePedido(1, 10, 2, 2, 15000.0, "");
        d1.setNombrePlato("Caesar Salad");
        DetallePedido d2 = new DetallePedido(2, 10, 5, 1, 35000.0, "");
        d2.setNombrePlato("Bandeja Paisa");

        pedido.agregarDetalle(d1);
        pedido.agregarDetalle(d2);

        assertEquals(2, pedido.getDetalles().size());
        assertEquals(65000.0, pedido.getTotal(), 0.001);
        assertTrue(pedido.esValido());

        pedido.removerDetalle(2);
        assertEquals(1, pedido.getDetalles().size());
        assertEquals(35000.0, pedido.getTotal(), 0.001);
    }

    @Test
    void testPedidoPreservaTotalCuandoListaDetallesVaciaONula() {
        Pedido pedido = new Pedido(2, 1, "Loaded from DB");
        pedido.setTotal(82000.0);

        pedido.setDetalles(List.of());
        assertEquals(82000.0, pedido.getTotal(), 0.001);

        pedido.setDetalles(null);
        assertNotNull(pedido.getDetalles());
        assertTrue(pedido.getDetalles().isEmpty());
        assertEquals(82000.0, pedido.getTotal(), 0.001);
    }

    @Test
    void testValidacionDominioMesa() {
        assertTrue(new Mesa(1, 1, "FREE").esValido());
        assertTrue(new Mesa(12, 20, "RESERVED").esValido());

        assertFalse(new Mesa(0, 4, "FREE").esValido(), "Table number must be > 0");
        assertFalse(new Mesa(1, 0, "FREE").esValido(), "Capacity must be >= 1");
        assertFalse(new Mesa(1, 21, "FREE").esValido(), "Capacity must be <= 20");
        assertFalse(new Mesa(1, 4, "   ").esValido(), "Status must not be blank");
    }

    @Test
    void testValidacionDominioPlatoYCategoria() {
        Plato valido = new Plato(1, "Ceviche", "Fresh fish", 28000.0, 15);
        assertTrue(valido.esValido());

        assertFalse(new Plato(1, "", "Desc", 15000.0, 10).esValido(), "Blank name invalid");
        assertFalse(new Plato(1, "Soup", "Desc", 0.0, 10).esValido(), "Zero price invalid (RN-004)");
        assertFalse(new Plato(1, "Soup", "Desc", -500.0, 10).esValido(), "Negative price invalid (RN-004)");
        assertFalse(new Plato(1, "Soup", "Desc", 15000.0, 0).esValido(), "Zero prep time invalid");
        assertFalse(new Plato(0, "Soup", "Desc", 15000.0, 10).esValido(), "Invalid category ID");

        assertTrue(new CategoriaPlato("Starters", "Appetizers", 1).esValido());
        assertFalse(new CategoriaPlato("", "Appetizers", 1).esValido());
        assertFalse(new CategoriaPlato("Starters", "Appetizers", -1).esValido());
    }

    @Test
    void testValidacionClienteYDetallePedido() {
        Cliente clienteValido = new Cliente("Laura Martinez", "3005544332", "laura@email.com", "Neiva");
        assertTrue(clienteValido.esValido());
        assertTrue(clienteValido.isActivo());

        Cliente clienteInvalido = new Cliente("   ", "3005544332", "laura@email.com", "Neiva");
        assertFalse(clienteInvalido.esValido());

        DetallePedido detalleValido = new DetallePedido(1, 5, 3, 18000.0);
        assertTrue(detalleValido.esValido());
        assertEquals(54000.0, detalleValido.getSubtotal(), 0.001);

        assertFalse(new DetallePedido(1, 5, 0, 18000.0).esValido(), "Quantity must be > 0 (RN-004)");
        assertFalse(new DetallePedido(1, 5, 2, -100.0).esValido(), "Negative unit price invalid");
        assertFalse(new DetallePedido(1, 0, 2, 18000.0).esValido(), "Invalid dish ID");
    }

    @Test
    void testValidacionFacturaYIva19PorCiento() {
        double subtotal = 56000.0;
        double iva19 = Math.round(subtotal * 0.19 * 100.0) / 100.0;
        double total = subtotal + iva19;

        assertEquals(10640.0, iva19, 0.001);
        assertEquals(66640.0, total, 0.001);

        Factura facturaEfectivo = new Factura(1, subtotal, iva19, total, Factura.MetodoPago.EFECTIVO, "INV-001");
        assertEquals(Factura.EstadoPago.PAGADO, facturaEfectivo.getEstadoPago());
        assertTrue(facturaEfectivo.esValido());

        Factura facturaTarjeta = new Factura(2, subtotal, iva19, total, Factura.MetodoPago.TARJETA_CREDITO, "INV-002");
        assertEquals(Factura.EstadoPago.PENDIENTE, facturaTarjeta.getEstadoPago());
        assertTrue(facturaTarjeta.esValido());

        Factura facturaMixta = new Factura(3, subtotal, iva19, total, Factura.MetodoPago.MIXTO, "INV-003");
        assertEquals(Factura.EstadoPago.PENDIENTE, facturaMixta.getEstadoPago());

        Factura facturaSinNumero = new Factura(1, subtotal, iva19, total, Factura.MetodoPago.QR, "  ");
        assertFalse(facturaSinNumero.esValido());

        Factura facturaNegativa = new Factura(1, -100.0, 0.0, -100.0, Factura.MetodoPago.EFECTIVO, "INV-004");
        assertFalse(facturaNegativa.esValido());
    }

    @Test
    void testConfiguracionAdminPinDisponible() {
        String pin = ConexionDB.getAdminPin();
        assertNotNull(pin);
        assertFalse(pin.isBlank(), "Admin PIN (RN-002) must have a non-blank value");
    }
}
