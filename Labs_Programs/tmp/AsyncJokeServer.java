import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

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
		//PrintStream outputStream = null;
		
		try
		{
			inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			//outputStream = new PrintStream(socket.getOutputStream());
			
			try
			{
				String name = inputStream.readLine();
				String uuid = inputStream.readLine();
				String message = "";
				
				if(AsyncJokeServer.serverMode.equals("proverb-mode"))
				{
					message = getMessage(uuid, name, proverbs, AsyncJokeServer.proverbMap, proverbPrefixList);
					socket.close();
					Thread.sleep(10000);
					sendMessageViaUDP(message, port, name);
				}
				else
				{
					message = getMessage(uuid, name, jokes, AsyncJokeServer.jokeMap, jokePrefixList);
					socket.close();
					Thread.sleep(10000);
					sendMessageViaUDP(message, port, name);
				}
				
			}
			catch(IOException ex)
			{
				System.out.println("Server read error"); 
				ex.printStackTrace();
			} 
			catch (InterruptedException e) 
			{
				e.printStackTrace();
			}
			  
		}
		catch(IOException e)
		{
			System.out.println(e); 
		}
	}
	
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
		int port = 4545;
		
//		if(a.length == 1)
//		{
//			try
//			{
//				port = Integer.parseInt(a[0]);
//			}
//			catch(NumberFormatException nfe)
//			{
//				System.out.println("Invalid port");
//				return;
//			}
//		}
		Socket socket;
		ServerSocket serverSocket = new ServerSocket(port, q_len);
		
		JokeAdminServer jokeAdminServer = new JokeAdminServer();
	    Thread t = new Thread(jokeAdminServer);
	    t.start();
	    
	    if(a.length == 1) //Detect an argument
		{ 
			if(a[0].equals("secondary")) //If it is "secondary", then start the secondary Joke and Admin servers
			{	
				SecondaryAsyncJokeServer secondaryJokeServer = new SecondaryAsyncJokeServer();
				Thread thread = new Thread(secondaryJokeServer);
				thread.start();
				
				SecondaryAdminServer secondaryAdminServer = new SecondaryAdminServer();
				Thread secAdminThread = new Thread(secondaryAdminServer);
				secAdminThread.start();
			}
			else
			{
				System.out.println("Invalid parameter");
			}
		}
		
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


/*********************************************** CODE FOR SECONDARY ASYNC SERVER ***********************************/

class SecondaryAsyncWorker extends Thread
{
	Socket secSocket;
	int port;
	
	String joke;
	String[] jokes = new String[4];
	String[] proverbs = new String[4];
	
	static int jIndex;
	static int pIndex;
	
	ArrayList<String> jokePrefixList = new ArrayList<>();
	ArrayList<String> proverbPrefixList = new ArrayList<>();
	
	SecondaryAsyncWorker (Socket sock, int p)
	{
		secSocket = sock;
		port = p;
		initializeJokesAndProverbsLists();
		initializeJokesAndProverbsPrefixLists();
	}
	

	/* These ArrayLists are used to help keep track of the jokes/proverbs that have 
	 * already been sent to the client.  The jokes/proverbs are saved in separate
	 * hash tables where a UUID (provided by the user) is the key and the value is 
	 * a list of jokes/proverbs for that user.  The approach taken was to loop 
	 * through the list of jokes/proverbs for a given user and pull off the prefixes  
	 * and store them in a list. Then, for every prefix in this list, use the 
  	 * indexOf() method on the jokePrefixArray and proverbPrefixArray to 
	 * find the indexes for the jokes/proverbs that have already been used.
	 * 
	 * For example, assume client was sent jokes B and C:
	 * 
	 * Access the user's entry in the joke table using the 
	 * id sent by the client.
	 * 
	 * Loop through the list of jokes for this client, pull off the 
	 * prefixes for jokes B and C.  The list would look like => [JB, JC]
	 * 
	 * Use the fact that the first joke in the joke array is prefixed 
	 * with JA, the second with JB, etc., you can use the indexOf 
	 * method on the jokePrefixArray to  find the corresponding index 
	 * into the joke array. In this case, call to indexOf for JB would
	 * give 1, and that means that jokes[1] has already been sent to 
	 * the client.
	 * 
	 * A similar example would apply for proverbs.
	 * */
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
	
