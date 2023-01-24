package com.imcys.bilibilias.home.ui.activity.video

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.baidu.mobstat.StatService
import com.imcys.bilibilias.MainActivity
import com.imcys.bilibilias.R
import com.imcys.bilibilias.base.BaseActivity
import com.imcys.bilibilias.base.app.App
import com.imcys.bilibilias.base.utils.DialogUtils
import com.imcys.bilibilias.common.base.AbsActivity
import com.imcys.bilibilias.common.base.api.BilibiliApi
import com.imcys.bilibilias.common.base.app.BaseApplication
import com.imcys.bilibilias.common.base.utils.VideoNumConversion
import com.imcys.bilibilias.common.base.utils.http.HttpUtils
import com.imcys.bilibilias.databinding.ActivityDkVideoBinding
import com.imcys.bilibilias.home.ui.activity.video.controller.DKVideoController
import com.imcys.bilibilias.home.ui.activity.video.model.DkVideoViewModel
import com.imcys.bilibilias.home.ui.adapter.BangumiSubsectionAdapter
import com.imcys.bilibilias.home.ui.adapter.SubsectionAdapter
import com.imcys.bilibilias.home.ui.model.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import master.flame.danmaku.danmaku.model.android.DanmakuContext
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser
import master.flame.danmaku.ui.widget.DanmakuView
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import okio.BufferedSink
import okio.buffer
import okio.sink
import xyz.doikki.videoplayer.player.VideoView
import java.io.*
import java.util.zip.Inflater


class PlayVideoActivity : AbsActivity() {

    //视频基本数据类，方便全局调用
    private lateinit var videoDataBean: VideoBaseBean

    lateinit var binding: ActivityDkVideoBinding

    //DK控制器
    private lateinit var videoController: DKVideoController

    lateinit var userBaseBean: UserBaseBean

