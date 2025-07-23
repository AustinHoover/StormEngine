@page BugLog Bug Log

A log of bugs encountered


### 08-16-2024
 - Starting a server with a local client didn't actually create the server socket/thread. This meant no other players could connect.
 - On a client disconnection, the server connection was cleared by IP instead of by socket, so if two connections were from the same IP (ie localhost) it would disconnect the first one to connect.

