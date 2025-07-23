
-- accounts definition
CREATE TABLE accounts (
	id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
	username TEXT NOT NULL,
	pwdhash TEXT NOT NULL
);
