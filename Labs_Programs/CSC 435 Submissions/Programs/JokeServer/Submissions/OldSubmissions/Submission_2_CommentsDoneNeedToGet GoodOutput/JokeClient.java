import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.UUID;

public class JokeClient {
	private static final int PORT = 4545;
	public static void main(String args[])
	{
		/* When the client is started on the command line, an argument may be given to denote
		the machine to which the client will connect.  If no argument is given, the client
		will connect to localhost */
		String serverMachine;
		String userInput;
		
		if(args.length < 1) 
			serverMachine = "localhost"; // 
		else
			serverMachine = args[0];
		
		//Print informative messages to the user
		System.out.println("Starting Joke Client, 1.8\n");
		System.out.println("Using server: " + serverMachine + ", Port: " + PORT);
		
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		
		/* Generate an id for the user to send to the server so server can 
		*  can check which messages have already been sent to this client */
		UUID uuid = UUID.randomUUID();
		
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
					getJoke(userName, serverMachine, uuid);
			}while(userInput.indexOf("quit") < 0);
			
			System.out.println("Cancelled by user request");
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
		}
	}//End main
	
	/* This method gets that joke/proverb that is sent from the server.
	 * The user name is sent so that it may inserted into the joke
	 * or proverb, and an id sent so that the user may lookup the
	 * state of the client with that id to determine which joke or 
	 * proverb to send */
	static void getJoke(String name, String serverName, UUID id)
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
	}//End getJoke()
}
