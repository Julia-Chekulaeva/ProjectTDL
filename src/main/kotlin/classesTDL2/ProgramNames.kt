package classesTDL2

import java.io.File

class ProgramNames(private val blocks: List<CommandBlock>, val file: File) {

    // Here is the beginning of the old functions

    class Imports {
        val importedFunctions = mutableMapOf<Pair<String, Int>, FunctionTDL>()

        val importedTypes = mutableMapOf<String, TypeTDL>()

        val importedVars = mutableMapOf<String, VariableTDL>()
    }

    val allFunctions = mutableMapOf<Pair<String, Int>, FunctionTDL>()

    val allTypes = mutableMapOf<String, TypeTDL>()

    val allVars = mutableMapOf<String, VariableTDL>()

    val importedFiles = mutableListOf<File>()

    val imports = Imports()

    val errorTypes = mutableListOf<String>()

    val errorFunctions = mutableListOf<Pair<String, Int>>()

    val errorInvokes = mutableListOf<String>()

    val errorVars = mutableListOf<String>()

    val types = mutableMapOf(
            "String" to TypeTDL("String", mutableMapOf()), "Integer" to TypeTDL("Integer", mutableMapOf())
    )

    val functions = mutableMapOf<Pair<String, Int>, FunctionTDL>()

    val invokes = mutableListOf<Triple<String, List<CommandBlock>, Int>>()

    val vars = mutableMapOf<String, VariableTDL>()

    fun getAllNames(): List<String> {
        val res = mutableListOf<String>()
        res.add("Types: ${types.filter { it.key != "String" && it.key != "Integer" }
                .values.joinToString { "${it.name}(${it.fields.keys.joinToString(", ")})" }}")
        res.add("Functions: ${functions.values.joinToString { "${it.name}(${it.args.keys.joinToString(", ")})" }}")
        res.add("Variables: ${vars.values.joinToString { it.name }}")
        res.add("Invokes on: ${types.filter { it.value.hasInvoke }.map { it.key }.joinToString(", ")}")
        return res.toList()
    }

    fun getUnused(): List<String> {
        val unusedTypes = types.filter { !it.value.used && it.key != "String" && it.key != "Integer" }.map { it1 ->
            it1.key to it1.value.fields.map { it.key }
        }
        val typesWithUnusedFields = types.filter { it.key != "String" && it.key != "Int" }.map { it1 ->
            it1.key to it1.value.fields.filter { !it.value }.map { it.key }
        }
        val unusedFuns = functions.filter { !it.value.used && it.key != "main" to 0 }.map { it1 ->
            it1.key.first to it1.value.args.map { it.key }
        }
        val funsWithUnusedFieldsAndVars = functions.map { it1 ->
            it1.key.first to (
                it1.value.args.filter { !it.value.first }.map { it.key }
                        to it1.value.localVars.filter { !it.value.used }.map { it.key }
                ) }
        val unusedVars = vars.filter { !it.value.used }.map { it.key }
        val unusedInvokes = types.filter { it.value.hasInvoke && !it.value.usedInvoke }.map { it.key }
        val invokesWithUnusedVars = types.filter { it.value.hasInvoke }.map { it1 ->
            it1.key to it1.value.localFunInvoke!!.args.filter { !it.value.first }.map { it.key }
        }
        val res = mutableListOf("Unused")
        res.add("\ttypes:")
        res.addAll(unusedTypes.map { it1 -> "\t\t${it1.first}(${it1.second.joinToString { it }})" })
        res.add("\ttype fields:")
        typesWithUnusedFields.forEach { it1 ->
            res.add("\t\t${it1.first}:")
            res.addAll(it1.second.map { "\t\t\t$it" })
        }
        res.add("\tfunctions:")
        res.addAll(unusedFuns.map { it1 -> "\t\t${it1.first}(${it1.second.joinToString { it }})" })
        res.add("\tinvokes:")
        res.addAll(unusedInvokes.map { "\t\t$it" })
        res.add("\tfunctions arguments and local variables:")
        funsWithUnusedFieldsAndVars.forEach { it1 ->
            res.add("\t\t${it1.first}:")
            res.add("\t\t\targuments:")
            res.addAll(it1.second.first.map { "\t\t\t\t$it" })
            res.add("\t\t\tlocal variables:")
            res.addAll(it1.second.second.map { "\t\t\t\t$it" })
        }
        res.add("\tinvokes local variables:")
        invokesWithUnusedVars.forEach { it1 ->
            res.add("\t\t${it1.first}:")
            res.addAll(it1.second.map { "\t\t\t$it" })
        }
        res.add("\tglobal variables:")
        res.addAll(unusedVars.map { "\t\t$it" })
        return res.toList()
    }

