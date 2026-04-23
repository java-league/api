-- Corrige nome incorreto da constraint de FK em bid.team_id
ALTER TABLE bid DROP CONSTRAINT IF EXISTS fk_bid_user;
ALTER TABLE bid
    ADD CONSTRAINT fk_bid_team FOREIGN KEY (team_id) REFERENCES team (id);

-- Índices para colunas de FK consultadas com frequência
CREATE INDEX idx_bid_player_id ON bid (player_id);
CREATE INDEX idx_bid_team_id ON bid (team_id);
CREATE INDEX idx_team_user_id ON team (user_id);
