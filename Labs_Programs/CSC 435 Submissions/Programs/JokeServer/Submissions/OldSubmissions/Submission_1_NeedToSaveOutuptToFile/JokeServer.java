import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

class Worker extends Thread
{
	Socket socket; 
	
	String joke;
	String[] jokes = new String[4];
	String[] proverbs = new String[4];
	
	static int jIndex;
	static int pIndex;
	
	ArrayList<String> jokePrefixArray = new ArrayList<>();
	ArrayList<String> proverbPrefixArray = new ArrayList<>();
	
	Worker (Socket sock)
	{
		socket = sock;
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
		jokePrefixArray.add("JA");
		jokePrefixArray.add("JB");
		jokePrefixArray.add("JC");
		jokePrefixArray.add("JD");
		
		proverbPrefixArray.add("PA");
		proverbPrefixArray.add("PB");
		proverbPrefixArray.add("PC");
		proverbPrefixArray.add("PD");
	}
	
	/* Populate the lists that will hold jokes and proverbs,
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
		jokes[0] = "JA <#name>: This is joke #1";
		jokes[1] = "JB <#name>: This is joke #2";
		jokes[2] = "JC <#name>: This is joke #3";
		jokes[3] = "JD <#name>: This is joke #4";
		
		proverbs[0] = "PA <#name>: This is proverb #1";
		proverbs[1] = "PB <#name>: This is proverb #2";
		proverbs[2] = "PC <#name>: This is proverb #3";
		proverbs[3] = "PD <#name>: This is proverb #4";	
	}
	
	public void run()
	{
		BufferedReader inputStream = null;
		PrintStream outputStream = null;
		String jokeFromServer = "";
		String proverbFromServer = "";
		
		ArrayList<Integer> existingJokeIndexes = new ArrayList<>();
		ArrayList<Integer> existingProverbIndexes = new ArrayList<>();
		
		try
		{
			/*Create input and output streams to read data from and write data to the socket,
			 * thus allowing communication between server and client.
			 * The underlying input stream that is read from is the socket's input stream.
			 * The underlying output stream that is written to is the socket's output stream */
			inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			outputStream = new PrintStream(socket.getOutputStream());
			
