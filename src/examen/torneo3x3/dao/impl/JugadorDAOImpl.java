package examen.torneo3x3.dao.impl;

import examen.torneo3x3.dao.JugadorDAO;
import examen.torneo3x3.dto.JugadorDTO;
import examen.torneo3x3.excepciones.AccesoDatosException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JugadorDAOImpl implements JugadorDAO {

    // ============================================================
    // EJERCICIO 4 (0,5 ptos)
    // ============================================================
    @Override
    public List<JugadorDTO> listarJugadoresDeEquipo(int idEquipo, Connection conn)
            throws SQLException {
        // TODO: SELECT ... WHERE id_equipo = ? ORDER BY dorsal
        // altura_cm puede ser NULL

        List<JugadorDTO> listaJugadores = new ArrayList<>();

        String SQL = """
                SELECT
                    id_jugador,
                    id_equipo,
                    dorsal,
                    nombre,
                    altura_cm
                FROM
                    jugadores
                WHERE
                    id_equipo = ?
                """;

        try (PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setInt(1, idEquipo);

            try (ResultSet rs = pstmt.executeQuery()) {

                if (rs.next()) {
                    listaJugadores = new ArrayList<>();
                    do {
                        Integer valor_altura = rs.getInt("altura_cm");
                        Integer altura = rs.wasNull() ? null : valor_altura;

                        listaJugadores.add(new JugadorDTO(
                                rs.getInt("id_jugador"),
                                rs.getInt("id_equipo"),
                                rs.getInt("dorsal"),
                                rs.getString("nombre"),
                                altura));

                    } while (rs.next());
                }

            } catch (SQLException e) {
                throw new AccesoDatosException("Error SQL(ResultSet)", e);
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("Error SQL(PreparedStatement)", e);
        }

        return listaJugadores;
    }

    // ============================================================
    // EJERCICIO 5 (1 pto) — devuelve null si no existe
    // ============================================================
    @Override
    public JugadorDTO buscarJugador(int id, Connection conn) throws SQLException {
        // TODO: SELECT ... WHERE id_jugador = ?
        // si hay fila → devolver el DTO
        // si no hay → devolver null

        JugadorDTO jugador = null;

        String SQL = """
                SELECT
                    id_jugador,
                    id_equipo,
                    dorsal,
                    nombre,
                    altura_cm
                FROM
                    jugadores
                WHERE
                    id_jugador = ?
                """;

        try (PreparedStatement pstmt = conn.prepareStatement(SQL)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {

                if (rs.next()) {

                    Integer valor_altura = rs.getInt("altura_cm");
                    Integer altura = rs.wasNull() ? null : valor_altura;
                    
                    jugador = new JugadorDTO(
                            rs.getInt("id_jugador"),
                            rs.getInt("id_equipo"),
                            rs.getInt("dorsal"),
                            rs.getString("nombre"),
                            altura);
                }

            } catch (SQLException e) {
                throw new AccesoDatosException("Error SQL(ResultSet)", e);
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("Error SQL(PreparedStatement)", e);
        }

        return jugador;
    }

    // ============================================================
    // EJERCICIO 6 (1 pto)
    // ============================================================
    @Override
    public int actualizarAlturaJugador(int id, int altura, Connection conn)
            throws SQLException {
        // TODO:
        // 1) if (altura < 150 || altura > 230)
        // throw new IllegalArgumentException(
        // "Altura fuera de rango [150,230]: " + altura);
        // 2) UPDATE jugadores SET altura_cm = ? WHERE id_jugador = ?
        // 3) devolver executeUpdate()
            if (altura < 150 || altura > 230) {
                throw new IllegalArgumentException("Altura fuera de rango [150,230]:" + altura);
            }

            String SQL = """
                    UPDATE 
                        jugadores
                    SET
                        altura_cm = ?
                    WHERE
                        id_jugador = ?
                    """;
            
            try (PreparedStatement pstmt = conn.prepareStatement(SQL)) {
                pstmt.setInt(1, altura);
                pstmt.setInt(2, id);

                return pstmt.executeUpdate();
                
            } catch (SQLException e) {
                throw new AccesoDatosException("Error SQL(PreparedStatement)", e);
            }
    }
}