package com.example.promptkeypad
object EmojiEngine {

    fun suggest(text: String): List<String> {

        val lower = text.lowercase()

        return when {
            lower.contains("happy") -> listOf("😊", "🎉", "😄")
            lower.contains("birthday") -> listOf("🎂", "🎉", "🎈")
            lower.contains("love") -> listOf("❤️", "😍", "💕")
            lower.contains("sad") -> listOf("😢", "💔", "🥺")
            else -> emptyList()
        }
    }
}
