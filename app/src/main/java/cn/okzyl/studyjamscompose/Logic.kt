package cn.okzyl.studyjamscompose

import androidx.compose.runtime.mutableStateListOf
import java.math.BigDecimal

fun CalculateState.onInput(buttonModel: ButtonModel): CalculateState? {
    fun calculate(): CalculateState? {
        kotlin.runCatching {
            return copy(result = Calculate.calculate(rawText))
        }
        return null
    }
    when (buttonModel.type) {
        ButtonType.DELETE -> {
            if (isEmpty) {
                return null
            }
            if (editing) {
                val index = editIndex
                var current = list[index].text
                current = if (current.length == 1) "0" else current.dropLast(1)
                list[index] = list[index].copy(text = current)
                return calculate()
            }
            var last = list.last().text
            if (last.isNotEmpty()) {
                last = if (last.length == 1 && list.size == 1) "0" else last.dropLast(1)
            }
            if (last.isEmpty()) {
                list.removeLast()
            } else {
                list[list.lastIndex] = list.last().copy(text = last)
            }
        }
        ButtonType.EMPTY -> {
            list.clear()
            list.add(CalculateUnit.from("0"))
        }
        ButtonType.SYMBOL -> {
            if (editing) {
                val index = editIndex
                list[index] = list[index].copy(text = buttonModel.text)
                return calculate()
            }
            if (list.isEmpty()) {
                list.add(CalculateUnit.from(buttonModel.text))
            }
            if (isLastOperator) {
                list.removeLast()
            }
            list.add(CalculateUnit.from(buttonModel.text))
        }
        ButtonType.CALCULATE -> {
            if (editing) {
                list.mapInPlace { it.copy(editing = false) }
                buttons.forEach {
                    it.mapInPlace { it.copy(enable = true) }
                }
                return null
            }
            record.add(rawText to (result?:"0"))
            return copy(list = mutableStateListOf(CalculateUnit(result?:"0")), result = null)
        }
        ButtonType.PERCENT -> {
            val index =
            if (editing) { editIndex
            } else list.lastIndex
            if (!isLastOperator) {
                list[index].text.toBigDecimalOrNull()?.run {
                    list[index] = list[index].copy(
                        text = (this.divide(BigDecimal(100))).toPlainString()
                    )
                }
            }
        }
        else -> {
            val isPoint = buttonModel.text=="."
            fun pointCheck(calculateUnit: CalculateUnit) = calculateUnit.text.contains('.') && buttonModel.text=="."
            if (editing) {
                val index = editIndex
                val sourceIsZero = list[index].text == "0"
                if (buttonModel.text == "0" && sourceIsZero) {
                    return null
                }
                if (pointCheck(list[index])){
                    return null
                }
                list[index] =
                    list[index].copy(text = if (sourceIsZero  && !isPoint) buttonModel.text else list[index].text + buttonModel.text)
                return calculate()
            }
            if (list.isEmpty()) {
                list.add(CalculateUnit.from(buttonModel.text))
            }
            if (isLastOperator) {
                list.add(CalculateUnit.from(buttonModel.text))
            } else {
                if (pointCheck(list.last())){
                    return null
                }
                list[list.lastIndex] = list.last()
                    .copy(text = (if (isEmpty && !isPoint) "" else list.last().text) + buttonModel.text)
            }
        }
    }
    return calculate()
}


fun isOperator(input: String) = arrayOf("*", "+", "-", "/").contains(input)