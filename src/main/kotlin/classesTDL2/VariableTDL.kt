package classesTDL2

class VariableTDL(val name: String, val exp: ExpressionTDL) {

    fun otherVar() = exp.variable

    var type: TypeTDL? = null

    var used = false

    fun setType(
            programNames: ProgramNames, variables: MutableMap<String, VariableTDL>,
            params: MutableMap<String, Pair<Boolean, TypeTDL?>>
    ): Pair<List<Pair<String, Pair<String, Int>>>, List<Pair<String, Int>>> {
        exp.parsingExpr()
        val res = exp.analysingExpr(programNames, variables, params)
        type = exp.type
        return res
    }
}
