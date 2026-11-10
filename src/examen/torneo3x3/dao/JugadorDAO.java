package examen.torneo3x3.dao;

import examen.torneo3x3.dto.JugadorDTO;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface JugadorDAO {

    /** Jugadores de un equipo ordenados por dorsal. */
    List<JugadorDTO> listarJugadoresDeEquipo(int idEquipo, Connection conn) throws SQLException;

    /**
     * Busca un jugador por id.
     * @return el DTO, o {@code null} si no existe
     */
    JugadorDTO buscarJugador(int id, Connection conn) throws SQLException;

    /** @return filas afectadas (0 o 1) */
    int actualizarAlturaJugador(int id, int altura, Connection conn) throws SQLException;
}