/**
 * @author Solomon Sonya
 */

package Implant.Payloads;


import java.io.*;
import java.net.*;
import java.util.*;



public class MapNetworkInterface 
{
	public static final String myClassName = "MapNetworkInterface";
	
	private static Enumeration<NetworkInterface> enum_network_interfaces = null;
	
	private static LinkedList<NetworkInterface> llActiveInterfaces = null;
	
	private static LinkedList<InetAddress> llInetAddresses = null;
	
	public MapNetworkInterface()
	{
		try
		{
			listInterfaces();	
			
			sop("\n --> Network Interface Map Complete.");
		}		
		catch(Exception e)
		{
			sop("Exception caught in Constructor mtd in " + myClassName);
		}
	}
	
	public static LinkedList displayInterfaces()
	{
		try
		{
			
			LinkedList interfaceList = listInterfaces();
			
			if(interfaceList == null || interfaceList.size() < 1)
			{
				sop("* * * ERROR --> I could not list the available interfaces on this system");
				
				
				return null;
			}
			
			sop("\n --> Displaying network interfaces:");
			
			NetworkInterface network_interface = null;
			Enumeration<InetAddress> enum_IP_Addresses = null;
			
			for(int i = 0; i < interfaceList.size(); i++)
			{
				try
				{
					
					network_interface = llActiveInterfaces.get(i);
					
					sop(network_interface.toString());
					
					//list the ip addresses for each interfaces
					enum_IP_Addresses = network_interface.getInetAddresses();
															
					for( InetAddress IP_Address : Collections.list(enum_IP_Addresses))
					{
						sop("\t- IP Address: " + IP_Address.toString().replaceAll("/", ""));
					}
					
				}
				catch(Exception e)
				{
					sop("invalid interace[" + i + "]");
					continue;
				}
				
				
				
			}
			
			//past interface code below:
			//
			//List all Discoverable Interfaces
			//
			/*enum_network_interfaces = NetworkInterface.getNetworkInterfaces();
			
			//
			//Iterate through the list and only keep the interfaces that are active
			//
			for(NetworkInterface network_interface : Collections.list(enum_network_interfaces))
			{
				if(network_interface == null)
				{
					continue;
				}
				
				//
				//Acquire the IP Addresses assigned to each interface in the enumerations list
				//
				Enumeration<InetAddress> enum_IP_Addresses = network_interface.getInetAddresses();
				for( InetAddress IP_Address : Collections.list(enum_IP_Addresses))
				{
					//sop("IP Address: " + IP_Address);
				}
				
				//
				//Analyze and only keep interfaces that are up
				//
				if(network_interface.isUp())
				{
					Driver.sop(network_interface..toString());
				}
			}*/
			
			
			
			sop("");
			
			return interfaceList;
		}
		catch(Exception e)
		{
			eop(myClassName, "displayInterfaces", e, false);
		}
		
		return null;
	}
	
	public static LinkedList<InetAddress> listIP_Addresses()
	{
		try
		{
			listInterfaces();
			
			sop("\nListing Available IP addresses...");
			for(int i = 0; i < llInetAddresses.size(); i++)
			{
				sop("IP Address: " + llInetAddresses.get(i));
			}
				
			
			return llInetAddresses;
		}
		catch(Exception e)
		{
			eop(myClassName, "listIP_Addresses", e, false);
			
		}
		
		return null;
	}
	
