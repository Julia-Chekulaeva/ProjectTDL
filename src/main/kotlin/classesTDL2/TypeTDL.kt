package classesTDL2

import java.io.File

class TypeTDL(val name: String, val fields: MutableMap<String, Boolean>) {

    var hasInvoke = false

    var used = false

    var usedInvoke = false

    var localFunInvoke: FunctionTDL? = null

    val invokeBody: MutableList<LexemBlock> = mutableListOf()

    fun runInvoke(file: File, programNames: ProgramNames) {
        val args = mutableMapOf<String, Pair<Boolean, TypeTDL?>>()
        for (fieldName in fields.keys)
            args[fieldName] = false to null
        val function = FunctionTDL(name, args, invokeBody)
        function.analysingFun(file, programNames)
        localFunInvoke = function
        val args2 = localFunInvoke!!.args
        for ((argName, usedAndType) in args2)
            if (usedAndType.first)
                fields[argName] = true
    }
}