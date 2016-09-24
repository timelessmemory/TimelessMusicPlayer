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
	private LrcProcess mLrcProcess; //��ʴ���  
	private List<LrcContent> lrcList = new ArrayList<LrcContent>(); //��Ÿ���б����  
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
			        MainActivity.lrcView.invalidate();//���¸��
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
			case TelephonyManager.CALL_STATE_RINGING://����
				if(mediaPlayer.isPlaying()){
					position=mediaPlayer.getCurrentPosition();
					mediaPlayer.pause();
				}
				break;
			case TelephonyManager.CALL_STATE_IDLE://�Ҷ�
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
		File music=new File(uriString);//��ȡ�ⲿ�洢Ŀ¼
		if(music.exists()){
			path=music.getAbsolutePath();
			try {
				mediaPlayer.reset();//��������ص���ʼ�����������һ������
				mediaPlayer.setDataSource(path);
				mediaPlayer.prepareAsync();//�����첽���л���
				mediaPlayer.setOnPreparedListener(new OnPreparedListener() {//��������
					public void onPrepared(MediaPlayer arg0) {//������ɵ���
						// TODO Auto-generated method stub
						mediaPlayer.start();	
						updateProgress();//�ڴ���� ����û�л���� ִ��update��Ч ���½������޷�����
					}
				});
				System.out.println("finsin play...");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else{
			path=null;
			Toast.makeText(this, "�ļ�������~~", Toast.LENGTH_SHORT).show();
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
		
		final String urlString=intent.getStringExtra("url");//ÿ�����¸�ֵurlString ��ֹ����һЩ�����������
		//System.out.println("url......."+urlString);
		String msg=intent.getStringExtra("MSG");//���������벻Ϊ��
		
		if(msg.equals("0")) {//�����ָ���쳣
			if(mediaPlayer.isPlaying()) {
				mediaPlayer.pause();
				playflag=true;
				Intent changeIcon=new  Intent();
				changeIcon.setAction(CHANGE_TO_START);
				sendBroadcast(changeIcon);
			} else if(playflag) {
				mediaPlayer.start();//��������
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
					mLrcProcess = new LrcProcess();//�������¸�ֵ ��Ȼ����ļ�����
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
		    	mediaPlayer.start();//��������
		    	mediaPlayer.seekTo(mediaPlayer.getDuration()*currentPosition/seekBarLength);
				updateProgress();
				playflag=false;
				Intent changeIcon=new  Intent();
				changeIcon.setAction(CHANGE_TO_PAUSE);
				sendBroadcast(changeIcon);
		    }
		}
		
		mediaPlayer.setOnCompletionListener(new OnCompletionListener() {//��������Զ�������һ��
			
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
        //�л���������ʾ���  
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
							if(lrcList!=null) {//�и�ʾ�֪ͨ���¸��
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
	
	public int lrcIndex() {  //���»�������ĸ��
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
