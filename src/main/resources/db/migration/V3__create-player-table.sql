CREATE TABLE player
(
    id        SERIAL PRIMARY KEY,
    name      TEXT    NOT NULL,
    overall   INTEGER NOT NULL,
    price     INTEGER NOT NULL,
    image_url TEXT    NOT NULL,
    team_id   INTEGER,

    CONSTRAINT fk_player_team FOREIGN KEY (team_id) REFERENCES team (id)
);