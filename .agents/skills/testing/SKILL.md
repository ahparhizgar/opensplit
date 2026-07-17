---
name: testing
description: Root testing guidelines
---

See these skills if relevant:

- /skill:kotest
- /skill:testing-architecture

Running all tests:

```shell
./gradlew :server:test --offline
./gradlew :app:shared:jvmTest --offline
# Or all at once:
./gradlew test jvmTest --offline
```
