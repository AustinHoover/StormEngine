
--characters
--positions
CREATE TABLE charaWorldPositions (playerId INTEGER PRIMARY KEY, id INTEGER, posX INTEGER, posY INTEGER);
CREATE INDEX charaWorldPositionsIDIndex ON charaWorldPositions (id);
CREATE INDEX charaWorldPositionsPosIndex ON charaWorldPositions (posX, posY);

--data
CREATE TABLE charaData (id INTEGER PRIMARY KEY AUTOINCREMENT, playerId INTEGER, dataVal VARCHAR);
CREATE INDEX charaDataIDIndex ON charaData (id);
