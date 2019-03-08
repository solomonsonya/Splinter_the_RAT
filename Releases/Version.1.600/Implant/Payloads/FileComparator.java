/**
 * @author Solomon Sonya
 */

package Implant.Payloads;
import java.util.*;
import java.io.File;

public class FileComparator implements Comparator<File>
{
	@Override
	 public int compare(File fle1, File fle2) 
	{
		try
		{
			return fle1.getCanonicalPath().compareTo(fle2.getCanonicalPath());
		}
		catch(Exception e)
		{
			
		}
		
		return 0;
    }

}
