package guoTeng.readCard;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.cnnk.IdentifyCard.Gpio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;

import Invs.Termb;

//回复 Android_Tutor：SkyGray说的对，进程并没有真正退出。
//可以在onCreate判断savedInstanceState是否等于NULL就可以知道是不是re-initialized了，
//或者在onBackPressed调用System.exit(0)真正退出进程。一点拙见，请轻砸。
public class readCard extends Activity implements OnClickListener{
	/** Called when the activity is first created. */
	ReadCardThread mReadCardThread = null;
	public int g_iStatus = 0;
	public boolean g_bTerminate = false;
	public boolean g_bDeviceOk=false;
	public boolean g_bReaded=false;
	public boolean g_bRun = false;
	public String g_szCardId="";
	private Bitmap bm;//图片资源Bitmap
	public String g_szSamid = "";

	public static final int Suc_SamId = 0;
	public static final int Err_SamId = 1;
	public static final int Suc_ReadCard = 2;
	public static final int Err_ReadCard = 3;
	public static final int Suc_FindCard = 4;
	public static final int Err_FindCard = 5;
	public static final int Err_ReadApp = 6;
	public static final int Tag_Once = 10;
	//安全模块电源管理库
	public int fd =0;

	private static final String TAG = "MyNewLog";

	//读卡成功次数
	public static long Suc_Num = 0;
	//读卡失败次数
	public static long Fau_Num = 0;
	//读卡总次数
	public static long Tot_Num = 0;


	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		//System.exit(0);
	}
	public void onClick(View v) {
		if(R.id.button1 == v.getId()){
			Button b1 = (Button) findViewById(R.id.button1);
			//b1.setClickable(false);
			b1.setEnabled(false);

			Button b2 = (Button) findViewById(R.id.button2);
			if (!b2.isEnabled()){
				g_iStatus = 0;
				b2.setEnabled(false);
				while(g_bRun){
					;
				}
			}else{
				b2.setEnabled(false);
			}
			g_iStatus = 1;
		}else if(R.id.button2 == v.getId()){
			Button b2 = (Button) findViewById(R.id.button2);
			b2.setEnabled(false);
			g_iStatus = 2;
			Button b1 = (Button) findViewById(R.id.button1);
			b1.setEnabled(true);
		}else if(R.id.button3 == v.getId()){
//			powerOff();
			Gpio.GPIO_pullLow(mGpioFd, GPIO_IDCARD);
			g_iStatus = 0;
			g_bTerminate = true;
			System.exit(0);
		}
	}
	public void InitView()
	{
		TextView v = (TextView)findViewById(R.id.textView1);
		//setTitle("国腾二代证蓝牙读卡程序");
		v.setText("姓名  ");
		v = (TextView)findViewById(R.id.textView2);
		v.setText("性别  ");
		v = (TextView)findViewById(R.id.textView3);
		v.setText("名族  ");
		v = (TextView)findViewById(R.id.textView4);
		v.setText("出生  ");
		v = (TextView)findViewById(R.id.textView5);
		v.setText("住址  ");
		v = (TextView)findViewById(R.id.textView6);
		v.setText("公民身份证号  ");
		v = (TextView)findViewById(R.id.textView7);
		v.setText("签发机关  ");
		v = (TextView)findViewById(R.id.textView8);
		v.setText("有效期限  ");
		v = (TextView)findViewById(R.id.textView9);
		v.setText(new String(" "));
		v = (TextView)findViewById(R.id.textView10);
		v.setText(new String(" "));
		v = (TextView)findViewById(R.id.textView11);
		v.setText(new String(" "));

		v = (TextView)findViewById(R.id.textView12);
		v.setText(new String(" "));

		if (g_szSamid != ""){
			v.setText("安全模块号 " + g_szSamid);
		}


		v = (TextView)findViewById(R.id.textView13);
		v.setText(new String("读卡成功次数 ") + String.valueOf(Suc_Num));
		v = (TextView)findViewById(R.id.textView14);
		v.setText(new String("读卡失败次数 " + String.valueOf(Fau_Num)));
		v = (TextView)findViewById(R.id.textView15);
		v.setText(new String("读卡总次数 ")+ String.valueOf(Tot_Num));

		ImageView mImageView = (ImageView)findViewById(R.id.imageView1);
		mImageView.setImageDrawable(getResources().getDrawable(R.drawable.tmp));
	}
	public void InitThread()
	{
		try {
			mReadCardThread = new ReadCardThread(this, mHandler);
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mReadCardThread.start();
	}
	void powerOn()
	{
		try {
			final String FILE_NAME = "/sys/class/gpio/gpio69/value";
			File targetFile=new File(FILE_NAME);
			RandomAccessFile raf=new RandomAccessFile(targetFile,"rw");
			raf.seek(0);
			raf.writeByte(49);  // 49 为 1字符  asicii code，48为 0 字符  asicii code
			raf.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void powerOff()
	{
		try {
			final String FILE_NAME = "/sys/class/gpio/gpio69/value";
			File targetFile=new File(FILE_NAME);
			RandomAccessFile raf=new RandomAccessFile(targetFile,"rw");
			raf.seek(0);
			raf.writeByte(48);  // 49 为 1字符  asicii code，48为 0 字符  asicii code
			raf.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	boolean mPoweronIsHigh = true;
	int mGpioFd = -1;
	String GPIO_FILE = "/dev/mtgpio";
	int GPIO_IDCARD = 119;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		//开电源代码，如硬件没有设计该功能可以去掉该代码
		mGpioFd = Gpio.GPIO_open(GPIO_FILE);
		if (mGpioFd < 0){
			Log.e(TAG, "  Open GPIO file "+GPIO_FILE+"  failed "+mGpioFd);
		}else{
			//Power on
			if (mPoweronIsHigh)  //Pull GPIO to High will power on
				Gpio.GPIO_pullHigh(mGpioFd, GPIO_IDCARD);
			else   //Pull GPIO to Low will power on
				Gpio.GPIO_pullLow(mGpioFd, GPIO_IDCARD);
		}//开电源结束

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		//this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		Button b1 = (Button) findViewById(R.id.button1);
		b1.setOnClickListener(this);
		Button b2 = (Button) findViewById(R.id.button2);
		b2.setOnClickListener(this);
		Button b3 = (Button) findViewById(R.id.button3);
		b3.setOnClickListener(this);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		Log.e(TAG, "onCreate");
		//setKeyguardEnabled(false);

		InitView();
//		powerOn();
		InitThread();
	};

	String mCard = "";
	private void Display(Bundle bundle)
	{    	TextView v;
		v = (TextView)findViewById(R.id.textView13);
		v.setText("读卡成功次数 " + String.valueOf(Suc_Num));
		v = (TextView)findViewById(R.id.textView14);
		v.setText("读卡失败次数 " + String.valueOf(Fau_Num));
		v = (TextView)findViewById(R.id.textView15);
		v.setText("读卡总次数 " + String.valueOf(Tot_Num));

		String id = (String)bundle.get("IdNo");
		if (mCard.compareToIgnoreCase(id) == 0){
			return;
		}
		mCard = id;


		v = (TextView)findViewById(R.id.textView1);
		v.setText("姓名  " + bundle.get("Name"));
		v = (TextView)findViewById(R.id.textView2);
		v.setText("性别  " + bundle.get("Sex"));
		v = (TextView)findViewById(R.id.textView3);
		v.setText("名族  " + bundle.get("Nation"));
		v = (TextView)findViewById(R.id.textView4);
		v.setText("出生  " + bundle.get("Birth"));
		v = (TextView)findViewById(R.id.textView5);
		v.setText("住址  " + bundle.get("Address"));
		v = (TextView)findViewById(R.id.textView6);
		v.setText("公民身份证号  " + bundle.get("IdNo"));
		v = (TextView)findViewById(R.id.textView7);
		v.setText("签发机关  " + bundle.get("Police"));
		v = (TextView)findViewById(R.id.textView8);
		v.setText("有效期限  " + bundle.get("Validate"));

		v = (TextView)findViewById(R.id.textView9);
		v.setText("读卡成功");



		byte[] ZpData = bundle.getByteArray("Zp");
		if (ZpData == null)
			return;
		try {
			writeBytesToFile(ZpData, getFilesDir()+ "/zp.bmp");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		File f = new File(getFilesDir()+ "/zp.bmp");
		if(f.exists()){
			bm = BitmapFactory.decodeFile(getFilesDir()+ "/zp.bmp");
			ImageView mImageView = (ImageView)findViewById(R.id.imageView1);
			mImageView.setImageBitmap(bm);
		}

		ToneGenerator toneGenerator = new ToneGenerator(
				AudioManager.STREAM_SYSTEM, ToneGenerator.MAX_VOLUME);
		toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP);
	}

	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			TextView v;
			Bundle bundle = msg.getData();
			int iCmd = bundle.getInt("cmd");
			int iCode = bundle.getInt("code");
			switch(iCmd){
				case Tag_Once:
					mCard = "";
					Button b1 = (Button) findViewById(R.id.button1);
					b1.setEnabled(true);
					Button b2 = (Button) findViewById(R.id.button2);
					b2.setEnabled(true);
					break;
				case Err_SamId:
					mCard = "";
					InitView();
					v = (TextView)findViewById(R.id.textView9);
					v.setText("未找到设备,错误码:" + iCode);
					break;
				case Suc_SamId:
					InitView();
					v = (TextView)findViewById(R.id.textView9);
					v.setText("找到设备");
					break;
				case Err_ReadCard:
					mCard = "";
					InitView();
					//l_Info.Caption := '请放卡';
					v = (TextView)findViewById(R.id.textView9);
					v.setText("Err_ReadCard请放卡,错误码:" + iCode);
					break;
				case Suc_ReadCard:
					//l_Info.Caption := '读卡成功，请取卡';
					Display(bundle);
					break;
				case Err_FindCard:
					mCard = "";
					InitView();
					v = (TextView)findViewById(R.id.textView9);
					v.setText("Err_FindCard请放卡,错误码:" + iCode);
					break;
				case Suc_FindCard:
					InitView();
					v = (TextView)findViewById(R.id.textView9);
					v.setText("Suc_FindCard请放卡,错误码:" + iCode);
					break;
			}

			super.handleMessage(msg);
		}
	};

	public class ReadCardThread extends Thread{
		private Handler mHandler = null;
		public ReadCardThread(readCard act, Handler handler) throws SecurityException, NoSuchMethodException, IOException{
			this.mHandler = handler;
		}

		public ReadCardThread(readCard act){

		}

		private void ErrorHandle(int iCmd, int iCode)
		{
			Bundle bundle = new Bundle();
			bundle.putInt("cmd", iCmd);
			bundle.putInt("code", iCode);
			Message msg = new Message();
			msg.setData(bundle);
			mHandler.sendMessage(msg);
		}

		private void SucHandle(	byte[] txt, byte[] wlt, byte[] bmp)
		{
			String sGbk = null;
			try {
				sGbk = new String(txt, "GBK");
				Log.i("sc", sGbk);
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			String sIdInfo[] = sGbk.split("\\|");

			Bundle bundle = new Bundle();
			bundle.putString("Name", sIdInfo[0]);
			bundle.putString("Sex", sIdInfo[1]);
			bundle.putString("Nation", sIdInfo[2]);
			String szTmp = sIdInfo[3].substring(0, 4) + "年"
					+ sIdInfo[3].substring(4, 6) + "月"
					+ sIdInfo[3].substring(6, 8) + "日";
			bundle.putString("Birth", szTmp);
			bundle.putString("Address", sIdInfo[4]);
			bundle.putString("IdNo", sIdInfo[5]);
			bundle.putString("Police", sIdInfo[6]);
			szTmp = sIdInfo[7].substring(0, 4) + "." +
					sIdInfo[7].substring(4, 6) + "." +
					sIdInfo[7].substring(6, 8) + "-";

			if (sIdInfo[7].length() >= 17){
				szTmp = szTmp + sIdInfo[7].substring(9, 13) + "." +
						sIdInfo[7].substring(13, 15) + "." +
						sIdInfo[7].substring(15, 17);
			}

			bundle.putString("Validate", szTmp);
			bundle.putByteArray("Zp", bmp);

			bundle.putInt("cmd", Suc_ReadCard);
			Message msg = new Message();
			msg.setData(bundle);
			mHandler.sendMessage(msg);
		}

		private boolean InitDev()
		{
			if (g_bDeviceOk)
				return true;

			g_szSamid = "";

			//int iError = Termb.InitComm("/dev/ttyHSL1");
			int iError = Termb.InitComm("/dev/ttyMT2");
			//int iError = Termb.InitComm("usb");
			if (iError != 1) {
				ErrorHandle(Err_SamId, iError);
				return false;
			}

			byte[] szSamid = Termb.ReadSamidCmd();
			String sGbk = null;
			try {
				g_szSamid = new String(szSamid, "GBK");
			} catch (UnsupportedEncodingException e1) {
			}

			g_bDeviceOk = true;
			ErrorHandle(Suc_SamId, 0);
			return true;
		}
    	/*
        private void ReadCard()
        {
        	int iRet = Termb.FindCardCmd();
        	if (iRet != 159){//卡已读过，或则未放卡
        		if (iRet == 0){
        			g_bDeviceOk = false;
        			return;
        		}

        	    if (g_iStatus != 1){//循环读卡
        	      if (g_szCardId == "" && g_bReaded){
        	    	  ErrorHandle(Err_FindCard, 0);
        	    	  return ;
        	      }
        	    }
        	}

        	iRet = Termb.SelCardCmd();
        	if (iRet != 144 && iRet != 129){
        		g_bDeviceOk = false;
        		return;
        	}

        	byte[] txt = new byte[256];
        	byte[] wlt = new byte[2048];
        	byte[] bmp = new byte[38862];

        	byte[] finger = new byte[1024];

    	    iRet = Termb.ReadCard(txt, wlt, bmp);//读普通卡
        	//iRet = Termb.ReadCardExt(txt, wlt, bmp);//读指纹卡

        	if (1 != iRet){
        		ErrorHandle(Err_ReadCard, iRet);
          	  	return;
        	}
        	//iRet = Termb.GetFingerData(finger);

        	SucHandle(txt, wlt, bmp);

        	g_bDeviceOk = true;
        	while (!g_bTerminate && 2 == g_iStatus && g_bDeviceOk){
        		SystemClock.sleep(100);
        		iRet = Termb.ReadApp(txt);
        	    if (0 == iRet){
        	    	g_bDeviceOk = false;
        	    	g_bReaded = false;
	        	    return;
        	    }

        	    if (144 != iRet && 145 != iRet) {
        	    	boolean bDeviceOk = g_bDeviceOk;
        	    	ErrorHandle(Err_ReadApp, iRet);
        	    	break;
        	    };
        	}
        }*/

		private void ReadCard()
		{
			int iRet = Termb.FindCardCmd();
			if (iRet != 159 && iRet != 128){
				g_bDeviceOk = false;
				return;
			}
			iRet = Termb.SelCardCmd();
			if (iRet != 144 && iRet != 129){
				g_bDeviceOk = false;
				return;
			}

			byte[] txt = new byte[256];
			byte[] wlt = new byte[2048];
			byte[] bmp = new byte[38862];

			byte[] finger = new byte[1024];

			iRet = Termb.ReadCard(txt, wlt, bmp);//读普通卡
			if (1 != iRet){
				ErrorHandle(Err_ReadCard, iRet);
				Fau_Num = Fau_Num + 1;
				Tot_Num = Tot_Num + 1;
				return;
			}
			Suc_Num = Suc_Num + 1;
			Tot_Num = Tot_Num + 1;
			SucHandle(txt, wlt, bmp);
		}

		public void run(){
			InitDev();

			while (!g_bTerminate){
				g_bDeviceOk = false;
				g_bReaded = false;
				g_bRun = false;
				SystemClock.sleep(50);
				while ((!g_bTerminate) && ((g_iStatus == 2) || (g_iStatus == 1))) {
					g_bRun = true;
					SystemClock.sleep(50);
					if (InitDev())
						ReadCard();
					if (g_iStatus == 1){
						g_iStatus = 0;
						ErrorHandle(Tag_Once, 0);
					}
				}
			}
		}
	}

	@Override
	protected void finalize() throws Throwable {
		// TODO Auto-generated method stub
		Log.e(TAG, "finalize");
		super.finalize();
	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub
		Log.e(TAG, "finish");
		super.finish();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		Log.e(TAG, "onPause");
		super.onPause();
	}

	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		Log.e(TAG, "onRestart");
		super.onRestart();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		Log.e(TAG, "onResume");
		super.onResume();

		/*
		//开安全模块电源
        fd = GtTermb.PowerOpen();
        if(fd<0){
        	Toast.makeText(this,"open device faile!",Toast.LENGTH_LONG).show();
        }else{
        	//Toast.makeText(this,"open device success!",Toast.LENGTH_LONG).show();
        	GtTermb.PowerIoCtl(1,1);
        }	*/
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		Log.e(TAG, "onStart");
		super.onStart();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		Log.e(TAG, "onStop");
		super.onStop();
		//关安全模块电源
		//GtTermb.PowerClose();		
	}

	@Override
	protected void onDestroy() {
		Log.e(TAG, "onDestroy");
//		powerOff();

		Gpio.GPIO_pullLow(mGpioFd, GPIO_IDCARD);

		super.onDestroy();
		//System.exit(0);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.option_menu, menu);
		return true;
	}



	@Override
	public synchronized boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		}
		return false;

	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		Log.e(TAG, "onBackPressed");
		//System.exit(0);
		super.onBackPressed();
	}

	private static byte[] ReadBytesFromFile(String pathAndNameString) throws IOException {
		File file = null;
		try {
			//if (android.os.Environment.getExternalStorageState().
			//		equals(android.os.Environment.MEDIA_MOUNTED)){

			file = new File(pathAndNameString);
			if (!file.exists()) {
				return null;
			}
			FileInputStream fis = new FileInputStream(file);
			byte[] buffer = new byte[38862];
			int iLen = fis.read(buffer);
			file = null;
			return buffer;
			//}
		} catch (IOException e) {
			return null;
		}
	}

	private File writeBytesToFile(byte[] inByte, String pathAndNameString) throws IOException {
		File file = null;
		try {
			//if (android.os.Environment.getExternalStorageState().
			//		equals(android.os.Environment.MEDIA_MOUNTED)){

			file = new File(pathAndNameString);
			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}
			file.delete();
			if(!file.exists()) {
				file.createNewFile();
			}
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(inByte);
			fos.close();
			//}
		} catch (IOException e) {
			//Log.w(TAG, "createNewFile IOException :" + e.toString());
			//throw new IOException("Could not completely write file "+file.getName());
			return null;
		}

		return file;
	}
}

