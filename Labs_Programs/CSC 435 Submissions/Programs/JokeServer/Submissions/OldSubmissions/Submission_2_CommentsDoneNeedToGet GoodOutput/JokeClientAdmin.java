import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

public class JokeClientAdmin 
{
	private static final int PORT = 5050;
	public static void main(String args[])
	{
		/*When the client is started on the command line, an argument may be given to denote
		the machine to which the client will connect.  If no argument is given, the client
		will connect to localhost */
		String serverMachine;
		
		if(args.length < 1) 
			serverMachine = "localhost";
		else
			serverMachine = args[0];
		
		//Print informative messages to the user
		System.out.println("Starting Joke Admin Client, 1.8\n");
		System.out.println("Using server: " + serverMachine + ", Port: " + PORT);
		
		/*Create a BufferedReader object to read the mode entered 
		by the user from the keyboard (i.e., System.in) */
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		
		try 
		{
			String modeFromUser;
			System.out.flush();
			
			do 
			{
				System.out.print("Enter mode: "); // Prompt user for mode
				modeFromUser = in.readLine();  // Sove mode in a variable
				if(modeFromUser.indexOf("quit") < 0)
					sendMode(modeFromUser, serverMachine);  // Send the mode to the server
			}while(modeFromUser.indexOf("quit") < 0);  // Keep looping until user types 'quit'
			
			System.out.println("Cancelled by user request");
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
		}
	}//End main
	
	static void sendMode(String mode, String serverName)
	{
		Socket socket;
		PrintStream toServer;
		BufferedReader fromServer;
		
		try
		{
			socket = new Socket(serverName, PORT);  // Connect to server on port 5050 (Admin port)
			toServer = new PrintStream(socket.getOutputStream()); // Stream used to send data to the server 
			fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));  // Stream used to read data from server
			
			toServer.println(mode);  // Send the mode to the server on the output stream
			toServer.flush();
			System.out.println("Sending mode...");  // Inform AdminClient user that mode is being sent
			String responseFromServer = fromServer.readLine();  // Get a response from the server from the input stream
			System.out.println("Response from server is: " + responseFromServer);  // Print it out to the AdminClient user
			socket.close();  // Close the connection to prevent resource leak
		}
		catch(IOException e)
		{
			System.out.println("Socket error");
			e.printStackTrace();
		}
	}//End getJoke()
}
