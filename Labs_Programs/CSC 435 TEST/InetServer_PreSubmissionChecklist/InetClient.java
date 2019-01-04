import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

public class InetClient {
	private static final int PORT = 4000; //Make this a constant so that it is easier to change ports
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
		System.out.println("Inet Client, 1.8\n");
		System.out.println("Using server: " + serverMachine + ", Port: " + PORT);
		
		//Create a BufferedReader object to read the hostname entered 
		//by the user from the keyboard (i.e., System.in)
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		try 
		{
			String hostName;
			do {
				//Prompt user for a hostname or IP Address to look up
				System.out.print("Enter a hostname or an IP address, (quit) to end: "); 
				System.out.flush();
				hostName = in.readLine(); //Read the hostname from the input stream and save it to a variable
				if(hostName.indexOf("quit") < 0) 
					getRemoteAddress(hostName, serverMachine); //Get the IP address of the hostName that user entered
			}while(hostName.indexOf("quit") < 0); //Continue prompting user for a hostname until user types 'quit'
			System.out.println("Cancelled by user request"); //Print message indicating that user terminated program
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
		}
	}//End main
	
	public String toText(byte ip[])
	{
		//Create string buffer, it can hold up to 16 characters by default
		StringBuffer result = new StringBuffer();
		
		//Go through the byte array representing the raw ip and convert it to a String representation of an IP address.
		for(int i = 0; i < ip.length; ++i)
		{
			if(i > 0)
				result.append(".");  //Add a period after first octet
			
			/*0xff & ip[i] represents the ith element of the byte array as a positive int between 0 and 255
			Note that if the ip[i] < 128, then the values for ip[i] and 0xff & ip[i] are the same.  The append 
			method takes an int and appends its String representation to the StringBuffer object, i.e., result.
			
			This explanation is from:
			http://stackoverflow.com/questions/9949856/anding-with-0xff-clarification-needed*/
			result.append(0xff & ip[i]);
		}
		return result.toString(); //Return String representation of the raw IP address
	}//End toText
	
	static void getRemoteAddress(String name, String serverName)
	{
		Socket socket; //Client socket object
		BufferedReader fromServer; //A BufferedReader object to read data from the socket
		PrintStream toServer; //Output stream to write data to the socket
		String textFromServer; //Variable to hold message that was read from the server
		
		try
		{
			/*Create the client socket, connecting to a port on a machine.  The port
			 * is the port that the server is listening on, and the machine is the
			 * machine the user entered as a command line argument when starting
			 * the client 
			 * 
			 * Create the input/output streams that will be used to get messages from the
			 * server and write message to the server.  The underlying input/output streams 
			 * are the socket's input/output streams */ 
			socket = new Socket(serverName, PORT);
			fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			toServer = new PrintStream(socket.getOutputStream());
			
			toServer.println(name); //Print the hostname to the output stream
			toServer.flush(); //Send the name to the server via the output stream
			
			/*Server is sending the client 3 messages:
			 * The first is a message informing the user what hostname the server is looking up
			 * The second is the hostname the user entered
			 * The third is the IP of the hostname that the user entered
			 * 
			The following for loop reads a message from the socket's input stream
			and saves it to a String variable, textFromServer,
			and writes the message to standard out (if it isn't null) */
			for(int i = 1; i <= 3; i++)
			{
				textFromServer = fromServer.readLine();
				if(textFromServer != null)
					System.out.println(textFromServer);
			}
			socket.close();  //Close the socket to prevent a resource leak
		}
		catch(IOException e)
		{
			System.out.println("Socket error"); //Error reading from or writing to the socket
			e.printStackTrace();
		}
	}//End getRemoteAddress
}
