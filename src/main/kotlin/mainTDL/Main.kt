package mainTDL

import classesTDL2.*
import java.io.File

fun countOfBrackets(string: String, file: File?, bracket: Pair<Char, Char>, startIndex: Int): Pair<Map<Int, Int>, String> {
    val sb = StringBuilder(string)
    val res = mutableMapOf<Int, Int>()
    val listOfIndex = mutableListOf<Int>()
    for ((i, sign) in string.withIndex()) {
        when (sign) {
            bracket.first -> listOfIndex.add(i + startIndex)
            bracket.second -> {
                if (listOfIndex.isEmpty()) {
                    if (file != null)
                        mapOfErrors[file]!!.addError(i + startIndex, "No pair for closing bracket")
                    sb.replace(i, i + 1, " ")
                }
                else {
                    res[listOfIndex.last()] = i + startIndex
                    listOfIndex.removeAt(listOfIndex.lastIndex)
                }
            }
        }
    }
    if (listOfIndex.isNotEmpty())
        for (i in listOfIndex) {
            if (file != null)
                mapOfErrors[file]!!.addError(i, "No pair for opening bracket")
            sb.append("}")
        }
    return res to sb.toString()
}

fun main(args: Array<String>) {
    val file = File("src/main/resources/examples/triangle.tdl")
    val parser = FilesParser()
    val list = parser.readingFiles(file)
    for (str in list) {
        println(str)
    }
}
