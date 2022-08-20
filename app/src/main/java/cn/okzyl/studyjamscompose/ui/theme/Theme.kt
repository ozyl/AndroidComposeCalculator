package cn.okzyl.studyjamscompose.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color

@Stable
class CalculateTheme(
    background:Color,
    fontColor:Color,
    complete:Color,
    unconfirmedFontColor:Color,
    primary:Color,
){
    var background by mutableStateOf(background, structuralEqualityPolicy())
        internal set
    var fontColor by mutableStateOf(fontColor, structuralEqualityPolicy())
        internal set
    var complete by mutableStateOf(complete, structuralEqualityPolicy())
        internal set
    var unconfirmedFontColor by mutableStateOf(unconfirmedFontColor, structuralEqualityPolicy())
        internal set
    var primary by mutableStateOf(primary, structuralEqualityPolicy())
        internal set
}
private val DarkColorPalette = CalculateTheme(
    background = Color.Black,
    fontColor = Color.White,
    complete = Color.Blue,
    unconfirmedFontColor = Color.Gray.copy(alpha = 0.6f),
    primary = Color(0xFFFF6B3B))


private val LightColorPalette = CalculateTheme(
    background = Color.White,
    fontColor = Color.Black,
    complete = Color.Blue,
    unconfirmedFontColor = Color.Gray.copy(alpha = 0.6f),
    primary = Color(0xFFFF6B3B)
)

val LocalAppColors = compositionLocalOf {
    LightColorPalette
}

var isDark by mutableStateOf<Boolean?>(null)
@Composable
fun StudyJamsComposeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    if (isDark==null){
        isDark = isSystemInDarkTheme()
    }
    val colors = if (isDark?:false) {
        DarkColorPalette
    } else {
        LightColorPalette
    }
    CompositionLocalProvider(LocalAppColors provides colors) {
        MaterialTheme(
            typography = Typography,
            shapes = Shapes,
            content = content
        )
    }
}