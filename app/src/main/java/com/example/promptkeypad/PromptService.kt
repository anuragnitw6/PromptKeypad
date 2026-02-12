package com.example.promptkeypad

import android.annotation.SuppressLint
import android.inputmethodservice.InputMethodService
import android.view.Gravity
import android.view.View
import android.widget.*
import android.graphics.Color

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

    private var currentTone = "PROFESSIONAL"

    @SuppressLint("MissingInflatedId")
    override fun onCreateInputView(): View {

        val view = layoutInflater.inflate(R.layout.keyboard_view, null)

        keyboardContainer = view.findViewById(R.id.keyboardContainer)

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

        keyboardContainer.removeAllViews()

        val rows = listOf(
            listOf("q","w","e","r","t","y","u","i","o","p"),
            listOf("a","s","d","f","g","h","j","k","l"),
            listOf("⇧","z","x","c","v","b","n","m","⌫"),
            listOf("?123","space","↵")
        )

        buildRows(rows)
    }

    private fun buildSymbolKeyboard() {

        keyboardContainer.removeAllViews()

        val rows = listOf(
            listOf("1","2","3","4","5","6","7","8","9","0"),
            listOf("!","?","@","#","$","%","&","*"),
            listOf("(",")","-","_","+","="),
            listOf("ABC","space","↵")
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

                keyView.text = key

                keyView.setOnClickListener {
                    handleKeyPress(key)
                }

                rowLayout.addView(keyView)
            }

            keyboardContainer.addView(rowLayout)
        }
    }

    private fun handleKeyPress(key: String) {

        val inputConnection = currentInputConnection ?: return

        when (key) {

            "space" -> inputConnection.commitText(" ", 1)

            "⌫" -> inputConnection.deleteSurroundingText(1,0)

            "↵" -> inputConnection.commitText("\n",1)

            "⇧" -> {
                // caps logic later
            }

            "?123" -> buildSymbolKeyboard()

            "ABC" -> buildQwertyKeyboard()

            else -> inputConnection.commitText(key,1)
        }

        updateSuggestions()
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
}
