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
 * ����¼���ļ�
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
	private static BmobMsg currentMsg;// ��������������ͬ�����Ĳ���

	public NewRecordPlayClickListener(Context context, BmobMsg msg,
			ImageView voice, boolean isSend) {
		this.context = context;
		this.message = msg;
		this.iv_voice = voice;
		this.isSend = isSend;

		userManager = BmobUserManager.getInstance(context);
	}

	/**
	 * ��������
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
			audioManager.setSpeakerphoneOn(false);// �ر�������
			audioManager.setMode(AudioManager.MODE_IN_CALL);
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
		}

		try {
			mediaPlayer.reset();
			// ����ʹ�ô˷����ᱨ���Ŵ���:setDataSourceFD failed.: status=0x80000000
			// mediaPlayer.setDataSource(filePath);
			// ��˲��ô˷�ʽ��������ִ���
			FileInputStream fis = new FileInputStream(new File(filePath));
			mediaPlayer.setDataSource(fis.getFD());
			mediaPlayer.prepareAsync();
			mediaPlayer.setOnPreparedListener(new OnPreparedListener() {
				@Override
				public void onPrepared(MediaPlayer arg0) {
					isPlaying = true;
					arg0.start();
					startRecordAnimation();
					
					// ��ʼ����ʱ,��¼��ǰ��Ƶ��Ϣ
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
			BmobLog.i("���Ŵ���:" + e.getMessage());
		}
	}

	/**
	 * ֹͣ����
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
	 * �������Ŷ���
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
	 * ֹͣ���Ŷ���
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
			// ������ڲ���,��ֹͣ�䲥��
			currentPlayListener.stopPlayRecord();
			currentPlayListener = null;

			// ������ڲ��ŵ��ǵ�ǰ����,��ֻ��ֹͣ����,��ִ�к������²���
			if (currentMsg != null
					&& currentMsg.hashCode() == message.hashCode()) {
				currentMsg = null;
				return;
			}
		}
		
		String filePath = isSend ? message.getContent().split("&")[0] : // ������Լ����͵�������Ϣ���򲥷ű��ص�ַ
				getDownLoadFilePath(message); // ������յ�����Ϣ������Ҫ�����غ󲥷�

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
		// �ڵ�ǰ�û���Ŀ¼������¼���ļ�
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