	public static LinkedList<NetworkInterface> listInterfaces()
	{
		try
		{
			sop("Listing Network Interfaces now...");
			
			llActiveInterfaces = new LinkedList<NetworkInterface>();
			llInetAddresses = new LinkedList<InetAddress>();
			
			//
			//List all Discoverable Interfaces
			//
			enum_network_interfaces = NetworkInterface.getNetworkInterfaces();
			
			//
			//Iterate through the list and only keep the interfaces that are active
			//
			for(NetworkInterface network_interface : Collections.list(enum_network_interfaces))
			{
				if(network_interface == null)
				{
					continue;
				}
				
				//
				//Acquire the IP Addresses assigned to each interface in the enumerations list
				//
				Enumeration<InetAddress> enum_IP_Addresses = network_interface.getInetAddresses();
				for( InetAddress IP_Address : Collections.list(enum_IP_Addresses))
				{
					//sop("IP Address: " + IP_Address);
					llInetAddresses.add(IP_Address);
				}
				
				//
				//Analyze and only keep interfaces that are up
				//
				if(network_interface.isUp())
				{
					llActiveInterfaces.add(network_interface);
				}
			}
			
			//
			//Up Interfaces and IP Addresses
			//
			for(int i = 0; i < llActiveInterfaces.size(); i++)
			{
				//sop(alActiveInterfaces.get(i).toString());
			}
			
			return llActiveInterfaces;
		}
		catch(SocketException se)
		{
			sop("ERROR: COULD NOT LIST INTERFACES");
		}
		catch(Exception e)
		{
			sop("Exception caught in listInterfaces mtd in " + myClassName);
		}
		
		return null;
	}
	
	public static int getInterfaceCount()
	{
		try
		{
			//
			//Ensure list is populated first
			//
			if(llActiveInterfaces == null || llActiveInterfaces.size() < 1)
			{
				
				listInterfaces();
			}
			
			//
			//If after that, still no list, return null
			//
			if(llActiveInterfaces == null || llActiveInterfaces.size() < 1)
			{
				
				return -1;
			}
			
			//otherwise, return the number of interfaces found
			return llActiveInterfaces.size();
			
			
		}
		catch(Exception e)
		{
			eop("getInterfaceCount", myClassName, e, false);
		}
		
		return -1;
	}
	
	public static InetAddress getInterfaceAddress(int index)
	{
		try
		{
			//
			//Ensure list is populated first
			//
			if(llActiveInterfaces == null || llActiveInterfaces.size() < 1)
			{
				
				listInterfaces();
			}
			
			//
			//If after that, still no list, return null
			//
			if(llActiveInterfaces == null || llActiveInterfaces.size() < 1)
			{
				
				return null;
			}
			
			//
			//Otherwise, grab the IP address of the interfae selected
			//
			NetworkInterface network_interface = llActiveInterfaces.get(index);
			
			//
			//Enumerate through the IP Addresses assigned to this interface
			//
			Enumeration<InetAddress> enum_inet_addresses = network_interface.getInetAddresses();
			
			//
			//Iterate through the address to return the appropriate one selected. Right now, we'll focus on IPv4 Addresses
			//
			ArrayList<InetAddress> al_IP_Addresses = new ArrayList<InetAddress>();
			for( InetAddress IP_Address : Collections.list(enum_inet_addresses))
			{
				//sop("IP Address: " + IP_Address);
				al_IP_Addresses.add(IP_Address);
			}
			
			//
			//Analyze Results
			//
			if(al_IP_Addresses.size() == 1)
			{
				return al_IP_Addresses.get(0);
			}
			
			//
			//Else, 2 found, perhaps one is an IPV6 and the other is an IPV4. Analyze and return the IPV4 Address
			//
			InetAddress inetToReturn = null;
			for(int i = 0; i < al_IP_Addresses.size(); i++)
			{
				inetToReturn = al_IP_Addresses.get(i);
				try
				{
					if(inetToReturn.toString().contains("."))
					{
						return inetToReturn;
					}
				}catch(Exception e){continue;}
			}
			
			//
			//Else, not sure, just return the first one!
			//
			return al_IP_Addresses.get(0);
			
		}
		catch(Exception e)
		{
			sop("Exception caught in getInterfaceAddress mtd in " + myClassName);
		}
		
		return null;
	}
	
	public static void sop(String out)
	{
		try
		{
			System.out.println(out);
		}catch(Exception e){}
	}
	
	public static void eop(String mtdName, String myClassName, Exception e, boolean printStackTrace)
	{
		try
		{
			sop("Exception handled in " + mtdName + " mtd in " + myClassName + " class.  Message: " + e.getLocalizedMessage());
		}
		catch(Exception ee)
		{
			sop("Exception handled in " + mtdName + " mtd in " + myClassName + " class.");
		}
		
		if(printStackTrace && e != null)
		{
			try
			{
				e.printStackTrace();
			}catch(Exception eee){}
		}
	}
}
