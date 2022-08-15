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
import androidx.compose.runtime.snapshots.SnapshotStateList
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
import java.math.BigDecimal

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StudyJamsComposeTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background) {
                    Greeting()
                }
            }
        }
    }
}

val buttons = arrayOf(
    arrayOf(
        ButtonModel("C", type = ButtonType.EMPTY),
        ButtonModel(type = ButtonType.DELETE, res = R.drawable.ic_delete),
        ButtonModel("%", type = ButtonType.PERCENT),
        ButtonModel(res = R.drawable.ic_chufa, type = ButtonType.SYMBOL, text = "/")
    ),

    arrayOf(
        ButtonModel("7"),
        ButtonModel("8"),
        ButtonModel("9"),
        ButtonModel(res = R.drawable.ic_chengfa, type = ButtonType.SYMBOL, text = "x")
    ),

    arrayOf(
        ButtonModel("4"),
        ButtonModel("5"),
        ButtonModel("6"),
        ButtonModel(res = R.drawable.ic_jianfa, type = ButtonType.SYMBOL, text = "-")
    ),
    arrayOf(
        ButtonModel("1"),
        ButtonModel("2"),
        ButtonModel("3"),
        ButtonModel(res = R.drawable.ic_jiafa, type = ButtonType.SYMBOL, text = "+")
    ),
    arrayOf(
        ButtonModel(type = ButtonType.CHANGE, res = R.drawable.ic_change),
        ButtonModel("0"),
        ButtonModel("."),
        ButtonModel(res = R.drawable.ic_dengyu, type = ButtonType.CALCULATE)
    ),
)

@Composable
fun Greeting() {
    Calculator()
}

@Composable
fun Calculator() {
    var calculateState by remember {
        mutableStateOf(CalculateState())
    }
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    Column(Modifier
        .background(Background), verticalArrangement = Arrangement.Bottom) {
        Box(
            Modifier
                .fillMaxWidth()
                .background(Color.Black)
                .weight(1f),
            contentAlignment = Alignment.BottomEnd
        ) {
            Column(modifier = Modifier
                .verticalScroll(scrollState, reverseScrolling = true),
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

fun CalculateState.onInput(buttonModel: ButtonModel): CalculateState? {
    when (buttonModel.type) {
        ButtonType.DELETE -> {
            if (list.isEmpty()) {
                return null
            }
            var last = list.last().text
            if (last.isNotEmpty()) {
                last = last.dropLast(1)
            }
            if (last.isEmpty()) {
                list.removeLast()
            } else {
                list[list.lastIndex] = list.last().copy(text = last)
            }
        }
        ButtonType.SYMBOL -> {
            if (list.isEmpty()) {
                list.add(CalculateUnit.from(buttonModel.text))
            }
            if (isOperator(list.last().text)) {
                list.removeLast()
            }
            list.add(CalculateUnit.from(buttonModel.text))
        }
        else -> {
            if (list.isEmpty()) {
                list.add(CalculateUnit.from(buttonModel.text))
            }
            if (isOperator(list.last().text)) {
                list.add(CalculateUnit.from(buttonModel.text))
            } else {
                list[list.lastIndex] = list.last().copy(text = list.last().text + buttonModel.text)
            }
        }
    }
    return null
}

fun isOperator(input: String) = arrayOf("x", "+", "-", "/").contains(input)

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
        Greeting()
    }
}

data class ButtonModel(
    val text: String = "",
    val enable: Boolean = true,
    val type: ButtonType = ButtonType.NUMBER,
    val res: Int? = null,
)

enum class ButtonType(val color: Color) {
    NUMBER(Color.Black), SYMBOL(Orange), CALCULATE(Color.White), DELETE(Orange),
    PERCENT(Orange), EMPTY(Orange), CHANGE(Orange)
}

data class CalculateState(
    val list: SnapshotStateList<CalculateUnit> = mutableStateListOf<CalculateUnit>(),
    val result: BigDecimal = BigDecimal("0"),
)

data class CalculateUnit(
    val text: String = "",
    val editing: Boolean = false,
) {
    companion object {
        fun from(text: String) = CalculateUnit(text)
    }
}