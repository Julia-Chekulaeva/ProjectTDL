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
        val programNames = ProgramNames(fileTDL.readFileWrongVersion(), file)
        programNames.analysingFile()
        mapOfFilesNamesAndProgramNames[file.path] = programNames
        return programNames
    }

    fun readingListOfFiles(vararg list: File): List<String> {
        val res = mutableListOf("Analysing files")
        for (file in list) {
            val programNames = creatingProgramNames(file)
            listOfProgramNames.add(programNames)
            for (importedFile in programNames.importedFiles) {
                mapOfFilesNamesAndProgramNames[importedFile.path] = creatingProgramNames(importedFile)
            }
        }
        for (i in 0 until listOfProgramNames.size) {
            val filesToAdd = listOfProgramNames[i].importedFiles.map { mapOfFilesNamesAndProgramNames[it.path]!! }
            listOfProgramNames[i].addingOtherProgramNames(filesToAdd)
        }
        for (fileName in list.map { it.path }) {
            mapOfFilesNamesAndProgramNames[fileName]!!.addImportsToThis()
            mapOfFilesNamesAndProgramNames[fileName]!!.invokesOn()
            mapOfFilesNamesAndProgramNames[fileName]!!.analysingVars()
            mapOfFilesNamesAndProgramNames[fileName]!!.analysingFunsAndInvokes()
            mapOfFilesNamesAndProgramNames[fileName]!!.separatingImportsFromThis()
        }
        for (file in list) {
            val programNames = mapOfFilesNamesAndProgramNames[file.path]!!
            for (importedFile in programNames.importedFiles) {
                mapOfFilesNamesAndProgramNames[importedFile.path] =
                        mapOfFilesNamesAndProgramNames[file.path]!!.
                        usedImports(mapOfFilesNamesAndProgramNames[importedFile.path]!!)
            }
        }
        for (file in list) {
            val programNames = mapOfFilesNamesAndProgramNames[file.path]!!
            res.add("File ${file.path}")
            res.add("Imported files:")
            res.addAll(programNames.importedFiles.map { "\t${it.path}" })
            res.addAll(programNames.getAllNames())
            res.addAll(mapOfErrors[file.path]!!.getErrorsMessages())
            res.addAll(programNames.getUnused())
        }
        return res
    }
}