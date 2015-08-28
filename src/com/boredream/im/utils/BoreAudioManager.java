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
	 * ��ʼ����������
	 * 
	 * @param context
	 * @param filePath �����ļ�·��
	 * @param isSpeakerPhoneOn �Ƿ�����������
	 * @param AudioPlayListener ��Ƶ���Żص�
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
		
		// �����Ƿ���Ҫ����������������Ƶ�����Ϣ
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
		// ����ʹ�ô˷����ᱨ���Ŵ���:setDataSourceFD failed.: status=0x80000000
		// mediaPlayer.setDataSource(filePath);
		// ��˲��ô˷�ʽ��������ִ���
		FileInputStream fis = new FileInputStream(new File(filePath));
		mediaPlayer.setDataSource(fis.getFD());
		mediaPlayer.prepareAsync();
		mediaPlayer.setOnPreparedListener(onPreparedListener);
		mediaPlayer.setOnCompletionListener(onCompletionListener);

		fis.close();
	}
	
	/**
	 * ֹͣ��������
	 */
	public void stopPlayRecord() {
		if (mediaPlayer != null) {
			mediaPlayer.stop();
			mediaPlayer.release();
		}
	}
	
	/**
	 * ��ȡ������Ƶ���ļ����·��
	 * 
	 * @param context
	 * @param msg ������Ƶ��Ϣ����Ϣ
	 * @return ������Ƶ�ļ���ŵ�·��
	 * 
	 * @throws IOException 
	 */
	public static String getDownloadAudioFilePath(Context context, BmobMsg msg) throws IOException {
		BmobUserManager userManager = BmobUserManager.getInstance(context);
		String accountDir = BmobUtils.string2MD5(userManager.getCurrentUserObjectId());
		
		// Ŀ¼
		File dir = new File(BmobConfig.BMOB_VOICE_DIR + File.separator
				+ accountDir + File.separator + msg.getBelongId());
		if (!dir.exists()) {
			dir.mkdirs();
		}
		
		// �ڵ�ǰ�û���Ŀ¼������¼���ļ�
		File audioFile = new File(dir.getAbsolutePath() + File.separator
				+ msg.getMsgTime() + ".amr");
		if (!audioFile.exists()) {
			audioFile.createNewFile();
		}
		
		return audioFile.getAbsolutePath();
	}
}
