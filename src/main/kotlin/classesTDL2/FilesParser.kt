package classesTDL2

import java.io.File

class FilesParser {

    private val mapOfFilesAndProgramNames = mutableMapOf<File, ProgramNames>()

    val listOfProgramNames = mutableListOf<ProgramNames>()

    private fun creatingProgramNames(file: File): ProgramNames {
        val fileTDL = FileTDL(file)
        fileTDL.createErrors()
        val programNames = ProgramNames(fileTDL.readFile(), file)
        programNames.analysingFile()
        mapOfFilesAndProgramNames[file] = programNames
        return programNames
    }

    fun readingFiles(vararg list: File): List<String> {
        val res = mutableListOf("Analysing files")
        for (file in list) {
            val programNames = creatingProgramNames(file)
            listOfProgramNames.add(programNames)
            for (importedFile in programNames.importedFiles) {
                mapOfFilesAndProgramNames[importedFile] = creatingProgramNames(importedFile)
            }
        }
        for (i in 0 until listOfProgramNames.size) {
            val filesToAdd = listOfProgramNames[i].importedFiles.map { mapOfFilesAndProgramNames[it]!! }
            listOfProgramNames[i].addingOtherProgramNames(filesToAdd)
        }
        for (file in list) {
            mapOfFilesAndProgramNames[file]!!.addImportsToThis()
            mapOfFilesAndProgramNames[file]!!.invokesOn()
            mapOfFilesAndProgramNames[file]!!.analysingVars()
            mapOfFilesAndProgramNames[file]!!.analysingFunsAndInvokes()
            mapOfFilesAndProgramNames[file]!!.separatingImportsFromThis()
            res.add("File ${file.absolutePath}")
            res.add("Imported files:")
            res.addAll(mapOfFilesAndProgramNames[file]!!.importedFiles.map { "\t${it.absolutePath}" })
            res.addAll(mapOfErrors[file]!!.getErrorsMessages())
            res.addAll(mapOfFilesAndProgramNames[file]!!.getUnused())
        }
        return res
    }
}