@file:OptIn(ExperimentalComposeUiApi::class)

package cn.okzyl.studyjamscompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.okzyl.studyjamscompose.ui.theme.Background
import cn.okzyl.studyjamscompose.ui.theme.Orange
import cn.okzyl.studyjamscompose.ui.theme.StudyJamsComposeTheme
import cn.okzyl.studyjamscompose.ui.theme.selectColor

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StudyJamsComposeTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background) {
                    Calculator()
                }
            }
        }
    }
}

@Composable
fun Calculator() {
    var calculateState by remember {
        mutableStateOf(CalculateState())
    }
    Column(Modifier
        .background(Background), verticalArrangement = Arrangement.Bottom) {
        Box(
            Modifier
                .fillMaxWidth()
                .background(Color.Black)
                .weight(1f),
            contentAlignment = Alignment.BottomEnd
        ) {
            InputShow(calculateState)
        }
        Column(Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.5f)) {
            buttons.forEachIndexed { oneIndex, it ->
                Row(Modifier.weight(1f)) {
                    it.forEachIndexed { twoIndex, it ->
                        CalculatorButton(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            it) {
                            calculateState.onInput(it)?.run {
                                calculateState = this
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InputShow(calculateState: CalculateState) {
    Column(modifier = Modifier
        .verticalScroll(rememberScrollState(), reverseScrolling = true),
        horizontalAlignment = Alignment.End) {
        val annotationText = buildAnnotatedString {
            calculateState.list.forEachIndexed { index, it ->
                pushStringAnnotation(tag = index.toString(), annotation = it.text)
                withStyle(style = SpanStyle(color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 30.sp,
                    background = if (it.editing) selectColor else Color.Unspecified)) {
                    append(it.text)
                }
                pop()
            }
        }
        ClickableText(text = annotationText) {
            val annotationList = annotationText.getStringAnnotations(it, it)
            annotationList.firstOrNull()?.let { annotation ->
                val iterator = calculateState.list.listIterator()
                while (iterator.hasNext()) {
                    val oldIndex = iterator.nextIndex()
                    val oldValue = iterator.next()
                    if (oldValue.editing || oldIndex == annotation.tag.toInt()) {
                        iterator.set(oldValue.copy(editing = annotation.tag.toInt() == oldIndex))
                    }
                }
            }
        }
        Text(text = calculateState.result.toString(),
            color = Color.White,
            fontSize = 100.sp)
    }
}


@Composable
fun CalculatorButton(modifier: Modifier, buttonModel: ButtonModel, onClick: () -> Unit = {}) {
    var down by remember {
        mutableStateOf(false)
    }
    Box(
        modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(radius = 30.dp)
            ) { onClick.invoke() },
        contentAlignment = Alignment.Center,
    ) {
        val size =
            animateFloatAsState(targetValue = if (down) 0.85f else 1f,
                animationSpec = tween(durationMillis = 150))
        Box(modifier = Modifier
            .graphicsLayer(scaleX = size.value, scaleY = size.value)
            .size(60.dp)
            .clip(CircleShape)
            .background(if (buttonModel.type == ButtonType.CALCULATE) Orange else Color.Unspecified),
            contentAlignment = Alignment.Center) {
            if (buttonModel.res != null) {
                Image(painter = painterResource(id = buttonModel.res),
                    contentDescription = null,
                    Modifier
                        .size(35.dp)
                )
            } else {
                Text(text = buttonModel.text, fontSize = 30.sp, color = buttonModel.type.color)
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun DefaultPreview() {
    StudyJamsComposeTheme {
        Calculator()
    }
}
