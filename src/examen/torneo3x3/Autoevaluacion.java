package examen.torneo3x3;

import examen.torneo3x3.conexion.ConexionDB;
import examen.torneo3x3.dao.*;
import examen.torneo3x3.dao.impl.*;
import examen.torneo3x3.dto.*;
import examen.torneo3x3.excepciones.AccesoDatosException;
import examen.torneo3x3.servicio.TorneoService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Autoevaluación del examen "Torneo 3x3".
 *
 * Ejecuta pruebas contra la BD real y produce un informe con la puntuación
 * desglosada por ejercicio (sobre 10 puntos).
 *
 * IMPORTANTE:
 *  - No modifica el código del alumno; solo lo invoca.
 *  - Las pruebas que escriben en BD limpian sus datos al terminar.
 *  - Si un ejercicio no está implementado (UnsupportedOperationException),
 *    se marca como NO PRESENTADO y se asignan 0 puntos.
 */
public class Autoevaluacion {

    // --- DAOs y servicio bajo prueba ---
    private static final EquipoDAO      equipoDAO      = new EquipoDAOImpl();
    private static final JugadorDAO     jugadorDAO     = new JugadorDAOImpl();
    private static final PartidoDAO     partidoDAO     = new PartidoDAOImpl();
    private static final EstadisticaDAO estadisticaDAO = new EstadisticaDAOImpl();
    private static final TorneoService  servicio       = new TorneoService(
            partidoDAO, estadisticaDAO, jugadorDAO);

    // --- Resultados ---
    private static final List<Resultado> resultados = new ArrayList<>();
    private static double totalObtenido = 0.0;
    private static double totalPosible  = 0.0;

    public static void main(String[] args) {
        System.out.println("==============================================");
        System.out.println(" AUTOEVALUACIÓN - TORNEO 3x3");
        System.out.println("==============================================");
        System.out.println();

        // Ejecutar cada prueba de forma aislada; una excepción
        // inesperada no debe tumbar el resto del informe.
        ejecutar("Ejercicio 1 - listarEquipos",                 0.5, Autoevaluacion::ej1_listarEquipos);
        ejecutar("Ejercicio 2 - insertarEquipo (UNIQUE)",       1.5, Autoevaluacion::ej2_insertarEquipo);
        ejecutar("Ejercicio 3 - buscarPartido (JOIN + null)",   1.5, Autoevaluacion::ej3_buscarPartido);
        ejecutar("Ejercicio 4 - listarJugadoresDeEquipo",       0.5, Autoevaluacion::ej4_listarJugadores);
        ejecutar("Ejercicio 5 - buscarJugador (null)",          1.0, Autoevaluacion::ej5_buscarJugador);
        ejecutar("Ejercicio 6 - actualizarAlturaJugador",       1.0, Autoevaluacion::ej6_actualizarAltura);
        ejecutar("Ejercicio 7 - rankingAnotadores (GROUP BY)",  2.0, Autoevaluacion::ej7_ranking);
        ejecutar("Ejercicio 8 - insertar estadística",          0.5, Autoevaluacion::ej8_insertarEstadistica);
        ejecutar("Ejercicio 9 - cancelarPartido (transacción)", 2.5, Autoevaluacion::ej9_cancelarPartido);

        imprimirInforme();
    }

    // ============================================================
    // INFRAESTRUCTURA DE EJECUCIÓN
    // ============================================================
    @FunctionalInterface
    interface Prueba { void ejecutar() throws Exception; }

    private static void ejecutar(String nombre, double puntos, Prueba prueba) {
        totalPosible += puntos;
        long t0 = System.currentTimeMillis();
        try {
            prueba.ejecutar();
            resultados.add(new Resultado(nombre, puntos, puntos, "OK", null,
                    System.currentTimeMillis() - t0));
            totalObtenido += puntos;
        } catch (UnsupportedOperationException e) {
            resultados.add(new Resultado(nombre, puntos, 0.0,
                    "NO PRESENTADO", "Método no implementado (TODO)", 
                    System.currentTimeMillis() - t0));
        } catch (AssertionError e) {
            resultados.add(new Resultado(nombre, puntos, 0.0,
                    "FALLO", e.getMessage(), System.currentTimeMillis() - t0));
        } catch (Exception e) {
            resultados.add(new Resultado(nombre, puntos, 0.0,
                    "ERROR", e.getClass().getSimpleName() + ": " + e.getMessage(),
                    System.currentTimeMillis() - t0));
        }
    }

