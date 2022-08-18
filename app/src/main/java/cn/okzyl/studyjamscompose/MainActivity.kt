package cn.okzyl.studyjamscompose

import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFontLoader
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import cn.okzyl.studyjamscompose.ui.theme.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StudyJamsComposeTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
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
    Column(
        Modifier
            .background(Background)
            .padding(bottom = 30.dp), verticalArrangement = Arrangement.Bottom
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(start = 15.dp, end = 15.dp, bottom = 15.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            InputShow(calculateState)
        }
        Box(Modifier
            .height(1.dp)
            .fillMaxWidth()
            .padding(horizontal = 13.dp)
            .background(Color(0xfff5f5f5)))
        Column(
            Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.5f)
        ) {
            buttons.forEachIndexed { oneIndex, it ->
                Row(Modifier.weight(1f)) {
                    it.forEachIndexed { twoIndex, it ->
                        CalculatorButton(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            it, calculateState
                        ) {
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

const val MAX_SIZE = 50
const val MIN_SIZE = 30

@Composable
private fun InputShow(calculateState: CalculateState) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState(), reverseScrolling = true),
        horizontalAlignment = Alignment.End
    ) {
        AutoSizeString(MAX_SIZE, MIN_SIZE, { size ->
            calculateState.list.forEachIndexed { index, it ->
                pushStringAnnotation(tag = index.toString(), annotation = it.text)
                withStyle(
                    style = SpanStyle(
                        color = FontColor,
                        fontSize = size.sp,
                        background = if (it.editing) selectColor else Color.Unspecified
                    )
                ) {
                    append(it.text)
                }
                pop()
            }
        }) { realString ->
            ClickableText(text = realString) {
                val annotationList = realString.getStringAnnotations(it, it)
                annotationList.firstOrNull()?.let { annotation ->
                    val iterator = calculateState.list.listIterator()
                    while (iterator.hasNext()) {
                        val oldIndex = iterator.nextIndex()
                        val oldValue = iterator.next()
                        if (oldValue.editing || oldIndex == annotation.tag.toInt()) {
                            iterator.set(oldValue.copy(editing = annotation.tag.toInt() == oldIndex))
                        }
//                        buttons.forEach {
//                            it.forEach {
//                                it.copy(enable = )
//                            }
//                        }
                    }
                }
            }
        }

        if (calculateState.result != null && !calculateState.isEmpty) {
            AutoSizeString(MAX_SIZE, 0, { size ->
                withStyle(
                    style = SpanStyle(
                        color = FontColor,
                        fontSize = size.sp,
                    )
                ) {
                    append("= " + calculateState.result.second)
                }
            }) {
                Text(
                    text = it,
                    style = TextStyle(
                        color = if (calculateState.result.first) FontColor else UnconfirmedFontColor
                    ),
                )
            }
        }
    }
}

@Composable
private fun AutoSizeString(
    maxSize: Int,
    minSize: Int,
    buildString: AnnotatedString.Builder.(size: Int) -> Unit,
    content: @Composable (AnnotatedString) -> Unit,
) {
    fun buildString(size: Int) = buildAnnotatedString {
        buildString.invoke(this, size)
    }
    BoxWithConstraints {
        var fontSize = maxSize
        val calculateIntrinsics = @Composable {
            val testString = buildString(fontSize)
            testString to MultiParagraphIntrinsics(testString, LocalTextStyle.current,
                density = LocalDensity.current,
                resourceLoader = LocalFontLoader.current,
                placeholders = emptyList()
            )
        }
        var result = calculateIntrinsics()
        var firstOverflow: Int? = null
        with(LocalDensity.current) {
            var intrinsics = result.second
            var currLength = result.first.length
            firstOverflow?.run {
                if (intrinsics.maxIntrinsicWidth <= maxWidth.toPx() && currLength <= this) {
                    fontSize = maxSize
                    firstOverflow = null
                }
            }
            while (intrinsics.maxIntrinsicWidth > maxWidth.toPx()) {
                intrinsics = result.second
                currLength = result.first.length
                if (firstOverflow == null) {
                    firstOverflow = currLength - 1
                }
                fontSize = kotlin.math.max((fontSize * 0.98).toInt(), minSize)
                Log.d("test", fontSize.toString())
                if (fontSize == minSize) return@with
                result = calculateIntrinsics()
            }
        }
        val realString = buildString(fontSize)
        content.invoke(realString)
    }
}


@Composable
fun CalculatorButton(
    modifier: Modifier,
    buttonModel: ButtonModel,
    state: CalculateState,
    onClick: () -> Unit = {},
) {
    var down by remember {
        mutableStateOf(false)
    }
    val interactionSource = remember { MutableInteractionSource() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    suspend fun PointerInputScope.pointerInput() {
        detectTapGestures(onPress = {
            down = true
            context.vibrator(0L to 30L, amplitude = 50)


            val notShowInteraction = buttonModel.type == ButtonType.CALCULATE
            var press: PressInteraction.Press? = null
            if (!notShowInteraction) {
                press = PressInteraction.Press(it)
                interactionSource.emit(press)
            }
            var job: Job? = null
            //Continuous delete
            if (buttonModel.type == ButtonType.DELETE) {
                var first = true
                job = scope.launch {
                    while (!state.isEmpty) {
                        delay(if (first) 500 else 70)
                        first = false
                        context.vibrator(0L to 10L, amplitude = 50)
                        onClick.invoke()
                    }
                }
            }
            val result = tryAwaitRelease()
            job?.cancel()

            press?.run {
                interactionSource.emit(PressInteraction.Release(this))
            }

            if (result) {
                onClick.invoke()
            }
            down = false
        })
    }
    Box(
        modifier
            .indication(interactionSource, rememberRipple(radius = 30.dp))
            .pointerInput(Unit, PointerInputScope::pointerInput),
        contentAlignment = Alignment.Center,
    ) {
        val sizeAnimate =
            animateFloatAsState(
                targetValue = if (down) 0.85f else 1f,
                animationSpec = tween(durationMillis = 50)
            )
        Box(
            modifier = Modifier
                .graphicsLayer(scaleX = sizeAnimate.value, scaleY = sizeAnimate.value)
                .size(60.dp)
                .clip(CircleShape)
                .background(if (buttonModel.type == ButtonType.CALCULATE) Orange else Color.Unspecified),
            contentAlignment = Alignment.Center
        ) {
            if (buttonModel.res != null) {
                Image(
                    painter = painterResource(id = buttonModel.res),
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
