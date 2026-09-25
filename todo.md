1. migrate UI tests to Kotest?
2. Improve code coverage for client
3. Implement delete group feature
4. Implement settlement
5. Add Currency model
6. Fix text fields font size problem
7. remove joining by id feature


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
14. remove `val payerId: String` from expense and instead add `creator` field to Expense model
15. remove currentTimeMillis function and use Clock.System.now() instead and note in AGENTS.md
16. expense sync has stopped working
17. make expense list screen top bar collapsable
18. add desktop views (large screen support)
19. use enums in DAOs where possible
20. separate dto and domain files and mention in AGENTS.md
21. backend returns Group.lastInteractionAt
22. delete recordChange("HOUSEHOLD") and use enum instead of string
23. can we use Instant directly in Exposed?
24. migrate server tests to Kotest and put assertion messages there

---
### After functional working
1. support large screens
2. introduce SSE
3. create currency and money model (use Int)
4. can I add a transport layer for Cloudflare workers?
5. request validation plugin (?) for Ktor
