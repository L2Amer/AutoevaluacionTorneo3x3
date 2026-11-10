package examen.torneo3x3.dto;

public record RankingDTO(String nombreJugador, String nombreEquipo, int totalPuntos) {
    @Override
    public String toString() {
        return nombreJugador + " (" + nombreEquipo + ") → " + totalPuntos + " pts";
    }
}