    // ============================================================
    // EJERCICIO 1 - listarEquipos (0,5)
    // ============================================================
    private static void ej1_listarEquipos() throws Exception {
        try (Connection conn = ConexionDB.getConexion()) {
            List<EquipoDTO> lista = equipoDAO.listarEquipos(conn);
            assertNotNull("La lista no puede ser null", lista);
            assertTrue("Debe haber al menos 9 equipos en la BD de prueba (hay "
                    + lista.size() + ")", lista.size() >= 9);
            // Verificar orden alfabético
            for (int i = 1; i < lista.size(); i++) {
                String a = lista.get(i - 1).nombre();
                String b = lista.get(i).nombre();
                assertTrue("Los equipos deben estar ordenados por nombre: '"
                        + a + "' > '" + b + "'", a.compareToIgnoreCase(b) <= 0);
            }
            // Verificar que los DTOs tienen nombre no vacío
            for (EquipoDTO e : lista) {
                assertNotNull("Equipo con nombre null en la lista", e.nombre());
                assertTrue("Equipo con nombre vacío", !e.nombre().isBlank());
            }
        }
    }

    // ============================================================
    // EJERCICIO 2 - insertarEquipo (1,5)
    // ============================================================
    private static void ej2_insertarEquipo() throws Exception {
        String nombreUnico = "AutoTest-" + UUID.randomUUID().toString().substring(0, 8);
        int idGenerado = -1;

        try (Connection conn = ConexionDB.getConexion()) {
            // 2.1) Insertar un equipo nuevo
            idGenerado = equipoDAO.insertarEquipo(new EquipoDTO(0, nombreUnico), conn);
            assertTrue("El id generado debe ser > 0, fue: " + idGenerado, idGenerado > 0);

            // 2.2) Comprobar que se ha insertado realmente
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) FROM equipos WHERE id_equipo = ? AND nombre = ?")) {
                ps.setInt(1, idGenerado);
                ps.setString(2, nombreUnico);
                try (var rs = ps.executeQuery()) {
                    assertTrue("El equipo insertado no aparece en la BD",
                            rs.next() && rs.getInt(1) == 1);
                }
            }

            // 2.3) Insertar duplicado → debe lanzar AccesoDatosException
            boolean lanzo = false;
            try {
                equipoDAO.insertarEquipo(new EquipoDTO(0, nombreUnico), conn);
            } catch (AccesoDatosException expected) {
                lanzo = true;
            }
            assertTrue("Insertar un nombre duplicado debe lanzar "
                    + "AccesoDatosException (UNIQUE constraint)", lanzo);

