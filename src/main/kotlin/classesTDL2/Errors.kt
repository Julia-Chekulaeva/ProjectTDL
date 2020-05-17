package classesTDL2

import java.io.File

val mapOfErrors = mutableMapOf<String, Errors>()

const val unresolved = "unresolved"

const val unmatchingArguments = "unmatching arguments"

const val ambiguity = "ambiguity"

const val emptyType = "empty type"

const val unrecognisedExpression = "unrecognised expression"

const val unrecognisedStringBlock = "unrecognised string block"

const val unclosedStringLiteral = "unclosedStringLiteral"

const val closingSignExpected1 = "closing \" expected"

const val closingSignExpected2 = "closing ';' expected"

const val nonClosedComment = "non-closed comment"

const val noPairForClosingBracket = "No pair for closing bracket"

const val noPairForOpeningBracket = "No pair for opening bracket"

const val variableTypeCannotBeReassigned = "variable type cannot be reassigned"

const val nameShouldStartWithLetter = "name should start with letter"

const val variableDeclarationNotAllowedHere = "variable declaration not allowed here"

const val invokeMustHaveABody = "invoke must have a body"

val keyWords = listOf<String>()

class Errors (private val listOfSeparatingIndices: List<Int>) {

    private val typedErrors = mutableListOf<Error>()

    fun addErrors(errors: List<Error>) {
        typedErrors.addAll(errors)
    }

    fun addError(index: Int, type: String) {
        for ((i, separatorInd) in listOfSeparatingIndices.withIndex().reversed()) {
            if (index > separatorInd) {
                // Т.к. пользователю удобнее нумеровать с единицы,
                // лучше сразу использовать символ на 1 больше "программистского"
                val srtIndex = i + 1
                val charIndex = index - separatorInd
                typedErrors.add(Error(srtIndex, charIndex, type))
                break
            }
        }
    }

    fun getErrorsMessages(): List<String> {
        val res = mutableListOf("Errors found: ${typedErrors.size}")
        for (errors in typedErrors.groupBy { it.strNumber }.map { it.key to it.value.sortedBy { it.message } }
                .sortedBy { it.first }.map { it.second })
            for (error in errors)
                res.add(error.getFullMessage())
        return res.toList()
    }

    class Error(val strNumber: Int, val index: Int, val message: String) {
        fun getFullMessage(): String {
            return "\tError in string $strNumber char $index: $message"
        }
    }
}