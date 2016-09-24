package com.example.timelessmusicplayer;

public class MP3Info {//·â×°¸èÇúÐÅÏ¢
	private long id;
	private String name;
	private String artist;
	private long totaltime;
	private long size;
	private String url;
	
	public MP3Info(long id, String name, String artist, long totaltime,
			long size, String url) {
		this.id = id;
		this.name = name;
		this.artist = artist;
		this.totaltime = totaltime;
		this.size = size;
		this.url = url;
	}

	public MP3Info(){}
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getArtist() {
		return artist;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public long getTotaltime() {
		return totaltime;
	}

	public void setTotaltime(long totaltime) {
		this.totaltime = totaltime;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
}