    //视频临时数据，方便及时调用，此方案考虑废弃
    var bvid: String = ""
    var avid: Int = 0
    var cid: Int = 0
    var epid: Long = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_dk_video)
        loadUserData()
        //加载视频首要信息
        initVideoData()
        //加载控件
        initView()
    }

    private fun loadUserData() {
        lifecycleScope.launch {
            userBaseBean = withContext(lifecycleScope.coroutineContext) { getUserData() }
        }
    }


    private fun initView() {
        binding.apply {
            //设置点击事件->这里将点击事件都放这个类了
            dkVideoViewModel = DkVideoViewModel(this@PlayVideoActivity, this)
        }
    }

    /**
     * 加载视频播放信息
     */
    private fun loadVideoPlay(type: String) {
        //这里默认用目前flv最高免费画质1080P，注意：flv已经被B站弃用，目前只能使用360P和1080P，后面得考虑想办法做音频分离。
        when (type) {
            "video" -> {
                HttpUtils.addHeader("cookie", BaseApplication.cookies)
                    .addHeader("referer", "https://www.bilibili.com")
                    .get("${BilibiliApi.videoPlayPath}?bvid=$bvid&cid=$cid&qn=80&fnval=0&fourk=1",
                        VideoPlayBean::class.java) {
                        //设置布局视频播放数据
                        binding.videoPlayBean = it
                        //真正调用饺子播放器设置视频数据
                        setDkPlayerConfig(it.data.durl[0].url, "")
                        binding.asVideoCd.visibility = View.VISIBLE
                        binding.asVideoBangumiCd.visibility = View.GONE
                    }
            }
            "bangumi" -> {
                HttpUtils.addHeader("cookie", BaseApplication.cookies)
                    .addHeader("referer", "https://www.bilibili.com")
                    .get("${BaseApplication.roamApi}pgc/player/web/playurl?ep_id=$epid&qn=80&fnval=0&fourk=1",
                        BangumiPlayBean::class.java) {

                        //设置布局视频播放数据
                        binding.bangumiPlayBean = it
                        //真正调用饺子播放器设置视频数据
                        setDkPlayerConfig(it.result.durl[0].url, "")
                    }
            }
            else -> "${BilibiliApi.videoPlayPath}?bvid=$bvid&cid=$cid&qn=64&fnval=0&fourk=1"
        }


    }


    /**
     * 加载视频数据
     */
    private fun initVideoData() {

        //这里必须通过外界获取数据
        val intent = intent

        val bvId = intent.getStringExtra("bvId")

        //初始化视频控制器
        videoController = DKVideoController(this)

        //这里才是真正的视频基本数据获取
        HttpUtils.addHeader("cookie", BaseApplication.cookies)
            .get(BilibiliApi.getVideoDataPath + "?bvid=$bvId", VideoBaseBean::class.java) {
                //设置数据
                videoDataBean = it
                //这里需要显示视频数据
                showVideoData()
                //TODO 设置基本数据，注意这里必须优先，因为我们在后面会复用这些数据
                setBaseData(it)
                //加载用户卡片
                loadUserCardData(it.data.owner.mid)
                //加载弹幕信息
                loadDanmakuFlameMaster()
                //加载视频列表信息，这里判断下是不是番剧

                it.data.redirect_url?.apply {

                    //通过正则表达式检查该视频是不是番剧
                    val epRegex = Regex("""(?<=ep)([0-9]*)""")
                    if (epRegex.containsMatchIn(this)) {
                        //加载番剧视频列表
                        epid = epRegex.find(this)?.value?.toLong()!!
                        loadBangumiVideoList()
                    }

                } ?: apply {
                    //加载视频播放信息
                    loadVideoPlay("video")
                    loadVideoList() //加载正常列表
                }
                //检查三连情况
                archiveHasLikeTriple()

            }
    }

    /**
     * 设置播放器参数配置
     * @param url String
     * @param title String
     */
    private fun setDkPlayerConfig(url: String, title: String) {
        //map["760P"] = url
        val headers: MutableMap<String, String> = mutableMapOf()
        headers["Cookie"] = BaseApplication.cookies
        headers["Referer"] = "https://www.bilibili.com/video/$bvid"
        //headers["Referer"] = "https://api.bilibili.com/x/player/playurl?bvid=$bvid&cid=${videoDataBean.data.cid}&qn=80"
        headers["User-Agent"] =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:67.0) Gecko/20100101 Firefox/67.0"


        //设置视频地址
        binding.playVideoView.setUrl(url,headers)
        //设置控制器
        binding.playVideoView.setVideoController(videoController)
        //设置弹幕路径
        val barragePath = getExternalFilesDir("temp").toString() + "/tempDm.xml";
        videoController.loadDanmaku(barragePath)
        videoController.setVideoName(videoDataBean.data.title)
        //开始播放
        binding.playVideoView.start()

    }

    /**
     * 检查三连情况
     */
    private fun archiveHasLikeTriple() {
        archiveHasLike()
        archiveCoins()
        archiveFavoured()
    }

    /**
     * 收藏检验
     */
    private fun archiveFavoured() {
        HttpUtils.get("${BilibiliApi.archiveFavoured}?aid=${bvid}",
            ArchiveFavouredBean::class.java) {
            binding.archiveFavouredBean = it
        }
    }

    /**
     * 检验投币情况
     */
    private fun archiveCoins() {
        HttpUtils.get("${BilibiliApi.archiveHasLikePath}?bvid=${bvid}",
            ArchiveHasLikeBean::class.java) {
            binding.archiveHasLikeBean = it
        }
    }

    /**
     * 检验是否点赞
     */
    private fun archiveHasLike() {
        HttpUtils.get("${BilibiliApi.archiveCoinsPath}?bvid=${bvid}",
            ArchiveCoinsBean::class.java) {
            binding.archiveCoinsBean = it
        }
    }

    /**
     * 加载番剧视频列表信息
     *
     */
    private fun loadBangumiVideoList() {
        HttpUtils.apply {
            if (BaseApplication.sharedPreferences.getBoolean("use_roam_cookie_state",
                    true)
            ) this.addHeader("cookie", BaseApplication.cookies)
        }.get(BaseApplication.roamApi + "pgc/view/web/season?ep_id=" + epid,
            BangumiSeasonBean::class.java) {

            isMember(it)

            binding.apply {

                if (it.result.episodes.size == 1) asVideoSubsectionRv.visibility = View.GONE

                //到这里就毋庸置疑的说，是番剧，要单独加载番剧缓存。
                binding.asVideoBangumiCd.visibility = View.VISIBLE
                binding.asVideoCd.visibility = View.GONE

                binding.bangumiSeasonBean = it
                asVideoSubsectionRv.adapter =
                    BangumiSubsectionAdapter(it.result.episodes.toMutableList(),
                        cid) { data, position ->
                        updateBangumiInformation(data, position)
                    }

                asVideoSubsectionRv.layoutManager =
                    LinearLayoutManager(this@PlayVideoActivity, LinearLayoutManager.HORIZONTAL, false)

            }
        }

    }


    /**
     * 更新番剧信息
     * @param data EpisodesBean
     * @param position Int
     */
    private fun updateBangumiInformation(
        data: BangumiSeasonBean.ResultBean.EpisodesBean,
        position: Int,
    ) {
        val userVipState = userBaseBean.data.vip.status
        if (data.badge == "会员" && userVipState != 1) {
            DialogUtils.dialog(
                this,
                "越界啦",
                "没大会员就要止步于此了哦，切换到不需要大会员的子集或者视频吧。",
                "我知道啦",
                positiveButtonClickListener = {
                }
            ).show()
        } else {
            //更新CID刷新播放页面
            cid = data.cid
            epid = data.id.toLong()
            //asJzvdStd.updatePoster(data.cover)

            //清空弹幕
            getDanmakuView()?.release()
            //刷新播放器
            loadVideoPlay("bangumi")
            //更新弹幕
            loadDanmakuFlameMaster()
        }

    }

    private fun isMember(bangumiSeasonBean: BangumiSeasonBean) {
        var memberType = false

        val userVipState = userBaseBean.data.vip.status
        bangumiSeasonBean.result.episodes.forEach {
            if (it.cid == cid && it.badge == "会员" && userVipState != 1) memberType = true
        }
        if (memberType) {
            DialogUtils.dialog(
                this,
                "越界啦",
                "没大会员就要止步于此了哦，切换到不需要大会员的子集或者视频吧。",
                "我知道啦",
                positiveButtonClickListener = {
                }
            ).show()
        } else {
            loadVideoPlay("bangumi")
        }
    }

    /**
     * 获取用户基础信息
     * @return UserBaseBean
     */
    private suspend fun getUserData(): UserBaseBean {
        return HttpUtils.addHeader("cookie", BaseApplication.cookies)
            .asyncGet("${BilibiliApi.userBaseDataPath}?mid=${BaseApplication.mid}",
                UserBaseBean::class.java)
    }

    /**
     * 加载视频列表信息
     *
     */
    private fun loadVideoList() {
        HttpUtils.get(BilibiliApi.videoPageListPath + "?bvid=" + bvid,
            VideoPageListData::class.java) {
            binding.apply {

                if (it.data.size == 1) asVideoSubsectionRv.visibility = View.GONE

                binding.videoPageListData = it
                asVideoSubsectionRv.adapter =
                    SubsectionAdapter(it.data.toMutableList()) { data, position ->
                        //更新CID刷新播放页面
                        cid = data.cid
                        //刷新播放器
                        loadVideoPlay("video")
                        //清空弹幕
                        getDanmakuView()?.release()
                        //更新弹幕
                        loadDanmakuFlameMaster()
                    }

                asVideoSubsectionRv.layoutManager =
                    LinearLayoutManager(this@PlayVideoActivity, LinearLayoutManager.HORIZONTAL, false)

            }
        }

    }

    /**
     * 写入基本变量数据
     * @param videoBaseBean VideoBaseBean
     */
    private fun setBaseData(videoBaseBean: VideoBaseBean) {
        bvid = videoBaseBean.data.bvid
        avid = videoBaseBean.data.aid
        cid = videoBaseBean.data.cid
        binding.videoBaseBean = videoBaseBean
    }

    /**
     * 加载弹幕信息
     */
    private fun loadDanmakuFlameMaster() {

        HttpUtils.addHeader("cookie", BaseApplication.cookies)
            .get("${BilibiliApi.videoDanMuPath}?oid=$cid",
                object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                    }

                    override fun onResponse(call: Call, response: Response) {

                        App.handler.post {
                            //储存弹幕
                            saveDanmaku(response.body!!.bytes())
                        }
                    }

                })
    }

    private fun loadUserCardData(mid: Long) {
        HttpUtils.addHeader("cookie", BaseApplication.cookies)
            .get(BilibiliApi.getUserCardPath + "?mid=$mid", UserCardBean::class.java) {
                showUserCard()
                binding.userCardBean = it
            }
    }

    /**
     * 显示用户卡片
     */
    private fun showUserCard() {
        binding.apply {
            asVideoUserCardLy.visibility = View.VISIBLE
        }
    }

    /**
     * 显示视频数据页面
     */
    private fun showVideoData() {
        binding.apply {
            asVideoDataLy.visibility = View.VISIBLE
        }
    }



    fun getDanmakuView(): DanmakuView? {
        return videoController.danmakuView
    }

    override fun onBackPressed() {

        //先让videoView处理返回事件
        if (videoController.onBackPressed()) {
            return
        }

        super.onBackPressed()
    }

    override fun onResume() {
        super.onResume()
        binding.playVideoView.resume()
        //恢复播放
        getDanmakuView()?.resume()
        //百度统计
        StatService.onResume(this)
    }

    override fun onPause() {
        super.onPause()
        //暂停视频
        binding.playVideoView.pause()
        getDanmakuView()?.pause()
        //百度统计
        StatService.onPause(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        //销毁播放器
        binding.playVideoView.release()
        getDanmakuView()?.release()
    }

    companion object {
        fun actionStart(context: Context, bvId: String) {
            val intent = Intent(context, PlayVideoActivity::class.java)
            intent.putExtra("bvId", bvId)
            context.startActivity(intent)
        }

        @Deprecated("B站已经在弱化aid的使用，我们不确定这是否会被弃用，因此这个方法将无法确定时效性")
        fun actionStart(context: Context, aid: Int) {
            val intent = Intent(context, PlayVideoActivity::class.java)
            intent.putExtra("bvId", VideoNumConversion.toBvidOffline(aid))
            context.startActivity(intent)
        }
    }


    /**
     * 储存弹幕内容
     */
    private fun saveDanmaku(bytes: ByteArray) {

        val bufferedSink: BufferedSink?
        val dest = File(getExternalFilesDir("temp").toString(), "tempDm.xml")
        if (!dest.exists()) dest.createNewFile()
        val sink = dest.sink() //打开目标文件路径的sink
        val decompressBytes =
            decompress(bytes) //调用解压函数进行解压，返回包含解压后数据的byte数组
        bufferedSink = sink.buffer()
        decompressBytes?.let { bufferedSink.write(it) } //将解压后数据写入文件（sink）中
        bufferedSink.close()

    }

    /**
     * 解压deflate数据的函数
     */
    fun decompress(data: ByteArray): ByteArray? {
        var output: ByteArray
        val decompresser = Inflater(true) //这个true是关键
        decompresser.reset()
        decompresser.setInput(data)
        val o = ByteArrayOutputStream(data.size)
        try {
            val buf = ByteArray(1024)
            while (!decompresser.finished()) {
                val i: Int = decompresser.inflate(buf)
                o.write(buf, 0, i)
            }
            output = o.toByteArray()
        } catch (e: Exception) {
            output = data
            e.printStackTrace()
        } finally {
            try {
                o.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        decompresser.end()
        return output
    }


}