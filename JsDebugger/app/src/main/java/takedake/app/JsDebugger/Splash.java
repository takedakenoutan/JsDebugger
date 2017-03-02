package takedake.app.JsDebugger;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.view.animation.*;
import android.view.*;

public class Splash extends Activity
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);
		
		ImageView splashImage = (ImageView)findViewById(R.id.splashImage);
		AlphaAnimation alpha = new AlphaAnimation(0, 1);
		alpha.setDuration(2700);
		splashImage.startAnimation(alpha);
		
		Handler handler = new Handler();
		handler.postDelayed(new Runnable(){
			@Override
			public void run(){
				finish();
			}
		}, 3 * 1000);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if(keyCode == KeyEvent.KEYCODE_BACK){
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
