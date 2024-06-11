package com.imcys.bilibilias.danmaku.change

import java.text.DecimalFormat
import java.util.concurrent.TimeUnit

/**
 * CC字母在JSON格式时转换为
 */
object CCJsonToAss {

    /**
     * 构筑头部信息
     */
    fun buildHeadersInfo(
        title: String,
        playResX: String,
        playResY: String,
    ): String =
        """
        [Script Info]
        ; Script generated by BILIBILIAS Danmaku Converter
        Title: $title
        ScriptType: v4.00+
        PlayResX:$playResX
        PlayResY:$playResY
        ScaledBorderAndShadow: no
        """.trimIndent()

//    fun jsonToAss(
//        videoCCInfo: VideoCCInfo,
//        title: String,
//        playResX: String,
//        playResY: String,
//        context: Context,
//    ): String {
//        // 组装头部信息
//        val headersInfo = buildHeadersInfo(title, playResX, playResY)
//
//        val fontStyleInfo = getFontStyleInfo()
//
//        val ccInfo = buildCCInfo(videoCCInfo)
//
//        // 封装身体
//        return """$headersInfo
//                  $fontStyleInfo
//                  $ccInfo
//        """.trimIndent()
//    }

    fun getFontStyleInfo(): String = """
        [V4+ Styles]
        Format: Name, Fontname, Fontsize, PrimaryColour, SecondaryColour, OutlineColour, BackColour, Bold, Italic, Underline, StrikeOut, ScaleX, ScaleY, Spacing, Angle, BorderStyle, Outline, Shadow, Alignment, MarginL, MarginR, MarginV, Encoding
        Style: Small,Microsoft YaHei,36,&H00FFFFFF, &H00FFFFFF, &H00000000, &H00000000, 0, 0, 0, 0, 100, 100, 0.00, 0.00, 1, 1, 0, 2, 20, 20, 20, 0
        Style: Medium,Microsoft YaHei,52,&H00FFFFFF, &H00FFFFFF, &H00000000, &H00000000, 0, 0, 0, 0, 100, 100, 0.00, 0.00, 1, 1, 0, 2, 20, 20, 20, 0
        Style: Large,Microsoft YaHei,64,&H00FFFFFF, &H00FFFFFF, &H00000000, &H00000000, 0, 0, 0, 0, 100, 100, 0.00, 0.00, 1, 1, 0, 2, 20, 20, 20, 0
        Style: Larger,Microsoft YaHei,72,&H00FFFFFF, &H00FFFFFF, &H00000000, &H00000000, 0, 0, 0, 0, 100, 100, 0.00, 0.00, 1, 1, 0, 2, 20, 20, 20, 0
        Style: ExtraLarge,Microsoft YaHei,90,&H00FFFFFF, &H00FFFFFF, &H00000000, &H00000000, 0, 0, 0, 0, 100, 100, 0.00, 0.00, 1, 1, 0, 2, 20, 20, 20, 0
    """.trimIndent()

    fun getFormat(): String = """
        [Events]
        Format: Layer, Start, End, Style, Name, MarginL, MarginR, MarginV, Effect, Text
    """.trimIndent()

/**
     * 构建字幕体
     */
//    private fun buildCCInfo(videoCCInfo: VideoCCInfo): String {
//        var danmakus = getFormat()
//        videoCCInfo.body.forEach {
//            val startTime = formatSeconds(it.from)
//            // 6秒延迟
//            val endTime = formatSeconds(it.to)
//
//            danmakus += "Dialogue: 0,$startTime,$endTime,Large,,0000,0000,0000,,{\\pos(960,1050)\\c&H000000&}${it.content}\n"
//        }
//
//        return danmakus
//    }

/**
     * 时间格式化
     */
    fun formatSeconds(seconds: Double): String {
        val formatter = DecimalFormat("00.00")
        val hours = TimeUnit.SECONDS.toHours(seconds.toLong())
        val minutes = TimeUnit.SECONDS.toMinutes(seconds.toLong()) % 60
        val remainingSeconds =
            seconds - TimeUnit.HOURS.toSeconds(hours) - TimeUnit.MINUTES.toSeconds(minutes)
        return "%d:%02d:%s".format(hours, minutes, formatter.format(remainingSeconds))
    }
}
