package com.imcys.bilibilias.home.ui.activity.video;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.LayoutRes;
import androidx.appcompat.app.AppCompatActivity;

import com.imcys.bilibilias.R;
import com.imcys.bilibilias.common.base.AbsActivity;
import com.imcys.bilibilias.home.ui.activity.video.controller.DKVideoController;

import java.io.File;

import master.flame.danmaku.ui.widget.DanmakuView;
import xyz.doikki.videoplayer.player.VideoView;


/**
 * @ClassName: AbstractActivity
 * @Author: molihuan
 * @Date: 2022/11/22/13:07
 * @Description:
 */
public class PlayLocalVideoActivity extends AbsActivity implements View.OnClickListener {

    //DK播放器视图
    private VideoView videoView;
    //DK控制器
    private DKVideoController videoController;

    //视频路径
    private String videoPath;

    public DanmakuView getDanmakuView() {
        return videoController.getDanmakuView();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置布局资源
        setContentView(R.layout.activity_dk_video);
        //获取组件
        getComponents();
        //初始化数据
        initData();
        //初始化视图
        initView();
        //设置监听
        setListeners();

    }

    public void getComponents() {
        videoView = findViewById(R.id.play_video_view);
    }


    public void initData() {
        Intent intent = getIntent();
        //获取播放视频的路径
        videoPath = intent.getStringExtra("videoPath");
        //全屏模式
        //ScreenUtils.setFullScreen(this);
        //设置视频地址
        videoView.setUrl(videoPath);
        //初始化视频控制器
        videoController = new DKVideoController(this);

        //设置控制器
        videoView.setVideoController(videoController);
        //开始播放
        videoView.start();


    }


    public void initView() {

    }


    public void setListeners() {

    }



    public void onClick(View v) {

    }

    @Override
    public void onBackPressed() {
        //先让videoView处理返回事件
        if (videoController != null && videoController.onBackPressed()) {
            return;
        }

        super.onBackPressed();
    }

    @Override
    public void onResume() {
        super.onResume();
        //恢复播放
        if (videoView != null) {
            videoView.resume();
        }

        getDanmakuView().resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        //暂停视频
        if (videoView != null) {
            videoView.pause();
        }

        getDanmakuView().pause();
    }

    @Override
    public void onDestroy() {
        //销毁播放器
        releaseVideoViewDanmakuView();
        super.onDestroy();
    }

    /**
     * 销毁播放器
     */
    protected void releaseVideoViewDanmakuView() {
        if (videoView != null) {
            videoView.release();
            videoView = null;
        }
        DanmakuView danmakuView = getDanmakuView();
        if (danmakuView != null) {
            danmakuView.release();
        }
    }


}
