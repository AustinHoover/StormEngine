@page LoadingHalting Debug Loading Halting

A very common issue is that the loading for the client never completes for a certain gamemode.
The goal of this doc is to track causes for this so that it can be debugged faster in the future.
(Hint, it's probably under initDrawCellManager in ClientLoading.java)


### 02-25-2024
Arena was not loading because handling of edge-of-world chunks was not being handled correctly
for a world of 2x2x2, when loading the chunk at (1,0,0) you need data for chunk (2,0,0)
(2,0,0) is out of bounds of the world size, but the drawcellmanager would not mark the chunk data as present because it wasn't properly checking for bounds
This was fixed by properly checking for bounds in drawcellmanager; however, it then started failing to fetch data for (2,0,0) and NPEing
Fixed by guarding in the method that generates chunk data

### 08-16-2024
Second client could not connect to server started with local player because the routine to start local server didn't actually start a socket

