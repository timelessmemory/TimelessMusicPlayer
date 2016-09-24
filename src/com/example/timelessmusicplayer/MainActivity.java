package com.example.timelessmusicplayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.ViewFlipper;

public class MainActivity extends Activity {
	
	private ViewFlipper viewFlipper;
	private float startX;
	private float endX;
	private Animation firstOut;
	private Animation secondIn;
	private Animation secondOut;
	private Animation firstIn;
	private ImageButton startButton;
	private MusicUtils musicUtils;
	private ListView listView;
	private ServiceReceiver serviceReceiver;
	private List<HashMap<String, String>> mp3list;
	private static final String PLAY_BY_BUTTON="0";
	private static final String PLAY_BY_ITEM="1";
	private static final String UPDATE_MEDIA="2";
	private int pos;
	private boolean isload=true;
	private String unitUrl;
	private ImageButton lastButton;
	private ImageButton nextButton;
	private TextView songNameView;
	private TextView endProgressView;
	private TextView startProgressView;
	private SeekBar seekBar;
	private int seekBarLength;
	private int musicNum;
	public static  LrcView lrcView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		viewFlipper=(ViewFlipper)findViewById(R.id.viewFlipper1);
		startButton=(ImageButton)findViewById(R.id.play);
		startButton.setBackgroundResource(R.drawable.play);
		startProgressView=(TextView)findViewById(R.id.startprogress);
		seekBar=(SeekBar)findViewById(R.id.seekBar1);
		listView=(ListView)findViewById(R.id.listView1);
		lastButton=(ImageButton)findViewById(R.id.last);
		nextButton=(ImageButton)findViewById(R.id.next);
		songNameView=(TextView)findViewById(R.id.songname);
		endProgressView=(TextView)findViewById(R.id.endprogress);
		lrcView=(LrcView)findViewById(R.id.lrcShowView);
		firstOut=AnimationUtils.loadAnimation(this, R.anim.firstout);
		secondIn=AnimationUtils.loadAnimation(this, R.anim.secondin);
		secondOut=AnimationUtils.loadAnimation(this, R.anim.secondout);
		firstIn=AnimationUtils.loadAnimation(this, R.anim.firstin);
		startButton.setOnClickListener(new StartListener());						
		seekBar.setOnSeekBarChangeListener(new SeekBarListener());
		listView.setOnItemClickListener(new MusicItemListener());	
		lastButton.setOnClickListener(new LastListener());
		nextButton.setOnClickListener(new NextListener());
		registBroadcast();
		musicUtils=new MusicUtils(this);		
		showList();
		unitUrl=unit();
	}
	
	public String unit(){//第一次如果点击的是播放键 那么随机一首
		if(mp3list != null&&mp3list.size()!=0) {		
			Random random=new Random();
			int position=random.nextInt(mp3list.size());
			HashMap<String, String> map  = mp3list.get(position); 		
		    songNameView.setText(map.get("name"));
			endProgressView.setText(map.get("totaltime"));
			pos=position;
			return map.get("url");
		}
		return null;
	}
	
	public class MusicItemListener implements OnItemClickListener{

		public void onItemClick(AdapterView<?> arg0, View arg1, int position,
				long arg3) {
			// TODO Auto-generated method stub			 
			 if(mp3list != null&&mp3list.size()!=0) {  
				    isload=false;
				    pos=position;
				    HashMap<String, String> map  = mp3list.get(position);
				    songNameView.setText(map.get("name"));
				    endProgressView.setText(map.get("totaltime"));
		            Intent intent = new Intent();  
		            intent.putExtra("url", map.get("url"));       
		            intent.putExtra("MSG",PLAY_BY_ITEM);
		            intent.setClass(MainActivity.this, MusicService.class);  
		            MainActivity.this.startService(intent);         
		        }  
		}
		
	}
	
	public class SeekBarListener implements OnSeekBarChangeListener{

		public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
			// TODO Auto-generated method stub
			//System.out.println("onProgressChanged..");
		}

		public void onStartTrackingTouch(SeekBar arg0) {
			// TODO Auto-generated method stub
			//System.out.println("onStartTrackingTouch..");
		}

		public void onStopTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
		//	System.out.println("onStopTrackingTouch..");
			int currentPosition=seekBar.getProgress();
			seekBarLength=seekBar.getMax();
			Intent intent = new Intent();
			intent.putExtra("seekbarlength",seekBarLength);//传递给service改变mediaplayer播放
			intent.putExtra("currentposition",currentPosition);
			intent.putExtra("MSG", UPDATE_MEDIA);
			intent.setClass(MainActivity.this, MusicService.class);
			MainActivity.this.startService(intent);       
		}
		
	}

	private void showList() {//在listview上呈现歌曲信息
		// TODO Auto-generated method stub
	    mp3list = new ArrayList<HashMap<String, String>>();;  
		List<MP3Info> list=musicUtils.readFromMediaStore();	
		if(list!=null) {		
			Iterator<MP3Info> iterator = list.iterator();
			while(iterator.hasNext()) {  
	            MP3Info mp3Info = (MP3Info)iterator.next();  
	            HashMap<String, String> map = new HashMap<String, String>();  
	            map.put("name", mp3Info.getName());  
	            map.put("artist", mp3Info.getArtist());  
	            map.put("totaltimems", String.valueOf(mp3Info.getTotaltime())); 
	            map.put("totaltime", MusicUtils.formatTime(mp3Info.getTotaltime()));
	            map.put("size", String.valueOf(mp3Info.getSize()));  
	            map.put("url", mp3Info.getUrl());  
	            mp3list.add(map);  
	        } 
		}				
		musicNum=mp3list.size();
		SimpleAdapter mAdapter = new SimpleAdapter(this, mp3list,
				R.layout.music_list_item_layout, new String[] { "name",
						"artist", "totaltime" }, new int[] { R.id.music_title,
						R.id.music_Artist, R.id.music_duration });
		listView.setAdapter(mAdapter);
	}

	public class ServiceReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context arg0, Intent intent) {//处理服务传递过来的操作 比如更新进度条
			// TODO Auto-generated method stub
			if(intent.getAction().equals(MusicService.CHANGE_TO_PAUSE))
				startButton.setBackgroundResource(R.drawable.pause);
			else if(intent.getAction().equals(MusicService.CHANGE_TO_START))
	            startButton.setBackgroundResource(R.drawable.play);	
			else if(intent.getAction().equals(MusicService.CHANGE_TO_PROGRESS))
			{
			
				int totaltime=Integer.valueOf(mp3list.get(pos).get("totaltimems"));
				int currenttime=intent.getIntExtra("currenttime",0);
				startProgressView.setText(String.valueOf(MusicUtils.formatTime(currenttime)));
				seekBarLength=seekBar.getMax();
				//System.out.println("currenttime:"+currenttime+"   totaltime:"+totaltime+"   seekbarlength:"+seekBarLength);
				seekBar.setProgress(currenttime*seekBarLength/totaltime);
			} else if(intent.getAction().equals(MusicService.COMPLETE)) {
				next();
			}
		}
		
	}

	private void registBroadcast() {//注册广播
		// TODO Auto-generated method stub
		serviceReceiver=new ServiceReceiver();
		IntentFilter intentFilter=new IntentFilter();
		intentFilter.addAction(MusicService.CHANGE_TO_PAUSE);
		intentFilter.addAction(MusicService.CHANGE_TO_START);
		intentFilter.addAction(MusicService.CHANGE_TO_PROGRESS);
		intentFilter.addAction(MusicService.COMPLETE);
		this.registerReceiver(serviceReceiver, intentFilter);
		
	}
	
	public class StartListener implements OnClickListener{//开始播放
    	
		public void onClick(View arg0) {
			// TODO Auto-generated method stub			
			Intent intent=new Intent(MainActivity.this,MusicService.class);	
			intent.putExtra("MSG", PLAY_BY_BUTTON);
			System.out.println("startlistener uinturl...."+unitUrl);
			if(isload&&unitUrl!=null) intent.putExtra("url", unitUrl);//初次点击开始按钮 随机一首歌
			MainActivity.this.startService(intent);	
			isload=false;
		}
    	
    }
    
	public class LastListener implements OnClickListener{//上一首

		public void onClick(View arg0) {
			// TODO Auto-generated method stub			
			int position=pos-1;//不可以直接操作pos，如果pos-1<0 会导致onreceive（）中引用指针出现空指针异常
			if(position<0) {
				Toast.makeText(MainActivity.this, "没有上一首了~~",Toast.LENGTH_SHORT).show();
				pos=0;
			} else pos=position;
			if(mp3list!=null&&mp3list.size()!=0) {
				HashMap<String, String> map  = mp3list.get(pos); 		
			    songNameView.setText(map.get("name"));
				endProgressView.setText(map.get("totaltime"));
				Intent intent = new Intent();  
	            intent.putExtra("url", map.get("url"));  
	            intent.putExtra("MSG", "last");//避免在service中接收后判定时出现空指针异常
	            intent.setClass(MainActivity.this, MusicService.class);  
	            MainActivity.this.startService(intent); 
			}			                   
		}
		
	}
	
	public class NextListener implements OnClickListener{//下一首

		public void onClick(View arg0) {
			// TODO Auto-generated method stub		
			next();    
		}
		
	}
	
	private void next() {//改变歌曲list中postion 从而传递给service一个新的url
		// TODO Auto-generated method stub
		int position=pos+1;//避免出现空指针异常
		if(position==musicNum) {
			Toast.makeText(MainActivity.this, "没有下一首了~~",Toast.LENGTH_SHORT).show();
			pos=musicNum-1;
		} else pos=position;
		if(mp3list!=null&&mp3list.size()!=0) {
			HashMap<String, String> map  = mp3list.get(pos); 
		    songNameView.setText(map.get("name"));
			endProgressView.setText(map.get("totaltime"));
			Intent intent = new Intent();  
	        intent.putExtra("url", map.get("url"));
	        intent.putExtra("MSG", "next");//避免空指针异常
	        intent.setClass(MainActivity.this, MusicService.class);  
	        MainActivity.this.startService(intent);   
		}		   
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {//切换布局
		if(event.getAction()==MotionEvent.ACTION_DOWN){
			startX=event.getX();
		}else if(event.getAction()==MotionEvent.ACTION_UP){
			endX=event.getX();
			//System.out.println("x:"+startX+"  y:"+endX);
			if(endX<startX){
				viewFlipper.setOutAnimation(firstOut);
				viewFlipper.setInAnimation(secondIn);
				viewFlipper.showNext();
			}else if(endX>startX){
				viewFlipper.setOutAnimation(secondOut);
				viewFlipper.setInAnimation(firstIn);
				viewFlipper.showPrevious();
			}
			return true;
		}
		return super.onTouchEvent(event);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {//退出操作
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {  
  
            new AlertDialog.Builder(this)  
                    .setIcon(R.drawable.exit)  
                    .setTitle("退出")  
                    .setMessage("你确定要退出？")  
                    .setNegativeButton("取消", null)  
                    .setPositiveButton("确定",new DialogInterface.OnClickListener() {  
                  
                                public void onClick(DialogInterface dialog,int which) { 
                                	MainActivity.this.unregisterReceiver(serviceReceiver);
                                	Intent intent = new Intent(MainActivity.this,MusicService.class);                                	                             	                                	                          	                                           
                                    if(isload==false) MainActivity.this.stopService(intent); // 停止后台服务 
                                    finish(); 
                                    android.os.Process.killProcess(android.os.Process.myPid());    //获取PID 
                                    
                                }  
                            }).show();  
  
        }  
		return super.onKeyDown(keyCode, event);
	}
	
	
}
