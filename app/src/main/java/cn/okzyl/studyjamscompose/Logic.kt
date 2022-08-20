package cn.okzyl.studyjamscompose

import cn.okzyl.studyjamscompose.ui.theme.isDark
import java.math.BigDecimal

fun CalculateState.onInput(buttonModel: ButtonModel): CalculateState? {
    val backText = rawText
    val backResult = result?.second ?: ""
    fun calculate(): CalculateState {
        buttons[0][0] = buttons[0][0].copy(
            text =
            if (!isEmpty) {
                "C"
            } else "AC"
        )
        kotlin.runCatching {
            if (isConfirm) {
                record.add(backText to backResult)
            }
            return copy(result = false to Calculate.calculate(rawText))
        }
        return copy(result = result?.copy(false))
    }
    when (buttonModel.type) {
        ButtonType.DELETE -> {
            if (isConfirm) {
                return null
            }
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
            if (buttonModel.text=="AC") record.clear()
            list.add(CalculateUnit.from("0"))
        }
        ButtonType.SYMBOL -> {
            if (isConfirm) {
                list.clear()
                list.add(CalculateUnit(result?.second ?: "0"))
            }
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
            if (isConfirm || isEmpty) return null
            return copy(result = result?.copy(first = true))
        }
        ButtonType.PERCENT -> {

            val index =
                when {
                    isConfirm -> {
                        list.clear()
                        list.add(CalculateUnit(result?.second ?: "0"))
                        0
                    }
                    editing -> {
                        editIndex
                    }
                    else -> list.lastIndex
                }
            if (!isLastOperator) {
                list[index].text.toBigDecimalOrNull()?.run {
                    list[index] = list[index].copy(
                        text = (this.divide(BigDecimal(100))).toPlainString()
                    )
                }
            }
        }
        ButtonType.CHANGE ->{
            isDark = !(isDark?:false)
            return null
        }
        else -> {
            if (isConfirm) {
                list.clear()
                list.add(CalculateUnit("0"))
            }
            val isPoint = buttonModel.text == "."
            fun pointCheck(calculateUnit: CalculateUnit) =
                calculateUnit.text.contains('.') && buttonModel.text == "."
            if (editing) {
                val index = editIndex
                val sourceIsZero = list[index].text == "0"
                if (buttonModel.text == "0" && sourceIsZero) {
                    return null
                }
                if (pointCheck(list[index])) {
                    return null
                }
                list[index] =
                    list[index].copy(text = if (sourceIsZero && !isPoint) buttonModel.text else list[index].text + buttonModel.text)
                return calculate()
            }
            if (list.isEmpty()) {
                list.add(CalculateUnit.from(buttonModel.text))
            }
            if (isLastOperator) {
                list.add(CalculateUnit.from(buttonModel.text))
            } else {
                if (pointCheck(list.last())) {
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