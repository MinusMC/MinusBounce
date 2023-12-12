/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.ui.font

import com.google.gson.*
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.FontRenderer
import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.utils.ClientUtils
import net.minusmc.minusbounce.utils.misc.HttpUtils.download
import java.awt.Font
import java.io.*
import java.util.zip.ZipInputStream

object Fonts {
    @field:FontDetails(fontName = "Roboto Medium", fontSize = 32)
    lateinit var font32: GameFontRenderer

    @field:FontDetails(fontName = "Roboto Medium", fontSize = 35)
    lateinit var font35: GameFontRenderer

    @field:FontDetails(fontName = "Roboto Medium", fontSize = 37)
    lateinit var font37: GameFontRenderer

    @field:FontDetails(fontName = "Roboto Medium", fontSize = 40)
    lateinit var font40: GameFontRenderer

    @field:FontDetails(fontName = "Roboto Medium", fontSize = 50)
    lateinit var font50: GameFontRenderer

    @field:FontDetails(fontName = "Roboto Medium", fontSize = 72)
    lateinit var font72: GameFontRenderer

    @field:FontDetails(fontName = "Roboto Medium", fontSize = 30)
    lateinit var fontSmall: GameFontRenderer

    @field:FontDetails(fontName = "Roboto Medium", fontSize = 24)
    lateinit var fontTiny: GameFontRenderer

    @field:FontDetails(fontName = "Roboto Medium", fontSize = 52)
    lateinit var fontLarge: GameFontRenderer

    @field:FontDetails(fontName = "SFUI Regular", fontSize = 35)
    lateinit var fontSFUI35: GameFontRenderer

    @field:FontDetails(fontName = "SFUI Regular", fontSize = 40)
    lateinit var fontSFUI40: GameFontRenderer

    @field:FontDetails(fontName = "Tahoma Bold", fontSize = 35)
    lateinit var fontTahoma: GameFontRenderer

    @field:FontDetails(fontName = "Bangers", fontSize = 45)
    lateinit var fontBangers: GameFontRenderer

    @field:FontDetails(fontName = "Blanka", fontSize = 70)
    lateinit var fontBlanka: GameFontRenderer

    @FontDetails(fontName = "Tenacity35", fontSize = 35)
    lateinit var fontTenacity35: GameFontRenderer

    @FontDetails(fontName = "tenacity40", fontSize = 40)
    lateinit var fontTenacity40: GameFontRenderer

    @FontDetails(fontName = "TenacityBold35", fontSize = 35)
    lateinit var fontTenacityBold35: GameFontRenderer

    @FontDetails(fontName = "tenacityBold40", fontSize = 40)
    lateinit var fontTenacityBold40: GameFontRenderer

    @FontDetails(fontName = "TenacityIcon30", fontSize = 30)
    lateinit var fontTenacityIcon30: GameFontRenderer

    @FontDetails(fontName = "NovolineIcon", fontSize = 80)
    lateinit var novolineIcon: GameFontRenderer

    @FontDetails(fontName = "NovolineIcon2", fontSize = 80)
    lateinit var novolineIcon2: GameFontRenderer

    @field:FontDetails(fontName = "Minecraft Font")
    val minecraftFont: FontRenderer = Minecraft.getMinecraft().fontRendererObj


    lateinit var fontTahomaSmall: TTFFontRenderer


    private val CUSTOM_FONT_RENDERERS: MutableList<GameFontRenderer> = ArrayList()

