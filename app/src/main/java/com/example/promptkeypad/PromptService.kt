package com.example.promptkeypad

import android.annotation.SuppressLint
import android.inputmethodservice.InputMethodService
import android.view.Gravity
import android.view.View
import android.widget.*
import android.graphics.Color
import com.example.promptkeypad.keyboard.WordDictionary
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.content.Context

class PromptService : InputMethodService() {

    private lateinit var keyboardContainer: LinearLayout

    private lateinit var btnEmail: Button
    private lateinit var btnSocial: Button
    private lateinit var btnStudy: Button
    private lateinit var btnCode: Button
    private lateinit var btnBusiness: Button
    private lateinit var btnImage: Button

    private lateinit var suggestion1: TextView
    private lateinit var suggestion2: TextView
    private lateinit var suggestion3: TextView
    private var manualShift = false

    private var currentTone = "PROFESSIONAL"
    private var isCaps = false              // Start with caps ON (blank input)
    private var isCapsLock = false         // For double-tap shift
    private var lastShiftTime = 0L
    @SuppressLint("MissingInflatedId")
    override fun onCreateInputView(): View {

        val view = layoutInflater.inflate(R.layout.keyboard_view, null)

        keyboardContainer = view.findViewById(R.id.keyboardContainer)
        // ✅ LOAD DICTIONARY HERE
        WordDictionary.load(this)
        buildQwertyKeyboard()

        // Prompt buttons
        btnEmail = view.findViewById(R.id.btnEmail)
        btnSocial = view.findViewById(R.id.btnSocial)
        btnStudy = view.findViewById(R.id.btnStudy)
        btnCode = view.findViewById(R.id.btnCode)
        btnBusiness = view.findViewById(R.id.btnBusiness)
        btnImage = view.findViewById(R.id.btnImage)

        val btnTone = view.findViewById<TextView>(R.id.btnTone)

        btnTone.setOnClickListener {
            currentTone = when (currentTone) {
                "PROFESSIONAL" -> "CASUAL"
                "CASUAL" -> "STRICT"
                else -> "PROFESSIONAL"
            }

            btnTone.text = when (currentTone) {
                "PROFESSIONAL" -> "🎯 Pro"
                "CASUAL" -> "😄 Casual"
                else -> "⚡ Strict"
            }
        }

        btnEmail.setOnClickListener { insertPrompt("EMAIL") }
        btnSocial.setOnClickListener { insertPrompt("SOCIAL") }
        btnStudy.setOnClickListener { insertPrompt("STUDY") }
        btnCode.setOnClickListener { insertPrompt("CODE") }
        btnBusiness.setOnClickListener { insertPrompt("BUSINESS") }
        btnImage.setOnClickListener { insertPrompt("IMAGE") }

        btnEmail.setOnLongClickListener { showPromptOptions("EMAIL"); true }
        btnSocial.setOnLongClickListener { showPromptOptions("SOCIAL"); true }
        btnStudy.setOnLongClickListener { showPromptOptions("STUDY"); true }
        btnCode.setOnLongClickListener { showPromptOptions("CODE"); true }
        btnBusiness.setOnLongClickListener { showPromptOptions("BUSINESS"); true }
        btnImage.setOnLongClickListener { showPromptOptions("IMAGE"); true }

        suggestion1 = view.findViewById(R.id.suggestion1)
        suggestion2 = view.findViewById(R.id.suggestion2)
        suggestion3 = view.findViewById(R.id.suggestion3)

        return view
    }

    // ================= KEYBOARD BUILD =================

    private fun buildQwertyKeyboard() {

        if (!manualShift && !isCapsLock) {
            isCaps = shouldAutoCap()
        }

        keyboardContainer.removeAllViews()

        val rows = listOf(
            listOf("q","w","e","r","t","y","u","i","o","p"),
            listOf("a","s","d","f","g","h","j","k","l"),
            listOf("⇧","z","x","c","v","b","n","m","⌫"),
            listOf(",","?123","space",".","↵")
        )

        buildRows(rows)
    }

