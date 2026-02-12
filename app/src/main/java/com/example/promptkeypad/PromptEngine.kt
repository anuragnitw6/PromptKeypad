package com.example.promptkeypad

object PromptEngine {

    fun generate(category: String, topic: String, app: String): String {

        val cleanTopic = topic.trim()

        return when (category) {

            "EMAIL" -> emailPrompts(cleanTopic)
            "SOCIAL" -> socialPrompts(cleanTopic)
            "STUDY" -> studyPrompts(cleanTopic)
            "CODE" -> codePrompts(cleanTopic)
            "BUSINESS" -> businessPrompts(cleanTopic)

            else -> "Write about \"$cleanTopic\" in a detailed and professional manner."
        }
    }

    // ---------------- EMAIL (4) ----------------

    private fun emailPrompts(topic: String): String {
        return """
1. Write a professional email about "$topic" including a clear subject line.
2. Draft a polite follow-up email regarding "$topic".
3. Compose a formal request email related to "$topic".
4. Write a short and concise email explaining "$topic".
""".trimIndent()
    }

    // ---------------- SOCIAL (4) ----------------

    private fun socialPrompts(topic: String): String {
        return """
1. Create a viral Instagram caption about "$topic" with engaging hashtags.
2. Write a LinkedIn post about "$topic" in professional tone.
3. Generate a Twitter thread idea around "$topic".
4. Write a short motivational social media post about "$topic".
""".trimIndent()
    }

    // ---------------- STUDY (4) ----------------

    private fun studyPrompts(topic: String): String {
        return """
1. Explain "$topic" in simple terms with examples.
2. Summarize "$topic" in bullet points for revision.
3. Provide key interview questions about "$topic".
4. Create a quick study guide for "$topic".
""".trimIndent()
    }

    // ---------------- CODE (4) ----------------

    private fun codePrompts(topic: String): String {
        return """
1. Explain the concept of "$topic" with code examples.
2. Write optimized code for "$topic".
3. Debug common issues related to "$topic".
4. Compare best practices for "$topic".
""".trimIndent()
    }

    // ---------------- BUSINESS (4) ----------------

    private fun businessPrompts(topic: String): String {
        return """
1. Create a business proposal about "$topic".
2. Write a startup pitch idea for "$topic".
3. Generate marketing strategy ideas for "$topic".
4. Draft a business plan outline for "$topic".
""".trimIndent()
    }
}
