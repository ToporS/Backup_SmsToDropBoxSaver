package ru.deader.smstodropboxsaver;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;
import ru.deader.smstodropboxsaver.ParsedMessage;;

public class SmsReceiver extends BroadcastReceiver
{

	public static final String SMS_EXTRA_NAME ="pdus";
	private static final String TAG = "SmsToDropBoxSaver";
	
	protected BlockingQueue<ServiceMessage> queue =new ArrayBlockingQueue<ServiceMessage>(128);;
	
	private ParsedMessage pm = null;
	private WorkerThread  worker = null;
	private String telNum;
	
	String body = "";
	
	@Override
	public void onReceive(Context context, Intent intent)
	{
		 // Get the SMS map from Intent
        Bundle extras = intent.getExtras();
         
        if ( extras != null )
        {
            // Get received SMS array
            Object[] smsExtra = (Object[]) extras.get( SMS_EXTRA_NAME );
            
            for ( int i = 0; i < smsExtra.length; ++i )
            {
                SmsMessage sms = SmsMessage.createFromPdu((byte[])smsExtra[i]);
               telNum =  sms.getOriginatingAddress();
                body = body + sms.getMessageBody().toString();
               
            }
            
            
          /*  Cursor contactsCursor = context.getContentResolver()
	                .query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);*/
            
            
            
            if (isSDCardReady())
            {
            	//Pattern p = Pattern.compile("[0-9]{10,12}\\sPMP"); old wrong
            	Pattern p = Pattern.compile("PMP;");
            	Matcher m = p.matcher(body);
            	
            	if (m.find()) 
            	{
            		worker = new WorkerThread(queue,context);
            		try
            		{
            			if (!worker.isAlive())
            				{
            					worker.start();
            				}
            			ServiceMessage msg = new ServiceMessage(body, telNum);
            			queue.put(msg);
            			Log.d(TAG, "All elements are queued....");
            		} catch (Exception e)
            			{
            				// TODO Auto-generated catch block
            				System.out.println("Error! Message was not queued");
            				e.printStackTrace();
            			}
            	//pm = new ParsedMessage(body);
            	this.abortBroadcast();
            	}
            	 else 
                 {
                 	Log.d(TAG, "Message not passed regex check... Going ahead");
                 }
            }
            else Toast.makeText( context, "SD CARD NOT READY! Message LOST!", Toast.LENGTH_LONG ).show();
        }
	}

	private boolean isSDCardReady()
	{
		String state = Environment.getExternalStorageState();
	//	if (state.equalsIgnoreCase(Environment.MEDIA_MOUNTED)) return true; else return false
	return	(state.equalsIgnoreCase(Environment.MEDIA_MOUNTED)) ? true  : false;
	}

}
