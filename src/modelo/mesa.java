package modelo;

public class mesa {

    private int numero;
    private int capacidad;
    private String estado;



public mesa(int numero, int capacidad, String estado) {

    this.numero = numero;
    this.capacidad = capacidad;
    this.estado = estado;
    }

    public int getNumero() {
        return numero;
    }

    public int getCapacidad() {
        return capacidad;
    }

    public String getEstado() {
        return estado;
    }

public void setNumero(int numero) {
        this.numero = numero;
    }

    public void setCapacidad(int capacidad) {
        this.capacidad = capacidad;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

}