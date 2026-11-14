package io.github.ducthinh0304.javainterviewprep.core.engine.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

val LocalThemePack = staticCompositionLocalOf<ThemePack> {
    JavaThemePack
}

object AppTheme {
    val pack: ThemePack
        @Composable
        @ReadOnlyComposable
        get() = LocalThemePack.current

    val colors: ThemeColors
        @Composable
        @ReadOnlyComposable
        get() = LocalThemePack.current.colors

    val typography: ThemeTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalThemePack.current.typography

    val shapes: ThemeShapes
        @Composable
        @ReadOnlyComposable
        get() = LocalThemePack.current.shapes

    val spacing: ThemeSpacing
        @Composable
        @ReadOnlyComposable
        get() = LocalThemePack.current.spacing

    val copyTone: CopyTone
        @Composable
        @ReadOnlyComposable
        get() = LocalThemePack.current.copyTone
}

@Composable
fun AppThemeProvider(
    themePack: ThemePack = JavaThemePack,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalThemePack provides themePack,
        content = content
    )
}
