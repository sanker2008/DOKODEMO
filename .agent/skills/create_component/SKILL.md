---
name: Create Industrial Component
description: Guidelines for creating UI components following the Industrial Neubrutalism style.
---

# Create Industrial Component

Follow these rules when creating custom UI components.

## 1. Container Style
- Use `IndustrialCard` or a `Box` with explicit borders.
- **Border**: Must be visible. Use `1.dp` or `2.dp` width. Colors: `IndustrialGrey` or `AcidLime`.
- **Corner Radius**: **0dp** (Zero). All corners must be sharp.
- **Shadow**: **None**. Elevation must be 0.dp.

## 2. State Feedback
- **Do not use Ripple Effects.**
- **Use Color Inversion**: When clicked, swap background and text colors (e.g., Black bg/White text -> White bg/Black text).
- **Use Border Changes**: Change border color to `AcidLime` on focus or selection.

## 3. Typography
- **Static Labels**: Use Sans-serif font with `FontWeight.Bold`. Colors: `IndustrialWhite` or `TextGrey`.
- **Data/Code**: Use `MonospaceFont` (JetBrains Mono). This applies to IP addresses, ports, server names, and technical data.

## Example

```kotlin
@Composable
fun IndustrialButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .border(2.dp, IndustrialGrey)
            .background(IndustrialBlack)
            .clickable(onClick = onClick) // Custom interaction source for no ripple
            .padding(16.dp)
    ) {
        Text(
            text = text,
            color = IndustrialWhite,
            fontWeight = FontWeight.Bold
        )
    }
}
```
