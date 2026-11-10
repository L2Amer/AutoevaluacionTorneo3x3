package examen.torneo3x3.dao;

import examen.torneo3x3.dto.EstadisticaDTO;

import java.sql.Connection;
import java.sql.SQLException;

public interface EstadisticaDAO {

    void insertar(EstadisticaDTO e, Connection conn) throws SQLException;

    /** @return filas borradas */
    int borrarPorPartido(int idPartido, Connection conn) throws SQLException;
}