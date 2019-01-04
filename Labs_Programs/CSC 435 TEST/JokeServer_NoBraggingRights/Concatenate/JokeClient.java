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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.UUID;

public class JokeClient 
{
	private static String primaryServerMachine = "";
	private static String secondaryServerMachine = "";
	private static final int PRIMARY_PORT = 4545;
	private static final int SECONDARY_PORT = 4546;
	private static final UUID PRIMARY_UUID = UUID.randomUUID();
	private static final UUID SECONDARY_UUID = UUID.randomUUID();
	
	public static void main(String args[])
	{
		String userInput;
		
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
							//System.out.println("useSecondary before toggle " + useSecondary);
							useSecondary = !useSecondary;
							//System.out.println("useSecondary after toggle " + useSecondary);
							String serverUsed = useSecondary ? secondaryServerMachine : primaryServerMachine;
							int portUsed = useSecondary ? SECONDARY_PORT : PRIMARY_PORT;
							System.out.println("Using " + serverUsed + " on port " + portUsed);
						}
					}
					//System.out.println("Calling getJoke() with useSecondary as " + useSecondary);
					getJoke(userName, useSecondary);
				}
			}while(userInput.indexOf("quit") < 0);
			System.out.println("Goodbye, " + userName + "!");
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
		}
	}//End main
	
	
	
	static void getJoke(String name, boolean secondary)
	{
		Socket socket;
		
		BufferedReader fromServer;
		PrintStream toServer;
		
		String textFromServer;
		
		String serverName = "";
		int port = 0;
		UUID id = null;
		
		if(secondary)
		{
			serverName = secondaryServerMachine;
			port = SECONDARY_PORT;
			id = SECONDARY_UUID;
		}
		else
		{
			serverName = primaryServerMachine;
			port = PRIMARY_PORT;
			id = PRIMARY_UUID;
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
	}//End getJoke() 
	
	
 
/*	static void getJoke(String name, String serverName, UUID id)
	{
		Socket socket;
		BufferedReader fromServer;
		PrintStream toServer;
		String textFromServer;
		
		try
		{
			socket = new Socket(serverName, PORT);  // Create a socket to the server, listening port 4545 
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
	}//End getJoke() */
}// End class JokeClient
