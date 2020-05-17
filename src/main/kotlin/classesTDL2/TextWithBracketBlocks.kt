package classesTDL2

import java.io.File

class TextWithBracketBlocks(
        val mainText: String, val blocks: List<TextWithBracketBlocks>, val mapOfIndex: Map<Int, Int>,
        val startIndex: Int, val wholeText: String
) {

    companion object {
        fun createBlock(text: String, mapOfIndex: Map<Int, Int>, startIndex: Int, separators: Pair<Char, Char>): TextWithBracketBlocks {
            val indicesOfThisBlock = mutableMapOf<Int, Int>()
            val listOfBlocks = mutableListOf<TextWithBracketBlocks>()
            val listOfText = mutableListOf<String>()
            var i = startIndex
            while (i < startIndex + text.length) {
                if (mapOfIndex[i] == null) {
                    i++
                    continue
                }
                indicesOfThisBlock[i] = mapOfIndex[i]!!
                i = mapOfIndex[i]!! + 1
            }
            val sortedMapOfInd = indicesOfThisBlock.map { it.key to it.value }.sortedBy { it.first }
            for ((key, value) in sortedMapOfInd) {
                val textInBrackets = text.substring(key + 1 - startIndex, value - startIndex)
                listOfText.add(textInBrackets)
                listOfBlocks.add(createBlock(
                        textInBrackets, mapOfIndex.filter { it.key in key + 1 until value }, key + 1, separators
                ))
            }
            var j = text.indexOf(separators.first) + 1
            var finalText = text
            for (elem in listOfText) {
                j = finalText.indexOf(elem, j)
                val openIndex = j
                j += elem.length
                j = finalText.indexOf(separators.second, j)
                finalText = finalText.substring(0, openIndex) + finalText.substring(j, finalText.length)
                j = openIndex + 1
            }
            println(finalText)
            return TextWithBracketBlocks(finalText, listOfBlocks, indicesOfThisBlock, startIndex, text)
        }
    }

    fun subtext(string: String, realIndex: Int): TextWithBracketBlocks {
        val index = realIndex - startIndex
        var i = startIndex
        var numberOfBlock = 0
        val subBlocks = mutableListOf<TextWithBracketBlocks>()
        val indicesOfThisBlock = mutableMapOf<Int, Int>()
        for (j in 0 until index) {
            if (mapOfIndex[i] != null) {
                i = mapOfIndex[i]!!
                numberOfBlock++
            }
            else i++
        }
        val startInd = i
        for (j in index until string.length) {
            if (mapOfIndex[i] != null) {
                indicesOfThisBlock[i] = mapOfIndex[i]!!
                i = mapOfIndex[i]!!
                subBlocks.add(blocks[numberOfBlock])
                numberOfBlock++
            }
            else i++
        }
        return TextWithBracketBlocks(string, subBlocks, indicesOfThisBlock, startInd, string)
    }

    private fun findSeparatorIndices(bracket: Char, vararg separatorChars: Char): List<Int> {
        val res = mutableListOf(startIndex - 1)
        var index = startIndex
        for (char in mainText) {
            when {
                char == bracket -> index = mapOfIndex[index]!! - 1
                separatorChars.contains(char) -> res.add(index)
            }
            index++
        }
        res.add(index)
        return res.toList()
    }

    fun split(brackets: Pair<Char, Char>, vararg separator: Char): List<TextWithBracketBlocks> {
        val bracketsTogether = "${brackets.first}${brackets.second}"
        val textBlocks = mainText.split(*separator)
        val res = mutableListOf<TextWithBracketBlocks>()
        val blockIndices = findSeparatorIndices(brackets.first, *separator)
        var indexOfBlock = 0
        for (i in textBlocks.indices) {
            val list = mutableListOf<TextWithBracketBlocks>()
            for (j in 1 until textBlocks[i].split(bracketsTogether).size) {
                list.add(blocks[indexOfBlock])
                indexOfBlock++
            }
            val startInd = blockIndices[i] + 1
            val endInd = blockIndices[i + 1]
            res.add(TextWithBracketBlocks(
                    textBlocks[i], list, mapOfIndex.filter { it.key in startInd until endInd },
                    blockIndices[i] + 1, wholeText.substring(startInd - startIndex, endInd - startIndex)
            ))
        }
        return res.toList()
    }

    fun createLexemBlocks(brackets: Pair<Char, Char>, separatorChar: Char, file: File): List<LexemBlock> {
        val bracketsTogether = "${brackets.first}${brackets.second}"
        val textBlocks = mainText.split(separatorChar)
        val res = mutableListOf<LexemBlock>()
        val blockIndices = findSeparatorIndices(brackets.first, separatorChar)
        var indexOfBlock = 0
        for (i in textBlocks.indices) {
            val lexemBlocks = if (textBlocks[i].split(bracketsTogether).size != 2)
                listOf()
            else
                blocks[indexOfBlock].createLexemBlocks(brackets, separatorChar, file)
            indexOfBlock += textBlocks[i].split(bracketsTogether).lastIndex
            res.add(LexemBlock(textBlocks[i], lexemBlocks, blockIndices[i] + 1))
        }
        if (res.last().text.trim() != "")
            mapOfErrors[file]!!.addError(startIndex + mainText.dropLastWhile { it == ' ' }.length, "closing ';' expected")
        return res.toList()
    }
}