	/* This method populates the lists that will hold jokes and proverbs,
	 * in order. That is, the first entry holds joke A, the
	 * second hold joke B, etc. Since you don't want to send
	 * the same joke to the client until all 4 jokes are sent,
	 * you need to keep track of which jokes are "valid" to send 
	 * to the user.  This arrangement makes it simple to know
	 * the valid indexes in this array once you learn which 
	 * joke(s) have already been sent to the client.  For 
	 * example, if you know that joke A has been sent, then 
	 * you know that the first index is not valid.
	 * 
	 * The "<#name>" is used as a placeholder so that the uer's
	 * name can be inserted into the jokes and proverbs sent
	 * from the server.*/
	private void initializeJokesAndProverbsLists()
	{
		jokes[0] = "JA <#name>: I saw a wino eating grapes.I told him, you gotta wait. (Mitch Hedberg)";
		jokes[1] = "JB <#name>: I couldn’t believe that the highway department called my dad a thief. But when I got home, all the signs were there.";
		jokes[2] = "JC <#name>: Why shouldn't you write with a broken pencil? Because it's pointless. ";		
		jokes[3] = "JD <#name>: What did 0 say to 8? Nice belt!";		

		proverbs[0] = "PA <#name>: Beauty is in the eye of the beholder";
		proverbs[1] = "PB <#name>: Don’t bite the hand that feeds you";
		proverbs[2] = "PC <#name>: Fortune favors the bold";
		proverbs[3] = "PD <#name>: There is no time like the present";	
	}
	
	public void run()
	{
		BufferedReader inputStream = null;
		PrintStream outputStream = null;
		
		try
		{
			/*Create input and output streams to read data from and write data to the socket,
			 * thus allowing communication between server and client.
			 * The underlying input stream that is read from is the socket's input stream.
			 * The underlying output stream that is written to is the socket's output stream */
			 
			 inputStream = new BufferedReader(new InputStreamReader(secSocket.getInputStream()));
			 outputStream = new PrintStream(secSocket.getOutputStream());
			
			try
			{
				// Get the user's name sent from the client and put it in a variable
				String name = inputStream.readLine();
				
				//Get the id sent from the user and put it in a variable
				String uuid = inputStream.readLine(); 

				/* Default mode is joke-mode, and any string other than "proverb-mode" or
				 * "quit" is taken to be "joke-mode*/
				
				/* Get the mes*/
				String message = "";
				
				if(AsyncJokeServer.serverMode.equals("proverb-mode"))
				{
					message = getMessage(uuid, name, proverbs, AsyncJokeServer.proverbMap, proverbPrefixList);
					secSocket.close();
					Thread.sleep(10000);
					sendMessageViaUDP(message, port, name);
				}
				else
				{
					message = getMessage(uuid, name, jokes, AsyncJokeServer.jokeMap, jokePrefixList);
					secSocket.close();
					Thread.sleep(10000);
					sendMessageViaUDP("<S2>" + message, port, name);
				}
			}//End inner try
			catch(IOException ex)
			{
				System.out.println("Server read error"); //Exception trying to read from input stream or write to output stream
				ex.printStackTrace();
			}
			catch (InterruptedException e) 
			{
				e.printStackTrace();
			}
		}//End outer try
		catch(IOException e)
		{
			System.out.println(e); //Exception creating the input/output streams
		}
	}// End run()
	
	
	
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
	}// End getMessage()
	
	/* This method takes the user's name, user id, the output stream for the server, an array 
	 * that contains jokes or proverbs, and a hash map that consists of the jokes or proverbs
	 * that were sent to the user with the given id
	 * 
	 * The method finds a joke or proverb (depending on the hash map) that has not yet been
	 * sent to the user and sends it.
	 */
	public void sendMessage(String id, String user, PrintStream out, String[] messageArray, Map<String, ArrayList<String>> map, ArrayList<String> prefixList)
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
			out.println("<S2> " + messageFromServer);
			
			//Add the joke or proverb to the user's list of jokes or proverbs that have been sent 
			map.get(id).add(messageFromServer);
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
			out.println("<S2> " + messageFromServer);
			msgs.add(messageFromServer);
			
			//printMap(map, user);
			
			if(msgs.size() == 4)
			{
				msgs.clear();
			}
		}
	}// End sendMessage()
	
	
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
			se.printStackTrace();
		}
		catch(UnknownHostException uhe)
		{
			uhe.printStackTrace();
		}	
	}
	
	/* This method takes the user id, a hash map of jokes or proverbs, and
	 * a list prefixes for jokes or provers, i.e., [JA, JC] or [PA, PD]
	 * 
	 * This method returns a list of integers that are indexes of the jokes 
	 * or proverb array for the jokes or proverbs that have already been sent 
	 * to the user
	 * 
	 * It first gets the list of jokes or proverbs (depending on the hashmap that 
	 * has been passed in) that have already been sent to this user.  This is 
	 * accomplished by doing a lookup of the hash table, using the passed-in id as
	 * the key. 
	 * 
	 * Then, for each joke in the list, it pulls off the the prefix,
	 * for example, JA, JC, and JD and uses indexOf() on the jokePrefixArray 
	 * to find the corresponding index for each prefix.  
	 * 
	 * For the example above, for JA, JC, and JD, the method would return 
	 * the list [0, 2, 3]
	 */
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
	}// End getIndexesOfPriorMessages()
	

	//Print out the joke or proverb hash map to verify things are working as expected
	void printMap(Map<String, ArrayList<String>> m, String uName)
	{
		System.out.println("\nMap for: " + uName);
		for(String k:m.keySet())
		{
			ArrayList<String> value = m.get(k);
			System.out.print(k + ":");
			
			for(String s:value)
			{
				s = s.substring(0, 2);
				System.out.print(s + "\t");
			}
		}
	}// End printMap()
	
	void write(Map<String, ArrayList<String>> map, File f)
	{
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		
		try 
		{
			f = new File(f.getName());
			fos = new FileOutputStream(f);
			oos = new ObjectOutputStream(fos);
			oos.writeObject(map);
			oos.close();		
		} 
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e) {

			e.printStackTrace();
		}
	}// End write()
} //End class SecondaryAsyncWorker