            // 2.4) El duplicado no debe haber insertado una segunda fila
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) FROM equipos WHERE nombre = ?")) {
                ps.setString(1, nombreUnico);
                try (var rs = ps.executeQuery()) {
                    assertTrue("El intento duplicado no debe insertar filas",
                            rs.next() && rs.getInt(1) == 1);
                }
            }
        } finally {
            // Limpieza: borrar el equipo de prueba
            if (idGenerado > 0) {
                try (Connection c = ConexionDB.getConexion();
                     PreparedStatement ps = c.prepareStatement(
                         "DELETE FROM equipos WHERE id_equipo = ?")) {
                    ps.setInt(1, idGenerado);
                    ps.executeUpdate();
                } catch (SQLException ignored) { }
            }
        }
    }

    // ============================================================
    // EJERCICIO 3 - buscarPartido (1,5)
    // ============================================================
    private static void ej3_buscarPartido() throws Exception {
        try (Connection conn = ConexionDB.getConexion()) {
            // 3.1) Buscar el partido 21 (existe, PENDIENTE)
            PartidoDTO p21 = partidoDAO.buscarPartido(21, conn);
            assertNotNull("El partido 21 debe existir (devuelve DTO, no null)", p21);
            assertTrue("El id del partido debe ser 21", p21.idPartido() == 21);
            assertNotNull("El nombre del equipo local no puede ser null",
                    p21.nombreLocal());
            assertNotNull("El nombre del equipo visitante no puede ser null",
                    p21.nombreVisitante());
            assertTrue("El estado del partido 21 debe ser PENDIENTE",
                    "PENDIENTE".equals(p21.estado()));
            assertTrue("En partido PENDIENTE, puntosLocal debe ser null",
                    p21.puntosLocal() == null);
            assertTrue("En partido PENDIENTE, puntosVisitante debe ser null",
                    p21.puntosVisitante() == null);

            // 3.2) Buscar un partido JUGADO con puntos
            PartidoDTO jugado = null;
            for (int id = 1; id <= 20; id++) {
                PartidoDTO p = partidoDAO.buscarPartido(id, conn);
                if (p != null && "JUGADO".equals(p.estado())) {
                    jugado = p;
                    break;
                }
            }
            assertNotNull("Debe existir al menos un partido JUGADO en la BD", jugado);
            assertNotNull("En partido JUGADO, puntosLocal no debe ser null",
                    jugado.puntosLocal());
            assertNotNull("En partido JUGADO, puntosVisitante no debe ser null",
                    jugado.puntosVisitante());

            // 3.3) Buscar un id inexistente → null
            PartidoDTO noExiste = partidoDAO.buscarPartido(999_999, conn);
            assertTrue("Buscar un partido inexistente debe devolver null, no "
                    + (noExiste == null ? "null" : "un DTO"), noExiste == null);
        }
    }

    // ============================================================
    // EJERCICIO 4 - listarJugadoresDeEquipo (0,5)
    // ============================================================
    private static void ej4_listarJugadores() throws Exception {
        try (Connection conn = ConexionDB.getConexion()) {
            List<JugadorDTO> titanes = jugadorDAO.listarJugadoresDeEquipo(1, conn);
            assertNotNull("La lista no puede ser null", titanes);
            assertTrue("Titanes 3x3 debe tener 5 jugadores (hay "
                    + titanes.size() + ")", titanes.size() == 5);

            // Verificar orden por dorsal
            for (int i = 1; i < titanes.size(); i++) {
                assertTrue("Los jugadores deben estar ordenados por dorsal",
                        titanes.get(i - 1).dorsal() <= titanes.get(i).dorsal());
            }
            // Verificar que todos son del equipo 1
            for (JugadorDTO j : titanes) {
                assertTrue("Todos los jugadores deben ser del equipo 1, "
                        + "encontrado id_equipo=" + j.idEquipo(), j.idEquipo() == 1);
            }
            // Equipo sin jugadores → lista vacía, no null
            List<JugadorDTO> novatos = jugadorDAO.listarJugadoresDeEquipo(2, conn);
            assertNotNull("Equipo sin jugadores debe devolver lista vacía, no null",
                    novatos);
            assertTrue("Equipo sin jugadores debe devolver lista vacía",
                    novatos.isEmpty());
        }
    }

    // ============================================================
    // EJERCICIO 5 - buscarJugador (1,0)
    // ============================================================
    private static void ej5_buscarJugador() throws Exception {
        try (Connection conn = ConexionDB.getConexion()) {
            JugadorDTO j1 = jugadorDAO.buscarJugador(1, conn);
            assertNotNull("El jugador 1 debe existir (no null)", j1);
            assertTrue("El id del jugador debe ser 1", j1.idJugador() == 1);
            assertNotNull("El nombre del jugador no puede ser null", j1.nombre());
            assertTrue("El jugador debe pertenecer al equipo 1", j1.idEquipo() == 1);

            JugadorDTO noExiste = jugadorDAO.buscarJugador(999_999, conn);
            assertTrue("Buscar un jugador inexistente debe devolver null",
                    noExiste == null);
        }
    }

    // ============================================================
    // EJERCICIO 6 - actualizarAlturaJugador (1,0)
    // ============================================================
    private static void ej6_actualizarAltura() throws Exception {
        try (Connection conn = ConexionDB.getConexion()) {
            // 6.1) Validación: altura fuera de rango → IllegalArgumentException
            boolean lanzoBajo = false;
            try {
                jugadorDAO.actualizarAlturaJugador(1, 149, conn);
            } catch (IllegalArgumentException expected) { lanzoBajo = true; }
            assertTrue("Altura 149 debe lanzar IllegalArgumentException", lanzoBajo);

            boolean lanzoAlto = false;
            try {
                jugadorDAO.actualizarAlturaJugador(1, 231, conn);
            } catch (IllegalArgumentException expected) { lanzoAlto = true; }
            assertTrue("Altura 231 debe lanzar IllegalArgumentException", lanzoAlto);

            // 6.2) Altura válida → 1 fila afectada
            int filas = jugadorDAO.actualizarAlturaJugador(1, 190, conn);
            assertTrue("Actualizar altura válida debe afectar 1 fila, afectó: "
                    + filas, filas == 1);

            // 6.3) Comprobar que la altura se ha actualizado de verdad
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT altura_cm FROM jugadores WHERE id_jugador = 1")) {
                try (var rs = ps.executeQuery()) {
                    assertTrue("El jugador 1 debe existir", rs.next());
                    assertTrue("La altura debe haberse actualizado a 190",
                            rs.getInt(1) == 190);
                }
            }

            // 6.4) Id inexistente → 0 filas (no excepción)
            int filasNoExiste = jugadorDAO.actualizarAlturaJugador(999_999, 190, conn);
            assertTrue("Actualizar un jugador inexistente debe devolver 0 filas, "
                    + "devolvió: " + filasNoExiste, filasNoExiste == 0);

            // 6.5) Restaurar altura original del jugador 1 (185)
            jugadorDAO.actualizarAlturaJugador(1, 185, conn);
        }
    }

    // ============================================================
    // EJERCICIO 7 - rankingAnotadores (2,0)
    // ============================================================
    private static void ej7_ranking() throws Exception {
        try (Connection conn = ConexionDB.getConexion()) {
            List<RankingDTO> ranking = partidoDAO.rankingAnotadores(conn);
            assertNotNull("El ranking no puede ser null", ranking);
            assertTrue("El ranking no puede estar vacío (hay partidos JUGADO)",
                    !ranking.isEmpty());

            // 7.1) Orden descendente por puntos
            for (int i = 1; i < ranking.size(); i++) {
                assertTrue("El ranking debe estar ordenado por puntos desc: "
                        + ranking.get(i - 1).totalPuntos() + " < "
                        + ranking.get(i).totalPuntos(),
                        ranking.get(i - 1).totalPuntos() >= ranking.get(i).totalPuntos());
            }

            // 7.2) Todos los totales > 0 y los campos rellenos
            for (RankingDTO r : ranking) {
                assertTrue("Cada total debe ser > 0", r.totalPuntos() > 0);
                assertNotNull("El nombre del jugador no puede ser null",
                        r.nombreJugador());
                assertNotNull("El nombre del equipo no puede ser null",
                        r.nombreEquipo());
            }

            // 7.3) Comprobación cruzada: calcular el total del primer jugador
            //      con una consulta independiente y compararlo.
            RankingDTO primero = ranking.get(0);
            int esperado;
            try (PreparedStatement ps = conn.prepareStatement("""
                SELECT SUM(es.puntos)
                  FROM estadisticas es
                  JOIN jugadores j ON j.id_jugador = es.id_jugador
                  JOIN partidos  p ON p.id_partido = es.id_partido
                 WHERE j.nombre = ? AND p.estado = 'JUGADO'
                """)) {
                ps.setString(1, primero.nombreJugador());
                try (var rs = ps.executeQuery()) {
                    assertTrue("El jugador del top debe existir", rs.next());
                    esperado = rs.getInt(1);
                }
            }
            assertTrue("El total de '" + primero.nombreJugador() + "' debe ser "
                    + esperado + ", el DAO devolvió " + primero.totalPuntos(),
                    esperado == primero.totalPuntos());
        }
    }

    // ============================================================
    // EJERCICIO 8 - insertar estadística (0,5)
    // ============================================================
    private static void ej8_insertarEstadistica() throws Exception {
        try (Connection conn = ConexionDB.getConexion()) {
            // Insertar estadística para el partido PENDIENTE 21 con jugador 1
            // (el partido está PENDIENTE, pero insertar estadísticas no valida
            //  estado; eso lo hace el servicio. El DAO solo inserta.)
            int idEst = -1;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) FROM estadisticas WHERE id_partido = ? AND id_jugador = ?")) {
                ps.setInt(1, 21);
                ps.setInt(2, 1);
                try (var rs = ps.executeQuery()) {
                    assertTrue("La estadística de prueba no debe existir antes",
                            rs.next() && rs.getInt(1) == 0);
                }
            }

            estadisticaDAO.insertar(new EstadisticaDTO(21, 1, 7), conn);

            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT id_estadistica, puntos FROM estadisticas "
                  + "WHERE id_partido = ? AND id_jugador = ?")) {
                ps.setInt(1, 21);
                ps.setInt(2, 1);
                try (var rs = ps.executeQuery()) {
                    assertTrue("La estadística debe haberse insertado", rs.next());
                    assertTrue("Los puntos deben ser 7", rs.getInt("puntos") == 7);
                    idEst = rs.getInt("id_estadistica");
                }
            }

            // Limpieza
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM estadisticas WHERE id_estadistica = ?")) {
                ps.setInt(1, idEst);
                ps.executeUpdate();
            }
        }
    }

    // ============================================================
    // EJERCICIO 9 - cancelarPartido (2,5)
    // ============================================================
    private static void ej9_cancelarPartido() throws Exception {
        // Creamos un partido PENDIENTE ad hoc para no depender de los datos
        // del enunciado (que el alumno puede haber modificado).
        int idPartidoNuevo = -1;
        try (Connection conn = ConexionDB.getConexion()) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO partidos(id_local, id_visitante, fecha, estado) "
                  + "VALUES (1, 3, '2030-01-01', 'PENDIENTE')",
                    PreparedStatement.RETURN_GENERATED_KEYS)) {
                ps.executeUpdate();
                try (var keys = ps.getGeneratedKeys()) {
                    assertTrue("No se pudo crear el partido de prueba", keys.next());
                    idPartidoNuevo = keys.getInt(1);
                }
            }
        }

        try {
            // 9.1) Cancelar el partido PENDIENTE → debe funcionar
            servicio.cancelarPartido(idPartidoNuevo);

            try (Connection conn = ConexionDB.getConexion();
                 PreparedStatement ps = conn.prepareStatement(
                     "SELECT estado FROM partidos WHERE id_partido = ?")) {
                ps.setInt(1, idPartidoNuevo);
                try (var rs = ps.executeQuery()) {
                    assertTrue("El partido debe existir tras cancelar", rs.next());
                    assertTrue("El estado debe ser CANCELADO, fue: "
                            + rs.getString(1), "CANCELADO".equals(rs.getString(1)));
                }
            }

            // 9.2) Cancelar el mismo partido otra vez → debe fallar
            //      (ya no está PENDIENTE) y NO debe cambiar nada.
            boolean lanzo = false;
            try {
                servicio.cancelarPartido(idPartidoNuevo);
            } catch (AccesoDatosException expected) {
                lanzo = true;
            }
            assertTrue("Cancelar un partido ya CANCELADO debe lanzar "
                    + "AccesoDatosException", lanzo);

            // 9.3) Cancelar un partido inexistente → AccesoDatosException
            boolean lanzoNoExiste = false;
            try {
                servicio.cancelarPartido(999_999);
            } catch (AccesoDatosException expected) {
                lanzoNoExiste = true;
            }
            assertTrue("Cancelar un partido inexistente debe lanzar "
                    + "AccesoDatosException", lanzoNoExiste);

            // 9.4) Comprobar rollback: el partido inexistente no debe haber
            //      dejado rastro (no hay nada que comprobar directamente,
            //      pero verificamos que la BD sigue siendo consultable).
            try (Connection conn = ConexionDB.getConexion();
                 PreparedStatement ps = conn.prepareStatement(
                     "SELECT COUNT(*) FROM partidos")) {
                try (var rs = ps.executeQuery()) {
                    assertTrue("La BD debe seguir siendo consultable tras los rollbacks",
                            rs.next());
                }
            }
        } finally {
            // Limpieza
            if (idPartidoNuevo > 0) {
                try (Connection c = ConexionDB.getConexion()) {
                    try (PreparedStatement ps = c.prepareStatement(
                            "DELETE FROM estadisticas WHERE id_partido = ?")) {
                        ps.setInt(1, idPartidoNuevo);
                        ps.executeUpdate();
                    }
                    try (PreparedStatement ps = c.prepareStatement(
                            "DELETE FROM partidos WHERE id_partido = ?")) {
                        ps.setInt(1, idPartidoNuevo);
                        ps.executeUpdate();
                    }
                } catch (SQLException ignored) { }
            }
        }
    }

    // ============================================================
    // INFORME
    // ============================================================
    private static void imprimirInforme() {
        System.out.println();
        System.out.println("==============================================");
        System.out.println(" INFORME DE AUTOEVALUACIÓN");
        System.out.println("==============================================");
        System.out.printf("%-55s %8s %8s%n", "Prueba", "Puntos", "Estado");
        System.out.println("-".repeat(75));

        for (Resultado r : resultados) {
            String estado = r.estado;
            if (r.detalle != null && !r.detalle.isBlank()) {
                estado += " (" + abreviar(r.detalle, 40) + ")";
            }
            System.out.printf("%-55s %5.1f/%-2.1f %s%n",
                    abreviar(r.nombre, 55), r.obtenido, r.posible, estado);
        }
        System.out.println("-".repeat(75));
        System.out.printf("%-55s %5.1f/%-4.1f%n",
                "TOTAL", totalObtenido, totalPosible);
        System.out.println();

        double nota = totalObtenido;   // sobre 10
        System.out.printf("NOTA ESTIMADA: %.1f / 10  (%s)%n",
                nota, calificacionCualitativa(nota));
        System.out.println();

        // Resumen de fallos para que el alumno sepa qué revisar
        List<Resultado> fallos = resultados.stream()
                .filter(r -> r.obtenido < r.posible)
                .toList();
        if (fallos.isEmpty()) {
            System.out.println("¡Enhorabuena! No hay fallos detectados.");
        } else {
            System.out.println("PENDIENTE DE REVISAR:");
            for (Resultado r : fallos) {
                System.out.printf("  - %s → %s%n", r.nombre,
                        r.detalle == null ? r.estado : r.detalle);
            }
        }
        System.out.println();
        System.out.println("NOTA: esta autoevaluación es orientativa. La nota");
        System.out.println("      definitiva la asigna el profesor revisando el código.");
    }

    private static String calificacionCualitativa(double nota) {
        if (nota >= 9) return "Sobresaliente";
        if (nota >= 7) return "Notable";
        if (nota >= 6) return "Bien";
        if (nota >= 5) return "Aprobado";
        return "Suspenso";
    }

    private static String abreviar(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 3) + "...";
    }

    // ============================================================
    // UTILIDADES DE ASERCIÓN
    // ============================================================
    private static void assertTrue(String msg, boolean cond) {
        if (!cond) throw new AssertionError(msg);
    }

    private static void assertNotNull(String msg, Object o) {
        if (o == null) throw new AssertionError(msg);
    }

    // ============================================================
    // ESTRUCTURA DE RESULTADO
    // ============================================================
    private static class Resultado {
        final String nombre;
        final double posible;
        final double obtenido;
        final String estado;
        final String detalle;
        final long ms;

        Resultado(String nombre, double posible, double obtenido,
                  String estado, String detalle, long ms) {
            this.nombre = nombre;
            this.posible = posible;
            this.obtenido = obtenido;
            this.estado = estado;
            this.detalle = detalle;
            this.ms = ms;
        }
    }
}