    fun loadFonts() {
        val l = System.currentTimeMillis()
        ClientUtils.logger.info("Loading Fonts.")
        downloadFonts()
        font32 = GameFontRenderer(getFont("Roboto-Medium.ttf", 32))
        font35 = GameFontRenderer(getFont("Roboto-Medium.ttf", 35))
        font37 = GameFontRenderer(getFont("Roboto-Medium.ttf", 37))
        font40 = GameFontRenderer(getFont("Roboto-Medium.ttf", 40))
        font50 = GameFontRenderer(getFont("Roboto-Medium.ttf", 50))
        font72 = GameFontRenderer(getFont("Roboto-Medium.ttf", 72))
        fontSmall = GameFontRenderer(getFont("Roboto-Medium.ttf", 30))
        fontTiny = GameFontRenderer(getFont("Roboto-Medium.ttf", 24))
        fontLarge = GameFontRenderer(getFont("Roboto-Medium.ttf", 60))
        fontSFUI35 = GameFontRenderer(getFont("sfui.ttf", 35))
        fontSFUI40 = GameFontRenderer(getFont("sfui.ttf", 40))
        fontTahoma = GameFontRenderer(getFont("TahomaBold.ttf", 35))
        fontTahomaSmall = TTFFontRenderer(getFont("Tahoma.ttf", 11))
        fontBangers = GameFontRenderer(getFont("Bangers-Regular.ttf", 45))
        fontBlanka = GameFontRenderer(getFont("Blanka-Regular.otf", 70))
        fontTenacity35 = GameFontRenderer(getFont("Tenacity.ttf", 35))
        fontTenacity40 = GameFontRenderer(getFont("Tenacity.ttf", 40))
        fontTenacityBold35 = GameFontRenderer(getFont("Tenacity-Bold.ttf", 35))
        fontTenacityBold40 = GameFontRenderer(getFont("Tenacity-Bold.ttf", 40))
        fontTenacityIcon30 = GameFontRenderer(getFont("TenacityIcon.ttf", 30))
        novolineIcon = GameFontRenderer(getFont("novolineNoti.ttf", 80))
        novolineIcon2 = GameFontRenderer(getFont("novolineNoti2.ttf", 80))
        try {
            CUSTOM_FONT_RENDERERS.clear()
            val fontsFile = File(MinusBounce.fileManager.fontsDir, "fonts.json")
            if (fontsFile.exists()) {
                val jsonElement = JsonParser().parse(BufferedReader(FileReader(fontsFile)))
                if (jsonElement is JsonNull) return
                val jsonArray = jsonElement as JsonArray
                for (element in jsonArray) {
                    if (element is JsonNull) return
                    val fontObject = element as JsonObject
                    CUSTOM_FONT_RENDERERS.add(
                        GameFontRenderer(
                            getFont(
                                fontObject["fontFile"].asString, fontObject["fontSize"].asInt
                            )
                        )
                    )
                }
            } else {
                fontsFile.createNewFile()
                val printWriter = PrintWriter(FileWriter(fontsFile))
                printWriter.println(GsonBuilder().setPrettyPrinting().create().toJson(JsonArray()))
                printWriter.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        ClientUtils.logger.info("Loaded Fonts. (" + (System.currentTimeMillis() - l) + "ms)")
    }

    private fun isExistFonts(): Boolean {
        val outputFile = File(MinusBounce.fileManager.fontsDir, "fonts.zip")
        if (!outputFile.exists()) return false
        val fonts = arrayOf("sfui.ttf", "Roboto-Medium.ttf", "TahomaBold.ttf", "Tahoma.ttf", "Bangers-Regular.ttf", "Blanka-Regular.otf", "Tenacity.ttf", "Tenacity-Bold.ttf", "TenacityIcon.ttf", "novolineIcon.ttf", "novolineIcon2.ttf")
        for (font in fonts) {
            val fontFile = File(MinusBounce.fileManager.fontsDir, font)
            if (!fontFile.exists()) return false
        }
        return true
    }

    private fun downloadFonts() {
        try {
            val outputFile = File(MinusBounce.fileManager.fontsDir, "fonts.zip")
            if (!isExistFonts()) {
                ClientUtils.logger.info("Downloading fonts...")
                download(MinusBounce.CLIENT_CLOUD + "/fonts/fonts.zip", outputFile)
                ClientUtils.logger.info("Extract fonts...")
                extractZip(outputFile.path, MinusBounce.fileManager.fontsDir.path)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun getFontRenderer(name: String, size: Int): FontRenderer {
        for (field in Fonts::class.java.getDeclaredFields()) {
            try {
                field.isAccessible = true
                val o = field[null]
                if (o is FontRenderer) {
                    val fontDetails = field.getAnnotation(FontDetails::class.java) ?: continue
                    if (fontDetails.fontName == name && fontDetails.fontSize == size) return o
                }
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            }
        }
        for (liquidFontRenderer in CUSTOM_FONT_RENDERERS) {
            val font = liquidFontRenderer.defaultFont.font
            if (font.name == name && font.size == size) return liquidFontRenderer
        }
        return minecraftFont
    }

    fun getFontDetails(fontRenderer: FontRenderer): Array<Any>? {
        for (field in Fonts::class.java.getDeclaredFields()) {
            try {
                field.setAccessible(true)
                val o = field[null]
                if (o == fontRenderer) {
                    val fontDetails = field.getAnnotation(FontDetails::class.java)
                    return arrayOf(fontDetails.fontName, fontDetails.fontSize)
                }
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            } catch (e: NullPointerException) {
                return arrayOf("Minecraft Font", -1)
            }
        }
        if (fontRenderer is GameFontRenderer) {
            val font = fontRenderer.defaultFont.font
            return arrayOf(font.name, font.size)
        }
        return null
    }

    val fonts: List<FontRenderer>
        get() {
            val fonts: MutableList<FontRenderer> = ArrayList()
            for (fontField in Fonts::class.java.getDeclaredFields()) {
                try {
                    fontField.setAccessible(true)
                    val fontObj = fontField[null]
                    if (fontObj is FontRenderer) fonts.add(fontObj)
                } catch (e: IllegalAccessException) {
                    e.printStackTrace()
                }
            }
            fonts.addAll(CUSTOM_FONT_RENDERERS)
            return fonts
        }

    private fun getFont(fontName: String, size: Int): Font {
        return try {
            val inputStream: InputStream = FileInputStream(File(MinusBounce.fileManager.fontsDir, fontName))
            var awtClientFont = Font.createFont(Font.TRUETYPE_FONT, inputStream)
            awtClientFont = awtClientFont.deriveFont(Font.PLAIN, size.toFloat())
            inputStream.close()
            awtClientFont
        } catch (e: Exception) {
            e.printStackTrace()
            Font("default", Font.PLAIN, size)
        }
    }

    private fun extractZip(zipFile: String, outputFolder: String) {
        val buffer = ByteArray(1024)
        try {
            val folder = File(outputFolder)
            if (!folder.exists()) folder.mkdir()
            val zipInputStream = ZipInputStream(FileInputStream(zipFile))
            var zipEntry = zipInputStream.getNextEntry()
            while (zipEntry != null) {
                val newFile = File(outputFolder + File.separator + zipEntry.name)
                File(newFile.getParent()).mkdirs()
                val fileOutputStream = FileOutputStream(newFile)
                var i: Int
                while (zipInputStream.read(buffer).also { i = it } > 0) fileOutputStream.write(buffer, 0, i)
                fileOutputStream.close()
                zipEntry = zipInputStream.getNextEntry()
            }
            zipInputStream.closeEntry()
            zipInputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