class SecondaryAsyncJokeServer implements Runnable
{
	public static boolean secControlSwitch = true;
	public static String serverMode = "joke-mode";  // Default mode is joke-mode

	public static File secJokeFile;
	public static File secProverbFile;
	
	/* Hash maps to store the jokes and proverbs for each client (user).  The hash maps 
	 * use the UUID sent from the client as a key and the value is the list of jokes
	 * or proverbs that have been sent to that client */
	public static Map<String, ArrayList<String>> secJokeMap;
	public static Map<String, ArrayList<String>> secProverbMap;
	
	 public void run()
	  { 
	    int qSec_len = 6; // Maximum length of the queue for incoming connections.  If this is exceeded, connection is refused
	    int secPort = 4546;  // Secondary server is listening at a different port than primary server
	    Socket secSock; // A Socket object that will be used for communication with the client
	    
//	    FileInputStream jokeFileIn = null;
//		FileInputStream proverbFileIn = null;
//		
//		ObjectInputStream jokeMapIn = null;
//		ObjectInputStream proverbMapIn = null;
		
		//Begin
//		secJokeFile = new File("joke.out");
//	    secProverbFile = new File("proverb.out");
//	    
//	    if(secJokeFile.exists())
//	    {
//	    	try
//	    	{
//	    	System.out.println("Seocndary joke file found, restoring state...");
//	    	jokeFileIn = new FileInputStream(secJokeFile);
//	    	jokeMapIn = new ObjectInputStream(jokeFileIn);
//	    	
//				secJokeMap = (Map<String, ArrayList<String>>) jokeMapIn.readObject();
//				jokeMapIn.close();
//			} 
//	    	catch (ClassNotFoundException e) 
//	    	{
//				e.printStackTrace();
//			}
//	    	catch(IOException ex)
//	    	{
//	    		ex.printStackTrace();
//	    	}
//	    }
//	    else
//	    {
//	    	//System.out.println("In JOKE else");
//	    	secJokeMap = new HashMap<>();
//	    }
//	    
//	    if(secProverbFile.exists())
//	    {
//	    	try 
//	    	{
//	    		System.out.println("Secondary proverb file found, restoring state...");
//	    		proverbFileIn = new FileInputStream(secProverbFile);
//	    		proverbMapIn = new ObjectInputStream(proverbFileIn);
//				secProverbMap = (Map<String, ArrayList<String>>) proverbMapIn.readObject();
//				proverbFileIn.close();
//			}
//	    	catch (ClassNotFoundException e) 
//	    	{
//				e.printStackTrace();
//			}
//	    	catch(IOException ex)
//	    	{
//	    		ex.printStackTrace();
//	    	}
//	    }
//	    else
//	    {
//	    	//System.out.println("In PROVERB else");
//	    	secProverbMap = new HashMap<>();
//	    }
		//End

	    try
	    {
	     
	     /*Create server socket that listens for requests from a client on the port hard-coded above.
	     * The server socket allows up to 6 connections */
	      ServerSocket secServsock = new ServerSocket(secPort, qSec_len);
	      System.out.println("Joke server 1.8 starting....listening at port " + secPort);
	      while (secControlSwitch) 
	      {
	    	  /*The server socket will listen for a connection to the socket and accept it once a connection 
				 has been made, creating a new socket to use for communication with the client.  Program execution 
				 is blocked until connection is made */
	    	  secSock = secServsock.accept();
	    	  /*Start Worker thread to handle the actual processing of client request. The socket that 
				 * was created is passed to the Worker constructor. The Worker class extends the
				 * Thread class, so it is a thread),  */
	    	  new SecondaryAsyncWorker(secSock, secPort).start();
	      }
	      //secServsock.close();
	    }
	    catch (IOException ioe) 
	    {
	    	System.out.println(ioe);
	    }
	  }// End run()
}// End class SecondaryJokeServer

