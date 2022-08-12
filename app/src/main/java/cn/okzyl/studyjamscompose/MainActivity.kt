package cn.okzyl.studyjamscompose

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.okzyl.studyjamscompose.ui.theme.*
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
    arrayOf("AC", "删", "%", "÷"),
    arrayOf("7", "8", "9", "×"),
    arrayOf("4", "5", "6", "-"),
    arrayOf("1", "2", "3", "+"),
    arrayOf("记录", "0", ".", "="),
)

val buttonColors = arrayOf(
    arrayOf(LightGray, LightGray, LightGray, LightGray),
    arrayOf(DarkGray, DarkGray, DarkGray, Orange),
    arrayOf(DarkGray, DarkGray, DarkGray, Orange),
    arrayOf(DarkGray, DarkGray, DarkGray, Orange),
    arrayOf(Orange, DarkGray, DarkGray, DarkGray),
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
        .background(Background)
        .padding(horizontal = 10.dp)) {
        Box(
            Modifier
                .fillMaxHeight(0.5f)
                .fillMaxWidth(),
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
                        while (iterator.hasNext()){
                            val oldIndex = iterator.nextIndex()
                            val oldValue = iterator.next()
                            if (oldValue.editing || oldIndex == annotation.tag.toInt()){
                                iterator.set(oldValue.copy(editing = annotation.tag.toInt() ==oldIndex))
                            }
                        }
                    }
                }
                Text(text = calculateState.result.toString(),
                    color = Color.White,
                    fontSize = 100.sp)
            }
        }
        Column(Modifier.fillMaxWidth()) {
            buttons.forEachIndexed { oneIndex, it ->
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    it.forEachIndexed { twoIndex, it ->
                        CalculatorButton(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f),
                            text = it,
                            color = buttonColors[oneIndex][twoIndex]) {
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

fun CalculateState.onInput(input: String): CalculateState? {
    when {
        input == "删" -> {
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
        isOperator(input) -> {
            if (list.isEmpty()) {
                list.add(CalculateUnit.from(input))
            }
            if (isOperator(list.last().text)) {
                list.removeLast()
            }
            list.add(CalculateUnit.from(input))
        }
        else -> {
            if (list.isEmpty()) {
                list.add(CalculateUnit.from(input))
            }
            if (isOperator(list.last().text)) {
                list.add(CalculateUnit.from(input))
            } else {
                list[list.lastIndex] = list.last().copy(text = list.last().text + input)
            }
        }
    }
    return null
}

fun isOperator(input: String) = arrayOf("×", "+", "-", "÷").contains(input)

@Composable
fun CalculatorButton(modifier: Modifier, text: String, color: Color, onClick: () -> Unit = {}) {
    Box(modifier
        .clickable { onClick.invoke() }
        .clip(CircleShape)
        .background(color),
        contentAlignment = Alignment.Center) {
        Text(text = text, fontSize = 40.sp, color = Color.White)
    }
}

@Preview(showSystemUi = true)
@Composable
fun DefaultPreview() {
    StudyJamsComposeTheme {
        Greeting()
    }
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