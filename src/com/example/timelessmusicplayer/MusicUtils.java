package com.example.timelessmusicplayer;

import java.util.ArrayList;
import java.util.List;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

public class MusicUtils {
	private Context context;

	public MusicUtils(Context context){
		this.context=context;
	}
	
	public List<MP3Info> readFromMediaStore() {
		 Cursor cursor=null;
		 List<MP3Info> list=new ArrayList<MP3Info>();		
		 ContentResolver contentResolver=context.getContentResolver();
		 cursor = contentResolver.query(  
	                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,  
			        MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
		 while(cursor!=null&&cursor.moveToNext()) { 
		        MP3Info mp3Info=new MP3Info();		      
		        long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));   //音乐id  
		        String name = cursor.getString((cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));//音乐标题  
		        String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));//艺术家  
		        long totaltime = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));//时长  
		        long size = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));  //文件大小  
		        String url = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)); //url             //文件路径  
		        int isMusic = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));//是否为音乐  
		       // System.out.println("id:"+id+"   name:"+name+"   artist:"+artist+"   totaltime:"+totaltime+"  size:"+size+"   url:"+url+"  ismusic:"+isMusic);
		    if (isMusic != 0) {     //只把音乐添加到集合当中  
		        mp3Info.setId(id);  
		        mp3Info.setName(name);  
		        mp3Info.setArtist(artist);  
		        mp3Info.setTotaltime(totaltime);  
		        mp3Info.setSize(size);  
		        mp3Info.setUrl(url);  
		        list.add(mp3Info);  
		        }  
		} 
		if(cursor!=null) cursor.close();//避免空指针
        return list;
	}
	
	public static String formatTime(long time) {  //数据库中时长为ms，转为mm:ss
        String min = time / (1000 * 60) + "";  
        String sec = time % (1000 * 60) + "";  
        if (min.length() < 2) {  
            min = "0" + time / (1000 * 60) + "";  
        } else {  
            min = time / (1000 * 60) + "";  
        }  
        if (sec.length() == 4) {  
            sec = "0" + (time % (1000 * 60)) + "";  
        } else if (sec.length() == 3) {  
            sec = "00" + (time % (1000 * 60)) + "";  
        } else if (sec.length() == 2) {  
            sec = "000" + (time % (1000 * 60)) + "";  
        } else if (sec.length() == 1) {  
            sec = "0000" + (time % (1000 * 60)) + "";  
        }  
        return min + ":" + sec.trim().substring(0, 2);  
    }  
}
