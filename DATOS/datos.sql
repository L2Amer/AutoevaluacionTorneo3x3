-- El PRAGMA foreign_keys=ON debe activarse en cada conexión;
PRAGMA foreign_keys = ON;

-- ============================================================
-- EQUIPOS (8)
-- ============================================================
INSERT INTO equipos (nombre) VALUES
 ('Titanes 3x3'),      -- id 1  (con jugadores y partidos)
 ('Novatos BC'),       -- id 2  (sin jugadores ni partidos → borrable)
 ('Halcones Urbanos'), -- id 3
 ('Rayo Callejero'),   -- id 4
 ('Marea Azul'),       -- id 5
 ('Fuego Norte'),      -- id 6
 ('Canasta Libre'),    -- id 7
 ('Cementerio Street'),-- id 8
 ('Tormenta BC');      -- id 9  (con partidos pero sin jugadores)

-- ============================================================
-- JUGADORES (~40). Dorsales únicos por equipo.
-- Titanes (id 1) con 5 jugadores; el resto entre 4 y 5.
-- ============================================================
INSERT INTO jugadores (id_equipo, dorsal, nombre, altura_cm) VALUES
 -- Titanes 3x3 (id 1)
 (1, 4,  'Mario Peña',      185),
 (1, 7,  'Lucas Vidal',     192),
 (1, 10, 'Iker Romero',     178),
 (1, 23, 'Dani Soto',       200),
 (1, 33, 'Hugo Lara',       188),

 -- Novatos BC (id 2): SIN jugadores (equipo recién creado)

 -- Halcones Urbanos (id 3)
 (3, 1,  'Álex Marín',      190),
 (3, 5,  'Pablo Nieto',     183),
 (3, 8,  'Rubén Gil',       196),
 (3, 12, 'Sergio Cano',     175),
 (3, 21, 'Adrián Vega',     187),

 -- Rayo Callejero (id 4)
 (4, 2,  'Diego Fuentes',   181),
 (4, 6,  'Jorge Prieto',    194),
 (4, 9,  'Nacho Reyes',     177),
 (4, 15, 'Emilio Cruz',     189),

 -- Marea Azul (id 5)
 (5, 3,  'Bruno Salas',     199),
 (5, 11, 'Gonzalo Prado',   186),
 (5, 14, 'César Mora',      180),
 (5, 20, 'Fermín Ortiz',    192),
 (5, 30, 'Álvaro Tomé',     184),

 -- Fuego Norte (id 6)
 (6, 0,  'Rodrigo Paz',     188),
 (6, 4,  'Tomás Reyes',     176),
 (6, 13, 'Kevin Alonso',    193),
 (6, 22, 'Javier Puente',   190),
 (6, 32, 'Óscar Cano',      182),

 -- Canasta Libre (id 7)
 (7, 5,  'Andrés Núñez',    179),
 (7, 7,  'Cristian Solís',  191),
 (7, 16, 'Guille Marín',    185),
 (7, 24, 'Ismael Roldán',   197),

 -- Cementerio Street (id 8)
 (8, 2,  'Felipe Duarte',   183),
 (8, 6,  'Marcos Cuesta',   188),
 (8, 10, 'Víctor Ríos',     194),
 (8, 18, 'Saúl Benítez',    176),
 (8, 25, 'Raúl Carmona',    190);

 -- Tormenta BC (id 9): SIN jugadores (para probar Tarea 7 con partidos)

-- ============================================================
-- PARTIDOS (20 jugados + 1 pendiente = 21). Fechas 2024.
-- ============================================================
INSERT INTO partidos (id_local, id_visitante, fecha, puntos_local, puntos_visitante, estado) VALUES
 (1, 3, '2024-03-02', 21, 18, 'JUGADO'),
 (1, 4, '2024-03-09', 17, 15, 'JUGADO'),
 (1, 5, '2024-03-16', 22, 19, 'JUGADO'),
 (1, 6, '2024-03-23', 14, 16, 'JUGADO'),   -- Titanes pierde este
 (3, 4, '2024-04-06', 20, 12, 'JUGADO'),
 (3, 5, '2024-04-13', 18, 18, 'JUGADO'),   -- empate
 (4, 6, '2024-04-20', 11, 21, 'JUGADO'),
 (5, 7, '2024-04-27', 19, 17, 'JUGADO'),
 (6, 8, '2024-05-04', 22, 20, 'JUGADO'),
 (7, 8, '2024-05-11', 15, 13, 'JUGADO'),
 (3, 6, '2024-05-18', 16, 19, 'JUGADO'),
 (4, 5, '2024-05-25', 13, 22, 'JUGADO'),
 (7, 1, '2024-06-01', 12, 21, 'JUGADO'),
 (8, 3, '2024-06-08', 18, 20, 'JUGADO'),
 (5, 1, '2024-06-15', 17, 17, 'JUGADO'),
 (6, 4, '2024-06-22', 21, 9,  'JUGADO'),
 (8, 7, '2024-06-29', 16, 15, 'JUGADO'),
 (3, 5, '2024-07-06', 19, 21, 'JUGADO'),
 (4, 8, '2024-07-13', 14, 18, 'JUGADO'),
 (6, 7, '2024-07-20', 23, 19, 'JUGADO'),

 -- Partido 21: PENDIENTE listo para Tarea 5
 (1, 8, '2024-08-10', NULL, NULL, 'PENDIENTE');

