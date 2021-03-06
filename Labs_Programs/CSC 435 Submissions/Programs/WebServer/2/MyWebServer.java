
import java.net.ServerSocket;
import java.net.Socket;
import java.io.* ;
import java.util.* ;

/*--------------------------------------------------------

1. Name: Urvi Patel

2. Due Date: 2/5/2017

2. Java version used: 1.8

3. Precise command-line compilation examples / instructions:

> javac MyWebServer.java

or

> javac *java

4. Precise examples / instructions to run this program:

e.g.:

In a shell window, type:

> java MyWebServer

Bring up Firefox and go to:

http://localhost:2540/<fileName> to get a file

or

http://localhost:2540/<directoryName> to see contents of a directory

or

http://condor.depaul.edu/elliott/435/hw/programs/mywebserver/addnums.html and enter a name and 2 numbers.
This webserver will add the numbers and send a message back to the client with the sum and the name entered


5. List of files needed for running the program.

e.g.:

 a. checklist.html
 b. MyWebServer.java

5. Notes:

None
----------------------------------------------------------*/


class Worker extends Thread
{
	Socket socket; 
	Worker (Socket sock)
	{
		socket = sock;
	}
	public void run()
	{
		BufferedReader inputStream = null;
		PrintStream outputStream = null;
		
		try
		{
			//Input and output streams to read data from and write data to the server
			inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			outputStream = new PrintStream(socket.getOutputStream());
			 			
			try
			{
				String request = inputStream.readLine(); //Get the GET request from the browser
				
				/* Null or empty check. There was a NPE thrown when MyWebServer was running,
				 * even when there was no address entered in the browser or any user 
				 * intervention at all. Sometimes the NPEs appeared right away, other times, 
				 * there was no NPE at all, even though the server had been running for 
				 * 10 - 15 minutes.  After some debugging, the reason for the NPE was found 
				 * to be that the request was null on occasion. I am guessing this must be 
				 * due to some threading issue.  To handle this, a check was placed here.  
				 * A catch for the NPE was also added.  Suspenders and a belt.  :)
				 * 
				 */
				if(request == null || request.isEmpty()) 
					return;
				
				//System.out.println(request); // For logging output
				String rest = inputStream.readLine(); //Not concerned with the rest, so eat it
				while(rest != null && !rest.isEmpty())
				{
					rest = inputStream.readLine();
					//System.out.println(rest); // For logging output
				}
				
				/*Verify that the request is valid, i.e. of form GET<space>/some/path/to/file/a.txt<space>HTTP/1.0 (or HTTP/1/1) 
				 * The substrings "GET " (5 character, with the space) and " HTTP/1.0" (or " HTTP/1.1") (9 characters, with the 
				 * space) will be present every request.  This means the request must be at least 5 + 9 = 13 characters long. 
				 */				
				if(request.startsWith("GET") && request.length() >= 13 && (request.endsWith("HTTP/1.0") || request.endsWith("HTTP/1.1"))) 
				{
					/*  
					 * The file name starts at the 5th position,so the substring is taken from index 4.
					 * Subtracting off the length of version number information from the length of the
					 * request leaves the path. The trim() method is called to remove any leading or
					 * trailing whitespace to ensure that file names are correct.
					 * */
					int reqLength = request.length();
					int versionLength = "HTTP/1.0".length();  
					String fileName = request.substring(4, reqLength - versionLength).trim();
					
					/*Looking for malicious user who tries to go to another directory*/
					 if (fileName.indexOf("..") != -1)
					 {
						 outputStream.print("Only access files in server directory");
							    
					 }
					 /* This check is done to determine a directory was requested.  The '/' will
					  * be the last character so this comes down to using charAt to find the c
					  * character at a certain index.  Since it is the last character in the 
					  * string, its index is the length of string minus 1 (since first index
					  * is 0)
					  * */
					 else if(fileName.charAt(fileName.length() - 1) == '/')
					 {
						 getFilesFromDirectory(outputStream, fileName);
					 }
					 /* This is simply checking if 'addnums' is in the request. This 
					  * means that a call needs to be made to an addnums method, in 
					  * this server itself, that calculates the sum of 2 numbers and
					  * sends a result back to the client.  The query is of the form
					  * 
					  * /cgi/addnums.fake-cgi?person=YourName&num1=4&num2=5
					  * 
					  * In order the excract the user name and numbers to add,
					  * the request is split at the "?", and taking the string 
					  * at index 1 gives:
					  * 
					  * person=YourName&num1=4&num2=5
					  * 
					  * Then this split at the "&" which gives an array of 3 elements:
					  * 
					  * person=YourName
					  * num1=4
					  * num2=5
					  * 
					  * The final step is to split each element at the "=" and take the
					  * string at index 1 to get the name and the numbers to add
					  * 
					  * */
					 else if(fileName.contains("addnums"))
					 {
						 String[] query = fileName.split("\\?");  //Needed to escape the "?" because it also used in reg expressions
						 String[] params = query[1].split("&");
						 String[] namePair = params[0].split("=");
						 String[] val1Pair = params[1].split("=");
						 String[] val2Pair = params[2].split("=");
						 addnums(outputStream, namePair[1], val1Pair[1], val2Pair[1]);
					 }
					 else //If none of the previous conditions are met, assume the request is for a file
					 {
						 retrieveFile(fileName, outputStream);
					 }
				}
				else 
				{
					outputStream.print("Malformed request"); //If the request is in the proper form, print message to user
				}
			}
			catch (NullPointerException npe) 
			{
				npe.printStackTrace(outputStream);
			}
			catch (Exception e) 
			{
					System.out.println(e);
			}
			if(socket != null)
			{
				socket.close();
			}
		}
		catch (NullPointerException npe) 
		{
			npe.printStackTrace(outputStream);
		}
		catch (Exception e) 
		{
			e.getStackTrace();
		}	
}//End run()
	
