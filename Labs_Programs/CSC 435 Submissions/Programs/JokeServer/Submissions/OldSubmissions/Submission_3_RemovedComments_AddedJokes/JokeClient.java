import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.UUID;

public class JokeClient {
	private static final int PORT = 4000;
	public static void main(String args[])
	{
		String serverMachine;
		String userInput;
		
		if(args.length < 1) 
			serverMachine = "localhost";
		else
			serverMachine = args[0];

		System.out.println("Starting Joke Client, 1.8\n");
		System.out.println("Using server: " + serverMachine + ", Port: " + PORT);
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		UUID uuid = UUID.randomUUID();
		
		try 
		{
			String userName;
			System.out.print("Enter your name: ");
			userName = in.readLine();
			System.out.flush();
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
	
	static void getJoke(String name, String serverName, UUID id)
	{
		Socket socket;
		BufferedReader fromServer;
		PrintStream toServer;
		String textFromServer;
		
		try
		{
			socket = new Socket(serverName, PORT);
			fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			toServer = new PrintStream(socket.getOutputStream());
			
			toServer.println(name);
			toServer.flush();
			toServer.println(id);
			while( (textFromServer = fromServer.readLine()) != null)
			{
				System.out.println(textFromServer);
			}
			socket.close();
		}
		catch(IOException e)
		{
			System.out.println("Socket error");
			e.printStackTrace();
		}
	}//End getJoke()
}