    fun addImportsToThis() {
        allFunctions.putAll(functions)
        allFunctions.putAll(imports.importedFunctions)
        allTypes.putAll(types)
        allTypes.putAll(imports.importedTypes)
        allVars.putAll(vars)
        allVars.putAll(imports.importedVars)
    }

    fun separatingImportsFromThis() {
        for (name in functions.keys)
            functions[name] = allFunctions[name]!!
        for (name in imports.importedFunctions.keys)
            imports.importedFunctions[name] = allFunctions[name]!!
        for (name in types.keys)
            types[name] = allTypes[name]!!
        for (name in imports.importedTypes.keys)
            imports.importedTypes[name] = allTypes[name]!!
        for (name in vars.keys)
            vars[name] = allVars[name]!!
        for (name in imports.importedVars.keys)
            imports.importedVars[name] = allVars[name]!!
    }

    fun analysingFile() {
        val blocksWithoutSpaces = blocks.filter { it.text.trim() != "" }
        var i = 0
        while (blocksWithoutSpaces[i].analysingImports(file, this))
            i++
        for (j in i until blocksWithoutSpaces.size)
            blocksWithoutSpaces[j].analyseTopLevelLexem(file, this)
        correctTypes()
        correctFunctions()
        correctInvokes()
        correctVars()
    }

    fun analysingFunsAndInvokes() {
        for (function in functions.values)
            function.analysingFun(file, this, null)
        for (type in types.values.filter { it.hasInvoke })
            type.runInvoke(file, this)
    }

    fun addingOtherProgramNames(programNamesFromFiles: List<ProgramNames>) {
        for (programNames in programNamesFromFiles) {
            for ((name, type) in programNames.types) {
                if (types[name] == null)
                    imports.importedTypes[name] = type
            }
            for ((key, function) in programNames.functions) {
                if (functions[key] == null)
                    imports.importedFunctions[key] = function
            }
            for ((name, variable) in programNames.vars) {
                if (types[name] == null)
                    imports.importedVars[name] = variable
            }
        }
    }

    fun usedImports(programNames: ProgramNames): ProgramNames {
        for ((name, type) in imports.importedTypes) {
            if (programNames.types[name] != null) {
                if (type.used)
                    programNames.types[name]!!.used = true
                for ((field, used) in type.fields)
                    if (used)
                        programNames.types[name]!!.fields[field] = true
            }
        }
        for ((name, function) in imports.importedFunctions)
            if (programNames.functions[name] != null) {
                if (function.used)
                    programNames.functions[name]!!.used = true
            }
        for ((name, variable) in imports.importedVars)
            if (programNames.vars[name] != null)
                if (variable.used)
                    programNames.vars[name]!!.used = true
        return programNames
    }

    fun invokesOn() {
        for (invoke in invokes) {
            val name = invoke.first
            if (allTypes[name] != null) {
                allTypes[name]!!.hasInvoke = true
                allTypes[name]!!.invokeBody.addAll(invoke.second)
            }
        }
    }

