package com.example.promptkeypad.keyboard

import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.view.inputmethod.InputConnection
import com.example.promptkeypad.R
import com.example.promptkeypad.WordDictionary

class KeyboardManager(
    private val keyboardView: KeyboardView,
    private val qwertyKeyboard: Keyboard
) {

    private var isCaps = false
    private var isNumberMode = false
    private var shouldAutoCap = true


    private val numberKeyboard =
        Keyboard(keyboardView.context, R.xml.numbers)

    fun handleKey(primaryCode: Int, inputConnection: InputConnection) {

        when (primaryCode) {

            KeyActions.SWITCH -> {
                isNumberMode = !isNumberMode
                keyboardView.keyboard =
                    if (isNumberMode) numberKeyboard else qwertyKeyboard
            }

            KeyActions.DELETE -> {
                inputConnection.deleteSurroundingText(1, 0)
            }

            KeyActions.SHIFT -> {
                isCaps = !isCaps
                qwertyKeyboard.isShifted = isCaps
                keyboardView.invalidateAllKeys()
            }
            KeyActions.SPACE -> {

                val text = inputConnection.getTextBeforeCursor(2, 0)?.toString() ?: ""

                if (text.endsWith(" ")) {
                    inputConnection.deleteSurroundingText(1, 0)
                    inputConnection.commitText(". ", 1)
                    shouldAutoCap = true
                    return
                }

                val fullText = inputConnection.getTextBeforeCursor(50, 0)?.toString() ?: ""
                val currentWord = fullText.split(" ").lastOrNull() ?: ""

                val correction = WordDictionary.getClosestMatch(currentWord)

                if (correction != null && correction != currentWord) {
                    inputConnection.deleteSurroundingText(currentWord.length, 0)
                    inputConnection.commitText("$correction ", 1)
                } else {
                    inputConnection.commitText(" ", 1)
                }
            }

            KeyActions.ENTER -> {

                val text = inputConnection.getTextBeforeCursor(50, 0)?.toString() ?: ""
                val lastWord = text.split(" ").lastOrNull() ?: ""

                WordDictionary.learnWord(lastWord, keyboardView.context)

                inputConnection.commitText("\n", 1)
            }



            else -> {
                var char = primaryCode.toChar()

                if (!isNumberMode) {
                    if (isCaps || shouldAutoCap) {
                        char = char.uppercaseChar()
                        shouldAutoCap = false
                    }
                }

                inputConnection.commitText(char.toString(), 1)
                if (char == '.' || char == '!' || char == '?') {
                    shouldAutoCap = true
                }


            }
        }
    }
}
