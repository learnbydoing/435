import java.io.*; // Get the Input Output libraries
import java.net.*; // Get the Java networking libraries
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
			DatagramSocket clientSocket = new DatagramSocket(asyncPort);
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			clientSocket.receive(receivePacket);
			String jokeOrProverb = new String(receivePacket.getData());
			messageFromServer = jokeOrProverb;
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
	private static final int PORT_NUMBER = 4546;
	public static final UUID uuid = UUID.randomUUID();
	
	public static void main (String args[]) 
	{
		String serverName;
		if (args.length < 1) 
			serverName = "localhost";
		else serverName = args[0];

		System.out.println("Async Joke Client, 1.8.\n");
		System.out.println("Using server: " + serverName + ", Port: " + PORT_NUMBER);
	
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		try 
		{
			String name;
			String input;
			System.out.print("Enter your name, (quit) to end: ");
			System.out.flush ();
			name = in.readLine ();
			do 
			{
				input = in.readLine();
				if (input.indexOf("quit") < 0)
				{
					getJoke(name, serverName, PORT_NUMBER, in);
					
				}

			} while (input.indexOf("quit") < 0);
			System.out.println ("Cancelled by user request.");
		} 
		catch (IOException x) 
		{
			x.printStackTrace ();
		}
	}//End main()


	static void getJoke(String name, String serverName, int port, BufferedReader input)
	{
		Socket sock;
		PrintStream toServer;
		
		try
		{
			AsyncWorker asyncWorker = new AsyncWorker(port);
			asyncWorker.start();
			
			sock = new Socket(serverName, port);
			// Create filter I/O streams for the socket:
			//fromServer = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			toServer = new PrintStream(sock.getOutputStream());
			toServer.println(name); 
			toServer.flush();
			toServer.println(uuid);
			System.out.println("Waiting...");

			while(asyncWorker.messageFromServer == null || asyncWorker.messageFromServer.isEmpty())
			{
				System.out.print("Enter integers to add, separated by a space");
				String userNumbers = input.readLine();
				ArrayList<Integer> numbers = getNumbers(userNumbers);
				if(!isValidInput(userNumbers) || numbers == null)
				{
					System.out.print("Bad input..enter integers to add separated by a space");
					userNumbers = input.readLine();
				}
				else
				{
					System.out.println("Sum is: " + addNumbers(numbers));
				}
				//Thread.sleep(10000);
			}
			System.out.println("Your joke or proverb is: " + asyncWorker.messageFromServer);
			sock.close();
		}
		catch (IOException x) 
		{
			System.out.println ("Socket error.");
			x.printStackTrace ();
		}
//		catch(InterruptedException ie)
//		{
//			ie.printStackTrace();
//		}
	}//End getjoke()
	
	
	public static boolean isValidInput(String input)
	{
		return(input != null && !input.isEmpty());
	}
	
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
