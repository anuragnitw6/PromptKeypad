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
            "IMAGE" -> imagePrompts(cleanTopic)

            else -> "Write about \"$cleanTopic\" in a detailed and professional manner."
        }
    }

    // ---------------- EMAIL (10) ----------------

    private fun emailPrompts(topic: String) = """
1. Write a professional email about "$topic" including a clear subject line.
2. Draft a polite follow-up email regarding "$topic".
3. Compose a formal request email related to "$topic".
4. Write a short and concise email explaining "$topic".
5. Create a persuasive email regarding "$topic".
6. Write a friendly but professional email about "$topic".
7. Draft an apology email related to "$topic".
8. Write an urgent email about "$topic".
9. Create a negotiation email for "$topic".
10. Write a reminder email regarding "$topic".
""".trimIndent()

    // ---------------- SOCIAL (10) ----------------

    private fun socialPrompts(topic: String) = """
1. Create a viral Instagram caption about "$topic" with engaging hashtags.
2. Write a LinkedIn post about "$topic" in professional tone.
3. Generate a Twitter thread idea around "$topic".
4. Write a short motivational social media post about "$topic".
5. Create a bold controversial take on "$topic".
6. Write a storytelling style post about "$topic".
7. Create a short YouTube video script about "$topic".
8. Generate trending hashtag ideas for "$topic".
9. Write a funny meme caption about "$topic".
10. Create a call-to-action post about "$topic".
""".trimIndent()

    // ---------------- STUDY (10) ----------------

    private fun studyPrompts(topic: String) = """
1. Explain "$topic" in simple terms with examples.
2. Summarize "$topic" in bullet points for revision.
3. Provide key interview questions about "$topic".
4. Create a quick study guide for "$topic".
5. Compare "$topic" with related concepts.
6. Provide real-world examples of "$topic".
7. Create a mind map structure for "$topic".
8. Generate multiple choice questions about "$topic".
9. Explain "$topic" as if teaching a beginner.
10. Provide advanced insights into "$topic".
""".trimIndent()

    // ---------------- CODE (10) ----------------

    private fun codePrompts(topic: String) = """
1. Explain the concept of "$topic" with code examples.
2. Write optimized code for "$topic".
3. Debug common issues related to "$topic".
4. Compare best practices for "$topic".
5. Provide a real-world implementation of "$topic".
6. Write production-ready code for "$topic".
7. Explain performance optimization techniques for "$topic".
8. Create unit tests for "$topic".
9. Refactor existing code related to "$topic".
10. Explain "$topic" step-by-step for beginners.
""".trimIndent()

    // ---------------- BUSINESS (10) ----------------

    private fun businessPrompts(topic: String) = """
1. Create a business proposal about "$topic".
2. Write a startup pitch idea for "$topic".
3. Generate marketing strategy ideas for "$topic".
4. Draft a business plan outline for "$topic".
5. Create a SWOT analysis for "$topic".
6. Write an investor pitch about "$topic".
7. Generate revenue model ideas for "$topic".
8. Create branding ideas for "$topic".
9. Write a product launch strategy for "$topic".
10. Generate competitive analysis for "$topic".
""".trimIndent()

    // ---------------- IMAGE (10) ----------------

    private fun imagePrompts(topic: String) = """
1. Generate a realistic image of "$topic".
2. Create a cinematic version of "$topic".
3. Design a futuristic concept of "$topic".
4. Generate a minimalistic illustration of "$topic".
5. Create a 3D render of "$topic".
6. Generate a Pixar-style version of "$topic".
7. Create a cyberpunk version of "$topic".
8. Design a professional poster for "$topic".
9. Create a logo concept for "$topic".
10. Generate a high-resolution wallpaper of "$topic".
""".trimIndent()
}
