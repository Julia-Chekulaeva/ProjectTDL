package classesTDL2

import testsTDL.countOfBrackets
import java.io.File

const val CHAR_FOR_STRINGS = '_'

class FileTDL(val file: File) {

    private val firstErrors: MutableList<Errors.Error> = mutableListOf()

    private val strings = removeComments(replaceStringConstants(file.readLines()).toMutableList())

    fun createErrors() {
        val listOfSeparatingIndices = mutableListOf(-1)
        for (string in strings)
            listOfSeparatingIndices.add(string.length + listOfSeparatingIndices.last() + 1)
        mapOfErrors[file.path] = Errors(listOfSeparatingIndices)
        mapOfErrors[file.path]!!.addErrors(firstErrors)
    }

    private fun replaceStringConstants(strings: List<String>): List<String> {
        // Если файл содержит символ '_', то могут возникнуть ошибки
        val res = mutableListOf<String>()
        for (str in strings) {
            val sb = StringBuilder(str)
            var index = 0
            val length = str.length
            while (index < length) {
                if (str[index] == '\"') {
                    index++
                    while (index < length && str[index] != '\"') {
                        if (str[index] == '\\' && index < length - 1) {
                            sb[index] = CHAR_FOR_STRINGS
                            index++
                        }
                        sb[index] = CHAR_FOR_STRINGS
                        index++
                    }
                }
                index++
            }
            res.add(sb.toString())
        }
        return res
    }

    fun readFile(): List<LexemBlock> {
        val wholeText = strings.joinToString(" ")
        val mapOfBraketsWithText = countOfBrackets(wholeText, file, '{' to '}', 0)
        val block = TextWithBracketBlocks.createBlock(mapOfBraketsWithText.second, mapOfBraketsWithText.first, 0, '{' to '}')
        return block.createLexemBlocks('{' to '}', ';', file)
    }

    private fun removeComments(strings: MutableList<String>): List<String> {
        var i = 0
        val size = strings.size
        while (i < size) {
            val s = strings[i]
            val commentTextInd = s.indexOf("/*")
            val commentLineInd = s.indexOf("//")
            if (commentLineInd == -1 && commentTextInd == -1) {
                if (strings[i].matches(Regex("""(.*[^$CHAR_FOR_STRINGS])?"$CHAR_FOR_STRINGS*"""))) {
                    firstErrors.add(Errors.Error(i + 1, strings[i].length, unclosedStringLiteral))
                }
                i++
                continue
            }
            if ((commentLineInd == -1 || commentTextInd < commentLineInd) && commentTextInd > -1) {
                if (s.contains("*/")) {
                    val closeInd1 = s.indexOf("*/") + 2
                    val sb1 = StringBuilder(closeInd1 - commentTextInd)
                    for (j in sb1.indices)
                        sb1[j] = ' '
                    strings[i] = s.substring(0, commentTextInd) + sb1.toString() + s.substring(closeInd1, s.length)
                    if (strings[i].matches(Regex("""(.*[^$CHAR_FOR_STRINGS])?"$CHAR_FOR_STRINGS*"""))) {
                        firstErrors.add(Errors.Error(i + 1, strings[i].length, closingSignExpected1))
                    }
                } else {
                    strings[i] = s.substring(0, commentTextInd)
                    i++
                    while (i < size && !strings[i].contains("*/")) {
                        strings[i] = ""
                        i++
                    }
                    if (i == size) {
                        firstErrors.add(Errors.Error(i + 1, strings.last().length, nonClosedComment))
                        return strings
                    }
                    val closeInd2 = strings[i].indexOf("*/") + 2
                    val sb2 = StringBuilder()
                    for (j in 0..closeInd2)
                        sb2.append(" ")
                    strings[i] = sb2.toString() + strings[i].substring(closeInd2, strings[i].length)
                    continue
                }
            } else {
                strings[i] = s.substring(0, commentLineInd)
                if (strings[i].matches(Regex("""(.*[^$CHAR_FOR_STRINGS])?"$CHAR_FOR_STRINGS*"""))) {
                    firstErrors.add(Errors.Error(i + 1, strings[i].length, unclosedStringLiteral))
                }
            }
        }
        return strings.toList()
    }
}