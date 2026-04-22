CREATE TABLE team
(
    id        SERIAL PRIMARY KEY,
    javalis   INTEGER,
    name      TEXT NOT NULL,
    uniform1  TEXT NOT NULL,
    uniform2  TEXT NOT NULL,
    emblem    TEXT NOT NULL,
    formation TEXT NOT NULL,
    user_id   INTEGER,

    CONSTRAINT fk_player_team FOREIGN KEY (user_id) REFERENCES users (id)
);