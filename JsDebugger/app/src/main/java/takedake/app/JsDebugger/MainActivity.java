package takedake.app.JsDebugger;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.webkit.JavascriptInterface;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.KeyEvent;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.content.Context;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import android.graphics.Typeface;
import android.content.DialogInterface;
import android.content.ClipboardManager;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Intent;
import android.support.v4.app.ShareCompat;
import java.util.concurrent.TimeUnit;
import java.net.URLDecoder;

public class MainActivity extends Activity 
{
	private WebView myWeb;
	private String viewState;
	private logFile logs=new logFile(this);
	private favFile favs=new favFile(this);
	private codeShare share=new codeShare(this);
	private intentReceiver receiver=new intentReceiver(this);
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
		
		setMain();
		
		Intent splash = new Intent();
		splash.setClassName("takedake.app.JsDebugger", "takedake.app.JsDebugger.Splash");
		startActivity(splash);
		
		final Handler handler=new Handler();
		
		myWeb = (WebView)findViewById(R.id.JeDebug);
		myWeb.setWebViewClient(new WebViewClient());
		myWeb.setWebChromeClient(new WebChromeClient());
		myWeb.loadUrl("file:///android_asset/JsDebugger/index.html");
		myWeb.getSettings().setJavaScriptEnabled(true);
		myWeb.addJavascriptInterface(new JsFunction(this, logs, favs, share, receiver), "android");
		
