package examen.torneo3x3.dto;

public record PartidoDTO(int idPartido, int idLocal, int idVisitante,
                         String fecha, Integer puntosLocal, Integer puntosVisitante,
                         String estado, String nombreLocal, String nombreVisitante) {
    @Override
    public String toString() {
        return "#" + idPartido + " " + nombreLocal + " vs " + nombreVisitante +
               " (" + fecha + ") " +
               (puntosLocal == null ? "-" : puntosLocal) + "-" +
               (puntosVisitante == null ? "-" : puntosVisitante) +
               " [" + estado + "]";
    }
}