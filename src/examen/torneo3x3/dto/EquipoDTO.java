package examen.torneo3x3.dto;

public record EquipoDTO(int idEquipo, String nombre) {
    @Override
    public String toString() {
        return "[" + idEquipo + "] " + nombre;
    }
}