    private fun buildSymbolKeyboard() {

        keyboardContainer.removeAllViews()

        val rows = listOf(
            listOf("1","2","3","4","5","6","7","8","9","0"),
            listOf("@","#","$","_","&","-","+","(",")"),
            listOf("*","\"","'",";",":","!","?","/"),
            listOf("ABC",",","space",".","↵")
        )

        buildRows(rows)
    }


    private fun buildRows(rows: List<List<String>>) {

        rows.forEach { rowKeys ->

            val rowLayout = LinearLayout(this)
            rowLayout.orientation = LinearLayout.HORIZONTAL
            rowLayout.gravity = Gravity.CENTER
            rowLayout.layoutParams =
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )

            rowKeys.forEach { key ->

                val keyView = layoutInflater.inflate(
                    R.layout.item_key,
                    rowLayout,
                    false
                ) as TextView

//                keyView.text = key
                keyView.text = if (isCaps && key.length == 1 && key.matches(Regex("[a-z]")))
                    key.uppercase()
                else
                    key
                keyView.setOnClickListener {

                    keyView.animate()
                        .scaleX(0.9f)
                        .scaleY(0.9f)
                        .setDuration(50)
                        .withEndAction {
                            keyView.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .duration = 50
                        }

                    showKeyPreview(keyView.text.toString(), keyView)
                    handleKeyPress(key)
                }


                keyView.setOnLongClickListener {

                    val accentMap = mapOf(
                        "a" to listOf("á","à","ä","â"),
                        "e" to listOf("é","è","ë","ê"),
                        "i" to listOf("í","ì","ï","î"),
                        "o" to listOf("ó","ò","ö","ô"),
                        "u" to listOf("ú","ù","ü","û"),
                        "n" to listOf("ñ")
                    )

                    when (key) {
                        "." -> return@setOnLongClickListener showQuickPopup(listOf("!", "?", ",", ";", ":"))
                        "," -> return@setOnLongClickListener showQuickPopup(listOf(";", ":", "!", "?"))
                    }

                    val base = key.lowercase()

                    if (accentMap.containsKey(base)) {
                        return@setOnLongClickListener showQuickPopup(accentMap[base]!!)
                    }

                    false
                }


                rowLayout.addView(keyView)
            }

            keyboardContainer.addView(rowLayout)
        }
    }
    private fun showQuickPopup(options: List<String>): Boolean {

        val inputConnection = currentInputConnection ?: return false

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.HORIZONTAL
        layout.setBackgroundColor(Color.WHITE)

        val popup = PopupWindow(
            layout,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        )

        options.forEach { symbol ->

            val tv = TextView(this)
            tv.text = symbol
            tv.textSize = 20f
            tv.setPadding(40,40,40,40)
            tv.setTextColor(Color.BLACK)

            tv.setOnClickListener {
                inputConnection.commitText(symbol, 1)
                popup.dismiss()
            }

            layout.addView(tv)
        }

        popup.showAtLocation(keyboardContainer, Gravity.TOP, 0, 0)

        return true
    }

    private fun handleKeyPress(key: String) {

        vibrate()   // 🔥 Add this line
        val inputConnection = currentInputConnection ?: return

        when (key) {
            "space" -> {

                val text = getFullText()

                if (text.length >= 2 &&
                    text[text.length - 1] == ' ' &&
                    text[text.length - 2] == ' ') {

                    inputConnection.deleteSurroundingText(2, 0)
                    inputConnection.commitText(". ", 1)
                    isCaps = true
                    buildQwertyKeyboard()
                    return
                }

                inputConnection.commitText(" ",1)
            }

            "⌫" -> inputConnection.deleteSurroundingText(1,0)

            "↵" -> inputConnection.commitText("\n",1)
            "⇧" -> {

                val now = System.currentTimeMillis()

                if (now - lastShiftTime < 400) {
                    isCapsLock = !isCapsLock
                    isCaps = isCapsLock
                    manualShift = false
                } else {
                    manualShift = true
                    isCaps = !isCaps
                    isCapsLock = false
                }

                lastShiftTime = now
                buildQwertyKeyboard()
            }


            "?123" -> buildSymbolKeyboard()

            "ABC" -> buildQwertyKeyboard()
            else -> {

                var char = key

                if (isCaps) {
                    char = char.uppercase()
                }

                inputConnection.commitText(char, 1)

                // If NOT caps lock, disable caps after one letter
                if (manualShift && !isCapsLock) {
                    isCaps = false
                    manualShift = false
                }


                // If punctuation → next letter caps
                if (char == "." || char == "!" || char == "?") {
                    isCaps = true
                }
                if (char == "," ) {
                    isCaps = false
                }

                buildQwertyKeyboard() // refresh UI immediately
            }


        }
        checkAutoCaps()
        updateSuggestions()
    }
    private fun checkAutoCaps() {

        if (manualShift || isCapsLock) return

        val text = getFullText()

        if (text.isBlank()) {
            isCaps = true
            return
        }

        if (text.endsWith(". ") ||
            text.endsWith("! ") ||
            text.endsWith("? ") ||
            text.endsWith("\n")) {
            isCaps = true
        } else {
            isCaps = false
        }
    }


    // ================= PROMPT =================

    private fun insertPrompt(category: String) {

        val inputConnection = currentInputConnection ?: return
        val text = getFullText().trim()

        if (text.isBlank()) return

        val fullPrompt = PromptEngine.generate(category, text, "")

        val firstPrompt = fullPrompt
            .lines()
            .firstOrNull { it.isNotBlank() }
            ?.replace(Regex("^\\d+\\.\\s*"), "")
            ?: return

        inputConnection.deleteSurroundingText(text.length, 0)
        inputConnection.commitText(firstPrompt, 1)
    }

    private fun showPromptOptions(category: String) {

        val inputConnection = currentInputConnection ?: return
        val text = getFullText().trim()
        if (text.isBlank()) return

        val fullPrompt = PromptEngine.generate(category, text, "")

        val prompts = fullPrompt
            .lines()
            .filter { it.isNotBlank() }
            .map { it.replace(Regex("^\\d+\\.\\s*"), "") }

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setBackgroundColor(Color.WHITE)

        val popup = PopupWindow(
            layout,
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        )

        prompts.forEach { prompt ->

            val tv = TextView(this)
            tv.text = prompt
            tv.setPadding(40,40,40,40)
            tv.setTextColor(Color.BLACK)

            tv.setOnClickListener {
                inputConnection.deleteSurroundingText(text.length, 0)
                inputConnection.commitText(prompt, 1)
                popup.dismiss()
            }

            layout.addView(tv)
        }
        popup.animationStyle = android.R.style.Animation_Dialog
        popup.showAtLocation(keyboardContainer, Gravity.TOP, 0, 0)
    }

    // ================= SUGGESTIONS =================

    private fun updateSuggestions() {

        val text = getFullText().trim()

        if (text.contains(" ") && text.length > 5) {
            showPromptSuggestions(text)
            return
        }

        val emojis = EmojiEngine.suggest(text)

        if (emojis.isNotEmpty()) {
            suggestion1.text = emojis.getOrNull(0) ?: ""
            suggestion2.text = emojis.getOrNull(1) ?: ""
            suggestion3.text = emojis.getOrNull(2) ?: ""

            suggestion1.setOnClickListener {
                currentInputConnection?.commitText(emojis[0], 1)
            }
            return
        }

        showWordSuggestions()
    }

    private fun showWordSuggestions() {

        val currentWord = getCurrentWord()
        val suggestions = WordDictionary.getSuggestions(currentWord)

        val views = listOf(suggestion1, suggestion2, suggestion3)

        for (i in views.indices) {
            if (i < suggestions.size) {
                views[i].text = suggestions[i]
                views[i].setOnClickListener {
                    replaceCurrentWord(suggestions[i])
                }
            } else {
                views[i].text = ""
                views[i].setOnClickListener(null)
            }
        }
    }

    private fun showPromptSuggestions(text: String) {

        val categories = listOf("REWRITE","EMAIL","SOCIAL")
        val views = listOf(suggestion1, suggestion2, suggestion3)

        for (i in views.indices) {

            val category = categories[i]

            views[i].text = getCategoryLabel(category)

            views[i].setOnClickListener {
                if (category == "REWRITE") {
                    autoRewrite(text)
                } else {
                    autoInsertPrompt(category, text)
                }
            }
        }
    }

    private fun autoRewrite(text: String) {
        val inputConnection = currentInputConnection ?: return
        val rewritten = rewrite(text)
        inputConnection.deleteSurroundingText(text.length, 0)
        inputConnection.commitText(rewritten, 1)
    }
    private fun getCategoryLabel(category: String): String {
        return when (category) {
            "REWRITE" -> "✨ Rewrite"
            "EMAIL" -> "✉ Email"
            "SOCIAL" -> "📱 Social"
            "STUDY" -> "📘 Study"
            "BUSINESS" -> "📊 Business"
            "CODE" -> "💻 Code"
            "IMAGE" -> "🖼 Image"
            else -> category
        }
    }

    private fun autoInsertPrompt(category: String, text: String) {
        val inputConnection = currentInputConnection ?: return
        val fullPrompt = PromptEngine.generate(category, text, "")
        val firstPrompt = fullPrompt.lines()
            .firstOrNull { it.isNotBlank() }
            ?.replace(Regex("^\\d+\\.\\s*"), "")
            ?: return
        inputConnection.deleteSurroundingText(text.length, 0)
        inputConnection.commitText(firstPrompt, 1)
    }

    fun rewrite(text: String): String {
        val clean = text.trim()
        return when {
            clean.contains("leave", true) ->
                "I would like to request leave for the mentioned duration."
            else ->
                "Rewrite professionally: \"$clean\""
        }
    }

    private fun getFullText(): String {
        return currentInputConnection?.getTextBeforeCursor(200, 0)?.toString() ?: ""
    }

    private fun getCurrentWord(): String {
        val text = getFullText()
        return text.split(" ").lastOrNull() ?: ""
    }

    private fun replaceCurrentWord(newWord: String) {
        val inputConnection = currentInputConnection ?: return
        val currentWord = getCurrentWord()
        inputConnection.deleteSurroundingText(currentWord.length, 0)
        inputConnection.commitText(newWord, 1)
    }
    private fun shouldAutoCap(): Boolean {

        val text = getFullText()

        if (text.isBlank()) return true

        val triggers = listOf(
            ". ",
            "! ",
            "? ",
            "\n"
        )

        for (trigger in triggers) {
            if (text.endsWith(trigger)) return true
        }

        // After colon
        if (text.endsWith(": ")) return true

        // After closing bracket
        if (text.endsWith(") ")) return true

        return false
    }

    private fun vibrate() {

        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(15, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            vibrator.vibrate(15)
        }
    }
    private fun showKeyPreview(letter: String, anchor: View) {

        val preview = TextView(this)
        preview.text = letter
        preview.textSize = 32f
        preview.setTextColor(Color.BLACK)
        preview.setBackgroundColor(Color.WHITE)
        preview.gravity = Gravity.CENTER
        preview.setPadding(40, 40, 40, 40)
        preview.elevation = 12f

        val popup = PopupWindow(
            preview,
            160,
            160,
            false
        )

        val location = IntArray(2)
        anchor.getLocationOnScreen(location)

        popup.showAtLocation(
            keyboardContainer,
            Gravity.NO_GRAVITY,
            location[0],
            location[1] - 180
        )

        anchor.postDelayed({ popup.dismiss() }, 200)
    }


}
