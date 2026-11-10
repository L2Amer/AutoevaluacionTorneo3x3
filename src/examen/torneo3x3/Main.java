package examen.torneo3x3;

import examen.torneo3x3.conexion.ConexionDB;
import examen.torneo3x3.dao.*;
import examen.torneo3x3.dao.impl.*;
import examen.torneo3x3.dto.*;
import examen.torneo3x3.excepciones.AccesoDatosException;
import examen.torneo3x3.servicio.TorneoService;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static final Scanner sc = new Scanner(System.in);

    // Composition root: aquí se construyen las implementaciones.
    // El servicio recibe sus DAOs inyectados por constructor.
    // Para consultas sueltas, el Main usa el DAO directamente;
    // para casos de uso con transacción, usa el servicio.
    private static final EquipoDAO      equipoDAO      = new EquipoDAOImpl();
    private static final JugadorDAO     jugadorDAO     = new JugadorDAOImpl();
    private static final PartidoDAO     partidoDAO     = new PartidoDAOImpl();
    private static final EstadisticaDAO estadisticaDAO = new EstadisticaDAOImpl();
    private static final TorneoService  servicio       = new TorneoService(
            partidoDAO, estadisticaDAO, jugadorDAO);

    public static void main(String[] args) {
        int op;
        do {
            mostrarMenu();
            op = leerEntero("Opción: ");
            try {
                switch (op) {
                    case 1  -> t1_listarEquipos();
                    case 2  -> t2_insertarEquipo();
                    case 3  -> t3_buscarPartido();
                    case 4  -> t4_listarJugadores();
                    case 5  -> t5_buscarJugador();
                    case 6  -> t6_actualizarAltura();
                    case 7  -> t7_ranking();
                    case 8  -> t8_insertarEstadistica();
                    case 9  -> t9_cancelarPartido();
                    case 10 -> t10_registrarResultado();
                    case 11 -> t11_eliminarEquipo();
                    case 0  -> System.out.println("Fin.");
                    default -> System.out.println("Opción no válida.");
                }
            } catch (AccesoDatosException e) {
                System.err.println("[AccesoDatos] " + e.getMessage());
                if (e.getCause() != null)
                    System.err.println("  causa: " + e.getCause().getMessage());
            } catch (IllegalArgumentException e) {
                System.err.println("[Validación] " + e.getMessage());
            } catch (SQLException e) {
                System.err.println("[SQL] " + e.getMessage());
            }
        } while (op != 0);
    }

    private static void mostrarMenu() {
        System.out.println("""
            
            ===== TORNEO 3x3 — Pruebas =====
             1) Listar equipos                     (ej. 1)
             2) Insertar equipo                    (ej. 2)
             3) Buscar partido por id              (ej. 3)
             4) Listar jugadores de un equipo      (ej. 4)
             5) Buscar jugador por id              (ej. 5)
             6) Actualizar altura de jugador       (ej. 6)
             7) Ranking de anotadores              (ej. 7)
             8) Insertar estadística               (ej. 8)
             9) Cancelar partido (transacción)     (ej. 9)
            10) Registrar resultado (REFERENCIA)
            11) Eliminar equipo     (REFERENCIA)
             0) Salir
            """);
    }

    private static void t1_listarEquipos() throws SQLException {
        try (Connection conn = ConexionDB.getConexion()) {
            List<EquipoDTO> lista = equipoDAO.listarEquipos(conn);
            if (lista.isEmpty()) { System.out.println("(sin equipos)"); return; }
            lista.forEach(e -> System.out.printf("  [%d] %s%n", e.idEquipo(), e.nombre()));
            System.out.println("Total: " + lista.size());
        }
    }

    private static void t2_insertarEquipo() throws SQLException {
        String nombre = leerTexto("Nombre del nuevo equipo: ");
        try (Connection conn = ConexionDB.getConexion()) {
            int id = equipoDAO.insertarEquipo(new EquipoDTO(0, nombre), conn);
            System.out.println("Equipo insertado con id = " + id);
        }
    }

    private static void t3_buscarPartido() throws SQLException {
        int id = leerEntero("Id partido: ");
        try (Connection conn = ConexionDB.getConexion()) {
            PartidoDTO p = partidoDAO.buscarPartido(id, conn);
            if (p == null) { System.out.println("No existe el partido " + id); return; }
            System.out.printf("  #%d  %s vs %s  (%s)  %s-%s  [%s]%n",
                p.idPartido(), p.nombreLocal(), p.nombreVisitante(), p.fecha(),
                p.puntosLocal() == null ? "-" : p.puntosLocal(),
                p.puntosVisitante() == null ? "-" : p.puntosVisitante(),
                p.estado());
        }
    }

    private static void t4_listarJugadores() throws SQLException {
        int idEquipo = leerEntero("Id equipo: ");
        try (Connection conn = ConexionDB.getConexion()) {
            List<JugadorDTO> lista = jugadorDAO.listarJugadoresDeEquipo(idEquipo, conn);
            if (lista.isEmpty()) { System.out.println("(equipo sin jugadores)"); return; }
            lista.forEach(j -> System.out.printf("  #%-2d %-20s altura=%s%n",
                j.dorsal(), j.nombre(),
                j.alturaCm() == null ? "-" : j.alturaCm()));
        }
    }

    private static void t5_buscarJugador() throws SQLException {
        int id = leerEntero("Id jugador: ");
        try (Connection conn = ConexionDB.getConexion()) {
            JugadorDTO j = jugadorDAO.buscarJugador(id, conn);
            if (j == null) {
                System.out.println("No existe el jugador " + id);
            } else {
                System.out.printf("  %s (dorsal %d, equipo %d, altura %s)%n",
                    j.nombre(), j.dorsal(), j.idEquipo(),
                    j.alturaCm() == null ? "-" : j.alturaCm());
            }
        }
    }

    private static void t6_actualizarAltura() throws SQLException {
        int id = leerEntero("Id jugador: ");
        int altura = leerEntero("Nueva altura (150-230): ");
        try (Connection conn = ConexionDB.getConexion()) {
            int filas = jugadorDAO.actualizarAlturaJugador(id, altura, conn);
            System.out.println("Filas afectadas: " + filas);
        }
    }

    private static void t7_ranking() throws SQLException {
        try (Connection conn = ConexionDB.getConexion()) {
            List<RankingDTO> ranking = partidoDAO.rankingAnotadores(conn);
            System.out.println("  #  Jugador                 Equipo              Puntos");
            System.out.println("  -- ----------------------- ------------------- ------");
            int i = 1;
            for (RankingDTO r : ranking) {
                System.out.printf("  %-2d %-23s %-19s %d%n",
                    i++, r.nombreJugador(), r.nombreEquipo(), r.totalPuntos());
            }
        }
    }

    private static void t8_insertarEstadistica() throws SQLException {
        int idPartido = leerEntero("Id partido: ");
        int idJugador = leerEntero("Id jugador: ");
        int puntos    = leerEntero("Puntos: ");
        try (Connection conn = ConexionDB.getConexion()) {
            estadisticaDAO.insertar(new EstadisticaDTO(idPartido, idJugador, puntos), conn);
            System.out.println("Estadística insertada.");
        }
    }

    private static void t9_cancelarPartido() {
        int id = leerEntero("Id partido a cancelar: ");
        servicio.cancelarPartido(id);
        System.out.println("Partido cancelado correctamente.");
    }

    private static void t10_registrarResultado() {
        int idPartido = leerEntero("Id partido PENDIENTE: ");
        int pl        = leerEntero("Puntos local: ");
        int pv        = leerEntero("Puntos visitante: ");
        int j1        = leerEntero("Id jugador local: ");
        int j2        = leerEntero("Id jugador visitante: ");
        List<EstadisticaDTO> ests = List.of(
            new EstadisticaDTO(idPartido, j1, pl),
            new EstadisticaDTO(idPartido, j2, pv));
        servicio.registrarResultado(idPartido, pl, pv, ests);
        System.out.println("Resultado registrado.");
    }

    private static void t11_eliminarEquipo() throws SQLException {
        int id = leerEntero("Id equipo a eliminar: ");
        try (Connection conn = ConexionDB.getConexion()) {
            boolean ok = equipoDAO.eliminarEquipo(id, conn);
            System.out.println(ok
                ? "Equipo eliminado."
                : "No se puede eliminar: tiene jugadores o partidos.");
        }
    }

    private static int leerEntero(String msg) {
        while (true) {
            try {
                System.out.print(msg);
                return Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Entrada no válida.");
            }
        }
    }

    private static String leerTexto(String msg) {
        System.out.print(msg);
        return sc.nextLine().trim();
    }
}