			try
			{
				// Get the user's name sent from the client and put it in a variable
				String user = inputStream.readLine();
				
				//Get the id sent from the user and put it in a variable
				String id = inputStream.readLine(); 

				/* Default mode is joke-mode, and any string other than "proverb-mode" or
				 * "quit" is taken to be "joke-mode*/
				if(JokeServer.serverMode.equals("proverb-mode"))
				{
					 // Check if the user has connected before
					if(JokeServer.proverbMap.get(id) == null)
					{
						/* If not, create an entry for the user in the proverb hash map 
						 * with the user's id as the key and an empty list */
						JokeServer.proverbMap.put(id, new ArrayList<String>()); 
						
						 /* Since the user has not connected and he/she has not received proverbs, 
						  * any proverb is valid. The proverb array contains 4 items, so get a
						  * number from 0 to 3 and get the proverb in the proverb array at that
						  * index.  Then, send this proverb to the client on the output stream.*/
						Random pRandom = new Random();
						int pIndex = pRandom.nextInt(4);
						proverbFromServer = proverbs[pIndex];
						proverbFromServer = proverbFromServer.replaceAll("<#name>", user);
						outputStream.println(proverbFromServer);
						
						//Add the proverb to the user's list of proverbs that have been sent 
						JokeServer.proverbMap.get(id).add(proverbFromServer);
						printProverbMap();
					}
					else //The else statement is hit when the user has connected previously
					{
						/* Get the proverbs that have already been sent to this user by
						looking them up in the proverb hash map*/
						ArrayList<String> clientProverbs = JokeServer.proverbMap.get(id);
						
						 /* Get the indexes into to the proverb array for the proverbs that
						  *  have already been sent to the user */
						existingProverbIndexes = getIndexesOfPriorProverbs(id);
						
						/* Get a random number between 0 and 3 and if that the 
						 * generated number is in the list of indexes that
						 * represent jokes already sent to the user, keep getting
						 * a random number until you get one that is not in the list of 
						 * indexes for proverbs already sent to the client*/
						Random r1 = new Random();
						int randomIndex1 = r1.nextInt(4);
						
						while(existingProverbIndexes.contains(randomIndex1)) 
						{
							randomIndex1 = r1.nextInt(4);
						}
						
						/* Once an index if found, use it to index into the proverbs
						 * array, insert the user name into the proverb and send the
						 * proverb to the client.
						 * 
						 * Lastly, add that proverb to the list of proverbs for
						 * that user id */
						proverbFromServer = proverbs[randomIndex1];
						proverbFromServer = proverbFromServer.replaceAll("<#name>", user);
						outputStream.println(proverbFromServer);
						clientProverbs.add(proverbFromServer);
						printProverbMap();
						
						if(clientProverbs.size() == 4)
						{
							clientProverbs.clear();
						}
					}
				}
				else  //This else is hit if the server is in joke-mode
				{	
					if(JokeServer.jokeMap.get(id) == null)
					{
						JokeServer.jokeMap.put(id, new ArrayList<String>());
						Random jRandom = new Random();
						int jIndex = jRandom.nextInt(4);
						jokeFromServer = jokes[jIndex];
						jokeFromServer = jokeFromServer.replaceAll("<#name>", user);
						outputStream.println(jokeFromServer);
						
						JokeServer.jokeMap.get(id).add(jokeFromServer);
						printJokeMap();
					}
					else
					{
						ArrayList<String> clientJokes = JokeServer.jokeMap.get(id);
						existingJokeIndexes = getIndexesOfPriorJokes(id);
						Random r2 = new Random();
				        int randomIndex2 = r2.nextInt(4);
				        while(existingJokeIndexes.contains(randomIndex2))
				        {
				        	randomIndex2 = r2.nextInt(4);
				        }
				        jokeFromServer = jokes[randomIndex2];
				        jokeFromServer = jokeFromServer.replaceAll("<#name>", user);
						outputStream.print(jokeFromServer);
						clientJokes.add(jokeFromServer);
						printJokeMap();
			
						if(clientJokes.size() == 4)
						{
							clientJokes.clear();
						}
					}
				}
			}//End inner try
			catch(IOException ex)
			{
				System.out.println("Server read error"); //Exception trying to read from input stream or write to output stream
				ex.printStackTrace();
			}
			socket.close();
		}//End outer try
		catch(IOException e)
		{
			System.out.println(e); //Exception creating the input/output streams
		}
	}
	
	public void processProverb(String id, String user, PrintStream out)
	{
		String proverbFromServer = "";
		ArrayList<Integer> existingProverbIndexes = new ArrayList<>();
		// Check if the user has connected before
		if(JokeServer.proverbMap.get(id) == null)
		{
			/* If not, create an entry for the user in the proverb hash map 
			 * with the user's id as the key and an empty list */
			JokeServer.proverbMap.put(id, new ArrayList<String>()); 
			
			 /* Since the user has not connected and he/she has not received proverbs, 
			  * any proverb is valid. The proverb array contains 4 items, so get a
			  * number from 0 to 3 and get the proverb in the proverb array at that
			  * index.  Then, send this proverb to the client on the output stream.*/
			Random pRandom = new Random();
			int pIndex = pRandom.nextInt(4);
			proverbFromServer = proverbs[pIndex];
			proverbFromServer = proverbFromServer.replaceAll("<#name>", user);
			out.println(proverbFromServer);
			
			//Add the proverb to the user's list of proverbs that have been sent 
			JokeServer.proverbMap.get(id).add(proverbFromServer);
			printProverbMap();
		}
		else //The else statement is hit when the user has connected previously
		{
			/* Get the proverbs that have already been sent to this user by
			looking them up in the proverb hash map*/
			ArrayList<String> clientProverbs = JokeServer.proverbMap.get(id);
			
			 /* Get the indexes into to the proverb array for the proverbs that
			  *  have already been sent to the user */
			existingProverbIndexes = getIndexesOfPriorProverbs(id);
			
			/* Get a random number between 0 and 3 and if that the 
			 * generated number is in the list of indexes that
			 * represent jokes already sent to the user, keep getting
			 * a random number until you get one that is not in the list of 
			 * indexes for proverbs already sent to the client*/
			Random r1 = new Random();
			int randomIndex1 = r1.nextInt(4);
			
			while(existingProverbIndexes.contains(randomIndex1)) 
			{
				randomIndex1 = r1.nextInt(4);
			}
			
			/* Once an index if found, use it to index into the proverbs
			 * array, insert the user name into the proverb and send the
			 * proverb to the client.
			 * 
			 * Lastly, add that proverb to the list of proverbs for
			 * that user id */
			proverbFromServer = proverbs[randomIndex1];
			proverbFromServer = proverbFromServer.replaceAll("<#name>", user);
			out.println(proverbFromServer);
			clientProverbs.add(proverbFromServer);
			printProverbMap();
			
			if(clientProverbs.size() == 4)
			{
				clientProverbs.clear();
			}
		}
	}// End ProcessProverb()
	
	
	
	
	/* This method takes the user id as parameter 
	 * 
	 * This method returns a list of integers that are indexes of 
	 * the jokes array for the jokes that have already been sent to
	 * the user
	 * 
	 * It first gets the list of jokes that have already been sent to this user
	 * by doing a lookup of the jokeMap hash table, using the passed-in id as
	 * the key. 
	 * 
	 * Then, for each joke in the list, it pulls off the the prefix,
	 * for example, JA, JC, and JD and uses indexOf() on the jokePrefixArray 
	 * to find the corresponding index for each prefix.  
	 * 
	 * For the example above, for JA, JC, and JD, the method would return 
	 * the list [0, 2, 3]
	 */
	ArrayList<Integer> getIndexesOfPriorJokes(String id)
	{
		ArrayList<String> cJokes = JokeServer.jokeMap.get(id);
		ArrayList<Integer> jIndexes = new ArrayList<>();
		
		for(String s : cJokes)
		{
			String prefix = s.substring(0, 2);
			jIndexes.add(jokePrefixArray.indexOf(prefix));
		}
		return jIndexes;
	}
	
	/* This method takes the user id as a parameter 
	 * 
	 * This method returns a list of integers that will
	 * be used to index into proverb array for proverbs 
	 * that have been sent to the user 
	 * 
	 * 
	 * It uses  the same logic as that the getIndexesOfPriorJokes 
	 * method, but instead accesses the proverb map and gets the 
	 * proverbs sent to the user
	 * 
	 */
	ArrayList<Integer> getIndexesOfPriorProverbs(String id)
	{
		ArrayList<String> cProverbs = JokeServer.proverbMap.get(id);
		ArrayList<Integer> pIndexes = new ArrayList<>();
		
		for(String s : cProverbs)
		{
			String prefix = s.substring(0, 2);
			//System.out.println("Prefix is " + prefix);
			pIndexes.add(proverbPrefixArray.indexOf(prefix));
		}
		return pIndexes;
	}
	
	
	void printJokeMap()
	{
		for(String k:JokeServer.jokeMap.keySet())
		{
			ArrayList<String> value = JokeServer.jokeMap.get(k);
			System.out.println(k + ":");
			
			for(String s:value)
			{
				System.out.println(s);
			}
		}
	}
	
	void printProverbMap()
	{
		for(String k:JokeServer.proverbMap.keySet())
		{
			ArrayList<String> value = JokeServer.proverbMap.get(k);
			System.out.println(k + ":");
			
			for(String s:value)
			{
				System.out.println(s);
			}
		}
	}
	
} //End class Worker


