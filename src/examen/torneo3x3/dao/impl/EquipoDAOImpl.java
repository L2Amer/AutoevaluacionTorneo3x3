package examen.torneo3x3.dao.impl;

import examen.torneo3x3.dao.EquipoDAO;
import examen.torneo3x3.dto.EquipoDTO;
import examen.torneo3x3.excepciones.AccesoDatosException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EquipoDAOImpl implements EquipoDAO {

    // ============================================================
    // EJERCICIO 1 (0,5 ptos)
    // ============================================================
    @Override
    public List<EquipoDTO> listarEquipos(Connection conn) throws SQLException {
        // TODO: SELECT id_equipo, nombre FROM equipos ORDER BY nombre
        List<EquipoDTO> listaEquipos = null;

        String SQL = """
                SELECT 
                    id_equipo, nombre 
                FROM 
                    equipos
                ORDER BY
                    nombre
                """;

        try (Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(SQL)) {
            
            if (rs.next()) {
                listaEquipos = new ArrayList<>();

                do {
                    listaEquipos.add(new EquipoDTO(
                        rs.getInt("id_equipo"),
                        rs.getString("nombre")
                    ));
                } while (rs.next());
            }

        } catch (SQLException e) {
            new AccesoDatosException("Error SQL", e);
        }

        return listaEquipos;
    }

    // ============================================================
    // EJERCICIO 2 (1,5 ptos)
    // ============================================================
    @Override
    public int insertarEquipo(EquipoDTO equipo, Connection conn) throws SQLException {
        // TODO: INSERT con RETURN_GENERATED_KEYS + captura de UNIQUE
        //       (mensaje "UNIQUE constraint failed: equipos.nombre")
        //       → AccesoDatosException("Equipo duplicado: " + nombre)

        String SQL = """
                INSERT INTO 
                    equipos (nombre)
                VALUES
                    (?)
                """;

                try (PreparedStatement pstmt = conn.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS)) {

                    pstmt.setString(1, equipo.nombre());
                
                    try {
                        pstmt.executeUpdate();
                    } catch (SQLException e) {
                        throw new AccesoDatosException("Equipo duplicado: " + equipo.nombre(), e);
                    }

                    try (ResultSet rs = pstmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            return rs.getInt(1);
                        }

                    } catch (SQLException e) {
                        throw new AccesoDatosException("Error SQL (ResultSet)", e);
                    }

                } catch (SQLException e) {
                    throw new AccesoDatosException("Error SQL (PreparedStatement)", e);
                }

                return -1;
    }

    // ============================================================
    // COMPLETO (referencia)
    // ============================================================
    @Override
    public boolean eliminarEquipo(int id, Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM jugadores WHERE id_equipo = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) return false;
            }
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM partidos WHERE id_local = ? OR id_visitante = ?")) {
            ps.setInt(1, id);
            ps.setInt(2, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) return false;
            }
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM equipos WHERE id_equipo = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        }
    }
}