import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import java.net.URL

class App : CliktCommand() {

    val file: String by option(help = "The url of the file to download").prompt("enter a file top download")

    override fun run() {
        val util = FileUtil()
        util.downloadFile(file, URL(file).file)

        println("downloaded to ${URL(file).file}")
    }
}

fun main(args: Array<String>) = App().main(args)