    fun analysingFieldsAndInvokes(
            allVarsWithFields: List<Pair<String, Pair<String, Int>>>, allVarsWithInvoke: List<Pair<String, Int>>,
            localVars: MutableMap<String, VariableTDL>, args: MutableMap<String, Pair<Boolean, TypeTDL?>>
    ) {
        for ((varName, fieldWithIndex) in allVarsWithFields) {
            val localVar = localVars[varName]
            val arg = args[varName]
            val globalVar = allVars[varName]
            when {
                localVar != null -> {
                    val type = localVar.type
                    if (type != null && type.fields.contains(fieldWithIndex.first))
                        allTypes[type.name]!!.fields[fieldWithIndex.first] = true
                    else
                        mapOfErrors[file.path]!!.addError(fieldWithIndex.second, unresolved)
                }
                arg != null -> {
                    val type = arg.second
                    if (type != null && type.fields.contains(fieldWithIndex.first))
                        allTypes[type.name]!!.fields[fieldWithIndex.first] = true
                    else
                        mapOfErrors[file.path]!!.addError(fieldWithIndex.second, unresolved)
                }
                globalVar != null -> {
                    val type = globalVar.type
                    if (type != null && type.fields.contains(fieldWithIndex.first))
                        allTypes[type.name]!!.fields[fieldWithIndex.first] = true
                    else
                        mapOfErrors[file.path]!!.addError(fieldWithIndex.second, unresolved)
                }
            }
        }
        for (varNameAndIndex in allVarsWithInvoke) {
            val varName = varNameAndIndex.first
            val localVar = localVars[varName]
            val arg = args[varName]
            val globalVar = allVars[varName]
            val function = allFunctions.map { it.key.first to it.value }.toMap()[varName]
            val typeTDL = allTypes[varName]
            when {
                localVar?.type != null && localVar!!.type!!.hasInvoke -> {
                    localVars[varNameAndIndex.first]!!.used = true
                    allTypes[localVar.type!!.name]!!.usedInvoke = true
                }
                arg?.second != null && arg!!.second!!.hasInvoke -> {
                    args[varNameAndIndex.first] = true to arg.second
                    allTypes[arg.second!!.name]!!.usedInvoke = true
                }
                globalVar?.type != null && globalVar!!.type!!.hasInvoke -> {
                    allVars[varNameAndIndex.first]!!.used = true
                    allTypes[globalVar.type!!.name]!!.usedInvoke = true
                }
                typeTDL ?: function != null ->
                    mapOfErrors[file.path]!!.addError(varNameAndIndex.second, unmatchingArguments)
                else -> mapOfErrors[file.path]!!.addError(varNameAndIndex.second, unresolved)
            }
        }
    }

    fun analysingVars() {
        val allVarsWithFields = mutableListOf<Pair<String, Pair<String, Int>>>()
        val allVarsWithInvoke = mutableListOf<Pair<String, Int>>()
        for (variable in allVars.values) {
            val pair = variable.setType(this, mutableMapOf(), mutableMapOf())
            allVarsWithFields.addAll(pair.first)
            allVarsWithInvoke.addAll(pair.second)
        }
        for (variable in vars.values) {
            var similaryTypedVariable: VariableTDL? = variable
            val problemVars = mutableListOf<String>()
            while (similaryTypedVariable?.otherVar() != null && similaryTypedVariable!!.type == null) {
                problemVars.add(similaryTypedVariable.name)
                similaryTypedVariable = vars[similaryTypedVariable.otherVar()!!.first]
            }
            similaryTypedVariable = if (problemVars.size == 0) null
            else imports.importedVars[problemVars.last()]
            while (similaryTypedVariable?.otherVar() != null && similaryTypedVariable!!.type == null) {
                similaryTypedVariable = imports.importedVars[similaryTypedVariable.otherVar()!!.first]
            }
            val type = similaryTypedVariable?.type
            for (variableTDL in problemVars)
                vars[variableTDL]!!.type = type
        }
        analysingFieldsAndInvokes(allVarsWithFields, allVarsWithInvoke, mutableMapOf(), mutableMapOf())
    }

    fun addType(name: String, params: List<String>) {
        val map = mutableMapOf<String, Boolean>()
        for (param in params)
            map[param] = false
        types[name] = TypeTDL(name, map)
    }

    fun addVar(name: String, exp: ExpressionTDL) {
        vars[name] = VariableTDL(name, exp)
    }

    fun addFunction(name: String, argsList: List<String>, body: List<CommandBlock>) {
        val args = mutableMapOf<String, Pair<Boolean, TypeTDL?>>()
        for (argName in argsList)
            args[argName] = false to null
        functions[name to argsList.size] = FunctionTDL(name, args, body)
    }

    private fun correctTypes() {
        for (type in errorTypes)
            types.remove(type)
    }

    private fun correctFunctions() {
        for (function in errorFunctions)
            functions.remove(function)
    }

    private fun correctInvokes() {
        for (invoke in errorInvokes)
            invokes.filter { it.first != invoke }
    }

    private fun correctVars() {
        for (varName in errorVars)
            vars.remove(varName)
    }

    fun addFile(file: File) {
        importedFiles.add(file)
    }
}