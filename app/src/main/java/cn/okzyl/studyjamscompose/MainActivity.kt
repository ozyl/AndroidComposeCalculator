
package cn.okzyl.studyjamscompose

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
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
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.ViewCompat
import cn.okzyl.studyjamscompose.ui.theme.*
import com.google.gson.Gson
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StudyJamsComposeTheme {
                window?.run {
                    statusBarColor = LocalAppColors.current.background.toArgb()
                }
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    val calculateState = rememberSaveable(saver = object :Saver<MutableState<CalculateState>,String>{
                        override fun restore(value: String): MutableState<CalculateState> {
                            return mutableStateOf(Gson().fromJson(value,CalculateState::class.java))
                        }

                        override fun SaverScope.save(value: MutableState<CalculateState>): String? {
                            return Gson().toJson(value.value)
                        }


                    })  {
                        mutableStateOf(CalculateState())
                    }
                    ViewCompat.getWindowInsetsController(LocalView.current)?.isAppearanceLightStatusBars = !(isDark?:false)
                    val isOrientation = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
                    if (isOrientation) CalculatorOrientation(calculateState) else
                    Calculator(calculateState)
                }
            }
        }
    }
}


@Composable
fun Calculator(calculateState: MutableState<CalculateState>) {

    Column(
        Modifier
            .background(LocalAppColors.current.background)
            .run {
                    padding(bottom = 30.dp)
            }, verticalArrangement = Arrangement.Bottom
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(start = 15.dp, end = 15.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            InputShow(calculateState.value) {
                calculateState.value = it
            }
        }
        Box(
            Modifier
                .height(1.dp)
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
                .background(Color(0xfff5f5f5))
        )
        Column(
            Modifier
                .fillMaxHeight(0.5f)
        ) {
            buttons.forEachIndexed { oneIndex, it ->
                Row(Modifier.weight(1f)) {
                    it.forEachIndexed { twoIndex, it ->
                        CalculatorButton(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            it, calculateState.value
                        ) {
                            calculateState.value.onInput(it)?.run {
                                calculateState.value = this
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun CalculatorOrientation(calculateState: MutableState<CalculateState>) {

    Row(
        Modifier
            .background(LocalAppColors.current.background)
            .run {
                padding(bottom = (10).dp)
            }, horizontalArrangement = Arrangement.End
    ) {
        Box(
            Modifier
                .fillMaxHeight()
                .weight(1f)
                .padding(start = 15.dp, end = 15.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            InputShow(calculateState.value) {
                calculateState.value = it
            }
        }
        Column(
            Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.35f)
        ) {
            buttons.forEachIndexed { oneIndex, it ->
                Row(Modifier.weight(1f)) {
                    it.forEachIndexed { twoIndex, it ->
                        CalculatorButton(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            it, calculateState.value
                        ) {
                            calculateState.value.onInput(it)?.run {
                                calculateState.value = this
                            }
                        }
                    }
                }
            }
        }
    }
}

const val MAX_SIZE = 50
const val MIN_SIZE = 24

@Composable
private fun InputShow(calculateState: CalculateState, update: (CalculateState) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState(), reverseScrolling = true),
        horizontalAlignment = Alignment.End
    ) {
        calculateState.record.forEach {
            Column(
                modifier = Modifier.padding(vertical = 20.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = it.first.prettyNumber,
                    color = LocalAppColors.current.unconfirmedFontColor,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(bottom = 5.dp)
                )

                Row(Modifier.horizontalScroll(rememberScrollState())) {
                    Text(
                        text = "= " + it.second.prettyNumber,
                        color = LocalAppColors.current.unconfirmedFontColor,
                        fontSize = 20.sp
                    )
                }
            }
        }


        val isConfirm = calculateState.isConfirm
        val inputSizeAnimate =
            animateIntAsState(
                targetValue = if (isConfirm) MIN_SIZE else MAX_SIZE,
                animationSpec = tween(durationMillis = 100)
            )
        val colors = LocalAppColors.current
        AutoSizeString(inputSizeAnimate.value, MIN_SIZE, { size ->
            calculateState.list.forEachIndexed { index, it ->
                pushStringAnnotation(tag = index.toString(), annotation = it.text)
                withStyle(
                    style = SpanStyle(
                        color = if (isConfirm) colors.unconfirmedFontColor else colors.fontColor,
                        fontSize = size.sp,
                        background = if (it.editing) selectColor else Color.Unspecified
                    )
                ) {
                    append(it.text.prettyNumber)
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
                    update.invoke(calculateState.copy(result = calculateState.result?.copy(false)))
                }
            }
        }

        if (calculateState.result != null && !calculateState.isEmpty) {
            val resultSizeAnimate =
                animateIntAsState(
                    targetValue = if (isConfirm) MAX_SIZE else MIN_SIZE,
                    animationSpec = tween(durationMillis = 300)
                )
            AutoSizeString(resultSizeAnimate.value, MIN_SIZE, { size ->
                withStyle(
                    style = SpanStyle(
                        color = if (isConfirm) colors.fontColor else colors.unconfirmedFontColor,
                        fontSize = size.sp,
                    )
                ) {
                    append("= " + calculateState.result.second.prettyNumber)
                }
            }) {
                Row(Modifier.horizontalScroll(rememberScrollState())) {
                    Text(
                        text = it
                    )
                }
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
        with(LocalDensity.current) {
            var intrinsics = result.second
            while (intrinsics.maxIntrinsicWidth > maxWidth.toPx()) {
                intrinsics = result.second
                fontSize = kotlin.math.max((fontSize * 0.98).toInt(), minSize)
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
            if (!buttonModel.enable) return@detectTapGestures
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
                animationSpec = tween(durationMillis = 150)
            )
        Box(
            modifier = Modifier
                .graphicsLayer(scaleX = sizeAnimate.value, scaleY = sizeAnimate.value)
                .size(60.dp)
                .clip(CircleShape)
                .background(
                    when (buttonModel.type) {
                        ButtonType.CALCULATE -> (if (state.editing) LocalAppColors.current.complete else LocalAppColors.current.primary)
                        else -> Color.Unspecified
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            val alpha = if (buttonModel.enable) 1f else 0.4f
            if (buttonModel.res != null) {
                Image(
                    painter = painterResource(
                        id =
                        when {
                            buttonModel.type == ButtonType.CALCULATE && state.editing -> R.drawable.ic_complete
                            else -> buttonModel.res
                        }
                    ),
                    contentDescription = null,
                    Modifier
                        .size(35.dp),
                    alpha = alpha
                )
            } else {
                Text(
                    text = buttonModel.text,
                    fontSize = 30.sp,
                    color = buttonModel.type.getColor(LocalAppColors.current).copy(alpha=alpha),
                )
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun DefaultPreview() {
    StudyJamsComposeTheme {
        val calculateState = remember {
            mutableStateOf(CalculateState())
        }
        Calculator(calculateState)
    }
}
