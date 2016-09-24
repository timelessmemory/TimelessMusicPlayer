package com.example.timelessmusicplayer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.widget.Toast;

public class MusicService extends Service{

	private MediaPlayer mediaPlayer;
	private String path;
	private boolean playflag;
	public static final String CHANGE_TO_PAUSE="com.timeless.action.CHANGE_TO_PAUSE";
	public static final String CHANGE_TO_START="com.timeless.action.CHANGE_TO_START";
	public static final String CHANGE_TO_PROGRESS="com.timeless.action.CHANGE_TO_PROGRESS";
	public static final String COMPLETE="com.timeless.action.COMPLETE_ACTION";
	private int position;
	private LrcProcess mLrcProcess; //歌词处理  
	private List<LrcContent> lrcList = new ArrayList<LrcContent>(); //存放歌词列表对象  
	private int index = 0; 
	private Handler h;
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		mediaPlayer=new MediaPlayer();
		TelephonyManager telephonyManager=(TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
		telephonyManager.listen(new MyPhoneListener(), PhoneStateListener.LISTEN_CALL_STATE);
		IntentFilter filter=new IntentFilter();
		filter.addAction("Android.intent.action.NEW_OUTGOING_CALL");  
	    registerReceiver(new PhoneListener(), filter); 
		h = new Handler() { 
	        @SuppressLint("HandlerLeak")
			public void handleMessage (Message msg) 
	        { 
	            if(msg.what==1){
	            	MainActivity.lrcView.setIndex(lrcIndex());  
			        MainActivity.lrcView.invalidate();//更新歌词
				}		         
	        } 
	    };
		super.onCreate();
	} 
	
	private final class PhoneListener extends BroadcastReceiver {  
        public void onReceive(Context context, Intent intent) {  
            if(mediaPlayer.isPlaying())  {
            	position=mediaPlayer.getCurrentPosition();
				mediaPlayer.pause();
            }
        }  
    }  
	
