package examen.torneo3x3.dao.impl;

import examen.torneo3x3.dao.EstadisticaDAO;
import examen.torneo3x3.dto.EstadisticaDTO;
import examen.torneo3x3.excepciones.AccesoDatosException;

import java.sql.*;

public class EstadisticaDAOImpl implements EstadisticaDAO {

    // ============================================================
    // EJERCICIO 8 (0,5 ptos)
    // ============================================================
    @Override
    public void insertar(EstadisticaDTO e, Connection conn) throws SQLException {
        // TODO: INSERT INTO estadisticas(id_partido, id_jugador, puntos) VALUES (?,?,?)

        String SQL = """
                INSERT INTO 
                    estadisticas(id_partido, id_jugador, puntos) 
                VALUES 
                    (?,?,?)
                """;

        try (PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setInt(1, e.idPartido());
            pstmt.setInt(2, e.idJugador());
            pstmt.setInt(3, e.puntos());
            
            pstmt.executeUpdate();
            
        } catch (SQLException er) {
            throw new AccesoDatosException("Error SQL(PreparedStatement)", er);
        }
        
    }

    // ============================================================
    // COMPLETO — lo necesita cancelarPartido (ejercicio 9)
    // ============================================================
    @Override
    public int borrarPorPartido(int idPartido, Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM estadisticas WHERE id_partido = ?")) {
            ps.setInt(1, idPartido);
            return ps.executeUpdate();
        }
    }
}