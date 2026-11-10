CREATE TABLE equipos (
    id_equipo     INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre        TEXT NOT NULL UNIQUE
);

CREATE TABLE jugadores (
    id_jugador    INTEGER PRIMARY KEY AUTOINCREMENT,
    id_equipo     INTEGER NOT NULL REFERENCES equipos(id_equipo),
    dorsal        INTEGER NOT NULL,
    nombre        TEXT NOT NULL,
    altura_cm     INTEGER,
    UNIQUE (id_equipo, dorsal)
);

CREATE TABLE partidos (
    id_partido    INTEGER PRIMARY KEY AUTOINCREMENT,
    id_local      INTEGER NOT NULL REFERENCES equipos(id_equipo),
    id_visitante  INTEGER NOT NULL REFERENCES equipos(id_equipo),
    fecha         TEXT NOT NULL,          -- 'YYYY-MM-DD'
    puntos_local  INTEGER,
    puntos_visitante INTEGER,
    estado        TEXT NOT NULL DEFAULT 'PENDIENTE'
                  CHECK (estado IN ('PENDIENTE','JUGADO','CANCELADO'))
);

CREATE TABLE estadisticas (
    id_estadistica INTEGER PRIMARY KEY AUTOINCREMENT,
    id_partido     INTEGER NOT NULL REFERENCES partidos(id_partido),
    id_jugador     INTEGER NOT NULL REFERENCES jugadores(id_jugador),
    puntos         INTEGER NOT NULL DEFAULT 0,
    UNIQUE (id_partido, id_jugador)
);
