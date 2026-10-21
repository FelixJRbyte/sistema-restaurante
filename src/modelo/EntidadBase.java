package modelo;

import java.time.LocalDateTime;

/**
 * Abstract base class for all domain entities.
 * Demonstrates OOP Inheritance and Polymorphism across the model layer.
 */
public abstract class EntidadBase {

    protected LocalDateTime fechaCreacion;

    protected EntidadBase() {
        this.fechaCreacion = LocalDateTime.now();
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    /**
     * Returns the primary identifier of the entity.
     */
    public abstract int getId();

    /**
     * Polymorphic method returning a formatted summary of the entity.
     */
    public abstract String obtenerResumen();

    /**
     * Polymorphic domain validation rule for the entity.
     */
    public abstract boolean esValido();
}
