import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Fricken Hamster
 * Date: 10/3/13
 * Time: 11:31 AM
 */
public class server_java_tcp implements Runnable
{
	
	private int port;
	
	private Boolean listening;
	
	private ServerSocket serverSocket;
	
	private ArrayList<Socket> connections;

	public server_java_tcp(int port)
	{
		this.port = port;
		listening = false;
	}

	@Override
	public void run()
	{
		listening = true;
		try
		{
			serverSocket = new ServerSocket(port);
			while (listening)
			{
				Socket socket = serverSocket.accept();
				connections.add(socket);
			}
		} catch (IOException e)
		{
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}

	}
	
	public static void main(String[] args)
	{
		server_java_tcp server = new server_java_tcp(Integer.parseInt(args[0]));
		server.run();
	}
	
}
