package classesTDL2

import java.io.File

class LexemBlock(val text: String, val blocks: List<LexemBlock>, val startIndex: Int) {

    fun analysingImports(file: File, programNames: ProgramNames): Boolean {
        val textWithoutSpaces = text.filter { it != ' ' }
        if (text.matches(""" *import +file +[\w.\d]+ *""".toRegex())) {
            val name = textWithoutSpaces.removePrefix("importfile").replace(".", File.separator)
            var fileName = "$name.tdl"
            var directory = file.parentFile
            if (!File(fileName).exists()) {
                while (directory != null) {
                    if (directory.listFiles().any {
                                it.absolutePath.removePrefix(directory.absolutePath) == "${File.separator}$fileName"
                            }) {
                        fileName = directory.absolutePath + "${File.separator}$fileName"
                        break
                    }
                    directory = directory.parentFile
                }
            }
            val fileToImport = File(fileName)
            if (fileToImport.exists()) {
                programNames.addFile(fileToImport)
                return true
            }
            mapOfErrors[file.absolutePath]!!.addError(text.indexOf(fileName,
                    text.indexOf(" file ") + 5) + startIndex, unresolved)
            return true
        }
        return false
    }

    fun analyseTopLevelLexem(file: File, programNames: ProgramNames) {
        val textWithoutSpaces = text.filter { it != ' ' }
        if (text.matches(""" *type +[\w\d]+ *\(( *[\w\d]+( *, *[\w\d]+)*)? *\) *""".toRegex())) {
            val split = textWithoutSpaces.removePrefix("type").removeSuffix(")").split("(")
            val name = split[0]
            if (name[0] in '0'..'9') {
                mapOfErrors[file.absolutePath]!!.addError(startIndex + text.indexOf(name), nameShouldStartWithLetter)
                return
            }
            var beginningOfArgs = text.indexOf("(")
            if (split[1].isNotEmpty()) {
                val params = split[1].split(",")
                for ((i, param) in params.withIndex()) {
                    if (param[0] in '0'..'9') {
                        mapOfErrors[file.absolutePath]!!.addError(
                                startIndex + text.indexOf(param, beginningOfArgs), nameShouldStartWithLetter
                        )
                        return
                    }
                    if (params.subList(0, i).contains(param)) {
                        mapOfErrors[file.absolutePath]!!.addError(
                                startIndex + text.indexOf(param, beginningOfArgs), ambiguity
                        )
                        return
                    }
                    beginningOfArgs += param.length + 1
                }
                if (programNames.types[name] != null) {
                    mapOfErrors[file.absolutePath]!!.addError(startIndex + text.indexOf(name), ambiguity)
                    programNames.errorTypes.add(name)
                    return
                }
                programNames.addType(name, params)
                return
            }
            mapOfErrors[file.absolutePath]!!.addError(startIndex + text.indexOf(name), emptyType)
            return
        }
        if (text.matches(""" *function +[\w\d]+ *\(( *[\w\d]+( *, *[\w\d]+)*)? *\) *(\{} *)?""".toRegex())) {
            val split = textWithoutSpaces.removePrefix("function").removeSuffix("{}").removeSuffix(")").split("(")
            val name = split[0]
            val params = if (split.size == 2 && split[1].trim().isNotEmpty())
                split[1].split(",")
            else listOf()
            var beginningOfArgs = text.indexOf("(")
            if (name[0] in '0'..'9') {
                mapOfErrors[file.absolutePath]!!.addError(startIndex + text.indexOf(name), nameShouldStartWithLetter)
                return
            }
            for ((i, param) in params.withIndex()) {
                if (param[0] in '0'..'9') {
                    mapOfErrors[file.absolutePath]!!.addError(startIndex + text.indexOf(param, beginningOfArgs), nameShouldStartWithLetter)
                    return
                }
                if (params.subList(0, i).contains(param)) {
                    mapOfErrors[file.absolutePath]!!.addError(startIndex + text.indexOf(param, beginningOfArgs), ambiguity)
                    return
                }
                beginningOfArgs += param.length + 1
            }
            val function = programNames.functions[name to params.size]
            if (function != null) {
                mapOfErrors[file.absolutePath]!!.addError(startIndex + text.indexOf(name), ambiguity)
                programNames.errorFunctions.add(name to params.size)
                return
            }
            programNames.addFunction(name, params, blocks)
            return
        }
        if (text.matches(""" *invoke +on +[\w\d]+ *(\{} *)?""".toRegex())) {
            if (!text.contains("{}")) {
                mapOfErrors[file.absolutePath]!!.addError(
                        startIndex + text.indexOf(text.trim()), invokeMustHaveABody
                )
                return
            }
            val type = textWithoutSpaces.removeSuffix("{}").removePrefix("invokeon")
            val hasOtherInvoke = programNames.invokes.map { it.first }.contains(type)
            if (hasOtherInvoke) {
                mapOfErrors[file.absolutePath]!!.addError(
                        startIndex + text.indexOf(type, text.indexOf(" on ")), ambiguity
                )
                programNames.errorInvokes.add(type)
                return
            }
            programNames.invokes.add(Triple(type, blocks, text.indexOf(type, text.indexOf(" on ")) + startIndex))
            return
        }
        if (text.matches(""" *[\w\d]* *=.*""".toRegex())) {
            val split = text.split("=")
            val name = split[0].trim()
            if (name[0] in '0'..'9') {
                mapOfErrors[file.absolutePath]!!.addError(startIndex + text.indexOf(name), nameShouldStartWithLetter)
                return
            }
            if (programNames.vars[name] != null) {
                mapOfErrors[file.absolutePath]!!.addError(startIndex + text.indexOf(name), ambiguity)
                programNames.errorVars.add(name)
                return
            }
            val expr = split[1]
            programNames.addVar(name, ExpressionTDL(expr, file, text.indexOf(expr, text.indexOf("=")) + startIndex))
            return
        }
        mapOfErrors[file.absolutePath]!!.addError(startIndex + text.indexOf(text.trim()), unrecognisedStringBlock)
    }