		Intent intent = getIntent();
		String action = intent.getAction();
		if (action.equals(Intent.ACTION_SEND)){
			new Thread(new Runnable(){
				@Override
				public void run(){
					Bundle extras = getIntent().getExtras();
					if(extras!=null) {
						final String ext = extras.getString(Intent.EXTRA_TEXT);
						try{
							TimeUnit.SECONDS.sleep(3);
						}
						catch (InterruptedException e){
							e.printStackTrace();
						}
						handler.post(new Runnable(){
							@Override
							public void run(){
								receiver.Save(ext);
								myWeb.loadUrl("javascript:_takedake_app_JsDebugger.receive();");
							}
						});
					}
				}
			}).start();
		}
		if (action.equals(Intent.ACTION_VIEW)){
			String pathCache = "";
			try{
				pathCache = URLDecoder.decode((intent.getDataString().split("//"))[1], "UTF-8");
			}
			catch (UnsupportedEncodingException e){
				e.printStackTrace();
			}
			final String path = pathCache;
			new Thread(new Runnable(){
				@Override
				public void run(){
					String text ="";
					try{
						BufferedReader reader = new BufferedReader( new InputStreamReader( new FileInputStream(path) , "UTF-8") );
						String tmp;
						while( (tmp = reader.readLine()) != null ){
							text = text + tmp + "\n";
						}
						reader.close();
					}catch(IOException e){
						e.printStackTrace();
					}
					try{
						TimeUnit.SECONDS.sleep(3);
					}
					catch (InterruptedException e){
						e.printStackTrace();
					}
					final String str = text;
					handler.post(new Runnable(){
						@Override
						public void run(){
							receiver.Save(str);
							myWeb.loadUrl("javascript:_takedake_app_JsDebugger.receive();");
						}
					});
				}
			}).start();
		}
    }
	@Override 
	public boolean onCreateOptionsMenu(Menu menu){
		getMenuInflater().inflate(R.menu.main, menu);
		return true; 
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		super.onOptionsItemSelected(item);
		if(viewState == "Debugger"){
			switch(item.getItemId()){
				case R.id.Debugger:
					myWeb.loadUrl("file:///android_asset/JsDebugger/index.html");
					viewState="Debugger";
				break;
				case R.id.Log:
					setSub();
					logWrite(logs.Load());
				break;
				case R.id.Fav:
					setFav();
					favWrite();
					TextView text=(TextView)findViewById(R.id.codeView);
					Typeface font=Typeface.createFromAsset(getAssets(), "font/AnonymousPro-Regular.ttf");
					text.setTypeface(font);
				break;
				case R.id.Share:
					myWeb.loadUrl("javascript:_takedake_app_JsDebugger.sha();");
				break;
				case R.id.help:
					myWeb.loadUrl("file:///android_asset/Help/index.html");
					viewState="help";
				break;
			}
		}else if(viewState != "Debugger"){
			switch(item.getItemId()){
				case R.id.Debugger:
					setMain();
					setWeb(this, "file:///android_asset/JsDebugger/index.html");
				break;
				case R.id.Log:
					setSub();
					logWrite(logs.Load());
				break;
				case R.id.Fav:
					setFav();
					favWrite();
					TextView text=(TextView)findViewById(R.id.codeView);
					Typeface font=Typeface.createFromAsset(getAssets(), "font/AnonymousPro-Regular.ttf");
					text.setTypeface(font);
				break;
				case R.id.Share:
					if(viewState=="Favorite"){
						codes();
					}else{
						Toast.makeText(this, "FavoriteかDebuggerに移動してください", Toast.LENGTH_LONG).show();
					}
				break;
				case R.id.help:
					setMain();
					setWeb(this, "file:///android_asset/Help/index.html");
					viewState="help";
				break;
			}
			logs.Delete();
		}else if(viewState == "Favorite"){
		}
		return true;
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if(keyCode == KeyEvent.KEYCODE_BACK && viewState == "Debugger"){
			return true;
		}else if(keyCode == KeyEvent.KEYCODE_BACK && viewState != "Debugger"){
			setMain();
			setWeb(this, "file:///android_asset/JsDebugger/index.html");
			logs.Delete();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	public void setMain(){
		setContentView(R.layout.main);
		viewState="Debugger";
	}
	public void setSub(){
		setContentView(R.layout.sub);
		viewState="Log";
	}
	public void setFav(){
		setContentView(R.layout.fav);
		viewState="Favorite";
	}
	public void setWeb(Activity self, String url){
		myWeb = (WebView)findViewById(R.id.JeDebug);
		myWeb.setWebViewClient(new WebViewClient());
		myWeb.setWebChromeClient(new WebChromeClient());
		myWeb.loadUrl(url);
		myWeb.getSettings().setJavaScriptEnabled(true);
		myWeb.addJavascriptInterface(new JsFunction(this, logs,favs, share, receiver), "android");
	}
	public void logWrite(String logStr){
		TextView text = (TextView)findViewById(R.id.logView);
		text.setText(logStr);
	}
	public void favWrite(){
		String[] arr=this.fileList();
		String[] _item;
		if(Arrays.asList(arr).contains("Log.txt")){
			List<String> temp=new ArrayList<>(Arrays.asList(arr));
			temp.remove("Log.txt");
			_item=temp.toArray(new String[0]);
		}else{
			_item=arr;
		}
		String[] item;
		if(Arrays.asList(_item).contains("cache.txt")){
			List<String> temp=new ArrayList<>(Arrays.asList(_item));
			temp.remove("cache.txt");
			item=temp.toArray(new String[0]);
		}else{
			item=_item;
		}
		ArrayAdapter adapter;
		if(item.length!=0){
			adapter =new ArrayAdapter(this, android.R.layout.simple_spinner_item, item);
		}else{
			adapter =new ArrayAdapter(this, android.R.layout.simple_spinner_item);
			adapter.add("None");
		}
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		Spinner titleName=(Spinner)findViewById(R.id.titleName);
		titleName.setAdapter(adapter);
		titleName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
			
			@Override
			public void onItemSelected(AdapterView obj, View view, int n, long m){
				String selectItem;
				selectItem=(obj.getSelectedItem()).toString();
				if(selectItem!="None"){
					String code=favs.Load(selectItem);
					String _code=code.replaceAll("\t", "    ");
					TextView text=(TextView)findViewById(R.id.codeView);
					text.setText(_code);
				}
			}
			@Override
			public void onNothingSelected(AdapterView parent){
				
			}
		});
	}
	public void Del(View view){
		Spinner titleName=(Spinner)findViewById(R.id.titleName);
		String item=(titleName.getSelectedItem()).toString();
		if(item!="None"){
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("!!!Delete!!!");
			builder.setMessage("本当に消していいですか？");
			builder.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dia, int n){
					Spinner titleName=(Spinner)findViewById(R.id.titleName);
					String item=(titleName.getSelectedItem()).toString();
					TextView text=(TextView)findViewById(R.id.codeView);
					if(item!="None"){
						favs.Delete(item);
						favWrite();
						Spinner _titleName=(Spinner)findViewById(R.id.titleName);
						String _item=(_titleName.getSelectedItem()).toString();
						if(_item=="None"){
							text.setText("code");
						}else{
							String code=favs.Load(_item);
							text.setText(code);
						}
					}
				}
			});
			builder.setNegativeButton("No", new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dia, int n){
					
				}
			});
			builder.create().show();
		}
	}
	public void Copy(View view){
		Spinner titleName=(Spinner)findViewById(R.id.titleName);
		String item=(titleName.getSelectedItem()).toString();
		String code=favs.Load(item);
		ClipData.Item txt=new ClipData.Item(code);
		String[] mimeType=new String[1];
		mimeType[0]=ClipDescription.MIMETYPE_TEXT_PLAIN;
		ClipData cd = new ClipData(new ClipDescription("text_data", mimeType), txt);
		ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
		cm.setPrimaryClip(cd);
	}
	public void intentDebugger(View view){
		Spinner titleName = (Spinner)findViewById(R.id.titleName);
		String item=(titleName.getSelectedItem()).toString();
		final String code=favs.Load(item);
		setMain();
		setWeb(this, "file:///android_asset/JsDebugger/index.html");
		final Handler handler=new Handler();
		new Thread(new Runnable(){
				@Override
				public void run(){
					try{
						TimeUnit.MILLISECONDS.sleep(500);
					}
					catch (InterruptedException e){
						e.printStackTrace();
					}
					handler.post(new Runnable(){
						@Override
						public void run(){
							receiver.Save(code);
							myWeb.loadUrl("javascript:_takedake_app_JsDebugger.receive();");
						}
					});
				}
			}).start();
		
	}
	public void codes(){
		String shareTitle="";
		String shareText ="";
		Spinner titleName=(Spinner)findViewById(R.id.titleName);
		shareTitle=(titleName.getSelectedItem()).toString();
		shareText=favs.Load(shareTitle);
		share.call(shareTitle + "\n\n" + shareText);
	}
}

