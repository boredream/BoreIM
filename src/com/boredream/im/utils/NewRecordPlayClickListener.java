package com.boredream.im.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.view.View;
import android.widget.ImageView;
import cn.bmob.im.BmobUserManager;
import cn.bmob.im.bean.BmobMsg;
import cn.bmob.im.config.BmobConfig;
import cn.bmob.im.util.BmobLog;
import cn.bmob.im.util.BmobUtils;

import com.boredream.im.R;

/**
 * 播放录音文件
 */
public class NewRecordPlayClickListener implements View.OnClickListener {

	private Context context;
	private BmobMsg message;
	private ImageView iv_voice;
	private boolean isSend;

	private AnimationDrawable anim;
	private MediaPlayer mediaPlayer;
	private BmobUserManager userManager;

	public static boolean isPlaying;
	public static NewRecordPlayClickListener currentPlayListener;
	private static BmobMsg currentMsg;// 用于区分两个不同语音的播放

	public NewRecordPlayClickListener(Context context, BmobMsg msg,
			ImageView voice, boolean isSend) {
		this.context = context;
		this.message = msg;
		this.iv_voice = voice;
		this.isSend = isSend;

		userManager = BmobUserManager.getInstance(context);
	}

	/**
	 * 播放语音
	 */
	public void startPlayRecord(String filePath, boolean isUseSpeaker) {
		if (!(new File(filePath).exists())) {
			return;
		}
		AudioManager audioManager = (AudioManager) context
				.getSystemService(Context.AUDIO_SERVICE);
		mediaPlayer = new MediaPlayer();
		if (isUseSpeaker) {
			audioManager.setMode(AudioManager.MODE_NORMAL);
			audioManager.setSpeakerphoneOn(true);
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
		} else {
			audioManager.setSpeakerphoneOn(false);// 关闭扬声器
			audioManager.setMode(AudioManager.MODE_IN_CALL);
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
		}

		try {
			mediaPlayer.reset();
			// 单独使用此方法会报错播放错误:setDataSourceFD failed.: status=0x80000000
			// mediaPlayer.setDataSource(filePath);
			// 因此采用此方式会避免这种错误
			FileInputStream fis = new FileInputStream(new File(filePath));
			mediaPlayer.setDataSource(fis.getFD());
			mediaPlayer.prepareAsync();
			mediaPlayer.setOnPreparedListener(new OnPreparedListener() {
				@Override
				public void onPrepared(MediaPlayer arg0) {
					isPlaying = true;
					arg0.start();
					startRecordAnimation();
					
					// 开始播放时,记录当前音频信息
					currentPlayListener = NewRecordPlayClickListener.this;
					currentMsg = message;
				}
			});
			mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
						@Override
						public void onCompletion(MediaPlayer mp) {
							stopPlayRecord();
						}
					});

			fis.close();
		} catch (Exception e) {
			BmobLog.i("播放错误:" + e.getMessage());
		}
	}

	/**
	 * 停止播放
	 */
	public void stopPlayRecord() {
		stopRecordAnimation();
		if (mediaPlayer != null) {
			mediaPlayer.stop();
			mediaPlayer.release();
		}
		isPlaying = false;
	}

	/**
	 * 开启播放动画
	 */
	private void startRecordAnimation() {
		iv_voice.setImageResource(isSend ? R.anim.anim_chat_voice_right
				: R.anim.anim_chat_voice_left);
		anim = (AnimationDrawable) iv_voice.getDrawable();
		if (anim != null) {
			anim.start();
		}
	}

	/**
	 * 停止播放动画
	 */
	private void stopRecordAnimation() {
		iv_voice.setImageResource(isSend ? R.drawable.voice_right_3
				: R.drawable.voice_left_3);
		if (anim != null) {
			anim.stop();
		}
	}

	@Override
	public void onClick(View arg0) {
		if (isPlaying) {
			// 如果正在播放,则停止其播放
			currentPlayListener.stopPlayRecord();
			currentPlayListener = null;

			// 如果正在播放的是当前对象,则只作停止操作,不执行后续的新播放
			if (currentMsg != null
					&& currentMsg.hashCode() == message.hashCode()) {
				currentMsg = null;
				return;
			}
		}
		
		String filePath = isSend ? message.getContent().split("&")[0] : // 如果是自己发送的语音消息，则播放本地地址
				getDownLoadFilePath(message); // 如果是收到的消息，则需要先下载后播放

		startPlayRecord(filePath, true);
	}

	public String getDownLoadFilePath(BmobMsg msg) {
		String accountDir = BmobUtils.string2MD5(userManager
				.getCurrentUserObjectId());
		File dir = new File(BmobConfig.BMOB_VOICE_DIR + File.separator
				+ accountDir + File.separator + msg.getBelongId());
		if (!dir.exists()) {
			dir.mkdirs();
		}
		// 在当前用户的目录下面存放录音文件
		File audioFile = new File(dir.getAbsolutePath() + File.separator
				+ msg.getMsgTime() + ".amr");
		try {
			if (!audioFile.exists()) {
				audioFile.createNewFile();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return audioFile.getAbsolutePath();
	}

}