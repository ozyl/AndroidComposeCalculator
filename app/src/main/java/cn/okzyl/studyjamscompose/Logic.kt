package cn.okzyl.studyjamscompose

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