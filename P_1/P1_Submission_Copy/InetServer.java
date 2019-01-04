import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

class Worker extends Thread //Extends thread class, this will be implement run() method which processes client requests
{
	Socket socket;  //Reference to a Socket object
	
	//Constructor for Worker object, it takes a Socket as an input parameter.  The Socket object is created in
	//the InetServer class and passed into the Worker constructor
	Worker (Socket sock)
	{
		socket = sock;  //Set the local socket variable to the one passed in from the InetServer class
	}
	/*The run() method is called when the thread is started and processes the client request.
	 * It creates input/output streams, gets a hostname from the user, gets the IP address of 
	 * the hostname, and sends this data back to the server via the socket that was passed into 
	 * the Worker constructor */
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
			inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			outputStream = new PrintStream(socket.getOutputStream());
			
			try
			{
				String hostName = inputStream.readLine(); //Read in the hostname that was sent from the client, and save it to 'name'
				System.out.println("Looking up " + hostName); //Print message that the server is looking up the information
				printRemoteAddress(hostName, outputStream); //Put the IP address of the machine and other messages onto the output stream
			}
			catch(IOException ex)
			{
				System.out.println("Server read error"); //Exception trying to read from input stream or write to output stream
				ex.printStackTrace();
			}
			socket.close();  //Close the socket to prevent a resource leak
		}
		catch(IOException e)
		{
			System.out.println(e); //Exception creating the input/output streams
		}
	}
	
	static void printRemoteAddress(String name, PrintStream out)
	{
		try
		{
			 //Put informational message to client on output stream
			out.println("Looking up " + name + "....");
			//Get the IP address of the machine based on the hostname given by the user, which is passed into this method
			InetAddress machine = InetAddress.getByName(name);
			//Get the hostname. Since the InetAddress object was created with a hostname,
			//this is what is returned.  Put this on the output stream to the client
			out.println("Host name: " + machine.getHostName());
			/*Get the IP address in raw form, as an array of bytes.
			 * Call toText to convert this byte array to a String
			 * Put this on the output stream to the client */
			out.println("Host IP: " + toText(machine.getAddress()));
		}
		catch(UnknownHostException uhe)
		{
			out.println("Failed in attempt to look up " + name); //Couldn't find IP address for the hostname given
		}
	}
	
	static String toText(byte ip[])
	{
		//Create string buffer, it can hold up to 16 characters by default
		StringBuffer result = new StringBuffer();
		
		//Go through the byte array representing the raw ip and convert it to a String representation of an IP address.
		for(int i = 0; i < ip.length; ++i)
		{
			if(i > 0)
				result.append("."); //Add a period after first octet
			/*0xff & ip[i] represents the ith element of the byte array as a positive int between 0 and 255
			 * Note that if the ip[i] < 128, then the values for ip[i] and 0xff & ip[i] the same.  The append 
			 * method takes an int and appends its String representation to the StringBuffer object, i.e., result */
			result.append(0xff & ip[i]); 
		}
		return result.toString(); //Return String representation of the raw IP address
	}
}
public class InetServer {
	public static void main(String a[]) throws IOException
	{
		//Maximum length of the queue for incoming connections.  If this is exceeded, connection is refused
		int q_len = 6;
		//Port on which the server will be listening for connections
		int port = 4000;
		//A Socket object that will be used for communication with the client
		Socket socket;
		
		/*Create server socket that listens for requests from a client on the port hard-coded above.
		 * The server socket allows up to 6 connections */
		ServerSocket serverSocket = new ServerSocket(port, q_len);
		 //Print out message to user that the server has started and is awaiting a connection
		System.out.println("Inet server 1.8 starting....listening at port " + port);
		while(true)
		{
			/*The server socket will listen for a connection to the socket and accept it once a connection 
			 has been made, creating a new socket to use for communication with the client.  Program execution 
			 is blocked until connection is made */
			socket = serverSocket.accept(); 
			/*Start Worker thread to handle the actual processing of client request. The socket that 
			 * was created is passed to the Worker constructor. The Worker class extends the
			 * Thread class, so it is a thread),  */
			new Worker(socket).start(); 
		}
	}
}