-- Partido de ejemplo para "Tormenta BC" (id 9): PENDIENTE para probar Tarea 7
INSERT INTO partidos (id_local, id_visitante, fecha, estado)
VALUES (9, 2, '2024-09-01', 'PENDIENTE');   -- id 22

-- ============================================================
-- ESTADÍSTICAS de los partidos JUGADOS.
-- Reparto: cada jugador de cada equipo marca varios partidos.
-- Valores sencillos para que el ranking sea verificable a mano.
-- ============================================================

-- Partido 1: 1 vs 3 (21-18)
INSERT INTO estadisticas (id_partido, id_jugador, puntos) VALUES
 (1, 1, 6), (1, 2, 5), (1, 3, 4), (1, 4, 6),          -- Titanes
 (1, 11, 5), (1, 12, 5), (1, 13, 4), (1, 14, 4);       -- Halcones

-- Partido 2: 1 vs 4 (17-15)
INSERT INTO estadisticas (id_partido, id_jugador, puntos) VALUES
 (2, 1, 4), (2, 2, 4), (2, 4, 5), (2, 5, 4),           -- Titanes
 (2, 16, 4), (2, 17, 5), (2, 18, 3), (2, 19, 3);       -- Rayo

-- Partido 3: 1 vs 5 (22-19)
INSERT INTO estadisticas (id_partido, id_jugador, puntos) VALUES
 (3, 1, 7), (3, 2, 6), (3, 3, 4), (3, 5, 5),           -- Titanes
 (3, 20, 6), (3, 21, 5), (3, 22, 4), (3, 23, 4);       -- Marea

-- Partido 4: 1 vs 6 (14-16)
INSERT INTO estadisticas (id_partido, id_jugador, puntos) VALUES
 (4, 1, 3), (4, 2, 4), (4, 4, 4), (4, 5, 3),           -- Titanes
 (4, 25, 5), (4, 26, 4), (4, 27, 4), (4, 28, 3);       -- Fuego

-- Partido 5: 3 vs 4 (20-12)
INSERT INTO estadisticas (id_partido, id_jugador, puntos) VALUES
 (5, 11, 6), (5, 12, 5), (5, 14, 5), (5, 15, 4),       -- Halcones
 (5, 16, 3), (5, 17, 4), (5, 18, 3), (5, 19, 2);       -- Rayo

-- Partido 6: 3 vs 5 (18-18)
INSERT INTO estadisticas (id_partido, id_jugador, puntos) VALUES
 (6, 11, 5), (6, 12, 5), (6, 13, 4), (6, 14, 4),       -- Halcones
 (6, 20, 5), (6, 21, 5), (6, 22, 4), (6, 24, 4);       -- Marea

-- Partido 7: 4 vs 6 (11-21)
INSERT INTO estadisticas (id_partido, id_jugador, puntos) VALUES
 (7, 16, 3), (7, 17, 3), (7, 18, 3), (7, 19, 2),       -- Rayo
 (7, 25, 6), (7, 26, 5), (7, 27, 5), (7, 29, 5);       -- Fuego

-- Partido 8: 5 vs 7 (19-17)
INSERT INTO estadisticas (id_partido, id_jugador, puntos) VALUES
 (8, 20, 5), (8, 21, 5), (8, 23, 5), (8, 24, 4),       -- Marea
 (8, 30, 5), (8, 31, 4), (8, 32, 4), (8, 33, 4);       -- Canasta

