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

public class JokeClientAdminB 
{
	private static String primaryServerMachine = "";
	private static String secondaryServerMachine = "";
	private static final int PRIMARY_PORT = 5050;
	private static final int SECONDARY_PORT = 5051;
	
	public static void main(String args[])
	{
		
		String inputFromUser;
		
		/*When the client is started on the command line, an argument may be given to denote
		the machine to which the client will connect.  If no argument is given, the client
		will connect to localhost.  If one argument is given, the client will connect to
		that IP. If two arguments are given, the first is considered to be the IP address 
		of primary server and the second argument is the IP of the secondary server*/
		
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
		
		/* Print informative messages to the user, reiterating his/her choices for  
		 * a primary server and (if applicable) a secondary server*/
		System.out.println("Starting Joke Admin Client, 1.8\n");
		
		if(args.length < 2)
		{
			System.out.println("Primary server: " + primaryServerMachine + ", Port: " + PRIMARY_PORT);
		}
		else
		{
			System.out.println("Primary server: " + primaryServerMachine + ", Port: " + PRIMARY_PORT);
			System.out.println("Secondary server: " + secondaryServerMachine + ", Port: " + SECONDARY_PORT);
		}
		
		/*Create a BufferedReader object to read the <enter> entered 
		by the user from the keyboard (i.e., System.in) */
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		
		boolean useSecondary = false; // Start out using the primary server
		String primaryOrSecondary = "";
		String serverUsed = "";
		int portUsed;
		
		try 
		{	
			do 
			{
				inputFromUser = in.readLine();  // Read in input from a user, only 'quit' will exit loop; any other key will toggle mode
				if(inputFromUser.indexOf("quit") < 0)
				{
					if(inputFromUser.equals("s"))  // Input of 's' toggles user between primary and secondary server
					{
						if(secondaryServerMachine.isEmpty())  // If no secondary server is given on command line, inform the user and user primary
						{
							System.out.println("No secondary server, using " + primaryServerMachine + " on " + PRIMARY_PORT);
						}
						else
						{
							useSecondary = !useSecondary; // Toggle between primary and secondary
							
							/* Display message to user which server/port they are on
							 based on the value of useSecondary variable, which is 
							 toggled upon user input */
							primaryOrSecondary = useSecondary ? "SECONDARY" : "PRIMARY"; 
						    serverUsed = useSecondary ? secondaryServerMachine : primaryServerMachine;
						    portUsed = useSecondary ? SECONDARY_PORT : PRIMARY_PORT;
							System.out.println("Using " + primaryOrSecondary + " server " + serverUsed + " on port " + portUsed);							
							continue; // Print message to user and continue, user can then hit <enter> to change mode or 's' toggle servers
						}
					}
					sendMode(useSecondary);
				}
			}while(inputFromUser.indexOf("quit") < 0);  // Keep looping until user types 'quit'
			
			System.out.println("Goodbye!");
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
		}
	}//End main
	

	/* sendMode() takes one parameter, a boolean, which denotes 
	 * whether the mode is to be sent to the primary or secondary 
	 * server.  
	 * 
	 * Based on the value of the 'secondary' variable passed in,
	 * the appropriate server and port are set
	 * 
	 * A socket, input stream and output stream are created
	 * 
	 * The socket connects to the server and port based on the 
	 * value of the 'secondary' variable passed in*/
	static void sendMode(boolean secondary)
	{
		Socket socket;
		PrintStream toServer;
		BufferedReader fromServer;
		
		String serverName = "";
		int port = 0;
		String mode = "";
		
		// Set machine and port to point to secondary server
		if(secondary) 
		{
			serverName = secondaryServerMachine;
			port = SECONDARY_PORT;
		}
		// Set machine and port to point to primary server
		else 
		{
			serverName = primaryServerMachine;
			port = PRIMARY_PORT;
		}
		
		try
		{
			socket = new Socket(serverName, port);  // Connect to server via socket on server and port, determined above
			toServer = new PrintStream(socket.getOutputStream()); // Stream used to send data to the server 
			fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));  // Stream used to read data from server
			
			String currMode = fromServer.readLine(); //Get current mode from server
			mode = currMode.equals("joke-mode") ? "proverb-mode" : "joke-mode";  // Toggle the current mode
			toServer.println(mode);  // Send the mode to the server on the output stream
			toServer.flush();
			String responseFromServer = fromServer.readLine();  // Get a response from the server from the input stream
			System.out.println(responseFromServer);  // Print it out to the AdminClient user
			socket.close();  // Close the connection to prevent resource leak
		}
		catch(IOException e)
		{
			System.out.println("Socket error");
			e.printStackTrace();
		}
	}//End sendMode()
}
