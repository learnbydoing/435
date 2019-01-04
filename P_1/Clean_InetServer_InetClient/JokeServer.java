import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

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
			inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			outputStream = new PrintStream(socket.getOutputStream());
			
			try
			{
				String hostName = inputStream.readLine();
				System.out.println("Looking up " + hostName);
				printRemoteAddress(hostName, outputStream);
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
			out.println("Looking up " + name + "....");
			InetAddress machine = InetAddress.getByName(name);
			out.println("Host name: " + machine.getHostName());
			out.println("Host IP: " + toText(machine.getAddress()));
		}
		catch(UnknownHostException uhe)
		{
			out.println("Failed in attempt to look up " + name); 
		}
	}
	
	static String toText(byte ip[])
	{
		StringBuffer result = new StringBuffer();
	
		for(int i = 0; i < ip.length; ++i)
		{
			if(i > 0)
				result.append(".");
			result.append(0xff & ip[i]); 
		}
		return result.toString();
	}
}
public class JokeServer {
	public static void main(String a[]) throws IOException
	{
		int q_len = 6;
		int port = 4000;
		Socket socket;
		ServerSocket serverSocket = new ServerSocket(port, q_len);
		System.out.println("Inet server 1.8 starting....listening at port " + port);
		while(true)
		{
			socket = serverSocket.accept(); 
			new Worker(socket).start(); 
		}
	}
}