public class JokeServer 
{
	public static boolean controlSwitch = true;
	public static String serverMode = "joke-mode";
	public static Map<String, ArrayList<String>> jokeMap = new HashMap<>();
	public static Map<String, ArrayList<String>> proverbMap = new HashMap<>();
	
	public static void main(String a[]) throws IOException
	{
		int q_len = 6;
		int port = 4000;
		Socket socket;
		ServerSocket serverSocket = new ServerSocket(port, q_len);
		
		AdminLooper AL = new AdminLooper(); // create a DIFFERENT thread
	    Thread t = new Thread(AL);
	    t.start();  // ...and start it, waiting for administration input
		
		System.out.println("Joke server 1.8 starting....listening at port " + port);
		while(controlSwitch)
		{
			socket = serverSocket.accept(); 
			new Worker(socket).start(); 
		}
	}
}//End class JokeServer

class AdminLooper implements Runnable {
	  public static boolean adminControlSwitch = true;

	  public void run()
	  { // Running the Admin listen loop
	    System.out.println("In the admin looper thread");
	    
	    int q_len = 6; /* Number of requests for OpSys to queue */
	    int port = 5050;  // We are listening at a different port for Admin clients
	    Socket sock;

	    try
	    {
	      ServerSocket servsock = new ServerSocket(port, q_len);
	      while (adminControlSwitch) 
	      {
	    	  // wait for the next ADMIN client connection:
	    	  sock = servsock.accept();
	    	  new AdminWorker (sock).start(); 
	      }
	      servsock.close();
	    }catch (IOException ioe) 
	    {
	    	System.out.println(ioe);
	    }
	  }
}//End class AdminLooper

class AdminWorker extends Thread
{
	Socket adminSocket;
	String mode = "";
	
	AdminWorker (Socket sock)
	{
		adminSocket = sock;
	}
	
	public void run()
	{
		BufferedReader inputStream = null;
		PrintStream outputStream = null;
		
		try
		{
			inputStream = new BufferedReader(new InputStreamReader(adminSocket.getInputStream()));
			outputStream = new PrintStream(adminSocket.getOutputStream());
			String mode = inputStream.readLine();
			System.out.println("Mode is: " + mode);
			outputStream.println("Setting mode to " + mode);
			JokeServer.serverMode = mode;
			adminSocket.close();
		}//End inner try
		catch(IOException e)
		{
			System.out.println(e); //Exception creating the input/output streams
		}
	}
}