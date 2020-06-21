package mainPackage

import classesTDL.FilesParser
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals

class TestsTDL {

    fun testingFile(codeStrings: List<String>, checkFile: File) {
        val fileWithCode = File("src${File.separator}test${File.separator}resources${File.separator}newFile").bufferedWriter()
        for (str in codeStrings) {
            fileWithCode.write(str)
            fileWithCode.newLine()
        }
        fileWithCode.close()
        assertEquals(
                checkFile.readLines().map { it.trimEnd().replace("\t", "    ") },
                File("src${File.separator}test${File.separator}resources${File.separator}newFile").
                readLines().map { it.trimEnd().replace("\t", "    ") }
        )
        File("src${File.separator}test${File.separator}resources${File.separator}newFile").delete()
    }

    @Test
    fun readDir() {
        testingFile(
                FilesParser.readDir("src${File.separator}test${File.separator}resources${File.separator}dir"),
                File("src${File.separator}test${File.separator}resources${File.separator}checkFile")
        )
    }

    @Test
    fun readingFiles() {
        testingFile(
                FilesParser.readingFiles(
                        "src${File.separator}test${File.separator}resources${File.separator}examples${File.separator}point.tdl",
                        "src${File.separator}test${File.separator}resources${File.separator}examples${File.separator}point2.tdl"
                ),
                File("src${File.separator}test${File.separator}resources${File.separator}checkFile2")
        )
    }

    @Test
    fun comments() {
        testingFile(
                FilesParser.readingFiles(
                        "src${File.separator}test${File.separator}resources${File.separator}examples${File.separator}pointComments.tdl"
                ),
                File("src${File.separator}test${File.separator}resources${File.separator}checkFileComments")
        )
    }

    @Test
    fun otherComments() {
        testingFile(
                FilesParser.readingFiles(
                        "src${File.separator}test${File.separator}resources${File.separator}examples${File.separator}pointComments2.tdl"
                ),
                File("src${File.separator}test${File.separator}resources${File.separator}checkFileComments2")
        )
    }
}
