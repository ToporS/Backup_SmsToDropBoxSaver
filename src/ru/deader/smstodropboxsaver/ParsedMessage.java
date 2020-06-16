package ru.deader.smstodropboxsaver;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/*
 * 
 *    Cправка по СМС сообщению
   M:М1/М2/М3;W:W1/W2;WL:WL0:WL1/WL2/WL3;Mb:Mb1/Mb2;Mc:Mc1/Mc2;",
  M1 - Общий счётчик денежных средств.
  M2 - Обнуляемый счётчик денежных средств.
  M3 - кол-во сдачи
  W1 - Общий счётчик проданной волы.
  W2 - Дневной счётчик проданной воды.
  WL0 - примерный уровень воды
  WL1 - Уровень воды ниже нижнего
  WL2 - Уровень воды ниже среднего
  WL3 - Уровень воды ниже верхнего
  Mb1 - денег в купюроприемнике
  Mb2 - кол-во купюр в купюроприемнике
  Mc1 - денег в ящике монетника
  Mc2 - кол-во монет в ящике монетника
  
  COIN - монетоприемник
  BILL - купюроприемник
  LEVEL - уровень 
  РАМР - не считает импульсы
 * 
 * */

public class ParsedMessage
{
	private static final String TAG = "SmsToDropBoxSaver";
	private String telNum;
	private String state;
	private String error;
	private Long totalMoney;
	private Long moneyNow;
	private Long rest;
	private Long totalWater;
	private Long todayWater;
	private Long waterLevel;
	private Boolean lowWater;
	private Boolean midWater;
	private Boolean highWater;
	private Long billSum;
	private Long billCount;
	private Long coinSum;
	private Long coinCount;
	private Date date;
	private Boolean inError;
	
	
	public ParsedMessage(){}
	
	public ParsedMessage(ServiceMessage serviceMessage)
	{
		int count = 0;
		String [] result = serviceMessage.getMsgBody().split(";");		
		//String[] strTelNum = result[0].split(" ");
		setTelNum(serviceMessage.getTelNum());
		String [] strState = result[1].split(":");
		
		setState(strState[1]);
		if (strState.length == 3) setError(strState[2]);
		if (strState.length == 4) setError(strState[3]);
		String[] moneyToken = result[2].split(":");
		String[] monTok = moneyToken[1].split("/");
		setTotalMoney(Long.parseLong(monTok[0]));
		setMoneyNow(Long.parseLong(monTok[1]));
		setRest(Long.parseLong(monTok[2]));
		
		String[] waterToken = result[3].split(":");
		String[] watTok = waterToken[1].split("/");
		setTotalWater(Long.parseLong(watTok[0]));
		setTodayWater(Long.parseLong(watTok[1]));
		
		String[] waterrToken = result[4].split(":");
		setWaterLevel(Long.parseLong(waterrToken[1]));
		String[] wattTok = waterrToken[2].split("/");
		if (wattTok[0].equalsIgnoreCase("0"))setLowWater(false); else setLowWater(true);
		if (wattTok[1].equalsIgnoreCase("0"))setMidWater(false); else setMidWater(true);
		if (wattTok[2].equalsIgnoreCase("0"))setHighWater(false); else setHighWater(true);
		
		String[] billTokens = result[5].split(":");
		String[] billTok = billTokens[1].split("/");
		setBillSum(Long.parseLong(billTok[0]));
		setBillCount(Long.parseLong(billTok[1]));
		
		String[] coinTokens = result[6].split(":");
		String[] coinTok = coinTokens[1].split("/");
		setCoinSum(Long.parseLong(coinTok[0]));
		setCoinCount(Long.parseLong(coinTok[1]));
		if (!this.error.equalsIgnoreCase("OK")) this.inError = Boolean.valueOf(true); else this.inError = Boolean.valueOf(false);
		
	}
		
