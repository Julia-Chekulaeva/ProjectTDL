package testsTDL

import classesTDL2.FilesParser
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals

class TestsTDL {

    @Test
    fun readingDir() {
        val checkFile = File("src${File.separator}test${File.separator}resources${File.separator}newFile").bufferedWriter()
        val list = FilesParser.readDir("src${File.separator}test${File.separator}resources")
        for (str in list) {
            checkFile.write(str)
            checkFile.newLine()
        }
        checkFile.close()
        assertEquals(
                File("src${File.separator}test${File.separator}resources${File.separator}checkFile").
                readLines().map { it.trimEnd().replace("\t", "    ") },
                File("src${File.separator}test${File.separator}resources${File.separator}newFile").
                readLines().map { it.trimEnd().replace("\t", "    ") }
        )
        File("src${File.separator}test${File.separator}resources${File.separator}newFile").delete()
    }

    @Test
    fun readFiles() {
        val checkFile = File("src${File.separator}test${File.separator}resources${File.separator}newFile").bufferedWriter()
        val list = FilesParser.readingFiles(
                "src${File.separator}test${File.separator}resources${File.separator}examples${File.separator}point.tdl",
                "src${File.separator}test${File.separator}resources${File.separator}examples${File.separator}point2.tdl"
        )
        for (str in list) {
            checkFile.write(str)
            checkFile.newLine()
        }
        checkFile.close()
        assertEquals(
                File("src${File.separator}test${File.separator}resources${File.separator}checkFile2").
                readLines().map { it.trimEnd().replace("\t", "    ") },
                File("src${File.separator}test${File.separator}resources${File.separator}newFile").
                readLines().map { it.trimEnd().replace("\t", "    ") }
        )
        File("src${File.separator}test${File.separator}resources${File.separator}newFile").delete()
    }
}
