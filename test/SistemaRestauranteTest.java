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

        List<EntidadBase> entidades = List.of(mesa, plato, cliente);

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
    void testValidacionDominioYCriteriosAceptacion() {
        Mesa mesaInvalida = new Mesa(0, 25, "FREE");
        assertFalse(mesaInvalida.esValido());

        Plato platoInvalido = new Plato(1, "", "Desc", -500.0, 10);
        assertFalse(platoInvalido.esValido());

        Factura facturaEfectivo = new Factura(1, 50000.0, 9500.0, 59500.0, Factura.MetodoPago.EFECTIVO, "INV-001");
        assertEquals(Factura.EstadoPago.PAGADO, facturaEfectivo.getEstadoPago());
        assertTrue(facturaEfectivo.esValido());

        Factura facturaTarjeta = new Factura(2, 50000.0, 9500.0, 59500.0, Factura.MetodoPago.TARJETA_CREDITO, "INV-002");
        assertEquals(Factura.EstadoPago.PENDIENTE, facturaTarjeta.getEstadoPago());
    }
}