	@Override
	public String toString()  //    telNum=" + telNum + ", 
	{
		return "ParsedMessage [Date=" + date + ", telNum=" + telNum + ", state=" + state
				+ ", error=" + error + ", totalMoney=" + totalMoney
				+ ", moneyNow=" + moneyNow + ", rest=" + rest + ", totalWater="
				+ totalWater + ", todayWater=" + todayWater + ", waterLevel="
				+ waterLevel + ", lowWater=" + lowWater + ", midWater="
				+ midWater + ", highWater=" + highWater + ", billSum="
				+ billSum + ", billCount=" + billCount + ", coinSum=" + coinSum
				+ ", coinCount=" + coinCount + "]";
	}

	public String getTelNum()
	{
		return telNum;
	}
	public void setTelNum(String string)
	{
		this.telNum = string;
	}
	public String getState()
	{
		return state;
	}
	public void setState(String state)
	{
		this.state = state;
	}
	public String getError()
	{
		return error;
	}
	public void setError(String error)
	{
		this.error = error;
	}
	public Long getTotalMoney()
	{
		return totalMoney;
	}
	public void setTotalMoney(Long totalMoney)
	{
		this.totalMoney = totalMoney;
	}
	public Long getMoneyNow()
	{
		return moneyNow;
	}
	public void setMoneyNow(Long moneyNow)
	{
		this.moneyNow = moneyNow;
	}
	public Long getRest()
	{
		return rest;
	}
	public void setRest(Long rest)
	{
		this.rest = rest;
	}
	public Long getWaterLevel()
	{
		return waterLevel;
	}
	public void setWaterLevel(Long waterLevel)
	{
		this.waterLevel = waterLevel;
	}
	public Boolean isLowWater()
	{
		return lowWater;
	}
	public void setLowWater(Boolean lowWater)
	{
		this.lowWater = lowWater;
	}
	public Boolean isMidWater()
	{
		return midWater;
	}
	public void setMidWater(Boolean midWater)
	{
		this.midWater = midWater;
	}
	public Boolean isHighWater()
	{
		return highWater;
	}
	public void setHighWater(Boolean highWater)
	{
		this.highWater = highWater;
	}
	public Long getBillSum()
	{
		return billSum;
	}
	public void setBillSum(Long billSum)
	{
		this.billSum = billSum;
	}
	public Long getBillCount()
	{
		return billCount;
	}
	public void setBillCount(Long billCount)
	{
		this.billCount = billCount;
	}
	public Long getCoinSum()
	{
		return coinSum;
	}
	public void setCoinSum(Long coinSum)
	{
		this.coinSum = coinSum;
	}
	public Long getCoinCount()
	{
		return coinCount;
	}
	public void setCoinCount(Long coinCount)
	{
		this.coinCount = coinCount;
	}
	
	public Long getTotalWater()
	{
		return totalWater;
	}

	public void setTotalWater(Long totalWater)
	{
		this.totalWater = totalWater;
	}

	public Long getTodayWater()
	{
		return todayWater;
	}

	public void setTodayWater(Long todayWater)
	{
		this.todayWater = todayWater;
	}
	
	//@SuppressLint("SimpleDateFormat")
	public String getDate()
	{
		//SimpleDateFormat sdf = new SimpleDateFormat("HH:mm \n dd.MM.yyyy",Locale.getDefault());// GregorianCalendar.getInstance()// new SimpleDateFormat("HH:mm dd.MM.yyyy");
	//	SimpleDateFormat sdf = new SimpleDateFormat("HH:mm \n dd.MM.yyyy");
		DateFormat df = SimpleDateFormat.getDateTimeInstance();
		Date today = new Date();
		String formattedDate = df.format(today);
		return formattedDate;
	}

	public void setDate(Date date)
	{
		this.date = date;
	}
	public Boolean getInError()
	{
		return inError;
	}

	public void setInError(Boolean inError)
	{
		this.inError = inError;
	}
	
}