	private static void retrieveFile(String fileName, PrintStream outputStream)
	{
		// Add a '.' to the file name to denote the current directory
		 fileName = "." + fileName;
		 File f = new File(fileName); 
		 
		  try 
		    { 
			  	/* Use the file to create an input stream from which to read and 
			  	 * send the content type and date and send the file
			  	 * */
		    	InputStream file = new FileInputStream(f);  
		    	outputStream.print("HTTP/1.0 200 OK\r\n" +
				   "Content-Type: " + getContentType(fileName) + "\r\n" +
				   "Date: " + new Date() + "\r\n" +
				   "Server: FileServer 1.0\r\n\r\n");
		    	sendFile(file, outputStream); // send bytes
		    } 
		    catch (FileNotFoundException e) 
		    { 
			// file not found
		    	outputStream.println("HTTP/1.1 404 Not Found\r\n"+
		    	          "Content-type: text/html\r\n\r\n"+
		    	          "<html><head></head><body><h3>"+fileName+" not found</h3></body></html>\n");
		    }
		    catch (NullPointerException e) 
	        {
				System.out.println(e.getMessage());
			}
		  
	}

	/* Determine the content type by using a series of if/else if
	 * statements. If nothing matches, set it to text/plain.
	 * */
    private static String getContentType(String path)
    {
	if (path.endsWith(".html") || path.endsWith(".htm")) 
	    return "text/html";
	else if (path.endsWith(".txt") || path.endsWith(".java")) 
	    return "text/plain";
	else if (path.endsWith(".gif")) 
	    return "image/gif";
	else if (path.endsWith(".jpg") || path.endsWith(".jpeg"))
	    return "image/jpeg";
	else 	
	    return "text/plain";
    }