-- Partido 9: 6 vs 8 (22-20)
INSERT INTO estadisticas (id_partido, id_jugador, puntos) VALUES
 (9, 25, 6), (9, 26, 6), (9, 27, 5), (9, 29, 5),       -- Fuego
 (9, 34, 5), (9, 35, 5), (9, 36, 5), (9, 38, 5);       -- Cementerio

-- Partido 10: 7 vs 8 (15-13)
INSERT INTO estadisticas (id_partido, id_jugador, puntos) VALUES
 (10, 30, 4), (10, 31, 4), (10, 32, 4), (10, 33, 3),   -- Canasta
 (10, 34, 4), (10, 35, 3), (10, 37, 3), (10, 38, 3);   -- Cementerio

-- Partido 11: 3 vs 6 (16-19)
INSERT INTO estadisticas (id_partido, id_jugador, puntos) VALUES
 (11, 11, 4), (11, 12, 4), (11, 13, 4), (11, 15, 4),   -- Halcones
 (11, 25, 5), (11, 26, 5), (11, 27, 5), (11, 28, 4);   -- Fuego

-- Partido 12: 4 vs 5 (13-22)
INSERT INTO estadisticas (id_partido, id_jugador, puntos) VALUES
 (12, 16, 4), (12, 17, 3), (12, 18, 3), (12, 19, 3),   -- Rayo
 (12, 20, 6), (12, 21, 6), (12, 22, 5), (12, 24, 5);   -- Marea

-- Partido 13: 7 vs 1 (12-21)
INSERT INTO estadisticas (id_partido, id_jugador, puntos) VALUES
 (13, 30, 3), (13, 31, 3), (13, 32, 3), (13, 33, 3),   -- Canasta
 (13, 1, 6), (13, 2, 5), (13, 4, 5), (13, 5, 5);       -- Titanes

-- Partido 14: 8 vs 3 (18-20)
INSERT INTO estadisticas (id_partido, id_jugador, puntos) VALUES
 (14, 34, 5), (14, 35, 4), (14, 36, 5), (14, 38, 4),   -- Cementerio
 (14, 11, 5), (14, 12, 5), (14, 14, 5), (14, 15, 5);   -- Halcones

-- Partido 15: 5 vs 1 (17-17)
INSERT INTO estadisticas (id_partido, id_jugador, puntos) VALUES
 (15, 20, 5), (15, 21, 4), (15, 23, 4), (15, 24, 4),   -- Marea
 (15, 1, 5), (15, 2, 4), (15, 4, 4), (15, 5, 4);       -- Titanes

-- Partido 16: 6 vs 4 (21-9)
INSERT INTO estadisticas (id_partido, id_jugador, puntos) VALUES
 (16, 25, 5), (16, 26, 5), (16, 27, 6), (16, 29, 5),   -- Fuego
 (16, 16, 2), (16, 17, 2), (16, 18, 2), (16, 19, 3);   -- Rayo

-- Partido 17: 8 vs 7 (16-15)
INSERT INTO estadisticas (id_partido, id_jugador, puntos) VALUES
 (17, 34, 4), (17, 35, 4), (17, 36, 4), (17, 38, 4),   -- Cementerio
 (17, 30, 4), (17, 31, 4), (17, 32, 4), (17, 33, 3);   -- Canasta

-- Partido 18: 3 vs 5 (19-21)
INSERT INTO estadisticas (id_partido, id_jugador, puntos) VALUES
 (18, 11, 5), (18, 12, 5), (18, 14, 5), (18, 15, 4),   -- Halcones
 (18, 20, 5), (18, 21, 5), (18, 23, 5), (18, 24, 6);   -- Marea

-- Partido 19: 4 vs 8 (14-18)
INSERT INTO estadisticas (id_partido, id_jugador, puntos) VALUES
 (19, 16, 4), (19, 17, 4), (19, 18, 3), (19, 19, 3),   -- Rayo
 (19, 34, 5), (19, 35, 5), (19, 36, 4), (19, 38, 4);   -- Cementerio

-- Partido 20: 6 vs 7 (23-19)
INSERT INTO estadisticas (id_partido, id_jugador, puntos) VALUES
 (20, 25, 6), (20, 26, 6), (20, 27, 5), (20, 29, 6),   -- Fuego
 (20, 30, 5), (20, 31, 5), (20, 32, 5), (20, 33, 4);   -- Canasta

-- (Los partidos 21 y 22 quedan PENDIENTES sin estadísticas)