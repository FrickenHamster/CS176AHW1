import java.io.*;
import java.math.*;
import java.net.*;
import java.security.*;
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
	
	private Vector<chatConnection> connections;
	
	private Dictionary<String, String> usernameDict;
	
	public static String WELCOME = "Kouhai chan kawaii";

	public static final byte CONNECT = 0x01;
	public static final byte ACCEPTED = 0x02;
	public static final byte PRINTALL = 0x03;
	public static final byte SEND = 0x04;
	public static final byte MESSAGE = 0x05;

	public Vector<String> messages;
	
	public server_java_tcp(int port)
	{
		this.port = port;
		listening = false;
		
		usernameDict = new Hashtable<String, String>(8);
		connections = new Stack<chatConnection>();
		messages = new Stack<String>();
	}

	@Override
	public void run()
	{
		listening = true;
		System.out.println("server started on port:" + port);
		try
		{
			serverSocket = new ServerSocket(port);
			while (listening)
			{
				Socket socket = serverSocket.accept();
				chatConnection cc = new chatConnection(socket, this);
				System.out.println("connection from:" + socket.getRemoteSocketAddress());
				connections.add(cc);
				cc.run();
			}
		} catch (IOException e)
		{
			System.out.println("error");
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
	}
	
	public void newMessage(String identifier, String msg)
	{
		String username = usernameDict.get(identifier);
		if (username == null)
			return;
		messages.add(username + ": " + msg + "\n");
	}
	
	public String generateIdentifier(String username)
	{
		String id = new BigInteger(130, new SecureRandom()).toString();
		usernameDict.put(id, username);
		return id;
	}
	
	public static void main(String[] args)
	{
		server_java_tcp server = new server_java_tcp(Integer.parseInt(args[0]));
		server.run();
	}
	
	private class chatConnection extends Thread
	{
		private Socket socket;
		private server_java_tcp server;
		private DataOutputStream outputStream;
		private DataInputStream inputStream;
		
		private String username;
		private String identifier;

		public chatConnection(Socket socket, server_java_tcp server)
		{
			this.socket = socket;
			this.server = server;
			
		}
		
		public void sendMessage(String msg)
		{
			try
			{
				ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
				DataOutputStream out = new DataOutputStream(byteOut);
				out.writeByte(MESSAGE);
				out.writeUTF(msg);
				sendByteArray(byteOut.toByteArray());
				System.out.println("sent msg:" + msg);
			} catch (IOException e)
			{
				e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			}
		}
		
		public void sendWelcome()
		{
			sendMessage("Welcome message: " + WELCOME);
		}

		public void sendByteArray( byte[] ba )
		{
			try
			{
				ByteArrayOutputStream bout = new ByteArrayOutputStream();
				DataOutputStream dd = new DataOutputStream( bout );
				dd.writeShort( ba.length );
				dd.write( ba );
				byte[] bb = bout.toByteArray();
				
				outputStream.write(bb);
				outputStream.flush();
			}
			catch( Exception e )
			{
			}
		}

		@Override
		public void run()
		{
			try
			{
				inputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
				outputStream = new DataOutputStream( socket.getOutputStream( ));

				while ( inputStream.available() != -1 )
				{
					int packetSize = inputStream.readShort() ;
					System.out.println( "read bytearray size:" + packetSize );
					byte packetBuffer[] = new byte[packetSize];
					int byteTrans = 0;
					while ( byteTrans < packetSize )
					{
						inputStream.read( packetBuffer , byteTrans , 1 );
						byteTrans++;
					}
					ByteArrayInputStream bin = new ByteArrayInputStream( packetBuffer );
					DataInputStream packetStream = new DataInputStream(bin);
					int id = packetStream.readByte();
					System.out.println("read id:" + id);
					
					switch (id)
					{
						case CONNECT:
						{
							username = packetStream.readUTF();
							System.out.println(username);
							this.identifier = server.generateIdentifier(username);
							ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
							DataOutputStream out = new DataOutputStream(byteOut);
							out.writeByte(ACCEPTED);
							out.writeUTF(identifier);
							System.out.println("send id:" + identifier);
							sendByteArray(byteOut.toByteArray());
							sendWelcome();
							break;
						}
					}
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			}
		}
	}
}
