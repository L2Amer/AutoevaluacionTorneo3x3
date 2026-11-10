package examen.torneo3x3.servicio;

import examen.torneo3x3.conexion.ConexionDB;
import examen.torneo3x3.dao.*;
import examen.torneo3x3.dto.*;
import examen.torneo3x3.excepciones.AccesoDatosException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TorneoService {

    private final PartidoDAO     partidoDAO;
    private final EstadisticaDAO estadisticaDAO;
    private final JugadorDAO     jugadorDAO;

    public TorneoService(PartidoDAO partidoDAO,
                         EstadisticaDAO estadisticaDAO,
                         JugadorDAO jugadorDAO) {
        this.partidoDAO = partidoDAO;
        this.estadisticaDAO = estadisticaDAO;
        this.jugadorDAO = jugadorDAO;
    }

    // ============================================================
    // COMPLETO — REFERENCIA para el ejercicio 9
    // ============================================================
    public void registrarResultado(int idPartido,
                                   int puntosLocal,
                                   int puntosVisitante,
                                   List<EstadisticaDTO> estadisticas) {

        if (puntosLocal < 0 || puntosVisitante < 0) {
            throw new IllegalArgumentException("Los puntos no pueden ser negativos");
        }
        Set<Integer> ids = new HashSet<>();
        for (EstadisticaDTO e : estadisticas) {
            if (!ids.add(e.idJugador())) {
                throw new IllegalArgumentException("Jugador duplicado: " + e.idJugador());
            }
        }

        Connection conn = null;
        try {
            conn = ConexionDB.getConexion();
            conn.setAutoCommit(false);

            PartidoDTO partido = partidoDAO.buscarPartido(idPartido, conn);
            if (partido == null) {
                throw new AccesoDatosException("Partido no encontrado: " + idPartido);
            }
            if (!"PENDIENTE".equals(partido.estado())) {
                throw new AccesoDatosException("Partido no PENDIENTE: " + partido.estado());
            }

            for (EstadisticaDTO e : estadisticas) {
                JugadorDTO j = jugadorDAO.buscarJugador(e.idJugador(), conn);
                if (j == null) {
                    throw new AccesoDatosException(
                        "Jugador inexistente: " + e.idJugador());
                }
                if (j.idEquipo() != partido.idLocal()
                        && j.idEquipo() != partido.idVisitante()) {
                    throw new AccesoDatosException(
                        "Jugador " + e.idJugador()
                        + " no es de ninguno de los dos equipos");
                }
            }

            int filas = partidoDAO.actualizarResultado(
                idPartido, puntosLocal, puntosVisitante, conn);
            if (filas != 1) {
                throw new AccesoDatosException("No se actualizó el partido");
            }
            for (EstadisticaDTO e : estadisticas) {
                estadisticaDAO.insertar(e, conn);
            }

            conn.commit();
        } catch (SQLException | AccesoDatosException ex) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) { }
            }
            if (ex instanceof AccesoDatosException ade) throw ade;
            throw new AccesoDatosException("Error registrando resultado", ex);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) { }
            }
        }
    }

    // ============================================================
    // EJERCICIO 9 (2,5 ptos)
    // ============================================================
    /**
     * Cancela un partido PENDIENTE.
     * Reglas:
     *   - El partido debe existir.
     *   - El partido debe estar PENDIENTE.
     *   - Se borran sus estadísticas (por si hubiera).
     *   - El estado final debe ser 'CANCELADO'.
     *   - Todo en una transacción: rollback si algo falla.
     *
     * PISTA: sigue EXACTAMENTE los 7 pasos de registrarResultado.
     * Métodos que necesitas:
     *   - partidoDAO.buscarPartido(id, conn)              → puede devolver null
     *   - partidoDAO.actualizarEstado(id, "CANCELADO", conn)
     *   - estadisticaDAO.borrarPorPartido(id, conn)
     */
    public void cancelarPartido(int idPartido) {

        Connection conn = null;
        try {
            conn = ConexionDB.getConexion();
            conn.setAutoCommit(false);

            PartidoDTO partido = partidoDAO.buscarPartido(idPartido, conn);

            if (partido == null) {
                throw new AccesoDatosException("Partido no encontrado: " + idPartido);
            }

            if (!"PENDIENTE".equals(partido.estado())) {
                throw new AccesoDatosException("Partido no PENDIENTE: " + partido.estado());
            }

            estadisticaDAO.borrarPorPartido(idPartido, conn);
            
            partidoDAO.actualizarEstado(idPartido, "CANCELADO", conn);
           
            conn.commit();
        } catch (SQLException | AccesoDatosException ex) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) { }
            }
            if (ex instanceof AccesoDatosException ade) throw ade;
            throw new AccesoDatosException("Error registrando resultado", ex);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) { }
            }
        }

    }
}