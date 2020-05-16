package classesTDL2

import java.io.File

val mapOfErrors = mutableMapOf<File, Errors>()

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