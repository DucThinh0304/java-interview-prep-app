package io.github.ducthinh0304.javainterviewprep.core.engine.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Immutable
data class ThemeColors(
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val borderHairline: Color,
    val borderFocus: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val accent: Color,
    val accentSecondary: Color,
    val warning: Color,
    val error: Color,
    val codeBackground: Color
)

@Immutable
data class ThemeTypography(
    val headline: TextStyle,
    val title: TextStyle,
    val body: TextStyle,
    val bodySmall: TextStyle,
    val code: TextStyle,
    val badge: TextStyle
)

@Immutable
data class ThemeShapes(
    val cardCornerRadius: Dp,
    val buttonCornerRadius: Dp,
    val badgeCornerRadius: Dp,
    val borderWidth: Dp
)

@Immutable
data class ThemeSpacing(
    val xxs: Dp = 2.dp,
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 16.dp,
    val lg: Dp = 24.dp,
    val xl: Dp = 32.dp,
    val xxl: Dp = 48.dp
)

@Immutable
data class CopyTone(
    val appName: String,
    val badgePrefix: String,
    val dailyHeader: String,
    val reRunHeader: String,
    val streakLabel: String
)

@Immutable
data class ThemePack(
    val id: String,
    val name: String,
    val colors: ThemeColors,
    val typography: ThemeTypography,
    val shapes: ThemeShapes,
    val spacing: ThemeSpacing,
    val copyTone: CopyTone
)

val JavaThemePack = ThemePack(
    id = "java-dark-dev",
    name = "Java Developer Obsidian",
    colors = ThemeColors(
        background = Color(0xFF0D1117),
        surface = Color(0xFF161B22),
        surfaceElevated = Color(0xFF21262D),
        borderHairline = Color(0xFF30363D),
        borderFocus = Color(0xFF58A6FF),
        textPrimary = Color(0xFFF0F6FC),
        textSecondary = Color(0xFF8B949E),
        textMuted = Color(0xFF484F58),
        accent = Color(0xFF238636),
        accentSecondary = Color(0xFF1F6FEB),
        warning = Color(0xFFD29922),
        error = Color(0xFFF85149),
        codeBackground = Color(0xFF090D13)
    ),
    typography = ThemeTypography(
        headline = TextStyle(
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 30.sp,
            letterSpacing = (-0.5).sp,
            fontFamily = FontFamily.SansSerif
        ),
        title = TextStyle(
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 24.sp,
            fontFamily = FontFamily.SansSerif
        ),
        body = TextStyle(
            fontSize = 15.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 22.sp,
            fontFamily = FontFamily.SansSerif
        ),
        bodySmall = TextStyle(
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 18.sp,
            fontFamily = FontFamily.SansSerif
        ),
        code = TextStyle(
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 19.sp,
            fontFamily = FontFamily.Monospace
        ),
        badge = TextStyle(
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 14.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 0.5.sp
        )
    ),
    shapes = ThemeShapes(
        cardCornerRadius = 4.dp,
        buttonCornerRadius = 4.dp,
        badgeCornerRadius = 2.dp,
        borderWidth = 1.dp
    ),
    spacing = ThemeSpacing(),
    copyTone = CopyTone(
        appName = "Java Interview Prep",
        badgePrefix = "BUILD",
        dailyHeader = "DAILY DRILL",
        reRunHeader = "RE-RUN QUEUE",
        streakLabel = "DAY STREAK"
    )
)
