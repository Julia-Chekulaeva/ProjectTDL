package classesTDL2

import java.io.File
import kotlin.system.exitProcess

class FilesParser {

    private val mapOfFilesNamesAndProgramNames = mutableMapOf<String, ProgramNames>()

    val listOfProgramNames = mutableListOf<ProgramNames>()

    companion object {

        fun readDir(directoryName: String): List<String> {
            val directory = File(directoryName)
            if (!directory.isDirectory) {
                System.err.println("Non-existing directory")
                exitProcess(1)
            }
            val listFiles = mutableListOf(directory)
            while (listFiles.any { it.isDirectory }) {
                val prevList = listFiles.toList()
                for (file in prevList) {
                    if (file.isDirectory) {
                        listFiles.remove(file)
                        listFiles.addAll(file.listFiles())
                    }
                }
            }
            val finalList = listFiles.filter { it.name.endsWith(".tdl") }.toTypedArray()
            val parserTDL = FilesParser()
            return listOf("Files with tdl-format found: ${finalList.size}") + parserTDL.readingListOfFiles(*finalList)
        }

        fun readingFiles(vararg fileNames: String): List<String> {
            val res = mutableListOf<File>()
            for (name in fileNames) {
                val file = File(name)
                if (!file.isFile) {
                    System.err.println("Non-existing file")
                    exitProcess(2)
                }
                if (!file.name.endsWith(".tdl")) {
                    System.err.println("Not tdl-format")
                    exitProcess(3)
                }
                res.add(file)
            }
            val parserTDL = FilesParser()
            return parserTDL.readingListOfFiles(*res.toTypedArray())
        }
    }

    private fun creatingProgramNames(file: File): ProgramNames {
        val fileTDL = FileTDL(file)
        fileTDL.createErrors()
        val programNames = ProgramNames(fileTDL.readFile(), file)
        programNames.analysingFile()
        mapOfFilesNamesAndProgramNames[file.absolutePath] = programNames
        return programNames
    }

    fun readingListOfFiles(vararg list: File): List<String> {
        val res = mutableListOf("Analysing files")
        for (file in list) {
            val programNames = creatingProgramNames(file)
            listOfProgramNames.add(programNames)
            for (importedFile in programNames.importedFiles) {
                mapOfFilesNamesAndProgramNames[importedFile.absolutePath] = creatingProgramNames(importedFile)
            }
        }
        for (i in 0 until listOfProgramNames.size) {
            val filesToAdd = listOfProgramNames[i].importedFiles.map { mapOfFilesNamesAndProgramNames[it.absolutePath]!! }
            listOfProgramNames[i].addingOtherProgramNames(filesToAdd)
        }
        for (fileName in list.map { it.absolutePath }) {
            mapOfFilesNamesAndProgramNames[fileName]!!.addImportsToThis()
            mapOfFilesNamesAndProgramNames[fileName]!!.invokesOn()
            mapOfFilesNamesAndProgramNames[fileName]!!.analysingVars()
            mapOfFilesNamesAndProgramNames[fileName]!!.analysingFunsAndInvokes()
            mapOfFilesNamesAndProgramNames[fileName]!!.separatingImportsFromThis()
        }
        for (file in list) {
            val programNames = mapOfFilesNamesAndProgramNames[file.absolutePath]!!
            for (importedFile in programNames.importedFiles) {
                mapOfFilesNamesAndProgramNames[importedFile.absolutePath] =
                        mapOfFilesNamesAndProgramNames[file.absolutePath]!!.
                        usedImports(mapOfFilesNamesAndProgramNames[importedFile.absolutePath]!!)
            }
        }
        for (file in list) {
            val programNames = mapOfFilesNamesAndProgramNames[file.absolutePath]!!
            res.add("File ${file.absolutePath}")
            res.add("Imported files:")
            res.addAll(programNames.importedFiles.map { "\t${it.absolutePath}" })
            res.addAll(programNames.getAllNames())
            res.addAll(mapOfErrors[file.absolutePath]!!.getErrorsMessages())
            res.addAll(programNames.getUnused())
        }
        return res
    }
}