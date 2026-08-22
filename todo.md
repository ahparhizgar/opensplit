1. use enums in DAOs where possible
2. request validation plugin (?) for Ktor
3. remove currentTimeMillis function and use Clock.System.now() instead and note in AGENTS.md
4. remove `val payerId: String` from expense and instead add `creator` field to Expense model

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
9. when there is more than two participants, there would be no quick split option
10. make YOU and EQUALLY buttons
11. start offline first approach
12. improve messages of default api call error messages
13. refactor backend structure using a Ktor course

---
### After functional working
1. support large screens
2. introduce SSE
3. create currency and money model (use Int)
