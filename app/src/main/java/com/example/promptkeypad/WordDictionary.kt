package com.example.promptkeypad.keyboard

import android.content.Context
import android.content.SharedPreferences
import kotlin.math.min

object WordDictionary {

    private val root = TrieNode()
    private var isLoaded = false

    private const val PREF_NAME = "user_dictionary"
    private const val KEY_USER_WORDS = "words"
    private val bigramMap = mutableMapOf<String, MutableList<String>>()

    private val frequencyMap = mutableMapOf<String, Int>()

    // ---------------- LOAD ----------------

    fun load(context: Context) {
        if (isLoaded) return

        // Load base dictionary
        context.assets.open("words.txt").bufferedReader().useLines { lines ->
            lines.forEach { insert(it.trim().lowercase()) }
        }
        // Load bigrams
        context.assets.open("bigram.txt").bufferedReader().useLines { lines ->
            lines.forEach { line ->
                val parts = line.trim().lowercase().split(" ")
                if (parts.size >= 2) {
                    val first = parts[0]
                    val second = parts[1]
                    bigramMap.getOrPut(first) { mutableListOf() }.add(second)
                }
            }
        }

        // Load user learned words
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val userWords = prefs.getStringSet(KEY_USER_WORDS, emptySet()) ?: emptySet()
        userWords.forEach { insert(it.lowercase()) }

        isLoaded = true
    }

    // ---------------- INSERT ----------------

    private fun insert(word: String) {
        if (word.isBlank()) return

        var node = root
        for (char in word) {
            node = node.children.getOrPut(char) { TrieNode() }
        }
        node.isWord = true
    }

    // ---------------- SUGGESTIONS ----------------

    fun getSuggestions(prefix: String): List<String> {
        if (prefix.isBlank()) return emptyList()

        val clean = prefix.lowercase()

        // Phrase suggestions
        phraseMap[clean]?.let { return it.take(3) }

        var node = root
        for (char in clean) {
            node = node.children[char] ?: return emptyList()
        }

        val results = mutableListOf<String>()
        collectWords(node, clean, results)
        // Bigram next-word prediction
        val words = clean.split(" ")
        if (words.size >= 1) {
            val lastWord = words.last()
            bigramMap[lastWord]?.let {
                return it.take(3)
            }
        }
        return results
            .sortedByDescending { frequencyMap[it] ?: 0 }
            .take(3)

    }

    private fun collectWords(node: TrieNode, prefix: String, result: MutableList<String>) {
        if (result.size >= 10) return

        if (node.isWord) result.add(prefix)

        for ((char, child) in node.children) {
            collectWords(child, prefix + char, result)
        }
    }

    // ---------------- AUTOCORRECT ----------------

    fun getClosestMatch(word: String): String? {
        if (word.isBlank()) return null

        val suggestions = getSuggestions(word)
        if (suggestions.isEmpty()) return null

        return suggestions.minByOrNull {
            levenshteinDistance(word.lowercase(), it)
        }
    }

    private fun levenshteinDistance(a: String, b: String): Int {
        val dp = Array(a.length + 1) { IntArray(b.length + 1) }

        for (i in 0..a.length) dp[i][0] = i
        for (j in 0..b.length) dp[0][j] = j

        for (i in 1..a.length) {
            for (j in 1..b.length) {
                dp[i][j] = if (a[i - 1] == b[j - 1]) {
                    dp[i - 1][j - 1]
                } else {
                    min(
                        dp[i - 1][j - 1],
                        min(dp[i - 1][j], dp[i][j - 1])
                    ) + 1
                }
            }
        }

        return dp[a.length][b.length]
    }

    // ---------------- LEARNING ----------------
    fun learnWord(word: String, context: Context) {

        if (word.length < 2) return

        insert(word.lowercase())

        frequencyMap[word.lowercase()] =
            (frequencyMap[word.lowercase()] ?: 0) + 1

        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val set = prefs.getStringSet(KEY_USER_WORDS, mutableSetOf())?.toMutableSet()
            ?: mutableSetOf()

        set.add(word.lowercase())
        prefs.edit().putStringSet(KEY_USER_WORDS, set).apply()
    }


    // ---------------- PHRASE ENGINE ----------------

    private val phraseMap = mapOf(
        "thank" to listOf("you", "you very much", "you for your support"),
        "good" to listOf("morning", "evening", "luck"),
        "how" to listOf("are you", "is it going", "to start"),
        "i" to listOf("am", "will", "would like to"),
        "please" to listOf("let me know", "confirm", "review")
    )

    // ---------------- NODE ----------------

    private class TrieNode {
        val children = mutableMapOf<Char, TrieNode>()
        var isWord = false
    }
}
