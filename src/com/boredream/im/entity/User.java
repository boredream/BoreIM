package com.boredream.im.entity;

import cn.bmob.im.bean.BmobChatUser;
import cn.bmob.v3.datatype.BmobGeoPoint;

/**
 * ����BmobChatUser����������������Ҫ���ӵ����Կ��ڴ����
 */
@SuppressWarnings("serial")
public class User extends BmobChatUser {

	/**
	 * ��ʾ����ƴ��������ĸ
	 */
	private String sortLetters;

	/**
	 * �Ա�-true-��
	 */
	private boolean sex;
	
	/**
	 * ����ǩ��
	 */
	private String description;

	/**
	 * ��������
	 */
	private BmobGeoPoint location;

	public String getSortLetters() {
		return sortLetters;
	}

	public void setSortLetters(String sortLetters) {
		this.sortLetters = sortLetters;
	}

	public boolean isSex() {
		return sex;
	}

	public void setSex(boolean sex) {
		this.sex = sex;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public BmobGeoPoint getLocation() {
		return location;
	}

	public void setLocation(BmobGeoPoint location) {
		this.location = location;
	}

	
}
