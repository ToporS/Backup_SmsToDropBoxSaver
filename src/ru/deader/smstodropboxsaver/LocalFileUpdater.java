package ru.deader.smstodropboxsaver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFHyperlink;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.format.CellFormat;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.util.Log;

public class LocalFileUpdater
{
	public static final int DATE_COLUMN_NUMBER = 0;
	public static final int ALIAS_COLUMN_NUMBER = 1;
	public static final int TELNUM_COLUMN_NUMBER = 2;
	public static final int ADRESS_COLUMN_NUMBER = 3;
	public static final int STATUS_COLUMN_NUMBER = 4;
	public static final int ERROR_COLUMN_NUMBER = 5;
	public static final int TOTALMONEY_COLUMN_NUMBER = 6;
	public static final int MONEYNOW_COLUMN_NUMBER = 7;
	public static final int REST_COLUMN_NUMBER = 8;
	public static final int TOTALWATER_COLUMN_NUMBER = 9;
	public static final int TODAYWATER_COLUMN_NUMBER = 10;
	public static final int WATERREST_COLUMN_NUMBER = 11;
	public static final int LOWWATER_COLUMN_NUMBER = 12;
	public static final int MIDWATER_COLUMN_NUMBER = 13;
	public static final int HIGHWATER_COLUMN_NUMBER = 14;
	public static final int BILLSUM_COLUMN_NUMBER = 15;
	public static final int BILLCOUNT_COLUMN_NUMBER = 16;
	public static final int COINSUM_COLUMN_NUMBER = 17;
	public static final int COINCOUNT_COLUMN_NUMBER = 18;
	private static final String TAG = "SmsToDropBoxSaver";
	
	ParsedMessage pm = null;
	
	private Row newRow = null;
	private Cell newCell = null;
	private HSSFWorkbook sourceWorkBook = null;
	private HSSFSheet sourceSheet = null;
	private HSSFSheet unitSheet = null;
	private HSSFSheet summarySheet = null;
	private HSSFRow destRow = null;
	private HSSFRow srcRow = null;
	private Context context = null;
	private FileInputStream myInput;
	private FileOutputStream myOutput;
	private String alias = null;
	private String adress=null;
	
	
	private static final String fileName = "Report.xls";
	private static final String tmpFileName = "Report.tmp";

	public LocalFileUpdater(ParsedMessage pm, Context context)
	{
		super();
		this.pm = pm;
		this.context = context;
	}
	
