package classesTDL2

data class Lexem(val startIndex: Int, val text: String) {

    companion object {

        private const val CHAR_LEXEMS = "+-/*<>=().{}; "

        fun createLexems(text: String, startIndex: Int): List<Lexem> {
            val res = mutableListOf<Lexem>()
            var startIndexForLexem = 0
            for ((i, char) in text.withIndex()) {
                if (char in CHAR_LEXEMS) {
                    res.add(Lexem(startIndexForLexem, text.substring(startIndexForLexem, i)))
                    startIndexForLexem = i + 1
                }
            }
            return res.map {
                Lexem(it.startIndex + startIndex, it.text)
            }
        }
    }
}