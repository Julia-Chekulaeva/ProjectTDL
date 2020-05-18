package classesTDL2

import testsTDL.countOfBrackets
import java.io.File

data class ExpressionTDL(val str: String, val file: File, val index: Int) {

    constructor(textWithBracketBlocks: TextWithBracketBlocks, file: File) :
            this(textWithBracketBlocks.wholeText, file, textWithBracketBlocks.startIndex)

    var typeName: String? = null

    var type: TypeTDL? = null

    var field: String? = null

    private val expressionsTDL = mutableListOf<ExpressionTDL>()

    var variable: Pair<String, Int>? = null

    var nameWithBrackets: Pair<Pair<String, Int>, Int>? = null

    var exprBlocks: TextWithBracketBlocks? = null

    fun parsingExpr() = creatingSplitExpressions()

    fun analysingExpr(
            programNames: ProgramNames, variables: MutableMap<String, VariableTDL>,
            params: MutableMap<String, Pair<Boolean, TypeTDL?>>
    ): Pair<List<Pair<String, Pair<String, Int>>>, List<Pair<String, Int>>> {
        val varsWithFields = mutableListOf<Pair<String, Pair<String, Int>>>()
        val varsWithInvoke = mutableListOf<Pair<String, Int>>()
        if (expressionsTDL.size != 0) {
            expressionsTDL.forEach {
                val previous = it.analysingExpr(programNames, variables, params)
                varsWithFields.addAll(previous.first)
                varsWithInvoke.addAll(previous.second)
            }
        }
        when {
            variable != null -> {
                val varName = variable!!.first
                if (variables[varName] != null) {
                    variables[varName]!!.used = true
                    type = variables[varName]!!.type
                } else if (params.contains(varName)) {
                    type = params[varName]!!.second
                    params[varName] = true to type
                } else if (programNames.allVars[varName] != null) {
                    programNames.allVars[varName]!!.used = true
                    type = programNames.allVars[varName]!!.type
                } else {
                    mapOfErrors[file.path]!!.addError(variable!!.second, unresolved)
                    variable = null
                }
            }
            nameWithBrackets != null -> {
                val nameAndCountOfArgs = nameWithBrackets!!.first
                val typeTDL = programNames.allTypes[nameAndCountOfArgs.first]
                when {
                    programNames.allFunctions[nameAndCountOfArgs] != null ->
                        programNames.allFunctions[nameAndCountOfArgs]!!.used = true
                    nameAndCountOfArgs.second == 0 ->
                        varsWithInvoke.add(nameAndCountOfArgs.first to nameWithBrackets!!.second)
                    typeTDL != null -> {
                        if (typeTDL.fields.size != nameAndCountOfArgs.second) {
                            mapOfErrors[file.path]!!.addError(nameWithBrackets!!.second, unmatchingArguments)
                        } else {
                            programNames.allTypes[nameAndCountOfArgs.first]!!.used = true
                            type = typeTDL
                        }
                    }
                    programNames.allFunctions.map { it.key.first to it.value }.
                    toMap()[nameAndCountOfArgs.first] != null ->
                        mapOfErrors[file.path]!!.addError(nameWithBrackets!!.second, unmatchingArguments)
                    else -> mapOfErrors[file.path]!!.addError(nameWithBrackets!!.second, unresolved)
                }
            }
            typeName != null -> {
                val typeName = typeName!!
                if (programNames.allTypes[typeName] != null) {
                    programNames.allTypes[typeName]!!.used = true
                    type = programNames.allTypes[typeName]!!
                } else
                    mapOfErrors[file.path]!!.addError(
                            index + str.indexOf(typeName, str.indexOf(" as ") + 3), unresolved
                    )
            }
        }
        if (type == null && expressionsTDL.size == 1 && field == null)
            type = expressionsTDL[0].type
        if (field != null) {
            if (expressionsTDL[0].variable != null)
                varsWithFields.add(expressionsTDL[0].variable!!.first to (field!! to index + str.indexOf(field!!, str.indexOf("."))))
            else if (type != null && type!!.fields.contains(field))
                programNames.allTypes[type!!.name]!!.fields[field!!] = true
            else
                mapOfErrors[file.path]!!.addError(index + str.indexOf(field!!, str.indexOf(".")), unresolved)
        }
        return varsWithFields to varsWithInvoke
    }

    private fun creatingExprBlocks() {
        val mapOfBrackets = countOfBrackets(str, file, '(' to ')', index)
        exprBlocks = TextWithBracketBlocks.createBlock(mapOfBrackets.second, mapOfBrackets.first, index, '(' to ')')
    }

    private fun creatingSplitExpressions() {
        creatingExprBlocks()
        val expressions = exprBlocks!!.split('(' to ')','-', '+', '>', '<', '*', '/')
        if (expressions.size == 1) {
            splitBySpecialSigns()
            return
        }
        for (i in expressions.indices)
            expressionsTDL.add(ExpressionTDL(expressions[i], file))
        expressionsTDL.withIndex().forEach { it ->
            it.value.exprBlocks = expressions[it.index]
            it.value.splitBySpecialSigns()
        }
    }

    private fun splitBySpecialSigns() {
        if (exprBlocks!!.mainText.matches(""" *[\w\d() ]+\. *\w[\w\d]* *""".toRegex())) {
            val split = exprBlocks!!.split('(' to ')', '.')
            val expr = ExpressionTDL(split[0], file)
            expr.exprBlocks = exprBlocks!!.subtext(split[0].mainText, index)
            expr.analysingSingleExpression()
            expressionsTDL.add(expr)
            field = split[1].mainText.trim()
            return
        }
        if (exprBlocks!!.mainText.matches(""" *[\w\d() ]+ as +\w[\w\d]*""".toRegex())) {
            val split = str.split(" as ")
            val expr = ExpressionTDL(split[0], file, index)
            expr.creatingExprBlocks()
            expr.analysingSingleExpression()
            expressionsTDL.add(expr)
            typeName = split[1].trim()
            return
        }
        analysingSingleExpression()
    }

    private fun analysingSingleExpression() {
        val text = exprBlocks!!.mainText
        if (text.matches(""" *(\d+|"$CHAR_FOR_STRINGS*"?) *""".toRegex())) return
        if (text.matches(""" *\(\) *""".toRegex())) {
            val expr = ExpressionTDL(exprBlocks!!.blocks[0], file)
            expr.creatingSplitExpressions()
            expressionsTDL.add(expr)
            return
        }
        if (text.matches(""" *\w[\w\d]* *""".toRegex())) {
            val name = text.trim()
            variable = name to text.indexOf(name) + index
            return
        }
        if (text.matches(""" *\w[\w\d]* *\(\) *""".toRegex())) {
            val name = text.filter { it != ' ' }.removeSuffix("()")
            val hasArgs = exprBlocks!!.blocks.isNotEmpty() && exprBlocks!!.blocks[0].mainText.trim() != ""
            val block = exprBlocks!!.blocks[0]
            val argsBlocks = if (!hasArgs)
                listOf() else
                block.split('(' to ')',',')
            if (hasArgs) {
                val listOfExpr = argsBlocks.map { ExpressionTDL(it, file) }
                listOfExpr.forEach { it.creatingSplitExpressions() }
                expressionsTDL.addAll(listOfExpr)
            }
            nameWithBrackets = name to argsBlocks.size to text.indexOf(name) + index
            return
        }
        mapOfErrors[file.path]!!.addError(index + str.indexOf(str.trim()), unrecognisedExpression)
    }

    override fun equals(other: Any?): Boolean {
        return super.equals(other)
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }

}