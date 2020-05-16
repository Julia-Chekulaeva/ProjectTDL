package classesTDL2

import java.io.File

class FunctionTDL(val name: String, val args: MutableMap<String, Pair<Boolean, TypeTDL?>>, val body: List<LexemBlock>) {

    var used = false

    val localVars = mutableMapOf<String, VariableTDL>()

    fun analysingFun(file: File, programNames: ProgramNames) {
        val bodyWithoutSpaces = body.filter { it.text.trim() != "" }
        val errorVars = mutableListOf<String>()
        for (lexem in bodyWithoutSpaces)
            lexem.analyseLowLevel(args, file, programNames, localVars, true, errorVars)
    }
}