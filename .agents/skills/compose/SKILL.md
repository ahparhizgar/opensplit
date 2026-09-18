---
name: compose
description: best practices for writing Compose code
---

Always use string resources instead of hardcoding them.

Avoid using background modifier in many cases. Instead, use Surface/Scaffold/Card or other Material
Design components.

Don't use clickable modifier when possible. Instead use Material Design components like Button, 
TextButton, IconButton, the overload of Surface/Card that accepts onClick parameter.

Icons.Default.XXX is deprecated. Use painterResource(Res.drawable.xxx) instead.

Keep the UI keyboard accessible, always define keyboardOptions and keyboardActions for TextField etc.
If a text field is single line, specify it, don't let Enter key create a new line.

User should be able to fill up the form and submit it without having to use a mouse/touch. Keyboard navigation should be possible.
Use Scaffold where possible.
---

# Design system

This design system is based on Material Design 3. Use Material Design components where possible.
Prefer using OutlinedTextField.
Use Rounded Icon variants.

## Buttons
```kotlin
Button(
    modifier = Modifier.heightIn(min = 56.dp),
    shape = ButtonDefaults.squareShape,
){
}
```
