
--main Table
DROP TABLE IF EXISTS mainTable;

--charas
--positions
DROP TABLE IF EXISTS charaWorldPositions;
DROP INDEX IF EXISTS charaWorldPositionsIDIndex;
DROP INDEX IF EXISTS charaWorldPositionsPosIndex;
--data
DROP TABLE IF EXISTS charaData;
DROP INDEX IF EXISTS charaDataIDIndex;
DROP INDEX IF EXISTS charaDataPosIndex;

--towns
--positions
DROP TABLE IF EXISTS townWorldPositions;
DROP INDEX IF EXISTS townWorldPositionsIDIndex;
DROP INDEX IF EXISTS townWorldPositionsPosIndex;

--data
DROP TABLE IF EXISTS townData;
DROP INDEX IF EXISTS townDataIDIndex;
DROP INDEX IF EXISTS townDataPosIndex;

--structs
--positions
DROP TABLE IF EXISTS structWorldPositions;
DROP INDEX IF EXISTS structWorldPositionsIDIndex;
DROP INDEX IF EXISTS structWorldPositionsPosIndex;

--data
DROP TABLE IF EXISTS structData;
DROP INDEX IF EXISTS structDataIDIndex;
DROP INDEX IF EXISTS structDataPosIndex;
