package com.imcys.network.utils

import com.imcys.network.constants.BILIBILI_WEB_URL
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMessageBuilder

fun HttpRequestBuilder.parameterBV(bvid: String): Unit =
    url.parameters.append("bvid", bvid)

fun HttpMessageBuilder.headerRefBilibili(): Unit = headers.append(HttpHeaders.Referrer, BILIBILI_WEB_URL)
fun HttpRequestBuilder.parameterPlatform(platform: String = "web"): Unit =
    url.parameters.append("platform", platform)

fun HttpRequestBuilder.parameterMediaID(mediaId: Long): Unit =
    url.parameters.append("media_id", mediaId.toString())

fun HttpRequestBuilder.parameterPN(pn: Int): Unit =
    url.parameters.append("pn", pn.toString())

fun HttpRequestBuilder.parameterPS(ps: Int): Unit =
    url.parameters.append("ps", ps.toString())
fun HttpRequestBuilder.parameterPageNum(num: Int): Unit =
    url.parameters.append("page_num", num.toString())
fun HttpRequestBuilder.parameterPageSize(size: Int): Unit =
    url.parameters.append("page_size", size.toString())

fun HttpRequestBuilder.parameterMID(mid: Long): Unit =
    url.parameters.append("mid", mid.toString())

fun HttpRequestBuilder.parameterCSRF(csrf: String): Unit =
    url.parameters.append("csrf", csrf)