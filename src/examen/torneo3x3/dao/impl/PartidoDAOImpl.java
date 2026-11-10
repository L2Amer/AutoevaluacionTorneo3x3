package examen.torneo3x3.dao.impl;

import examen.torneo3x3.dao.PartidoDAO;
import examen.torneo3x3.dto.PartidoDTO;
import examen.torneo3x3.dto.RankingDTO;
import examen.torneo3x3.excepciones.AccesoDatosException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PartidoDAOImpl implements PartidoDAO {

    private static final String SELECT_BASE = """
            SELECT p.id_partido, p.id_local, p.id_visitante, p.fecha,
                   p.puntos_local, p.puntos_visitante, p.estado,
                   el.nombre AS nom_local, ev.nombre AS nom_visitante
            FROM partidos p
            JOIN equipos el ON el.id_equipo = p.id_local
            JOIN equipos ev ON ev.id_equipo = p.id_visitante
            """;

    // ============================================================
    // EJERCICIO 3 (1,5 ptos) — devuelve null si no existe
    // ============================================================
    @Override
    public PartidoDTO buscarPartido(int id, Connection conn) throws SQLException {
        // TODO: SELECT_BASE + " WHERE p.id_partido = ?"
        // si hay fila → mapear y devolver
        // si no → devolver null
        // puntos_local / puntos_visitante pueden ser NULL

        PartidoDTO partido = null;

        String SQL = """
                SELECT
                    id_partido,
                    id_local,
                    id_visitante,
                    fecha,
                    puntos_local,
                    puntos_visitante,
                    estado
                FROM
                    partidos
                WHERE
                    id_partido = ?
                """;

        try (PreparedStatement pstmt = conn.prepareStatement(SQL)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {

                    Integer valor_ptos_local = rs.getInt("puntos_local");
                    Integer ptos_local = rs.wasNull() ? null : valor_ptos_local;

                    Integer valor_ptos_visitante = rs.getInt("puntos_visitante");
                    Integer puntos_visitante = rs.wasNull() ? null : valor_ptos_visitante;

                    String SQLnombreLocal = """
                            SELECT DISTINCT
                                nombre
                            FROM
                                equipos
                            INNER JOIN
                                partidos ON (equipos.id_equipo = partidos.id_local)
                            WHERE
                                equipos.id_equipo = (SELECT id_local FROM partidos WHERE id_partido = ?)
                                                """;

                    String SQLnombreVisitante = """
                            SELECT DISTINCT
                                nombre
                            FROM
                                equipos
                            INNER JOIN
                                partidos ON (equipos.id_equipo = partidos.id_visitante)
                            WHERE
                                equipos.id_equipo = (SELECT id_visitante FROM partidos WHERE id_partido = ?)
                                                        """;

                    String nombreLocal = null;
                    String nombreVisitante = null;

                    try (PreparedStatement pstmtLocal = conn.prepareStatement(SQLnombreLocal);
                            PreparedStatement pstmtVisitante = conn.prepareStatement(SQLnombreVisitante)) {

                        pstmtLocal.setInt(1, id);
                        pstmtVisitante.setInt(1, id);

                        try (ResultSet rsLocal = pstmtLocal.executeQuery();
                                ResultSet rsVisitante = pstmtVisitante.executeQuery()) {

                            if (rsLocal.next()) {
                                nombreLocal = rsLocal.getString("nombre");
                            }

                            if (rsVisitante.next()) {
                                nombreVisitante = rsVisitante.getString("nombre");
                            }

                        } catch (SQLException e) {
                            throw new AccesoDatosException("Error SQL ResultSet(Nombres de Equipos)", e);
                        }

                    } catch (SQLException e) {
                        throw new AccesoDatosException("Error SQL PreparedStatemen(Nombres de Equipos)", e);
                    }

                    partido = new PartidoDTO(
                            rs.getInt("id_partido"),
                            rs.getInt("id_local"),
                            rs.getInt("id_visitante"),
                            rs.getString("fecha"),
                            ptos_local,
                            puntos_visitante,
                            rs.getString("estado"),
                            nombreLocal,
                            nombreVisitante);
                }

            } catch (Exception e) {
                throw new AccesoDatosException("Error SQL (ResultSet)", e);

            }
        } catch (SQLException e) {
            throw new AccesoDatosException("Error SQL (PreparedStatement)", e);
        }

        return partido;
    }

    // ============================================================
    // COMPLETO (referencia) — usado por el Main
    // ============================================================
    @Override
    public List<PartidoDTO> listarPartidosPorEstado(String estado, Connection conn)
            throws SQLException {
        String sql = SELECT_BASE + " WHERE p.estado = ? ORDER BY p.fecha";
        List<PartidoDTO> out = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, estado);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    out.add(mapear(rs));
            }
        }
        return out;
    }

    @Override
    public int actualizarResultado(int id, int pl, int pv, Connection conn)
            throws SQLException {
        String sql = """
                UPDATE partidos
                   SET puntos_local = ?, puntos_visitante = ?, estado = 'JUGADO'
                 WHERE id_partido = ? AND estado = 'PENDIENTE'
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pl);
            ps.setInt(2, pv);
            ps.setInt(3, id);
            return ps.executeUpdate();
        }
    }

    @Override
    public int actualizarEstado(int id, String estado, Connection conn)
            throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE partidos SET estado = ? WHERE id_partido = ?")) {
            ps.setString(1, estado);
            ps.setInt(2, id);
            return ps.executeUpdate();
        }
    }

    // ============================================================
    // EJERCICIO 7 (2 ptos)
    // ============================================================
    @Override
    public List<RankingDTO> rankingAnotadores(Connection conn) throws SQLException {
        // TODO: JOIN estadisticas+juegos+equipos+partidos WHERE estado='JUGADO'
        // GROUP BY jugador, equipo · ORDER BY total DESC
        List<RankingDTO> rankingAnotadores = null;

        String SQL = """
                SELECT
                    (SELECT nombre FROM jugadores WHERE id_jugador = es.id_jugador) AS nombreJugador,
                    (SELECT nombre FROM equipos WHERE id_equipo = j.id_equipo) AS nombreEquipo,
                    SUM(es.puntos) AS totalPuntos
                FROM
                    estadisticas es
                INNER JOIN
                    jugadores j ON es.id_jugador = j.id_jugador
                INNER JOIN
                    equipos eq ON (eq.id_equipo = j.id_equipo)
                INNER JOIN
                    partidos p ON (p.id_partido = es.id_partido)
                WHERE
                    p.estado = 'JUGADO'
                GROUP BY
                    j.id_jugador
                ORDER BY
                    totalPuntos DESC
                                """;
        
        try (Statement stmt = conn.createStatement()) {
            
            try (ResultSet rs = stmt.executeQuery(SQL)) {

                if (rs.next()) {
                    rankingAnotadores = new ArrayList<>();

                    do {
                        rankingAnotadores.add(new RankingDTO(
                            rs.getString("nombreJugador"), 
                            rs.getString("nombreEquipo"), 
                            rs.getInt("totalPuntos"))
                        );
                    } while (rs.next());
                }


            } catch (SQLException e) {
                throw new AccesoDatosException("Error SQL(Statement)", e);

            }
        } catch (SQLException e) {
            throw new AccesoDatosException("Error SQL(Statement)", e);
        }

        return rankingAnotadores;
    }

    // ============================================================
    // Utilidad de mapeo (NO modificar)
    // ============================================================
    private PartidoDTO mapear(ResultSet rs) throws SQLException {
        return new PartidoDTO(
                rs.getInt("id_partido"),
                rs.getInt("id_local"),
                rs.getInt("id_visitante"),
                rs.getString("fecha"),
                (Integer) rs.getObject("puntos_local"),
                (Integer) rs.getObject("puntos_visitante"),
                rs.getString("estado"),
                rs.getString("nom_local"),
                rs.getString("nom_visitante"));
    }
}