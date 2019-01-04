import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

class Worker extends Thread
{
	Socket socket; 
	int port;
	
	String joke;
	String[] jokes = new String[4];
	String[] proverbs = new String[4];
	
	static int jIndex;
	static int pIndex;
	
	ArrayList<String> jokePrefixList = new ArrayList<>();
	ArrayList<String> proverbPrefixList = new ArrayList<>();
	
	Worker (Socket sock, int p)
	{
		socket = sock;
		port = p;
		
		initializeJokesAndProverbsLists();
		initializeJokesAndProverbsPrefixLists();
	}
	
	private void initializeJokesAndProverbsPrefixLists()
	{
		jokePrefixList.add("JA");
		jokePrefixList.add("JB");
		jokePrefixList.add("JC");
		jokePrefixList.add("JD");
		
		proverbPrefixList.add("PA");
		proverbPrefixList.add("PB");
		proverbPrefixList.add("PC");
		proverbPrefixList.add("PD");
	}
	
	private void initializeJokesAndProverbsLists()
	{
		jokes[0] = "JA <#name>: A recent scientific study showed that out of 2,293,618,367 people, 94% are too lazy to actually read that number.";
		jokes[1] = "JB <#name>: What time did the man go to the dentist? Tooth hurt-y";
		jokes[2] = "JC <#name>: Why did the Clydesdale give the pony a glass of water? Because he was a little horse!";		
		jokes[3] = "JD <#name>: I had a dream that I was a muffler last night. I woke up exhausted!";		

		proverbs[0] = "PA <#name>: There is no such thing as a free lunch";
		proverbs[1] = "PB <#name>: Early to bed, early to rise";
		proverbs[2] = "PC <#name>: A picture is worth a thousand words";
		proverbs[3] = "PD <#name>: Absence makes the heart grow fonder";	
	}
	
	
	public void run()
	{
		BufferedReader inputStream = null;
		PrintStream outputStream = null;
		
		try
		{
			inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			outputStream = new PrintStream(socket.getOutputStream());
			
			try
			{
				
				//socket.close();
				//sendMessageViaUDP("Hello", port, "Urvi");
				//String joke = "Sending joke UDP";
				String name = inputStream.readLine();
				String uuid = inputStream.readLine();
				String message = "";
				
				if(AsyncJokeServer.serverMode.equals("proverb-mode"))
				{
					message = getMessage(uuid, name, proverbs, AsyncJokeServer.proverbMap, proverbPrefixList);
					socket.close();
					Thread.sleep(40000);
					sendMessageViaUDP(message, port, name);
				}
				else
				{
					message = getMessage(uuid, name, jokes, AsyncJokeServer.jokeMap, jokePrefixList);
					socket.close();
					Thread.sleep(40000);
					sendMessageViaUDP(message, port, name);
				}
				
			}
			catch(IOException ex)
			{
				System.out.println("Server read error"); 
				ex.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			  
		}
		catch(IOException e)
		{
			System.out.println(e); 
		}
	}
	
	//public String sendMessage(String id, String user, PrintStream out, String[] messageArray, Map<String, ArrayList<String>> map, ArrayList<String> prefixList)
	public String getMessage(String id, String user, String[] messageArray, Map<String, ArrayList<String>> map, ArrayList<String> prefixList)
	{
		String messageFromServer = "";
		
		/* Holds the indexes for jokes[] or proverbs[] array for the jokes or proverbs that have 
		 * already been sent to the client */
		ArrayList<Integer> existingIndexes = new ArrayList<>();
		
		// Check if the user has connected before
		if(map.get(id) == null)
		{
			/* If not, create an entry for the user in the appropriate hash map 
			 * with the user's id as the key and an empty list */
			map.put(id, new ArrayList<String>()); 
			
			 /* Since the user has not connected and he/she has not received any
			  * thing from the server as of yet.  This means that any joke or 
			  * proverb is valid. The message array contains 4 items, so get a
			  * number from 0 to 3 and get the joke or proverb in the joke or proverb array 
			  * at that index.  Then, send it to the client on the output stream.*/
			Random pRandom = new Random();
			int pIndex = pRandom.nextInt(4);
			messageFromServer = messageArray[pIndex];
			messageFromServer = messageFromServer.replaceAll("<#name>", user);
			//out.println(messageFromServer);
			
			//Add the joke or proverb to the user's list of jokes or proverbs that have been sent 
			map.get(id).add(messageFromServer);
			return messageFromServer;
			//printMap(map, user);
		}
		else //The else statement is hit when the user has connected previously
		{
			/* Get the jokes or proverbs that have already been sent to this user by
			looking them up in the appropriate hash map*/
			ArrayList<String> msgs = map.get(id);
			
			 /* Get the indexes into to the joke or proverb array for the 
			  * jokes or proverbs that have already been sent to the user */
			existingIndexes = getIndexesOfPriorMessages(id, map, prefixList);
			
			/* Get a random number between 0 and 3 and if that the 
			 * generated number is in the list of indexes that
			 * represent jokes/proverbs already sent to the user, keep getting
			 * a random number until you get one that is not in the list of 
			 * indexes for jokes or proverbs already sent to the client*/
			Random r1 = new Random();
			int randomIndex1 = r1.nextInt(4);
			
			while(existingIndexes.contains(randomIndex1)) 
			{
				randomIndex1 = r1.nextInt(4);
			}
			
			/* Once an index if found, use it to index into the jokes or proverbs
			 * array to find a joke or proverb that has not yet been sent to the
			 * client, insert the user name into the joke or proverb and send the
			 * joke or proverb to the client.
			 * 
			 * Lastly, add that joke or proverb to the list of jokes or proverbs for
			 * that user id */
			messageFromServer = messageArray[randomIndex1];
			messageFromServer = messageFromServer.replaceAll("<#name>", user);
			//out.println(messageFromServer);
			msgs.add(messageFromServer);
			
			//printMap(map, user);
			
			if(msgs.size() == 4)
			{
				msgs.clear();
			}
			return messageFromServer;
		}
	}// End sendMessage()
	
	ArrayList<Integer> getIndexesOfPriorMessages(String id, Map<String, ArrayList<String>> map, ArrayList<String> prefixes)
	{
		ArrayList<String> msgs = map.get(id);
		ArrayList<Integer> mIndexes = new ArrayList<>();
		
		for(String s : msgs)
		{
			String prefix = s.substring(0, 2);
			mIndexes.add(prefixes.indexOf(prefix));
		}
		return mIndexes;
	}
	
	static void sendMessageViaUDP(String msg, int port, String name) throws IOException
	{
		try
		{
			DatagramSocket serverSocket = new DatagramSocket();
			byte[] sendData = new byte[msg.getBytes().length];
			sendData = msg.getBytes();
			InetAddress ip = InetAddress.getByName("localhost");
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ip, port);
			System.out.println("Server sending packet");
			serverSocket.send(sendPacket);
		}
		catch(SocketException se)
		{
			
		}
		catch(UnknownHostException uhe)
		{
			
		}	
	}
}

