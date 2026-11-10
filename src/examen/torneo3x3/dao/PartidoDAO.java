package examen.torneo3x3.dao;

import examen.torneo3x3.dto.PartidoDTO;
import examen.torneo3x3.dto.RankingDTO;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface PartidoDAO {

    /**
     * Busca un partido por id, con nombres de equipos resueltos.
     * @return el DTO, o {@code null} si no existe
     */
    PartidoDTO buscarPartido(int id, Connection conn) throws SQLException;

    List<PartidoDTO> listarPartidosPorEstado(String estado, Connection conn) throws SQLException;

    /** @return filas afectadas (0 o 1) */
    int actualizarResultado(int id, int pl, int pv, Connection conn) throws SQLException;

    /** @return filas afectadas (0 o 1) */
    int actualizarEstado(int id, String nuevoEstado, Connection conn) throws SQLException;

    /** Ranking de anotadores (solo partidos JUGADO), ordenado por puntos desc. */
    List<RankingDTO> rankingAnotadores(Connection conn) throws SQLException;
}