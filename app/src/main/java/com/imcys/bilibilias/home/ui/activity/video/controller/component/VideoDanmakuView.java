package com.imcys.bilibilias.home.ui.activity.video.controller.component;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import com.imcys.bilibilias.R;
import com.imcys.bilibilias.danmaku.BiliDanmukuParser;
import com.imcys.bilibilias.home.ui.activity.video.controller.DKVideoController;


import java.io.File;
import java.io.InputStream;

import master.flame.danmaku.controller.DrawHandler;
import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.DanmakuTimer;
import master.flame.danmaku.danmaku.model.IDanmakus;
import master.flame.danmaku.danmaku.model.android.DanmakuContext;
import master.flame.danmaku.danmaku.model.android.Danmakus;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;
import master.flame.danmaku.danmaku.parser.IDataSource;
import master.flame.danmaku.danmaku.parser.android.AndroidFileSource;
import master.flame.danmaku.ui.widget.DanmakuView;
import xyz.doikki.videoplayer.controller.ControlWrapper;
import xyz.doikki.videoplayer.controller.IGestureComponent;

/**
 * @ClassName: VideoDanmakuView
 * @Author: molihuan
 * @Date: 2023/01/01/21:30
 * @Description:
 */
public class VideoDanmakuView extends FrameLayout implements IGestureComponent {
    //弹幕视图
    private DanmakuView danmakuView;

    //弹幕内容
    private DanmakuContext danmakuContext;

    private ControlWrapper mControlWrapper;
    private String barragePath;
    private Context mContext;
    private DKVideoController dkVideoController;
    //本地弹幕文件是否存在
    private boolean localXmlExists = false;

    public DanmakuView getDanmakuView() {
        return danmakuView;
    }

    public DanmakuContext getDanmakuContext() {
        return danmakuContext;
    }

    public boolean isLocalXmlExists() {
        return localXmlExists;
    }

    public VideoDanmakuView(@NonNull Context context, DKVideoController dkVideoController) {
        super(context);
        //获取组件
        getComponents();
        //初始化数据
        initData(context,  dkVideoController);
        //初始化视图
        initMyView();
        //设置监听
        setListeners();
    }

    private void getComponents() {
        LayoutInflater.from(getContext()).inflate(R.layout.fragment_dk_video_danmaku, this, true);
        danmakuView = findViewById(R.id.danmaku_show_area);
    }

    private void initData(Context context, DKVideoController dkVideoController) {
        mContext = context;
        this.dkVideoController = dkVideoController;
    }

    private void initMyView() {
        //初始化弹幕
        //initDanmakuView();

        //获取弹幕是否显示配置
        if (false) {
            danmakuView.hide();
        }

        //初始化弹幕配置
        initDanmakuConfig();
    }




    /**
     * 加载弹幕
     * @param barragePath
     * @param isOnlineFile
     */
    public void loadDanmaku(String barragePath,boolean isOnlineFile) {

        if (danmakuView != null) {
            //String barragePath = mContext.getExternalFilesDir("temp").toString() + "/tempDm.xml";

            if (!(new File(barragePath).exists())) {
                localXmlExists = false;
                danmakuView.prepare(new BaseDanmakuParser() {
                    @Override
                    protected IDanmakus parse() {
                        return new Danmakus();
                    }
                }, danmakuContext);
                return;
            }
            //String barragePath = "https://comment.bilibili.com/367013145.xml";
            IDataSource<InputStream> danmakuFileSource ;
            if (isOnlineFile){
                danmakuFileSource = new AndroidFileSource(Uri.parse(barragePath));
            }else {
                danmakuFileSource = new AndroidFileSource(barragePath);
            }
            //自定义弹幕解析工具
            BaseDanmakuParser danmakuParser = new BiliDanmukuParser();
            danmakuParser.load(danmakuFileSource);
            //设置弹幕绘制回调
            danmakuView.setCallback(new DrawHandler.Callback() {
                @Override
                public void prepared() {
                    //Mtools.log("弹幕准备完成--------弹幕");
                    danmakuView.start();
                }

                @Override
                public void updateTimer(DanmakuTimer timer) {

                }

                @Override
                public void danmakuShown(BaseDanmaku danmaku) {

                }

                @Override
                public void drawingFinished() {
                    //Mtools.log("弹幕全部绘制完成--------弹幕");
                    //先暂停如果停止了就启动不了弹幕了
                    danmakuView.hideAndPauseDrawTask();
                }
            });

            //进行弹幕准备
            danmakuView.prepare(danmakuParser, danmakuContext);
            //显示FPS
            //danmakuView.showFPS(true);
            //开启绘制缓存
            danmakuView.enableDanmakuDrawingCache(true);
            localXmlExists = true;
        }

    }

    private void initDanmakuConfig() {
        //创建弹幕配置
        danmakuContext = DanmakuContext.create();

//         滚动弹幕最大显示5行
//        HashMap<Integer, Integer> maxLinesPair = new HashMap<>();
//        maxLinesPair.put(BaseDanmaku.TYPE_SCROLL_RL, 5);
//        danmakuContext.setMaximumLines(maxLinesPair)//设置滚动弹幕最大显示行

        //默认值应该从配置中读取
        int danmakuSize = 100;
        int danmakuAlpha = 100;
        int danmakuSpeed = 100;

        //设置基本弹幕配置
        danmakuContext
                .setSpecialDanmakuVisibility(true)//显示特殊弹幕
                .setScaleTextSize((float) danmakuSize / 100)//设置弹幕大小
                .setDanmakuTransparency((float) danmakuAlpha / 100)//设置透明度
                .setScrollSpeedFactor((float) (200 - danmakuSpeed) / 100)//设置弹幕滚动速度系数
        ;


    }

    private void setListeners() {
    }

    @Override
    public void onStartSlide() {

    }

    @Override
    public void onStopSlide() {

    }

    @Override
    public void onPositionChange(int slidePosition, int currentPosition, int duration) {

    }

    @Override
    public void onBrightnessChange(int percent) {

    }

    @Override
    public void onVolumeChange(int percent) {

    }

    @Override
    public void attach(@NonNull ControlWrapper controlWrapper) {
        mControlWrapper = controlWrapper;
    }

    @Nullable
    @Override
    public View getView() {
        return this;
    }

    @Override
    public void onVisibilityChanged(boolean isVisible, Animation anim) {

    }

    @Override
    public void onPlayStateChanged(int playState) {

    }

    @Override
    public void onPlayerStateChanged(int playerState) {

    }

    @Override
    public void setProgress(int duration, int position) {

    }

    @Override
    public void onLockStateChanged(boolean isLocked) {

    }
}
