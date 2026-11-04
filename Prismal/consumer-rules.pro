# PrismalTextMagnifierHost replaces Compose's text drag magnifier with a glass loupe. Compose
# has no public API for that, so it finds these internals by name (see
# PrismalPlatformMagnifierBridge). Keep their names (shrinking is still allowed) so the
# glass loupe also works in minified builds; without these it falls back to the system magnifier.
# Preserve the reflective fields on MagnifierNode.
-keepclassmembers class androidx.compose.foundation.MagnifierNode {
    androidx.compose.foundation.PlatformMagnifierFactory platformMagnifierFactory;
    androidx.compose.foundation.PlatformMagnifier magnifier;
}

# Preserve both interfaces and their methods.
-keep interface androidx.compose.foundation.PlatformMagnifierFactory {
    *;
}

-keep interface androidx.compose.foundation.PlatformMagnifier {
    *;
}