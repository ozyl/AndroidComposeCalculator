package cn.okzyl.studyjamscompose

import java.io.File.separator
import java.math.BigDecimal

fun CalculateState.onInput(buttonModel: ButtonModel): CalculateState? {
    when (buttonModel.type) {
        ButtonType.DELETE -> {
            if (list.isEmpty()) {
                return null
            }
            if (isEmpty) {
                return null
            }
            var last = list.last().text
            if (last.isNotEmpty()) {
                last = if (last.length==1&&list.size==1) "0" else last.dropLast(1)
            }
            if (last.isEmpty()) {
                list.removeLast()
            } else {
                list[list.lastIndex] = list.last().copy(text = last)
            }
        }
        ButtonType.EMPTY ->{
            list.clear()
            list.add(CalculateUnit.from("0"))
        }
        ButtonType.SYMBOL -> {
            if (list.isEmpty()) {
                list.add(CalculateUnit.from(buttonModel.text))
            }
            if (isLastOperator) {
                list.removeLast()
            }
            list.add(CalculateUnit.from(buttonModel.text))
        }
        ButtonType.CALCULATE -> {
            return copy(result=true to "未实现")
        }
        ButtonType.PERCENT -> {
            if (!isLastOperator){
                list.last().text.toBigDecimalOrNull()?.run {
                    list[list.lastIndex] = list.last().copy(
                        text = (this.divide(BigDecimal(100))).toPlainString()
                    )
                }
            }
        }
        else -> {
            if (list.isEmpty()) {
                list.add(CalculateUnit.from(buttonModel.text))
            }
            if (isLastOperator) {
                list.add(CalculateUnit.from(buttonModel.text))
            } else {
                list[list.lastIndex] = list.last().copy(text = (if (isEmpty) "" else list.last().text) + buttonModel.text)
            }
        }
    }
    kotlin.runCatching {
        return copy(result = false to Calculate.calculate(list.joinToString(separator="") { it.text }) )
    }

    return null
}


fun isOperator(input: String) = arrayOf("*", "+", "-", "/").contains(input)