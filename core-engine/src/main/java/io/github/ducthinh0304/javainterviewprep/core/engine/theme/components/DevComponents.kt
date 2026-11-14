package io.github.ducthinh0304.javainterviewprep.core.engine.theme.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.ducthinh0304.javainterviewprep.core.engine.theme.AppTheme

@Composable
fun DevCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = AppTheme.colors.surface,
    borderColor: Color = AppTheme.colors.borderHairline,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(AppTheme.shapes.cardCornerRadius)
    val clickModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .clip(shape)
            .border(
                width = AppTheme.shapes.borderWidth,
                color = borderColor,
                shape = shape
            )
            .background(backgroundColor)
            .then(clickModifier)
            .padding(AppTheme.spacing.md)
    ) {
        content()
    }
}

@Composable
fun DevBadge(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = AppTheme.colors.surfaceElevated,
    textColor: Color = AppTheme.colors.textSecondary,
    borderColor: Color = AppTheme.colors.borderHairline
) {
    val shape = RoundedCornerShape(AppTheme.shapes.badgeCornerRadius)
    Box(
        modifier = modifier
            .border(
                width = AppTheme.shapes.borderWidth,
                color = borderColor,
                shape = shape
            )
            .background(backgroundColor, shape = shape)
            .padding(horizontal = AppTheme.spacing.xs, vertical = AppTheme.spacing.xxs),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = AppTheme.typography.badge,
            color = textColor
        )
    }
}

@Composable
fun DevButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isPrimary: Boolean = true
) {
    val shape = RoundedCornerShape(AppTheme.shapes.buttonCornerRadius)
    val bgColor = when {
        !enabled -> AppTheme.colors.surfaceElevated
        isPrimary -> AppTheme.colors.accent
        else -> AppTheme.colors.surface
    }
    val contentColor = when {
        !enabled -> AppTheme.colors.textMuted
        isPrimary -> Color.White
        else -> AppTheme.colors.textPrimary
    }
    val borderColor = when {
        !enabled -> AppTheme.colors.borderHairline
        isPrimary -> AppTheme.colors.accent
        else -> AppTheme.colors.borderHairline
    }

    Box(
        modifier = modifier
            .clip(shape)
            .border(
                width = AppTheme.shapes.borderWidth,
                color = borderColor,
                shape = shape
            )
            .background(bgColor)
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = AppTheme.spacing.md, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = AppTheme.typography.body.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Medium),
            color = contentColor
        )
    }
}

@Composable
fun DevCodeBlock(
    code: String,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(AppTheme.shapes.cardCornerRadius)
    Column(
        modifier = modifier
            .clip(shape)
            .border(
                width = AppTheme.shapes.borderWidth,
                color = AppTheme.colors.borderHairline,
                shape = shape
            )
            .background(AppTheme.colors.codeBackground)
            .padding(AppTheme.spacing.sm)
    ) {
        Text(
            text = code,
            style = AppTheme.typography.code,
            color = AppTheme.colors.textPrimary
        )
    }
}