public class AsyncJokeServer 
{
	public static String serverMode = "joke-mode";  // Default mode is joke-mode
	
	public static Map<String, ArrayList<String>> jokeMap = new HashMap<>();
	public static Map<String, ArrayList<String>> proverbMap = new HashMap<>();
	
	public static void main(String a[]) throws IOException
	{
		int q_len = 6;
		int port = 4546;
		Socket socket;
		ServerSocket serverSocket = new ServerSocket(port, q_len);
		
		JokeAdminServer jokeAdminServer = new JokeAdminServer();
	    Thread t = new Thread(jokeAdminServer);
	    t.start();
		
		System.out.println("Inet server 1.8 starting....listening at port " + port);
		while(true)
		{
			socket = serverSocket.accept(); 
			new Worker(socket, port).start(); 
		}
	}
}

class JokeAdminServer implements Runnable 
{
	  public static boolean adminControlSwitch = true;

	  // Running the JokeClientAdmin listen loop
	  public void run()
	  { 
	    int q_len = 6; // Maximum length of the queue for incoming connections.  If this is exceeded, connection is refused
	    int port = 5050;  // Admin client is listening at port 5050 at a different port for Admin clients
	    Socket sock; // A Socket object that will be used for communication with the client

	    try
	    {
	     
	     /*Create server socket that listens for requests from a client on the port hard-coded above.
	     * The server socket allows up to 6 connections */
	      ServerSocket servsock = new ServerSocket(port, q_len);
	      
	      while (adminControlSwitch) 
	      {
	    	  /*The server socket will listen for a connection to the socket and accept it once a connection 
				 has been made, creating a new socket to use for communication with the client.  Program execution 
				 is blocked until connection is made */
	    	  sock = servsock.accept();
	    	  /*Start Worker thread to handle the actual processing of client request. The socket that 
				 * was created is passed to the Worker constructor. The Worker class extends the
				 * Thread class, so it is a thread),  */
	    	  new AdminWorker (sock).start(); 
	      }
	      //servsock.close();
	    }catch (IOException ioe) 
	    {
	    	System.out.println(ioe);
	    }
	  }
}//End class JokeAdminServer

/* This is the class for the JokeClientAdmin's worker thread.  
 * The constructor takes in a socket, which is a connection to
 * the server on port 5050, and sets this socket to its local
 * variable, adminSocet.*/
class AdminWorker extends Thread
{
	Socket adminSocket;
	String mode = "";
	
	AdminWorker (Socket sock)
	{
		adminSocket = sock;
	}
	
	/*The run() method is called when the thread is started and processes the client request.
	 * It creates input/output streams, read in a mode from the user and passes this to 
	 * the server */
	public void run()
	{
		BufferedReader inputStream = null;
		PrintStream outputStream = null;
		
		try
		{
			inputStream = new BufferedReader(new InputStreamReader(adminSocket.getInputStream())); // Stream to read from
			outputStream = new PrintStream(adminSocket.getOutputStream()); // Stream to write to
			
			outputStream.println(AsyncJokeServer.serverMode);  // Send current mode to JokeAdminClient so it can be toggled
			outputStream.flush();
			
			String mode = inputStream.readLine();  // Read in the mode from the AdminClient user
			System.out.println("Mode is: " + mode); // Print a message to the console on server
			
			outputStream.println("Setting mode to " + mode);  // Send message to JokeClientAdmin user that mode is being set
			AsyncJokeServer.serverMode = mode; // Set the server mode to that which the user entered
			adminSocket.close();  // Close the socket to prevent resource leak
		}//End try
		catch(IOException e)
		{
			System.out.println(e); //Exception creating the input/output streams
		}
	}
}// End class AdminWorker