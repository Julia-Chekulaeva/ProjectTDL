package testsTDL

import classesTDL2.*
import java.io.File

fun countOfBrackets(string: String, file: File, bracket: Pair<Char, Char>, startIndex: Int): Pair<Map<Int, Int>, String> {
    val sb = StringBuilder(string)
    val res = mutableMapOf<Int, Int>()
    val listOfIndex = mutableListOf<Int>()
    for ((i, sign) in string.withIndex()) {
        when (sign) {
            bracket.first -> listOfIndex.add(i + startIndex)
            bracket.second -> {
                if (listOfIndex.isEmpty()) {
                    mapOfErrors[file.path]!!.addError(i + startIndex, noPairForClosingBracket)
                    sb[i] = ' '
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
            mapOfErrors[file.path]!!.addError(i, noPairForOpeningBracket)
            sb.append("}")
        }
    return res to sb.toString()
}

fun main(args: Array<String>) {
    val list1 = FilesParser.readingFiles("src/main/resources/examples/point.tdl", "src/main/resources/examples/triangle.tdl")
    val list2 = FilesParser.readDir("src")
    for (str in list1) {
        println(str)
    }
    println()
    println()
    println("New analyse")
    println()
    println()
    for (str in list2) {
        println(str)
    }
}
