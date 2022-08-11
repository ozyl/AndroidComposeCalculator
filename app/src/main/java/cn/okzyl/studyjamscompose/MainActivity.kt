package cn.okzyl.studyjamscompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
    arrayOf("AC", "删", "%", "/"),
    arrayOf("7", "8", "9", "x"),
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
                Text(text = calculateState.target,
                    color = Color.White,
                    fontSize = 100.sp)
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
                            calculateState = calculateState.onInput(it)
                        }
                    }
                }
            }
        }
    }
}

fun CalculateState.onInput(input: String): CalculateState {
    return when (input) {
        "删" -> {
            if (target.length <= 1) {
                copy(target = "0")
            } else {
                copy(target = target.dropLast(1))
            }
        }
        else -> copy(target = target + input)
    }

}

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
    val target: String = "0",
    val result: BigDecimal = BigDecimal("0"),
)