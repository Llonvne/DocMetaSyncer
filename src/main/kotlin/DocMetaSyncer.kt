import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.time.ZonedDateTime
import java.util.*

@Serializable
data class Config(
    val replaces: Map<String, String>
)

class DocMetaSyncer(

    // MarkDown 文件符号表
    private val mdFileSymbols: Map<String, File> = File("./doc").walk()
        .filter { it.isFile && (it.extension in setOf("md", "markdown")) }
        .filter { it.toPath().none { path -> path.equals("output") } }.associateBy { it.name }.toMap()
) {
    @OptIn(ExperimentalSerializationApi::class)
    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
        explicitNulls = true
    }

    fun resolve() {
        var config: Config? = runCatching<DocMetaSyncer, Config> {
            json.decodeFromString(File("./config.json").readText())
        }.getOrNull()

        if (config == null) {
            println("DocMetaSyncer 无法读取 config.json")
            return
        }
        val date = ZonedDateTime.now()
        val newReplacer = config.replaces.toMutableMap()
        newReplacer["{{__now__}}"] = "${date.year}年${date.monthValue}月${date.dayOfMonth}日"
        config = config.copy(replaces = newReplacer)

        for ((filename, file) in mdFileSymbols) {
            println("正在处理 $filename")

            File("./output/${filename}/").mkdirs()


            val repr = "${date.year}-${date.month}-${date.dayOfMonth}-${date.hour}:${date.minute}:${date.second}"
            val uuid = UUID.randomUUID().toString().substring(0, 10)
            val name = "./output/${filename}/${filename}-${repr}-${uuid}.md"

            val output = File(name)
            output.createNewFile()

            file.forEachLine { line ->
                for ((from, to) in config.replaces) {
                    output.appendText(line.replace(from, to)
                        .let {
                            it + "\n"
                        }
                        .also { after ->
                        if (line != after) {
                            println("$line ==> $after")
                        }
                    })
                }
            }

            println("$filename 处理完毕 => ${output.absolutePath}")
        }
    }
}
