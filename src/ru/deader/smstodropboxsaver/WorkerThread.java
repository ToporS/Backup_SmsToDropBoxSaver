package ru.deader.smstodropboxsaver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;

import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import android.content.Context;
import android.database.Cursor;
import android.os.Environment;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.util.Log;

public class WorkerThread extends Thread
{
	 protected BlockingQueue<ServiceMessage> queue = null;
	 private ParsedMessage parsMess = null;
	 private static final String TAG = "SmsToDropBoxSaver";
	 private static final String fileName = "Report.xls";
	 private LocalFileUpdater excellUpdater = null;
	 private UploadToDropBox uploader = null;
	 private SmsManager smsManager = null;  
	 private Context context = null;
	 
	// private static final String AccessToken = "CAKk61d1CGAAAAAAAAAAGKHrRlmS-0sMrJFtOpsjeUPI5L2mLrggAWyuTWWjoTrp";

	    public WorkerThread(BlockingQueue<ServiceMessage> queue, Context context) {
	        this.queue = queue;
	        this.context = context;
	    }

	
	
	@Override
	public void run()
	{
		
		
       while(!Thread.interrupted()) //Проверка прерывания. Класс Thread содержит в себе скрытое булево поле - флаг прерывания. 
									//Установить этот флаг можно вызвав метод interrupt() потока.
		{
			try
			{
				parsMess = new ParsedMessage((ServiceMessage)queue.take());
				if (parsMess.getInError()) sendMsgToOperator(parsMess);   //WORKING!!!
				excellUpdater = new LocalFileUpdater(parsMess,context);
				excellUpdater.update();
				uploader = new UploadToDropBox();
				uploader.upload();
			} catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	}



	private void sendMsgToOperator(ParsedMessage pm)
	{
		smsManager = SmsManager.getDefault();
		Log.d(TAG, "trying with contacts");
		Cursor contactsCursor = context.getContentResolver()
                .query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
		 if (contactsCursor != null && contactsCursor.getCount() > 0) {
			 while (contactsCursor.moveToNext())
			 {				 
				 String contactName = contactsCursor.getString(contactsCursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
				 //Log.d(TAG, "Contact Name: "+ contactName);
				 if (contactName.equalsIgnoreCase("VendServNum"))
				 {
					 String telNum = contactsCursor.getString(contactsCursor.getColumnIndex(ContactsContract.Data.HAS_PHONE_NUMBER));
					 String contactId = contactsCursor.getString(contactsCursor.getColumnIndex(ContactsContract.Contacts._ID));
					 Cursor phones = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ contactId, null,null);
					 phones.moveToNext();
					 String phoneNumber = phones.getString(phones.getColumnIndex(
                             ContactsContract.CommonDataKinds.Phone.NUMBER));
					 Log.d(TAG, "TelNum: "+phoneNumber);
					 smsManager = SmsManager.getDefault();
					 String msgText = pm.getTelNum() + " has error state: " + pm.getError() ;
					 smsManager.sendTextMessage(phoneNumber, null, msgText, null, null);
					 phones.close();
				 }
			 }
		   }
		 contactsCursor.close();
		
	}


}
