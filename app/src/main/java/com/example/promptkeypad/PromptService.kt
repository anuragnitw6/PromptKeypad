package com.example.promptkeypad
import android.inputmethodservice.InputMethodService
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.example.promptkeypad.keyboard.KeyboardManager

class PromptService : InputMethodService(),
    KeyboardView.OnKeyboardActionListener {

    private lateinit var keyboardView: KeyboardView
    private lateinit var keyboardManager: KeyboardManager
    private lateinit var btnEmail: Button
    private lateinit var btnSocial: Button
    private lateinit var btnStudy: Button
    private lateinit var btnCode: Button
    private lateinit var btnBusiness: Button
    private lateinit var btnImage: Button

    private lateinit var suggestion1: TextView
    private lateinit var suggestion2: TextView
    private lateinit var suggestion3: TextView
    private lateinit var qwertyKeyboard: Keyboard
    private lateinit var symbolKeyboard: Keyboard
    private var isSymbol = false
    private var currentTone = "PROFESSIONAL"


    override fun onCreateInputView(): View {

        val view = layoutInflater.inflate(R.layout.keyboard_view, null)

        keyboardView = view.findViewById(R.id.keyboardView)
        qwertyKeyboard = Keyboard(this, R.xml.qwerty)
        symbolKeyboard = Keyboard(this, R.xml.symbols)

        keyboardView.keyboard = qwertyKeyboard

        keyboardView.setOnKeyboardActionListener(this)

        keyboardManager = KeyboardManager(keyboardView, qwertyKeyboard)

        // PROMPT BUTTONS
        btnEmail = view.findViewById(R.id.btnEmail)
        btnSocial = view.findViewById(R.id.btnSocial)
        btnStudy = view.findViewById<Button>(R.id.btnStudy)
        btnCode = view.findViewById<Button>(R.id.btnCode)
        btnBusiness = view.findViewById<Button>(R.id.btnBusiness)
        btnImage = view.findViewById<Button>(R.id.btnImage)

        btnEmail.isClickable = true
        btnSocial.isClickable = true
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

        btnEmail.setOnClickListener {
            android.util.Log.d("PromptKeypad", "Email clicked")
            insertPrompt("EMAIL")
        }

        btnSocial.setOnClickListener {
            android.util.Log.d("PromptKeypad", "Social clicked")
            insertPrompt("SOCIAL")
        }

        btnStudy.setOnClickListener { insertPrompt("STUDY") }
        btnCode.setOnClickListener { insertPrompt("CODE") }
        btnBusiness.setOnClickListener { insertPrompt("BUSINESS") }
        btnImage.setOnClickListener { insertPrompt("IMAGE") }
        btnEmail.setOnLongClickListener {
            showPromptOptions("EMAIL")
            true
        }

        btnSocial.setOnLongClickListener {
            showPromptOptions("SOCIAL")
            true
        }
        btnStudy.setOnLongClickListener {
            showPromptOptions("STUDY")
            true
        }

        btnCode.setOnLongClickListener {
            showPromptOptions("CODE")
            true
        }
        btnBusiness.setOnLongClickListener {
            showPromptOptions("BUSINESS")
            true
        }

        btnImage.setOnLongClickListener {
            showPromptOptions("IMAGE")
            true
        }
        // Suggestions
        suggestion1 = view.findViewById(R.id.suggestion1)
        suggestion2 = view.findViewById(R.id.suggestion2)
        suggestion3 = view.findViewById(R.id.suggestion3)

        return view
    }
    override fun onKey(primaryCode: Int, keyCodes: IntArray?) {

        val inputConnection = currentInputConnection ?: return

        when (primaryCode) {

            -10 -> {   // Switch layout
                isSymbol = !isSymbol
                keyboardView.keyboard = if (isSymbol) symbolKeyboard else qwertyKeyboard
                return
            }

            else -> keyboardManager.handleKey(primaryCode, inputConnection)
        }

        updateSuggestions()
    }



    override fun onPress(primaryCode: Int) {}

    override fun onRelease(primaryCode: Int) {}

    override fun onText(text: CharSequence?) {
        val inputConnection = currentInputConnection ?: return
        inputConnection.commitText(text, 1)
    }

    override fun swipeLeft() {}

    override fun swipeRight() {}

    override fun swipeDown() {}

    override fun swipeUp() {}

    private fun getCurrentWord(): String {
        val inputConnection = currentInputConnection ?: return ""
        val text = inputConnection.getTextBeforeCursor(50, 0)?.toString() ?: ""

        return text.split(" ").lastOrNull() ?: ""
    }
    private fun replaceCurrentWord(newWord: String) {
        val inputConnection = currentInputConnection ?: return
        val text = inputConnection.getTextBeforeCursor(50, 0)?.toString() ?: ""

        val words = text.split(" ")
        val currentWord = words.lastOrNull() ?: return

        inputConnection.deleteSurroundingText(currentWord.length, 0)
        inputConnection.commitText(newWord, 1)
    }
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

        val app = getCurrentApp()

        val categories = when {

            app.contains("gmail", true) ->
                listOf("REWRITE", "EMAIL", "BUSINESS")

            app.contains("whatsapp", true) ->
                listOf("REWRITE", "SOCIAL", "EMAIL")

            app.contains("linkedin", true) ->
                listOf("REWRITE", "BUSINESS", "EMAIL")

            else ->
                listOf("REWRITE", "EMAIL", "SOCIAL")
        }

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
            else -> category
        }
    }

    fun rewrite(text: String): String {

        val clean = text.trim()

        return when {

            clean.contains("not coming", true) ->
                "I will be unavailable tomorrow."

            clean.contains("leave", true) ->
                "I would like to request leave for the specified duration."

            clean.contains("sorry", true) ->
                "I sincerely apologize for the inconvenience."

            else ->
                "Rewrite the following sentence in a professional and clear manner: \"$clean\""
        }
    }


    private fun autoInsertPrompt(category: String, text: String) {

        val inputConnection = currentInputConnection ?: return

        val fullPrompt = PromptEngine.generate(category, text, "")

        val firstPrompt = fullPrompt
            .lines()
            .firstOrNull { it.isNotBlank() }
            ?.replace(Regex("^\\d+\\.\\s*"), "")
            ?: return

        inputConnection.deleteSurroundingText(text.length, 0)
        inputConnection.commitText(firstPrompt, 1)
    }

    private fun insertPrompt(category: String) {

        val inputConnection = currentInputConnection ?: return
        val text = inputConnection.getTextBeforeCursor(300, 0)?.toString() ?: ""

        if (text.isBlank()) return

        // Generate full 4-prompt block
        val fullPrompt = PromptEngine.generate(category, text.trim(), "")

        // Extract ONLY first line
        val firstPrompt = fullPrompt
            .lines()
            .firstOrNull { it.isNotBlank() }
            ?.replace(Regex("^\\d+\\.\\s*"), "") // remove "1. "
            ?: return

        // Delete typed sentence
        inputConnection.deleteSurroundingText(text.length, 0)

        // Insert only first prompt
        inputConnection.commitText(firstPrompt, 1)
    }
    private fun showPromptOptions(category: String) {

        val inputConnection = currentInputConnection ?: return
        val text = inputConnection.getTextBeforeCursor(300, 0)?.toString() ?: ""
        if (text.isBlank()) return

        val fullPrompt = PromptEngine.generate(category, text.trim(), "")

        val prompts = fullPrompt
            .lines()
            .filter { it.isNotBlank() }
            .map { it.replace(Regex("^\\d+\\.\\s*"), "") }

        // Create vertical layout for popup
        val layout = android.widget.LinearLayout(this)
        layout.orientation = android.widget.LinearLayout.VERTICAL
        layout.setBackgroundColor(android.graphics.Color.WHITE)

        val popup = android.widget.PopupWindow(
            layout,
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        prompts.forEach { prompt ->

            val tv = android.widget.TextView(this)
            tv.text = prompt
            tv.setPadding(30, 30, 30, 30)
            tv.setTextColor(android.graphics.Color.BLACK)

            tv.setOnClickListener {

                inputConnection.deleteSurroundingText(text.length, 0)
                inputConnection.commitText(prompt, 1)
                popup.dismiss()
            }

            layout.addView(tv)
        }

        popup.showAtLocation(
            keyboardView,
            android.view.Gravity.TOP,
            0,
            0
        )
    }
    private fun getFullText(): String {
        val inputConnection = currentInputConnection ?: return ""
        return inputConnection.getTextBeforeCursor(200, 0)?.toString() ?: ""
    }
    private fun getCurrentApp(): String {
        return currentInputEditorInfo?.packageName ?: ""
    }


}
