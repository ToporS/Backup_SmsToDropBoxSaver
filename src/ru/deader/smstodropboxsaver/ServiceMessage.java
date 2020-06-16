package ru.deader.smstodropboxsaver;

public class ServiceMessage
{
	private String msgBody;
	private String telNum;
	
	public ServiceMessage(String msgBody, String telNum)
	{
		super();
		this.msgBody = msgBody;
		this.telNum = telNum;
	}
	public String getMsgBody()
	{
		return msgBody;
	}
	public void setMsgBody(String msgBody)
	{
		this.msgBody = msgBody;
	}
	public String getTelNum()
	{
		return telNum;
	}
	public void setTelNum(String telNum)
	{
		this.telNum = telNum;
	}
	
	
}
