package examen.torneo3x3.dto;

public record EstadisticaDTO(int idPartido, int idJugador, int puntos) {
    @Override
    public String toString() {
        return "partido=" + idPartido + " jugador=" + idJugador + " puntos=" + puntos;
    }
}