	public void update()
	{
		File extdir = Environment.getExternalStorageDirectory();
		Log.d(TAG, "Path: " + extdir.getAbsolutePath());
		File directory = new File(extdir.getAbsolutePath() + "/smsreports/");
		if(!directory.isDirectory()) directory.mkdirs();  //create directory if not exist
		File inputfile = new File(directory, fileName);	
		Log.d(TAG, "Input file Path: " + inputfile.getAbsolutePath());
		File outputfile = new File(directory, tmpFileName);	
		Log.d(TAG, "Output file Path: " + outputfile.getAbsolutePath() );
		if(!inputfile.exists())
			try
			{
				Log.d(TAG, "Creating new file");
				HSSFWorkbook wb = new HSSFWorkbook();
				FileOutputStream fileOut = new FileOutputStream(inputfile);
				wb.write(fileOut);
				fileOut.close();
				fillNewFile(inputfile, outputfile, extdir);
			} catch (IOException e1)
			{
				e1.printStackTrace();
			}
		
		Log.d(TAG, "OpeningFile...");
		try
		{			
			alias = getAlias();
			adress = getAdress();
			myInput = new FileInputStream(inputfile);
			myOutput = new FileOutputStream(outputfile);
			sourceWorkBook = new HSSFWorkbook(myInput);
		    sourceSheet = sourceWorkBook.getSheet("General_Report"); 
		    HSSFCellStyle myStyle = createMyCellStyle(sourceWorkBook);
		    Iterator<Row> it = sourceSheet.iterator();		    
		    int counter = 0;
		    boolean found = false;		    
		    while (it.hasNext())
		    {
		    	Row searchedRow = it.next();
		    	if (searchedRow.getCell(TELNUM_COLUMN_NUMBER).getStringCellValue().equalsIgnoreCase(pm.getTelNum()))
		    	{
		    		newRow = sourceSheet.createRow(counter);
		    		fillCells(myStyle, newRow, pm);			       
			        found = true;
			        Log.d(TAG, "Values Added to existing row..");
		    	}		    	
		    	counter++;
		    }
		    
		    if (!found)
		    {		    	
		    	newRow = sourceSheet.createRow(sourceSheet.getLastRowNum()+1);
		    	fillCells(myStyle, newRow, pm);		       
		        Log.d(TAG, "Values Added to new row..");
		    }
	        updateUnitSheet();
	       // updateSummarySheet();
	        createHyperlinks();
	        sourceWorkBook.write(myOutput);
	        sourceWorkBook.close();//Closing source workbook
	        myOutput.close();//Closing output stream
	        myInput.close();//Closing input stream
	        Log.d(TAG, "Files are closed..");
	        outputfile.renameTo(new File (extdir.getAbsolutePath() + "/smsreports/Report.xls"));
	        myStyle = null;
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private void updateSummarySheet()
	{
		summarySheet = sourceWorkBook.getSheet("Summary");
		if (summarySheet == null) 
		{
			summarySheet = sourceWorkBook.createSheet("Summary");
			newRow = summarySheet.createRow(0);
			newCell = newRow.createCell(0);
			summarySheet.addMergedRegion(new CellRangeAddress(0,1,0,0));
			summarySheet.addMergedRegion(new CellRangeAddress(0,0,1,2));
			
			newCell.setCellValue("Дата");
			//newCell = newRow.createCell();
			
			
		}
		
	}

	private void createHyperlinks()
	{
		Row row = null;
		Cell cell = null;
		HSSFSheet testSheet = null;
		HSSFHyperlink link = null;
		
		CreationHelper createHelper = sourceWorkBook.getCreationHelper();
		Iterator<Row> iter = sourceSheet.iterator();
		while (iter.hasNext())
		{
			row = iter.next();
			
			if ((sourceWorkBook.getSheet(row.getCell(TELNUM_COLUMN_NUMBER).getStringCellValue()) != null) && (row.getCell(TELNUM_COLUMN_NUMBER).getStringCellValue().equalsIgnoreCase(pm.getTelNum())))
			{
				link = (HSSFHyperlink) createHelper.createHyperlink(Hyperlink.LINK_DOCUMENT);
				link.setAddress("'"+pm.getTelNum()+"'!A1");
				Log.d(TAG, "Adress: "+link.getAddress());
				row.getCell(TELNUM_COLUMN_NUMBER).setHyperlink(link);
				row.getCell(TELNUM_COLUMN_NUMBER).setCellStyle(createHyperlinkCellStyle(sourceWorkBook));
			}			
		}
	}

	private void updateUnitSheet()
	{
		unitSheet = sourceWorkBook.getSheet(pm.getTelNum());
		if (unitSheet == null) 
		{
			unitSheet = sourceWorkBook.createSheet(pm.getTelNum());
			autoSizeColumns(unitSheet);
			HSSFCellStyle headStyle = createHeaderStyle(sourceWorkBook);			
	        createHeader(unitSheet, headStyle);	
	        headStyle = null;
		}
		HSSFCellStyle myCellStyle = createMyCellStyle(sourceWorkBook);
		newRow = unitSheet.createRow(unitSheet.getLastRowNum()+1);
		fillCells(myCellStyle, newRow, pm);
		myCellStyle = null;
	}
	
	private String getAdress()
	{
		Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(pm.getTelNum()));
	    String adress = "Адрес не задан";	   
	    ContentResolver contentResolver = context.getContentResolver();
	    Cursor contactLookup = contentResolver.query(uri, new String[] {BaseColumns._ID,
	            ContactsContract.PhoneLookup.DISPLAY_NAME }, null, null, null);

	    try {
	        if (contactLookup != null && contactLookup.getCount() > 0) {
	            contactLookup.moveToNext();
	            String contactId = contactLookup.getString(contactLookup.getColumnIndex(BaseColumns._ID));
	            Cursor phones = context.getContentResolver().query(ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_URI,null , ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ contactId, null,null);
				if (phones != null && phones.getCount() > 0) 
				{
					phones.moveToNext();
					adress = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS));
					Log.d(TAG, "Adress: "+adress);
				}	            
	        }
	        else adress = "Адрес не задан";
	    } finally {
	        if (contactLookup != null) {
	            contactLookup.close();
	        }
	    }
		return adress;
	}

	private String getAlias()
	{
		Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(pm.getTelNum()));
	    String alias = "Псевдоним не задан";

	    ContentResolver contentResolver = context.getContentResolver();
	    Cursor contactLookup = contentResolver.query(uri, new String[] {BaseColumns._ID,
	            ContactsContract.PhoneLookup.DISPLAY_NAME }, null, null, null);

	    try {
	        if (contactLookup != null && contactLookup.getCount() > 0) {
	            contactLookup.moveToNext();
	            alias = contactLookup.getString(contactLookup.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
	        }
	        else alias = "Псевдоним не задан";
	    } finally {
	        if (contactLookup != null) {
	            contactLookup.close();
	        }
	    }
	    Log.d(TAG, "Alias: "+alias);
		return alias;
	}
	
	private HSSFCellStyle createHyperlinkCellStyle(HSSFWorkbook wb)
	{
		 HSSFFont font = wb.createFont();
	     font.setUnderline(HSSFFont.U_SINGLE);
	     font.setBoldweight(HSSFFont.BOLDWEIGHT_NORMAL);
	     font.setColor(new HSSFColor.BLUE().getIndex());
	     // создаем стиль для ячейки
	     HSSFCellStyle style = wb.createCellStyle();
	     // и применяем к этому стилю жирный шрифт
	     style.setFont(font);	     
	     style.setAlignment(CellStyle.ALIGN_CENTER);
	     style.setFillBackgroundColor(new HSSFColor.LIGHT_GREEN().getIndex());
		return style;
	}
	
	private HSSFCellStyle createMyCellStyle(HSSFWorkbook wb)
	{
		 HSSFFont font = wb.createFont();
	     font.setBoldweight(HSSFFont.BOLDWEIGHT_NORMAL);
	     font.setColor(new HSSFColor.BLACK().getIndex());
	     // создаем стиль для ячейки
	     HSSFCellStyle style = wb.createCellStyle();
	     // и применяем к этому стилю жирный шрифт
	     style.setFont(font);
	     
	     style.setAlignment(CellStyle.ALIGN_RIGHT);
	     style.setFillBackgroundColor(new HSSFColor.LIGHT_GREEN().getIndex());
		return style;
	}
	
	private HSSFCellStyle createHeaderStyle(HSSFWorkbook wb)
	{
		HSSFFont font = wb.createFont();
        // указываем, что хотим его видеть жирным
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        font.setColor(new HSSFColor.LIGHT_GREEN().getIndex());
        // создаем стиль для ячейки
        HSSFCellStyle style = wb.createCellStyle();
        // и применяем к этому стилю жирный шрифт
        style.setFont(font);
        style.setAlignment(CellStyle.ALIGN_CENTER);
        
        style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        style.setFillForegroundColor(new HSSFColor.BLUE().getIndex());
        //style.setFillBackgroundColor(new HSSFColor.YELLOW().getIndex());
        return style;
	}

	private void autoSizeColumns(HSSFSheet sheet)
	{
		sheet.setColumnWidth(DATE_COLUMN_NUMBER, 5000);
		sheet.setColumnWidth(ALIAS_COLUMN_NUMBER, 5000);
		sheet.setColumnWidth(TELNUM_COLUMN_NUMBER, 5000);
		sheet.setColumnWidth(ADRESS_COLUMN_NUMBER, 10000);
		sheet.setColumnWidth(STATUS_COLUMN_NUMBER, 5000);
		sheet.setColumnWidth(ERROR_COLUMN_NUMBER, 5000);
		sheet.setColumnWidth(TOTALMONEY_COLUMN_NUMBER, 5000);
		sheet.setColumnWidth(MONEYNOW_COLUMN_NUMBER, 5000);
		sheet.setColumnWidth(REST_COLUMN_NUMBER, 5000);
		sheet.setColumnWidth(TOTALWATER_COLUMN_NUMBER, 5000);
		sheet.setColumnWidth(TODAYWATER_COLUMN_NUMBER, 5000);
		sheet.setColumnWidth(WATERREST_COLUMN_NUMBER, 5000);
		sheet.setColumnWidth(LOWWATER_COLUMN_NUMBER, 5000);
		sheet.setColumnWidth(MIDWATER_COLUMN_NUMBER, 5000);
		sheet.setColumnWidth(HIGHWATER_COLUMN_NUMBER, 5000);
		sheet.setColumnWidth(BILLSUM_COLUMN_NUMBER, 5000);
		sheet.setColumnWidth(BILLCOUNT_COLUMN_NUMBER, 5000);
		sheet.setColumnWidth(COINSUM_COLUMN_NUMBER, 5000);
		sheet.setColumnWidth(COINCOUNT_COLUMN_NUMBER, 5000);		
	}

	private void fillNewFile(File inputfile, File outputfile, File extdir)
	{
		try
		{
		myInput = new FileInputStream(inputfile);
		myOutput = new FileOutputStream(outputfile);
		sourceWorkBook = new HSSFWorkbook(myInput);
	    sourceSheet = sourceWorkBook.createSheet("General_Report"); 
	    autoSizeColumns(sourceSheet);
	    // создаем шрифт
	    HSSFCellStyle headStyle = createHeaderStyle(sourceWorkBook);        
        createHeader(sourceSheet, headStyle);	    
        sourceWorkBook.write(myOutput);
        sourceWorkBook.close();
        myInput.close();
        myOutput.close();
        Log.d(TAG, "Header Created...");
        outputfile.renameTo(new File (extdir.getAbsolutePath() + "/smsreports/Report.xls"));
        Log.d(TAG, "Sheet: " + sourceSheet.getSheetName());
		} catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private void createHeader(HSSFSheet sheet, HSSFCellStyle style)
	{
		newRow = sheet.createRow(0);	    
        newCell = newRow.createCell(DATE_COLUMN_NUMBER);
        newCell.setCellValue("Дата");
        newCell.setCellStyle(style);        
        newCell = newRow.createCell(ALIAS_COLUMN_NUMBER);
        newCell.setCellValue("Псевдоним");
        newCell.setCellStyle(style);
        newCell = newRow.createCell(TELNUM_COLUMN_NUMBER);
        newCell.setCellValue("Телефон");
        newCell.setCellStyle(style);
        newCell = newRow.createCell(ADRESS_COLUMN_NUMBER);
        newCell.setCellValue("Адрес");
        newCell.setCellStyle(style);
        newCell = newRow.createCell(STATUS_COLUMN_NUMBER);
        newCell.setCellValue("Состояние");
        newCell.setCellStyle(style);
        newCell = newRow.createCell(ERROR_COLUMN_NUMBER);
        newCell.setCellValue("Ошибка?");
        newCell.setCellStyle(style);
        newCell = newRow.createCell(TOTALMONEY_COLUMN_NUMBER);
        newCell.setCellValue("Оборот, руб");
        newCell.setCellStyle(style);
        newCell = newRow.createCell(MONEYNOW_COLUMN_NUMBER);
        newCell.setCellValue("Оборот, руб (О.)");
        newCell.setCellStyle(style);
        newCell = newRow.createCell(REST_COLUMN_NUMBER);
        newCell.setCellValue("Сдача");
        newCell.setCellStyle(style);
        newCell = newRow.createCell(TOTALWATER_COLUMN_NUMBER);
        newCell.setCellValue("Оборот, литр");
        newCell.setCellStyle(style);
        newCell = newRow.createCell(TODAYWATER_COLUMN_NUMBER);
        newCell.setCellValue("Оборот, литр (О.)");
        newCell.setCellStyle(style);
        newCell = newRow.createCell(WATERREST_COLUMN_NUMBER);
        newCell.setCellValue("Остаток воды");
        newCell.setCellStyle(style);
        newCell = newRow.createCell(LOWWATER_COLUMN_NUMBER);
        newCell.setCellValue("Мало воды");
        newCell.setCellStyle(style);
        newCell = newRow.createCell(MIDWATER_COLUMN_NUMBER);
        newCell.setCellValue("Меньше половины");
        newCell.setCellStyle(style);
        newCell = newRow.createCell(HIGHWATER_COLUMN_NUMBER);
        newCell.setCellValue("Полон");
        newCell.setCellStyle(style);
        newCell = newRow.createCell(BILLSUM_COLUMN_NUMBER);
        newCell.setCellValue("Сумма купюрами");
        newCell.setCellStyle(style);
        newCell = newRow.createCell(BILLCOUNT_COLUMN_NUMBER);
        newCell.setCellValue("Количество купюр");
        newCell.setCellStyle(style);
        newCell = newRow.createCell(COINSUM_COLUMN_NUMBER);
        newCell.setCellValue("Сумма монетами");
        newCell.setCellStyle(style);
        newCell = newRow.createCell(COINCOUNT_COLUMN_NUMBER);
        newCell.setCellValue("Количество монет");
        newCell.setCellStyle(style);		
	}
	
	private void fillCells(HSSFCellStyle celStyl, Row rowToUpdate, ParsedMessage parMess)
	{
		newCell = rowToUpdate.createCell(DATE_COLUMN_NUMBER);
	    newCell.setCellValue(parMess.getDate());
	    newCell.setCellStyle(celStyl);
	    newCell = rowToUpdate.createCell(ALIAS_COLUMN_NUMBER);
	    newCell.setCellValue(alias);
	    newCell.setCellStyle(celStyl);
	    newCell = rowToUpdate.createCell(TELNUM_COLUMN_NUMBER);
	    newCell.setCellValue(parMess.getTelNum());
	    newCell.setCellStyle(celStyl);
	    newCell = rowToUpdate.createCell(ADRESS_COLUMN_NUMBER);
	    newCell.setCellValue(adress);
	    newCell.setCellStyle(celStyl);
	    newCell = rowToUpdate.createCell(STATUS_COLUMN_NUMBER);
	    newCell.setCellValue(parMess.getState());
	    newCell.setCellStyle(celStyl);
	    newCell = rowToUpdate.createCell(ERROR_COLUMN_NUMBER);
	    newCell.setCellValue(parMess.getError());
	    newCell.setCellStyle(celStyl);
	    newCell = rowToUpdate.createCell(TOTALMONEY_COLUMN_NUMBER);
	    newCell.setCellValue(parMess.getTotalMoney());
	    newCell.setCellStyle(celStyl);
	    newCell = rowToUpdate.createCell(MONEYNOW_COLUMN_NUMBER);
	    newCell.setCellValue(parMess.getMoneyNow());
	    newCell.setCellStyle(celStyl);
	    newCell = rowToUpdate.createCell(REST_COLUMN_NUMBER);
	    newCell.setCellValue(parMess.getRest());
	    newCell.setCellStyle(celStyl);
	    newCell = rowToUpdate.createCell(TOTALWATER_COLUMN_NUMBER);
	    newCell.setCellValue(parMess.getTotalWater());
	    newCell.setCellStyle(celStyl);
	    newCell = rowToUpdate.createCell(TODAYWATER_COLUMN_NUMBER);
	    newCell.setCellValue(parMess.getTodayWater());
	    newCell.setCellStyle(celStyl);
	    newCell = rowToUpdate.createCell(WATERREST_COLUMN_NUMBER);
	    newCell.setCellValue(parMess.getWaterLevel());
	    newCell.setCellStyle(celStyl);
	    newCell = rowToUpdate.createCell(LOWWATER_COLUMN_NUMBER);
	    newCell.setCellValue(parMess.isLowWater()?"Да":"Нет");
	    newCell.setCellStyle(celStyl);
	    newCell = rowToUpdate.createCell(MIDWATER_COLUMN_NUMBER);
	    newCell.setCellValue(parMess.isMidWater()?"Да":"Нет");
	    newCell.setCellStyle(celStyl);
	    newCell = rowToUpdate.createCell(HIGHWATER_COLUMN_NUMBER);
	    newCell.setCellValue(parMess.isHighWater()?"Да":"Нет");
	    newCell.setCellStyle(celStyl);
	    newCell = rowToUpdate.createCell(BILLSUM_COLUMN_NUMBER);
	    newCell.setCellValue(parMess.getBillSum());
	    newCell.setCellStyle(celStyl);
	    newCell = rowToUpdate.createCell(BILLCOUNT_COLUMN_NUMBER);
	    newCell.setCellValue(parMess.getBillCount());
	    newCell.setCellStyle(celStyl);
	    newCell = rowToUpdate.createCell(COINSUM_COLUMN_NUMBER);
	    newCell.setCellValue(parMess.getCoinSum());
	    newCell.setCellStyle(celStyl);
	    newCell = rowToUpdate.createCell(COINCOUNT_COLUMN_NUMBER);
	    newCell.setCellValue(parMess.getCoinCount());
	    newCell.setCellStyle(celStyl);
	}
}
