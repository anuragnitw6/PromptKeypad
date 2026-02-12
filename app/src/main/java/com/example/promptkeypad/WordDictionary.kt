package com.example.promptkeypad
import android.content.Context

object WordDictionary {

    private val words = mutableSetOf(
        "hello", "help", "helmet", "healthy",
        "how", "house", "home", "honest",
        "thank", "thanks", "thankful",
        "good", "great", "going", "gone",
        "yes", "yesterday", "your", "young"
    )

    fun getSuggestions(prefix: String): List<String> {
        if (prefix.isEmpty()) return emptyList()

        return words.filter {
            it.startsWith(prefix.lowercase())
        }.take(3)
    }

    fun getClosestMatch(word: String): String? {

        if (word.length < 3) return null

        val match = words.minByOrNull {
            levenshteinDistance(it, word.lowercase())
        } ?: return null

        val distance = levenshteinDistance(match, word.lowercase())

        return if (distance <= 1) match else null
    }

    fun learnWord(word: String, context: Context) {
        if (word.length > 2) {
            words.add(word.lowercase())

            val prefs = context.getSharedPreferences("keyboard_words", Context.MODE_PRIVATE)
            prefs.edit().putStringSet("words", words).apply()
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
                    1 + minOf(
                        dp[i - 1][j],
                        dp[i][j - 1],
                        dp[i - 1][j - 1]
                    )
                }
            }
        }

        return dp[a.length][b.length]
    }
    fun loadWords(context: Context) {
        val prefs = context.getSharedPreferences("keyboard_words", Context.MODE_PRIVATE)
        val saved = prefs.getStringSet("words", emptySet()) ?: emptySet()
        words.addAll(saved)
    }

}
