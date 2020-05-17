package classesTDL2

import java.io.File

class FunctionTDL(val name: String, val args: MutableMap<String, Pair<Boolean, TypeTDL?>>, val body: List<LexemBlock>) {

    var used = false

    val localVars = mutableMapOf<String, VariableTDL>()

    fun analysingFun(file: File, programNames: ProgramNames, invokeType: TypeTDL?) {
        val bodyWithoutSpaces = body.filter { it.text.trim() != "" }
        val errorVars = mutableListOf<String>()
        if (invokeType != null) {
            localVars["this"] = VariableTDL("this", ExpressionTDL("0", file, -1))
            localVars["this"]!!.type = invokeType
        }
        for (lexem in bodyWithoutSpaces)
            lexem.analyseLowLevel(args, file, programNames, localVars, true, errorVars)
    }
}