class JsFunction{
	private Context ctx;
	private logFile logs;
	private favFile favs;
	private codeShare share;
	private intentReceiver receiver;
	
	public JsFunction(Context c, logFile logObj, favFile favObj, codeShare codeObj, intentReceiver receiveObj){
		ctx=c;
		logs=logObj;
		favs=favObj;
		share=codeObj;
		receiver=receiveObj;
	}
	@JavascriptInterface
	public void addLog(String message){
		logs.Save(message);
	}
	@JavascriptInterface
	public void favorite(String title, String str){
		favs.Save(title, str);
	}
	@JavascriptInterface
	public void JsShare(String text){
		share.call(text);
	}
	@JavascriptInterface
	public String Sender(){
		return receiver.Load();
	}
}

class logFile{
	private Context ctx;
	public logFile(Context c){
		ctx=c;
	}
	public void Save(String str){
		try{
			FileOutputStream log;
			log = ctx.openFileOutput("Log.txt", ctx.MODE_APPEND);
			log.write((str+"\n").getBytes());
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	public String Load(){
		String str = "";
		try{
			FileInputStream log;
			log=ctx.openFileInput("Log.txt");
			BufferedReader reader = new BufferedReader( new InputStreamReader( log , "UTF-8") );
			String tmp;
			while( (tmp = reader.readLine()) != null ){
				str = str + tmp + "\n";
			}
			reader.close();
		}catch(IOException e){
			e.printStackTrace();
		}
		return str;
	}
	public void Delete(){
		ctx.deleteFile("Log.txt");
	}
}
class favFile{
	private Context ctx;
	public favFile(Context c){
		ctx=c;
	}
	public void Save(String title, String str){
		if(title.length()>=1){
			String[] arr=ctx.fileList();
			if(!(Arrays.asList(arr).contains(title))){
				try{
					FileOutputStream fav;
					fav = ctx.openFileOutput(title,ctx.MODE_PRIVATE);
					fav.write(str.getBytes());
				}catch(IOException e){
					e.printStackTrace();
				}
			}else{
				Toast.makeText(ctx, "!!!同じ名前では保存できません!!!", Toast.LENGTH_LONG).show();
			}
		}else{
			Toast.makeText(ctx, "!!!名前を入力してください!!!", Toast.LENGTH_LONG).show();
		}
	}
	public String Load(String title){
		String str="";
		try{
			FileInputStream fav;
			fav=ctx.openFileInput(title);
			BufferedReader reader = new BufferedReader( new InputStreamReader( fav , "UTF-8") );
			String tmp;
			while( (tmp = reader.readLine()) != null ){
				str = str + tmp + "\n";
			}
			reader.close();
		}catch(IOException e){
			e.printStackTrace();
		}
		return str;
	}
	public void Delete(String title){
		ctx.deleteFile(title);
	}
}
class codeShare{
	private Activity act;
	public codeShare(Activity a){
		act=a;
	}
	public void call(String text){
		ShareCompat.IntentBuilder builder=ShareCompat.IntentBuilder.from(act);
		builder.setChooserTitle("Choose Share App.");
		builder.setSubject("Code by JsDebugger");
		builder.setText(text);
		builder.setType("text/plain");
		builder.startChooser();
	}
}
class intentReceiver{
	private Context ctx;
	public intentReceiver(Context c){
		ctx=c;
	}
	public void Save(String str){
		try{
			
			FileOutputStream cache;
			cache = ctx.openFileOutput("cache.txt", ctx.MODE_PRIVATE);
			cache.write((str).getBytes());
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	public String Load(){
		String str = "";
		try{
			FileInputStream cache;
			cache=ctx.openFileInput("cache.txt");
			BufferedReader reader = new BufferedReader( new InputStreamReader( cache , "UTF-8") );
			String tmp;
			while( (tmp = reader.readLine()) != null ){
				str = str + tmp + "\n";
			}
			reader.close();
		}catch(IOException e){
			e.printStackTrace();
		}
		return str;
	}
}
