package com.boredream.im.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import cn.bmob.im.BmobUserManager;
import cn.bmob.im.bean.BmobMsg;
import cn.bmob.im.config.BmobConfig;
import cn.bmob.im.util.BmobUtils;

public class BoreAudioManager {
	
	public MediaPlayer mediaPlayer = new MediaPlayer();
	
	/**
	 * 初始化播放语音
	 * 
	 * @param context
	 * @param filePath 语音文件路径
	 * @param isSpeakerPhoneOn 是否扬声器播放
	 * @param AudioPlayListener 音频播放回调
	 * @throws FileNotFoundException 
	 * 
	 * @throws IOException 
	 */
	public BoreAudioManager(Context context, String filePath, boolean isSpeakerPhoneOn,
			OnPreparedListener onPreparedListener, OnCompletionListener onCompletionListener) throws IOException{
		if (!(new File(filePath).exists())) {
			return;
		}
		
		AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		
		// 根据是否需要扬声器播放设置音频相关信息
		if (isSpeakerPhoneOn) {
			audioManager.setSpeakerphoneOn(true);
			audioManager.setMode(AudioManager.MODE_NORMAL);
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
		} else {
			audioManager.setSpeakerphoneOn(false);
			audioManager.setMode(AudioManager.MODE_IN_CALL);
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
		}

		mediaPlayer.reset();
		// 单独使用此方法会报错播放错误:setDataSourceFD failed.: status=0x80000000
		// mediaPlayer.setDataSource(filePath);
		// 因此采用此方式会避免这种错误
		FileInputStream fis = new FileInputStream(new File(filePath));
		mediaPlayer.setDataSource(fis.getFD());
		mediaPlayer.prepareAsync();
		mediaPlayer.setOnPreparedListener(onPreparedListener);
		mediaPlayer.setOnCompletionListener(onCompletionListener);

		fis.close();
	}
	
	/**
	 * 停止播放语音
	 */
	public void stopPlayRecord() {
		if (mediaPlayer != null) {
			mediaPlayer.stop();
			mediaPlayer.release();
		}
	}
	
	/**
	 * 获取下载音频的文件存放路径
	 * 
	 * @param context
	 * @param msg 包含音频信息的消息
	 * @return 下载音频文件存放的路径
	 * 
	 * @throws IOException 
	 */
	public static String getDownloadAudioFilePath(Context context, BmobMsg msg) throws IOException {
		BmobUserManager userManager = BmobUserManager.getInstance(context);
		String accountDir = BmobUtils.string2MD5(userManager.getCurrentUserObjectId());
		
		// 目录
		File dir = new File(BmobConfig.BMOB_VOICE_DIR + File.separator
				+ accountDir + File.separator + msg.getBelongId());
		if (!dir.exists()) {
			dir.mkdirs();
		}
		
		// 在当前用户的目录下面存放录音文件
		File audioFile = new File(dir.getAbsolutePath() + File.separator
				+ msg.getMsgTime() + ".amr");
		if (!audioFile.exists()) {
			audioFile.createNewFile();
		}
		
		return audioFile.getAbsolutePath();
	}
}
