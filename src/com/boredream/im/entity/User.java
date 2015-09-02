package com.boredream.im.entity;

import cn.bmob.im.bean.BmobChatUser;
import cn.bmob.v3.datatype.BmobGeoPoint;

/**
 * 重载BmobChatUser对象：若还有其他需要增加的属性可在此添加
 */
@SuppressWarnings("serial")
public class User extends BmobChatUser {

	/**
	 * 显示数据拼音的首字母
	 */
	private String sortLetters;

	/**
	 * 性别-true-男
	 */
	private boolean sex;
	
	/**
	 * 个人签名
	 */
	private String description;

	/**
	 * 地理坐标
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
