import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.UUID;


class AsyncWorker extends Thread
{
	int asyncPort;
	public String messageFromServer = "";
	
	
	AsyncWorker(int port)
	{
		asyncPort = port;
	}
	public void run()
	{
		byte[] receiveData = new byte[1024];
		try 
		{
			/* Create a socket with the port number the server is listening on.
			 * This is used for receiving datagram packets from the server. The port 
			 * is passed in from the main loop to the AsynWorker. 
			*  */
			DatagramSocket clientSocket = new DatagramSocket(asyncPort);
			
			/* Datagram packets are used in UDP and contain the information needed to get
			 * form one machine to another.  The constructor takes a byte array that 
			 * will be used to contain the data that the client will get back from 
			 * the server as well the number of bytes to read
			 * */
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			
			/* Get a datagram packet for the socket.  This method returns the data in the
			 * packet's buffer, as well as the IP adddress and port number of the server's
			 * machine. It blocks until a datagram is received
			 * */
			clientSocket.receive(receivePacket);
			
			/* Get the data from the datagram packet. As discussed, the receive method puts
			 * the data in the packet's buffer, and getData() method is used to retrieve it. 
			 * 
			 * */
			String jokeOrProverb = new String(receivePacket.getData());
			
			/* Set the variable messageFromServer to the string received from the server. 
			 * This is the variable that is checked in the while loop wherein the user is
			 * repeatedly prompted for numbers and shown their sum.
			 * */
			messageFromServer = jokeOrProverb;
			
			// Close the connection to the server
			clientSocket.close(); 
		} 
		catch (SocketException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		
		
	}
}//End class AsyncWorker

public class AsyncJokeClient
{
	//private static final int PORT_NUMBER = 4546;  // Server is listening on port 4546
	//public static final UUID uuid = UUID.randomUUID(); // Generate a unique id for the client
	
	private static String primaryServerMachine = "";
	private static String secondaryServerMachine = "";
	private static final int PRIMARY_PORT = 4545;
	private static final int SECONDARY_PORT = 4546;
	
	
	public static void main (String args[]) 
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
		
		// Stream to get console input
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		boolean useSecondary = false;
		
		try 
		{
			/* Get the user name from the user, store it in a variable and send 
			 *  it to the server. The server will then insert it into the joke 
			 * or proverb that it sends to the client.  */
			String name;
			System.out.print("Enter your name, (quit) to end: ");
			System.out.flush ();
			name = in.readLine (); // Read in username and save it to a variable
			
			do 
			{
				userInput = in.readLine(); // Read in user command for joke/proverb (i.e., hitting <Enter> key)
				if (userInput.indexOf("quit") < 0)
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
					getMessage(name, useSecondary, in);  // Get joke from the server
				}
			} while (userInput.indexOf("quit") < 0); // Continue getting jokes/proverbs until user types 'quit'
			System.out.println ("Cancelled by user request.");  // Message shown when user types 'quit'
		} 
		catch (IOException x) 
		{
			x.printStackTrace ();
		}
	}//End main()

	/*  The getMessage() method takes the user name and a boolean 
	 *  that determines if the user is on the primary or secondary
	 *  server.  The server machine, and port are determined based
	 *  on this boolean value and a uuid is generated.   
	 *  
	 *  It creates a socket to the server using the server's machine name a port.  
	 *  
	 *  The input stream is from stdin and is used to get input from the user 
	 *  when h/she is adding numbers while waiting for server to come back with 
	 *  a joke or proverb.  
	 * 
	 * */

	//static void getMessage(String name, String serverName, int port, BufferedReader input)
	static void getMessage(String name, boolean secondary, BufferedReader input)
	{
		Socket sock;
		PrintStream toServer;  // Output stream used to send data to the server
		
		String serverName = "";
		int port = 0;
		UUID id = null;  // Id to sent to server, the value is determine below
		
		if(secondary)  // If user is on secondary server, use the appropriate machine name and port
		{
			serverName = secondaryServerMachine;
			port = SECONDARY_PORT;
			id = UUID.randomUUID();
		}
		else
		{
			serverName = primaryServerMachine;
			port = PRIMARY_PORT;
			id = UUID.randomUUID();
		}
		
		
		try
		{
			AsyncWorker asyncWorker = new AsyncWorker(port);
			asyncWorker.start();
			
			sock = new Socket(serverName, port); // Create socket to server machine/port
			toServer = new PrintStream(sock.getOutputStream()); // Create output stream to send data to server
			toServer.println(name); // Send the server the user name
			toServer.flush();
			toServer.println(id); // Send the server the user's unique id

			/* The request for a joke or proverb has just been sent to the server and
			 * now the client is waiting for a response.  In the meantime, the user is
			 * prompted repeated for numbers to add and their sum is displayed after 
			 * each batch of numbers in entered.  There is also some error checking to
			 * ensure that the user has indeed entered integers.  This "game" continues
			 * until the server comes back with a joke/proverb and saves it into the
			 * messageFromServer variable. Once this happens, the user is shown the 
			 * joke or proverb.  Note that if the user is prompted to enter numbers 
			 * to be added and the server has returned with a joke/proverb then the 
			 * user must enter numbers and get a sum before seeing the joke/proverb.
			 * 
			 * */
			while(asyncWorker.messageFromServer == null || asyncWorker.messageFromServer.isEmpty())
			{
				System.out.print("Enter integers to add, separated by a space: ");  // Prompt user for numbers to add
				String userNumbers = input.readLine();  // Save the input to a variable
				ArrayList<Integer> numbers = getNumbers(userNumbers);  // Get the numbers in the input
				if(!isValidInput(userNumbers) || numbers == null) // Check that the numbers input are valid
				{
					System.out.print("Bad input..");  // Print message to user if input is incorrect 
				}
				else
				{
					System.out.println("Sum is: " + addNumbers(numbers));  // If input is valid, add the numbers and show sum
				}
			}
			// Break out of while loop when server returns joke/proverb
			// and show joke/proverb to user 
			System.out.println("\n" + asyncWorker.messageFromServer);  
			sock.close();
		}
		catch (IOException x) 
		{
			System.out.println ("Socket error.");
			x.printStackTrace ();
		}
	}//End getjoke()
	
	
	public static boolean isValidInput(String input)
	{
		return(input != null && !input.isEmpty());  // Null or empty check
	}
	
	/* The getNumbers() method splits the input at the space
	 * character and goes through the array, converts the 
	 * string to integers and puts the integers in a list.
	 * If the conversion to integer fails then a value of null
	 * is returned, if the conversion is successful, the list
	 * of integers is entered.  This means that in the calling
	 * method, you just need to check for null to make sure that
	 * valid numbers were input by the user
	 *  
	 */
	public static ArrayList<Integer> getNumbers(String input)
	{
		String[] numbers = input.split(" ");
		ArrayList<Integer> numbersToAdd = new ArrayList<Integer>();
		
		try
		{
			for(String s : numbers)
			{
				numbersToAdd.add(Integer.parseInt(s));
			}
		}
		catch(NumberFormatException nfe)
		{
			return null;
		}
		return numbersToAdd;
	}
	
	
	/* THe addNumbers() method takes the list of integers returned from
	 * the getNumbers() method, adds them together and returns the sum
	 * */
	public static int addNumbers(ArrayList<Integer> numbersToAdd)
	{
		int sum = 0;
		
		for(Integer n : numbersToAdd)
		{
			sum += n;
		}
		return sum;
	}
	
} // End class AsyncJokeClient