	private final class MyPhoneListener extends PhoneStateListener{

		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			// TODO Auto-generated method stub
			switch (state) {
			case TelephonyManager.CALL_STATE_RINGING://来电
				if(mediaPlayer.isPlaying()){
					position=mediaPlayer.getCurrentPosition();
					mediaPlayer.pause();
				}
				break;
			case TelephonyManager.CALL_STATE_IDLE://挂断
				if(position>0){
					mediaPlayer.start();
					mediaPlayer.seekTo(position);
					position=0;
				}
				break;
			default:
				break;
			}
		}
	}
	
	private void Play(String uriString) {
		// TODO Auto-generated method stub
		File music=new File(uriString);//获取外部存储目录
		if(music.exists()){
			path=music.getAbsolutePath();
			try {
				mediaPlayer.reset();//各项参数回到初始，比如下面的一下设置
				mediaPlayer.setDataSource(path);
				mediaPlayer.prepareAsync();//采用异步进行缓冲
				mediaPlayer.setOnPreparedListener(new OnPreparedListener() {//监听缓冲
					public void onPrepared(MediaPlayer arg0) {//缓冲完成调用
						// TODO Auto-generated method stub
						mediaPlayer.start();	
						updateProgress();//在此添加 避免没有缓冲好 执行update无效 导致进度条无法更新
					}
				});
				System.out.println("finsin play...");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else{
			path=null;
			Toast.makeText(this, "文件不存在~~", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		if(mediaPlayer != null){  
            mediaPlayer.stop();  
            mediaPlayer.release();  
            mediaPlayer=null;
        }  
		super.onDestroy();
	}

	@SuppressLint("HandlerLeak")
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		
		final String urlString=intent.getStringExtra("url");//每次重新赋值urlString 防止后面一些函数多次运行
		//System.out.println("url......."+urlString);
		String msg=intent.getStringExtra("MSG");//传过来必须不为空
		
		if(msg.equals("0")) {//避免空指针异常
			if(mediaPlayer.isPlaying()) {
				mediaPlayer.pause();
				playflag=true;
				Intent changeIcon=new  Intent();
				changeIcon.setAction(CHANGE_TO_START);
				sendBroadcast(changeIcon);
			} else if(playflag) {
				mediaPlayer.start();//继续播放
				updateProgress();	
				playflag=false;
				Intent changeIcon=new  Intent();
				changeIcon.setAction(CHANGE_TO_PAUSE);
				sendBroadcast(changeIcon);
				}
		}
		
		if(urlString!=null) {
			
			new Thread(new Runnable() {				
				public void run() {
					// TODO Auto-generated method stub
					mLrcProcess = new LrcProcess();//必须重新赋值 不然歌词文件叠加
					mLrcProcess.readLRC(urlString);				
					lrcList = mLrcProcess.getLrcList();			
					if(MainActivity.lrcView!=null) MainActivity.lrcView.setmLrcList(lrcList);
					System.out.println(" urlstring ...."+urlString);
					Play(urlString);
					Intent changeIcon=new  Intent();
					changeIcon.setAction(CHANGE_TO_PAUSE);
					sendBroadcast(changeIcon);
				}
			}) .start();					
		    							
		} 
		
		if(msg.equals("2")) {
			int currentPosition=intent.getIntExtra("currentposition", 1);
		    int seekBarLength=intent.getIntExtra("seekbarlength",2);
		    if(mediaPlayer.isPlaying()) mediaPlayer.seekTo(mediaPlayer.getDuration()*currentPosition/seekBarLength);
		    else if(playflag) {
		    	mediaPlayer.start();//继续播放
		    	mediaPlayer.seekTo(mediaPlayer.getDuration()*currentPosition/seekBarLength);
				updateProgress();
				playflag=false;
				Intent changeIcon=new  Intent();
				changeIcon.setAction(CHANGE_TO_PAUSE);
				sendBroadcast(changeIcon);
		    }
		}
		
		mediaPlayer.setOnCompletionListener(new OnCompletionListener() {//播放完成自动播放下一首
			
			public void onCompletion(MediaPlayer arg0) {
				// TODO Auto-generated method stub
				Intent completeIntent=new  Intent();
				completeIntent.setAction(COMPLETE);
				sendBroadcast(completeIntent);
			}
		});
		return START_REDELIVER_INTENT;
	}


/*	public void initLrc(String url){        
        if(MainActivity.lrcView!=null) MainActivity.lrcView.setmLrcList(lrcList);  
        //切换带动画显示歌词  
       // MainActivity.lrcView.setAnimation(AnimationUtils.loadAnimation(PlayerService.this,R.anim.alpha_z));
    
		 final Handler h = new Handler() { 
		        public void handleMessage (Message msg) 
		        { 
		            if(msg.what==1){
		            	MainActivity.lrcView.setIndex(lrcIndex());  
				        MainActivity.lrcView.invalidate();
					}		         
		        } 
		    };		   
	*/
	public void updateProgress(){
		
			new Thread(new Runnable() {				
					public void run() {
						// TODO Auto-generated method stub
						while(mediaPlayer.isPlaying()) {
							Intent changeProgress=new  Intent();
							//System.out.println("mediaplayer:currenttime"+mediaPlayer.getCurrentPosition());
							changeProgress.putExtra("currenttime", mediaPlayer.getCurrentPosition());
							changeProgress.setAction(CHANGE_TO_PROGRESS);
							sendBroadcast(changeProgress);	
							if(lrcList!=null) {//有歌词就通知更新歌词
								Message msg = new Message(); 
						         msg.what = 1; 
						         h.sendMessage(msg); 
							}
							try {
								Thread.sleep(1000);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}							
					}				
			}).start(); 
	}
	
	public int lrcIndex() {  //重新绘制哪里的歌词
		int currentTime=0;
		int duration=0;
	    if(mediaPlayer.isPlaying()) {  
	         currentTime = mediaPlayer.getCurrentPosition();  
	         duration = mediaPlayer.getDuration();  
	    }  
	    if(currentTime < duration) {  
	        for (int i = 0; i < lrcList.size(); i++) {  
	            if (i < lrcList.size() - 1) {  
	                if (currentTime < lrcList.get(i).getLrcTime() && i == 0) {  
	                    index = i;  
	                }  
	                if (currentTime > lrcList.get(i).getLrcTime()  
	                        && currentTime < lrcList.get(i + 1).getLrcTime()) {  
	                    index = i;  
	                }  
	            }  
	            if (i == lrcList.size() - 1  
	                    && currentTime > lrcList.get(i).getLrcTime()) {  
	                index = i;  
	            }  
	        }  
	    }  
	    return index;  
	}  
}
