---
name: compose
description: best practices for writing Compose code
---

Always use string resources instead of hardcoding them.

Avoid using background modifier in many cases. Instead use Surface/Scaffold/Card or other Material
Design components.

Don't use clickable modifier when possible. Instead use Material Design components like Button, 
TextButton, IconButton, the overload of Surface/Card that accepts onClick parameter.

Icons.Default.XXX is deprecated. Use painterResource(Res.drawable.xxx) instead.
