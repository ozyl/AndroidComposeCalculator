@file:OptIn(ExperimentalFoundationApi::class)

package cn.okzyl.studyjamscompose

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.LocalOverScrollConfiguration
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFontLoader
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
            CompositionLocalProvider(LocalOverScrollConfiguration provides null) {
                InputShow(calculateState)
            }
        }
        Box(
            Modifier
                .height(1.dp)
                .fillMaxWidth()
                .padding(bottom = 13.dp)
                .background(Color(0xfff5f5f5))
        )
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

@ExperimentalFoundationApi
@Composable
private fun InputShow(calculateState: CalculateState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState(), reverseScrolling = true),
        horizontalAlignment = Alignment.End
    ) {
        calculateState.record.forEach {
            Column(modifier = Modifier.padding(vertical = 20.dp), horizontalAlignment = Alignment.End){
                Text(text = it.first,color= UnconfirmedFontColor, fontSize = 20.sp, modifier = Modifier.padding(bottom = 5.dp))
                Text(text = "= "+it.second,color= UnconfirmedFontColor, fontSize = 20.sp)
            }
        }
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
                        val allowType = if (isOperator(annotation.item)) operAllow else allow
                        buttons.forEach {
                            it.mapInPlace {
                                it.copy(enable = allowType.contains(it.type))
                            }
                        }
                    }
                }
            }
        }

        if (calculateState.result != null && !calculateState.isEmpty) {
            AutoSizeString(MIN_SIZE, 0, { size ->
                withStyle(
                    style = SpanStyle(
                        color = UnconfirmedFontColor,
                        fontSize = size.sp,
                    )
                ) {
                    append("= " + calculateState.result)
                }
            }) {
                Text(
                    text = it
                )
            }
        }
        Spacer(modifier = Modifier.height(15.dp))
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
            testString to MultiParagraphIntrinsics(
                testString, LocalTextStyle.current,
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
            if (!buttonModel.enable)return@detectTapGestures
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
                        context.vibrator(0L to 30L, amplitude = 30)
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
            .pointerInput(buttonModel, PointerInputScope::pointerInput),
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
                .background(
                    when (buttonModel.type) {
                        ButtonType.CALCULATE -> (if (state.editing) Complete else Orange)
                        else -> Color.Unspecified
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            val alpha = if (buttonModel.enable) 1f else 0.4f
            if (buttonModel.res != null) {
                Image(
                    painter = painterResource(id =
                    when {
                        buttonModel.type == ButtonType.CALCULATE && state.editing ->  R.drawable.ic_complete
                        else -> buttonModel.res
                    }),
                    contentDescription = null,
                    Modifier
                        .size(35.dp),
                    alpha = alpha
                )
            } else {
                Text(text = buttonModel.text, fontSize = 30.sp, color = buttonModel.type.color.copy(alpha))
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
