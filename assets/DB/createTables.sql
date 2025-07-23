
-- main table
CREATE TABLE mainTable (propName VARCHAR PRIMARY KEY, propValue VARCHAR);
INSERT INTO mainTable (propName, propValue) VALUES ("ver","1");






-- CHARACTERS

-- data
CREATE TABLE charaData (id INTEGER PRIMARY KEY AUTOINCREMENT, playerId INTEGER, dataVal VARCHAR);
CREATE INDEX charaDataIDIndex ON charaData (id);






-- AUTH
-- accounts definition
CREATE TABLE accounts (
	id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
	username TEXT NOT NULL,
	pwdhash TEXT NOT NULL
);