class SecondaryAdminServer implements Runnable 
{
	public static boolean secAdminControlSwitch = true;
	
	public void run() 
	{
		int q_len = 6; // Maximum length of the queue for incoming connections.  If this is exceeded, connection is refused
	    int port = 5051;  // Admin client is listening at port 5050 at a different port for Admin clients
	    Socket sock; // A Socket object that will be used for communication with the client

	    try
	    {
	     
	     /*Create server socket that listens for requests from a client on the port hard-coded above.
	     * The server socket allows up to 6 connections */
	      ServerSocket servsock = new ServerSocket(port, q_len);
	      
	      while (secAdminControlSwitch) 
	      {
	    	  /*The server socket will listen for a connection to the socket and accept it once a connection 
				 has been made, creating a new socket to use for communication with the client.  Program execution 
				 is blocked until connection is made */
	    	  sock = servsock.accept();
	    	  /*Start Worker thread to handle the actual processing of client request. The socket that 
				 * was created is passed to the Worker constructor. The Worker class extends the
				 * Thread class, so it is a thread),  */
	    	  new SecondaryAdminWorker (sock).start(); 
	      }
	      //servsock.close();
	    }catch (IOException ioe) 
	    {
	    	System.out.println(ioe);
	    }
	}
}// End class SecondaryAdminServer


class SecondaryAdminWorker extends Thread
{
	Socket secAdminSocket;
	String mode = "";
	
	SecondaryAdminWorker (Socket sock)
	{
		secAdminSocket = sock;
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
			inputStream = new BufferedReader(new InputStreamReader(secAdminSocket.getInputStream())); // Stream to read from
			outputStream = new PrintStream(secAdminSocket.getOutputStream()); // Stream to write to
			
			outputStream.println(SecondaryAsyncJokeServer.serverMode); // Send current mode to JokeAdminClient so it can be toggled
			outputStream.flush();
			
			String mode = inputStream.readLine();  // Read in the mode from the AdminClient user
			System.out.println("<S2> Mode is: " + mode); // Print a message to the console on server
			
			outputStream.println("<S2> Setting mode to " + mode);  // Send message to AdminClient user that mode is being set
			SecondaryAsyncJokeServer.serverMode = mode; // Set the server mode to that which the user entered
			secAdminSocket.close();  // Close the socket to prevent resource leak
		}//End try
		catch(IOException e)
		{
			System.out.println(e); //Exception creating the input/output streams
		}
	}
} // End class SecondaryAdminWorker
 