    fun analyseLowLevel(
            args: MutableMap<String, Pair<Boolean, TypeTDL?>>, file: File,
            programNames: ProgramNames, localVars: MutableMap<String, VariableTDL>,
            varDeclaration: Boolean, errorVars: MutableList<String>
    ) {
        if (text.matches(""" *if *\(.*\) *\{} *(else *\{} *)?""".toRegex())) {
            val exprString = text.trim().removePrefix("if").removeSuffix("{}").trim().
                    removePrefix("(").removeSuffix("else").trimEnd().removeSuffix("{}").trimEnd().removeSuffix(")")
            val expr = ExpressionTDL(exprString, file, text.indexOf("(") + startIndex + 1)
            expr.parsingExpr()
            val pair = expr.analysingExpr(programNames, localVars, args)
            programNames.analysingFieldsAndInvokes(pair.first, pair.second, localVars, args)
            for (lexemBlock in blocks.filter { it.text.trim() != "" })
                lexemBlock.analyseLowLevel(args, file, programNames, localVars, false, errorVars)
            return
        }
        if (text.matches(""" *\w[\w\d]* +as +\w[\w\d]* *""".toRegex())) {
            val split = text.split("as")
            val param = split[0].trim()
            val type = split[1].trim()
            if (args[param] == null) {
                if (localVars[param] == null && programNames.vars[param] == null)
                    mapOfErrors[file.absolutePath]!!.addError(startIndex + text.indexOf(param), unresolved)
                else
                    mapOfErrors[file.absolutePath]!!.addError(startIndex + text.indexOf(param),
                            variableTypeCannotBeReassigned)
                return
            }
            if (programNames.allTypes[type] == null) {
                mapOfErrors[file.absolutePath]!!.addError(
                        startIndex + text.indexOf(type, text.indexOf(" as ") + 4), unresolved
                )
                return
            }
            args[param] = args[param]!!.first to programNames.allTypes[type]
            programNames.allTypes[type]!!.used = true
            return
        }
        if (text.matches(""" *[\w\d]* *=.*""".toRegex())) {
            val split = text.split("=")
            val name = split[0].trim()
            if (name[0] in '0'..'9') {
                mapOfErrors[file.absolutePath]!!.addError(
                        startIndex + text.indexOf(name), nameShouldStartWithLetter
                )
                return
            }
            if (!varDeclaration) {
                mapOfErrors[file.absolutePath]!!.addError(
                        startIndex + text.indexOf(name), variableDeclarationNotAllowedHere
                )
                return
            }
            if (localVars[name] != null) {
                mapOfErrors[file.absolutePath]!!.addError(startIndex + text.indexOf(name), ambiguity)
                errorVars.add(name)
                return
            }
            val expr = split[1]
            localVars[name] = VariableTDL(name, ExpressionTDL(expr, file, text.indexOf(expr, text.indexOf("=")) + startIndex))
            val pair = localVars[name]!!.setType(programNames, localVars, args)
            programNames.analysingFieldsAndInvokes(pair.first, pair.second, localVars, args)
            return
        }
        if (text.matches(""" *return .*""".toRegex())) {
            val exprString = text.trim().removePrefix("return ")
            val expr = ExpressionTDL(exprString, file, startIndex + text.indexOf(exprString, text.indexOf("return " + 6)))
            expr.parsingExpr()
            val pair = expr.analysingExpr(programNames, localVars, args)
            programNames.analysingFieldsAndInvokes(pair.first, pair.second, localVars, args)
            return
        }
        //if (text.matches(""" *\w[\w\d]* *\(.*\) *""".toRegex())) {
        val expr = ExpressionTDL(text, file, startIndex)
        expr.parsingExpr()
        val pair = expr.analysingExpr(programNames, localVars, args)
        programNames.analysingFieldsAndInvokes(pair.first, pair.second, localVars, args)
        return
        //mapOfErrors[file.absolutePath]!!.addError(startIndex + text.indexOf(text.trim()), unrecognisedStringBlock)
    }
}