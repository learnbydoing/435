/*--------------------------------------------------------

Urvi Patel 

Due date: 1/22/2017

Java version: 1.8


Command-line compilation examples / instructions:

To compile, place .java files in same directory and run:

> javac JokeServer.java JokeCient.java JokeClientAdmin.java

or if these are the only files in the directory:

> javac *.java

4. Precise examples / instructions to run this program:

The JokeServer, JokeClient, and JokeClientAdmin may run on the same machine 
or on different machines.  For illustration purposes, assume that the JokeServer 
runs on 140.192.1.23


If the JokeServer runs primary server (only) and JokeClient is and JokeClientAdmin 
are on same machine as JokeServer.  That is, on command lines args for JokeClient 
and JokeClientAdmin assume that the server is running on localhost

In separate shell windows, type the following:

> java JokeServer
> java JokeClient
> java JokeClientAdmin

If the JokeServer runs on primary and JokeClient or JokeClientAdmin are running on 
another machine then you must put the IP address of the server to the command line.  
For example, if the JokeClient is on a different machine from the JokeServer and the 
JokeAdminClient is on the same machine as the JokeServer, then in separate shell 
windows, type the following:

> java JokeServer
> java JokeClient 140.192.1.23 
> java JokeAdminClient

If the JokeServer runs on primary and JokeClient or JokeClientAdmin are running on 
another machine then you must put the IP address of the server to the command line.  
For example, if the JokeClient is on a different machine from the JokeServer and the 
JokeAdminClient is on the same machine as the JokeServer, then in separate shell 
windows, type the following::

> java JokeServer secondary
> java JokeClient 140.192.1.23 
> java JokeAdminClient

List of files needed for running the program.

 b. JokeServer.java
 c. JokeClient.java
 d. JokeClientAdmin.java

Notes:

Used Java class Random to generate random numbers from 0 - 3 to get random 
indexes into joke/proverb array. 

----------------------------------------------------------*/

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class JokeClient 
{
	private static String primaryServerMachine = "";
	private static String secondaryServerMachine = "";
	private static final int PRIMARY_PORT = 4545;
	private static final int SECONDARY_PORT = 4546;
	
	private static Map<String, UUID> priEmailUUIDMappings;
	private static Map<String, UUID> secEmailUUIDMappings;
	
	public static void main(String args[])
	{
		String userInput;
		
		//Determine the primary and secondary machines
		if(args.length == 0)
		{
			primaryServerMachine = "localhost";
		}
		if(args.length == 1)
		{
			primaryServerMachine = args[0];
		}
		if(args.length == 2)
		{
			primaryServerMachine = args[0];
			secondaryServerMachine = args[1];
		}
		
		/*Print informative messages to the user, reiterating his/her choices for  
		 * a primary server and (if applicable) a secondary server*/
		System.out.println("Starting Joke Client, 1.8\n");
		
		if(args.length < 2)
		{
			System.out.println("Using server: " + primaryServerMachine + ", Port: " + PRIMARY_PORT);
		}
		else
		{
			System.out.println("Using server: " + primaryServerMachine + ", Port: " + PRIMARY_PORT);
			System.out.println("Using server: " + secondaryServerMachine + ", Port: " + SECONDARY_PORT);
		}
		
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		boolean useSecondary = false;
		
		/* Generate an id for the user to send to the server so server can 
		*  can check which messages have already been sent to this client */
		
		
		try 
		{
			/* Get user's email address so you can use it as a key
			 * to a hash map, where the value is the users UUID
			 * This hashmap is used to lookup the uuid given the 
			 * user's email address. This map saves the state of 
			 * the client
			 */
			String email;
			System.out.print("Enter email address: ");
			email = in.readLine();
			System.out.flush();
			
			/* Get the user name from the user, store it in a variable and send 
			 *  it to the server. The server will then insert it into the joke 
			 * or proverb that it sends to the client.  */
			String userName;
			System.out.print("Enter your name: ");
			userName = in.readLine();
			System.out.flush();
			
			/* Have user hit <Enter> key to get more jokes or proverbs,
			 * or type 'quit' to exit  */
			 
			do 
			{
				userInput = in.readLine();
				if(userInput.indexOf("quit") < 0)
				{
					if(userInput.equals("s"))
					{
						if(secondaryServerMachine.isEmpty())
						{
							System.out.println("No secondary server, using " + primaryServerMachine + " on " + PRIMARY_PORT);
						}
						else
						{
							useSecondary = !useSecondary;
							String serverUsed = useSecondary ? secondaryServerMachine : primaryServerMachine;
							int portUsed = useSecondary ? SECONDARY_PORT : PRIMARY_PORT;
							System.out.println("Using " + serverUsed + " on port " + portUsed);
						}
					}
					UUID uuid = getJoke(userName, email, useSecondary);
					write(email, uuid, useSecondary);
				}
			}while(userInput.indexOf("quit") < 0);
			System.out.println("Goodbye, " + userName + "!");
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
		}
	}//End main
	
	/* The write() method writes the hash map of email address <--> uuids
	 * to a file on disk. The name of the file depends on 
	 * whether the user is on the primary or secondary server.
	 */
	static void write(String email, UUID uuid, boolean secondary)
	{	
		FileOutputStream clientFOS = null;
		ObjectOutputStream clientOOS = null;
		String fileName = secondary ? "client_sec_urvi.out" : "client_primary_urvi.out"; //Determine output file name
		
		/* Place (email, uuid) key/value pair in the correct hashmap,
		*  to keep track of the users on the primary or secondary
		*/
		if(secondary)
		{
			secEmailUUIDMappings.put(email, uuid);    
		}
		priEmailUUIDMappings.put(email,uuid);
		
		try
		{
			clientFOS = new FileOutputStream(new File(fileName));
			clientOOS = new ObjectOutputStream(clientFOS);
			if(secondary)
			{
				clientOOS.writeObject(secEmailUUIDMappings);  // Write the hashmap to a file
			}
			else
			{
				clientOOS.writeObject(priEmailUUIDMappings);  //Write the hashmap to a file
			}
			clientOOS.close();
			clientFOS.close();
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
		}
	}
	
	/* The printMap() method prints out the hashmap containing
	 * the (email, uuid) key/value pairs.  Useful for debugging
	 * */
	static void printMap(Map<String, UUID> m, String uName)
	{
		System.out.println("\nMap for: " + uName);
		for(String k:m.keySet())
		{
			UUID value = m.get(k);
			System.out.print(k + ":" + value.toString());
		}
	}
	static UUID getJoke(String name, String email, boolean secondary)
	{
		Socket socket;
		BufferedReader fromServer;
		PrintStream toServer;
		String textFromServer;
		String serverName = "";
		int port = 0;
		
		File primaryFile = new File("client_primary_urvi.out");
		File secondaryFile = new File("client_sec_urvi.out");
		
		FileInputStream fileInputStream = null;
		ObjectInputStream objectInputStream = null;
		
		UUID id = null;  // Id to sent to server, the value is determine below

		
		if(secondary)  // If user is on secondary server, use the appropriate machine name and port
		{
			serverName = secondaryServerMachine;
			port = SECONDARY_PORT;
			
			/*If there is a file saved for the secondary server, use that file
			 * to create the hashmap with (email, uuid) key/value pairs
			 * */
			if(secondaryFile.exists())  
			{
				try
		    	{
					System.out.println("Secondary file found, restoring state...");
					fileInputStream = new FileInputStream(secondaryFile);
					objectInputStream = new ObjectInputStream(fileInputStream);
					secEmailUUIDMappings = (Map<String, UUID>) objectInputStream.readObject();
					objectInputStream.close();
					fileInputStream.close();
				} 
				catch (ClassNotFoundException e) 
		    	{
					e.printStackTrace();
				}
		    	catch(IOException ex)
		    	{
		    		ex.printStackTrace();
		    	}
				/* Using the email as a key, find the corresponding uuid for this
				 * user.  This is the id that is sent to the server.
				 */
				if(secEmailUUIDMappings.containsKey(email))
				{
					id = secEmailUUIDMappings.get(email);
					System.out.println("Found email in secondary map, using uuid: " + id.toString());
				}
				/* If the key is not in the map, then this means that the user never connected before
				 * so create a new entry for this user and add it to the map*/
				else  
				{
					id = UUID.randomUUID();
					secEmailUUIDMappings.put(email, id);
					System.out.println("Email not found in secondary map, created a new entry: " + id.toString());
				}
			}
			else //On secondary, but no file exists, create a Mapping hashmap, add the user, and set the id
			{
				id = UUID.randomUUID();
				secEmailUUIDMappings = new HashMap<String, UUID>();
				secEmailUUIDMappings.put(email, id);
				
				
			}
		}
		else // Using primary server
		{
			serverName = primaryServerMachine;
			port = PRIMARY_PORT;
			
			/*If there is a file saved for the primary server, use that file
			 * to create the hashmap with (email, uuid) key/value pairs
			 * */
			if(primaryFile.exists())
			{
				try
		    	{
					System.out.println("Primary file found, restoring state...");
					fileInputStream = new FileInputStream(primaryFile);
					objectInputStream = new ObjectInputStream(fileInputStream);
					priEmailUUIDMappings = (Map<String, UUID>) objectInputStream.readObject();
					objectInputStream.close();
					fileInputStream.close();
				} 
				catch (ClassNotFoundException e) 
		    	{
					e.printStackTrace();
				}
		    	catch(IOException ex)
		    	{
		    		ex.printStackTrace();
		    	}
				/* Using the email as a key, find the corresponding uuid for this
				 * user.  This is the id that is sent to the server.
				 */
				if(priEmailUUIDMappings.containsKey(email))
				{
					id = priEmailUUIDMappings.get(email);
					//printMap(priEmailUUIDMappings, email);
					System.out.println("Found email in primary map, using uuid: " + id.toString());
				}
				/* If the key is not in the map, then this means that the user never connected before
				 * so create a new entry for this user and add it to the map*/
				else
				{
					id = UUID.randomUUID();
					priEmailUUIDMappings.put(email, id);
					System.out.println("Email not found in primary map, created a new entry: " + id.toString());
					//printMap(priEmailUUIDMappings, email);
				}
			}
			else // On primary, but no file exists, create a Mapping hashmap, add the user, and set the id
			{
				id = UUID.randomUUID();
				priEmailUUIDMappings = new HashMap<String, UUID>();
				priEmailUUIDMappings.put(email, id);
				//printMap(priEmailUUIDMappings, email);
			}			
		}
		
		try
		{
			socket = new Socket(serverName, port);  // Create a socket to the server, listening port 4545 
			fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));  // Input stream to read data from server
			toServer = new PrintStream(socket.getOutputStream());  // Output stream on which to place data to send to server
			
			toServer.println(name);  // Send the user name to the server
			toServer.flush();
			toServer.println(id);  // Send the id to the server
			
			while( (textFromServer = fromServer.readLine()) != null)
			{
				System.out.println(textFromServer);  // Print out joke/proverb received from server
			}
			socket.close();  // Close the socket connection for this client to prevent resource leak
		}
		catch(IOException e)
		{
			System.out.println("Socket error");  // Catch exceptions related to writing to or reading from socket
			e.printStackTrace();
		}
		return id;
	}//End getJoke() 
}// End class JokeClient
