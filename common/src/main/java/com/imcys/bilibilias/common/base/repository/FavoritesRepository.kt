package com.imcys.bilibilias.common.base.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.imcys.bilibilias.common.base.api.BilibiliApi
import com.imcys.bilibilias.common.base.model.Collections
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import javax.inject.Inject

/**
 * ![收藏夹内容](https://github.com/SocialSisterYi/bilibili-API-collect/blob/master/docs/fav/list.md#%E6%94%B6%E8%97%8F%E5%A4%B9%E5%86%85%E5%AE%B9)
 */
class FavoritesRepository @Inject constructor(private val httpClient: HttpClient) : PagingSource<Int, Int>() {

    /**
     * **url参数：**
     *
     * | 参数名    | 类型  | 内容                     | 必要性   | 备注                                                         |
     * | -------- | ---- | ------------------------ | ------ | ------------------------------------------------------------ |
     * | media_id | num  | 目标收藏夹mlid（完整id） | 必要   |                                                              |
     * | tid      | num  | 分区tid                  | 非必要 | 默认为全部分区<br />0：全部分区<br />[详见说明](../video/video_zone.md) |
     * | keyword  | str  | 搜索关键字               | 非必要 |                                                              |
     * | order    | str  | 排序方式                 | 非必要 | 按收藏时间:mtime<br />按播放量: view<br />按投稿时间：pubtime |
     * | type     | num  | 查询范围           | 非必要 | 0：当前收藏夹（对应media_id）<br /> 1：全部收藏夹                                                       |
     * | ps       | num  | 每页数量                 | 必要   |   定义域：1-20                                                           |
     * | pn       | num  | 页码                     | 非必要 | 默认为1                                                      |
     * | platform | str  | 平台标识                 | 非必要 | 可为web（影响内容列表类型）                                  |
     */
    suspend fun getDetailedListFavoritesContent(mediaId: Long, page: Int, limit: Int = 20, platform: String = "web") =
        httpClient.get(BilibiliApi.getFavoritesContentList) {
            parameter("media_id", mediaId)
            parameter("ps", page)
            parameter("ps", limit.coerceAtMost(20))
            parameter("platform", platform)
        }.body<Collections>()

    suspend fun getAllFavoritesContents(mediaId: Long, platform: String = "web") =
        httpClient.get(BilibiliApi.allFavoritesContents) {
            parameter("media_id", mediaId)
            parameter("platform", platform)
        }

    suspend fun getUserAllFavorites(mid: Long) = httpClient.get(BilibiliApi.userAllFavorites) {
        parameter("up_mid", mid)
    }

    override fun getRefreshKey(state: PagingState<Int, Int>): Int? {
        TODO()
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Int> {
        TODO()
    }
}
