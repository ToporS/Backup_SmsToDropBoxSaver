package ru.deader.smstodropboxsaver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.dropbox.*;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AppKeyPair;

public class MainActivity extends Activity implements OnClickListener
{
	final static private String APP_KEY = "c5v7niap7bztqqz";
	final static private String APP_SECRET = "c5v7niap7bztqqz";
	private static final String TAG = "SmsToDropBoxSaver";
	
	Button btnConnect;
	
	private DropboxAPI<AndroidAuthSession> mDBApi;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
	//	AppKeyPair appKeys = new AppKeyPair(APP_KEY, APP_SECRET);
		//AndroidAuthSession session = new AndroidAuthSession(appKeys);
	//	mDBApi = new DropboxAPI<AndroidAuthSession>(session);
		setContentView(R.layout.activity_main);
		btnConnect = (Button) findViewById(R.id.GoButton);
		btnConnect.setOnClickListener(this);
		
		
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onResume()
	{
		// TODO Auto-generated method stub
		super.onResume();
		Log.d(TAG, "Inside onResume");
		// if (mDBApi.getSession().authenticationSuccessful()) {
		//        try {
		            // Required to complete auth, sets the access token on the session
		        //    mDBApi.getSession().finishAuthentication();

		        //    String accessToken = mDBApi.getSession().getOAuth2AccessToken();
		     //   } catch (IllegalStateException e) {
		      //      Log.d(TAG, "Error authenticating", e);
		     //   }
		//    }
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings)
		{
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void onClick(View v)
	{
		Log.d(TAG, "Inside onClick");
		
		/*File file = new File("working-draft.txt");
		FileInputStream inputStream = null;
		try
		{
			inputStream = new FileInputStream(file);
		} catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Entry response = null;
		try
		{
			response = mDBApi.putFile("/magnum-opus.txt", inputStream,
			                                file.length(), null, null);
		} catch (DropboxException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.d(TAG, "The uploaded file's rev is: " + response.rev);*/
	}
}
