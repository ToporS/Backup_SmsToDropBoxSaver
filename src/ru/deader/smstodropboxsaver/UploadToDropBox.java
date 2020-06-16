package ru.deader.smstodropboxsaver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import android.content.Context;
import android.os.Environment;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;

public class UploadToDropBox
{
	private DropboxAPI<?> dropbox;
	private Context context;
	private static final String fileName = "Report.xls";
	private static final String AccessToken ="CAKk61d1CGAAAAAAAAAAHB7kGnrDm_AH2FV0E7QJjuSaTStAYs4IvVOh6mDp-uF8"; //"UtjxEd5T1FMAAAAAAAAAQhbhKaOg5X0mVCHQNRXR4vBtg4ykFuCmyXvujBKG-8b1" - Misha;
	final static public String ACCESS_KEY = "bswm2gkklfac71c";
	final static public String ACCESS_SECRET = "4dfbv5qc2bz88h4";//App secret
	

	final static public AccessType ACCESS_TYPE = AccessType.DROPBOX;
	private AndroidAuthSession session;
	private String accessSecret, accessKey;
	private AccessTokenPair myAccessToken;
	private static final String TAG = "SmsToDropBoxSaver";
	private Entry dbx = null;
	
	public void upload()
	{
		AppKeyPair appKeys = new AppKeyPair(ACCESS_KEY, ACCESS_SECRET);
		session = new AndroidAuthSession(appKeys, ACCESS_TYPE);
		session.setOAuth2AccessToken(AccessToken);
		dropbox = new DropboxAPI<AndroidAuthSession>(session);
		File directory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/smsreports/");
		File fileToUpload = new File(directory, fileName);
		try {
			FileInputStream fileInputStream = new FileInputStream(fileToUpload);
			dropbox.putFileOverwrite("/Report.xls", fileInputStream, fileToUpload.length(), null);
			fileInputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (DropboxException e) {
			e.printStackTrace();
		}
	}
}
