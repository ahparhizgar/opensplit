1. refactor backend structure using a Ktor course
2. introduce SSE
3. improve messages of default api call error messages
4. create currency and money model (use Int)
5. start offline first approach
6. make YOU and EQUALLY buttons
7. when there is more than two participants, there would be no quick split option
8. color for left and over
### Done
--- 
1. write whole app component e2e test
2. write compose e2e test
3. make android buildable
4. predictive back
5. showing user messages automatically
6. make the app keyboard friendly (e.g. login flow)
7. server tests are too slow
8. logs out when DB scheme is updated (caused by in-memory H2 database wiping data on restart; mitigated by adding DB check to JWT validation which forces a clean logout on the client)
