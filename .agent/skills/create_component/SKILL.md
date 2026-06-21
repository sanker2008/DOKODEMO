---
name: Create Doko Component
description: Guidelines for creating UI components following the Mist & Dawn style.
---

# Create Doko Component

Follow these rules when creating custom UI components.

## 1. Container Style
- Use `DokoCard` or a `Surface`.
- **Border**: Minimal or None. Use Glassmorphism effects or gentle dropshadows instead of harsh borders.
- **Corner Radius**: **16dp** minimum. All corners must be smoothly rounded to provide a warm, companion-like feel.
- **Shadow**: Avoid harsh drop shadows. Use delicate inner shadows or background blurs (Glassmorphism).

## 2. State Feedback
- **Do not use harsh color changes.**
- **Use Soft Colors**: Connect/Active states should use the Soft Blue (`#A0C4E3`). Success states use Soft Mint Green (`#B7D5C7`).
- **Use Glow Effects**: For critical success actions, implement a momentary breathing glow effect (like the main connect button).

## 3. Typography
- **Static Labels**: Use clean, sans-serif, rounded fonts. Primary colors should be Cool Blue-Grey (`#607D8B`) instead of pure black, avoiding high-contrast strain.
- **Data/Code**: Use `MonospaceFont` (JetBrains Mono) for IP addresses, ports, server names, and technical data.

## Example

```kotlin
@Composable
fun DokoButton(
    text: String,
    onClick: () -> Unit,
    isActive: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Text(
            text = text,
            color = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
    }
}
```