    /* Send the file in raw bytes. Read the file into a buffer of 1000 bytes
     * and write out the contents of the buffer to the server.
     * */
    private static void sendFile(InputStream file, OutputStream out)
    {
    	try 
    	{
    		byte[] buffer = new byte[1000];
    		int bytes = 0;
    		while((bytes = file.read(buffer)) != -1)
    		{
    			out.write(buffer, 0, bytes);
    		}   		
    	}
    	catch (IOException ex) 
    	{ 
    		System.out.println(ex); 
    	}
    }
    
    private static void getFilesFromDirectory(PrintStream out, String dir)
    {
        // Create a file object for your root directory
    	File f1 = new File("./" + dir);
        
        out.println("HTTP/1.1 200 OK\r\n" + "Content-type: text/html\r\n\r\n"); //Set content type to text/html to show links
        
        // Get all the files and directories under your directory
        File[] strFilesDirs = f1.listFiles ( );
        
        if(strFilesDirs == null)
        {
//        	out.println("HTTP/1.1 404 Not Found\r\n"+
//	    	          "Content-type: text/html\r\n\r\n"+
//	    	          "<html><head></head><body><h3>"+ f1 +" not found</h3></body></html>\n");
        	out.println("<html><head></head><body><h3>"+ f1 +" not found</h3></body></html>\n");
        	return;
        }
        try
        {
        	out.println("<html><body><h3>Index of " + f1.getName() + "</h3></body></html>");  // Show the directory name
        
        	for ( int i = 0 ; i < strFilesDirs.length ; i ++ ) 
        	{
        		if ( strFilesDirs[i].isDirectory ( ) ) 
        		{
        			/* If the item is a directory, show it as a link with a trailing slash.  When the link is clicked, 
        			 * the user is taken to a page that shows the contents of that directory 
        			 * Add 3 <br> for spacing purposes
        			 * */
        			out.println("<html><body><a href=\"./" + strFilesDirs[i].getName() + "/" + "\">" + strFilesDirs[i].getName() + "/</a>" + "</body></html>\n");
        			out.println("<br>");
        			out.println("<br>");
        			out.println("<br>");
        		}
        		else if ( strFilesDirs[i].isFile ( ) )
        		{
        			/* If the item is a file name, show it as link that when clicked, the file is retrieved
        			* Add 3 <br> for spacing purposes */
        			out.println("<html><head></head><body><a href=\"./" + strFilesDirs[i].getName() + "\">" + strFilesDirs[i].getName() + "</a>" + "</body></html>\n");
        			out.println("<br>");
        			out.println("<br>");
        			out.println("<br>");
        		}
        	}
        }
        // Saw NPEs when running the program so tried to catch them
        catch (NullPointerException e) 
        {
			System.out.println(e.getStackTrace().toString());
		}
    }
    
    /* Takes in Strings and converts them to integers so they may be added.  
     * The name and the sum is then reported to the browser. If integers
     * are not entered, a message is displayed and the user can click the
     * back button to try again.
     * */
    private static void addnums(PrintStream out, String name, String a, String b)
    {
    	try
    	{
    	int v1 = Integer.parseInt(a);
    	int v2 = Integer.parseInt(b);
    	int sum = v1 + v2;
    	out.println("Hello " + name + ", " + v1 + " plus " + v2 + " equals " + sum);
    	}
    	catch(NumberFormatException nfe)
    	{
    		out.println("Enter a valid integer");
    	}
    }
    
}// End class Worker



public class MyWebServer {
	public static void main(String a[]) throws IOException
	{
		try
		{
			int q_len = 6;
			int port = 2540;
			Socket socket;
			ServerSocket serverSocket = new ServerSocket(port, q_len);
			System.out.println("Urvi's MyWebServer 1.8 starting....listening at port " + port);
			while(true)
			{
				socket = serverSocket.accept(); //Wait for connection
				new Worker(socket).start(); // When browser connects, start new thread to process request
			}
		}
		//Trying to catch the NPE seen when running the browser
		 catch (NullPointerException e) 
        {
			System.out.println(e.getMessage());
		}
	}
}
