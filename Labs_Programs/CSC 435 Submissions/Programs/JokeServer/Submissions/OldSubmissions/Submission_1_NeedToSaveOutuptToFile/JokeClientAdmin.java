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
		String serverMachine;
		
		if(args.length < 1) 
			serverMachine = "localhost";
		else
			serverMachine = args[0];

		System.out.println("Starting Joke Admin Client, 1.8\n");
		System.out.println("Using server: " + serverMachine + ", Port: " + PORT);
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		
		try 
		{
			String modeFromUser;
			System.out.flush();
			
			do 
			{
				System.out.print("Enter mode: ");
				modeFromUser = in.readLine();
				if(modeFromUser.indexOf("quit") < 0)
					sendMode(modeFromUser, serverMachine);
			}while(modeFromUser.indexOf("quit") < 0);
			
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
			socket = new Socket(serverName, PORT);
			toServer = new PrintStream(socket.getOutputStream());  
			fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			toServer.println(mode);
			toServer.flush();
			System.out.println("Sending mode...");
			String responseFromServer = fromServer.readLine();
			System.out.println("Response from server is: " + responseFromServer);
			socket.close();
		}
		catch(IOException e)
		{
			System.out.println("Socket error");
			e.printStackTrace();
		}
	}//End getJoke()
}
