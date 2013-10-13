import java.io.*;
import java.net.*;
import java.util.Scanner;

/**
 * Created with IntelliJ IDEA.
 * User: Fricken Hamster
 * Date: 10/3/13
 * Time: 11:30 AM
 */
public class client_java_tcp
{

	/*
	Most of the code here was adapted from a server I made for a flash game in Java. I believe I followed this tutorial at the time
	http://www.broculos.net/2008/03/how-to-make-multi-client-flash-java.html#.UloZhhA5kgk
	I looked at the lecture slides and used logic to work out how a client would work with tcp sockets
	*/
	
	private String host;
	private int port;
	private String username;

	private Socket socket;

	private String identifier;

	private DataOutputStream outputStream;
	private DataInputStream inputStream;


	public static final byte CONNECT = 0x01;
	public static final byte ACCEPTED = 0x02;
	public static final byte PRINTALL = 0x03;
	public static final byte SEND = 0x04;
	public static final byte CONTENT = 0x05;

	public client_java_tcp(String host, int port, String userName)
	{
		if (port < 1024 || port > 49151)
		{
			System.err.println("Invalid port. Terminating.");
			System.exit(1);
		}
		this.host = host;
		this.port = port;
		this.username = userName;
	}

	public void connect()
	{
		try
		{
			//try to connection to server
			socket = new Socket(host, port);

			outputStream = new DataOutputStream(socket.getOutputStream());
			inputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

			sendConnect();
			outer:
			while (inputStream.available() != -1)
			{
				//listen for initialization
				int packetSize = inputStream.readShort();
//				System.out.println("read bytearray size:" + packetSize);
				byte packetBuffer[] = new byte[packetSize];
				int byteTrans = 0;
				while (byteTrans < packetSize)
				{
					inputStream.read(packetBuffer, byteTrans, 1);
					byteTrans++;
				}//this snippit basically waits until entire packet is received
				ByteArrayInputStream bin = new ByteArrayInputStream(packetBuffer);
				DataInputStream packetStream = new DataInputStream(bin);
				int id = packetStream.readByte();
//				System.out.println("read id:" + id);
				switch (id)
				{
					case ACCEPTED:
					{
						identifier = packetStream.readUTF();
						break;
					}

					case CONTENT:
					{
						System.out.println(packetStream.readUTF());
						break outer;
					}
					
					default:
					{
						System.err.println("Invalid server initialization. Terminating.");
						System.exit(1);
					}
				}
			}
			
			enterCommand();
		}catch (IOException e)
		{
			System.err.println("Could not connect to server. Terminating.");
			System.exit(1);
		}
	}
	
	public void enterCommand()
	{
		
		Scanner scanner = new Scanner(System.in);
		System.out.println("Enter a command: (send, print, or exit)");
		String command = scanner.nextLine();
//		System.out.println(command);
		if (command.equals( "send"))
		{
			System.out.println("Enter your message:");
			String msg = scanner.nextLine();
			sendMessage(msg);
			enterCommand();
		} else if (command.equals("print"))
		{
			sendPrintAll();
			enterCommand();
		} else if (command.equals("exit"))
		{
			System.exit(0);
		} else
		{
			enterCommand();
		}
	}

	public void sendConnect()
	{
		try
		{//send connect packet
			ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(byteOut);
			out.writeByte(CONNECT);
			out.writeUTF(username);
			byte[] bb = byteOut.toByteArray();
			sendByteArray(bb);
//			System.out.println("sent connect packet");
		} catch (IOException e)
		{
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
	}

	public void sendPrintAll()
	{
		try
		{//send print packet
			ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(byteOut);
			out.writeByte(PRINTALL);
			byte[] bb = byteOut.toByteArray();
			sendByteArray(bb);
//			System.out.println("sent printall packet");
			
			outer:
			while (inputStream.available() != -1)
			{
				//waits for the printed message
				int packetSize = inputStream.readShort();
				byte packetBuffer[] = new byte[packetSize];
				int byteTrans = 0;
				while (byteTrans < packetSize)
				{
					inputStream.read(packetBuffer, byteTrans, 1);
					byteTrans++;
				}
				ByteArrayInputStream bin = new ByteArrayInputStream(packetBuffer);
				DataInputStream packetStream = new DataInputStream(bin);
				int id = packetStream.readByte();
//				System.out.println(id);
				switch (id)
				{
					case CONTENT:
					{
						System.out.println(packetStream.readUTF());
						break outer;
					}
					default:
						System.err.println("Invalid packet from server. Terminating.");
						System.exit(1);
						break;
				}
			}
		} catch (IOException e)
		{
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
	}

	public void sendMessage(String msg)
	{
		try
		{
			ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(byteOut);
			out.writeByte(SEND);
			out.writeUTF(identifier);
			out.writeUTF(msg + '\n');
			byte[] bb = byteOut.toByteArray();
			sendByteArray(bb);
//			System.out.println("sent connect packet");
		} catch (IOException e)
		{
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
	}

	public void sendByteArray(byte[] ba)
	{
		try
		{
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			DataOutputStream dd = new DataOutputStream(bout);
			dd.writeShort(ba.length);
			dd.write(ba);
			byte[] bb = bout.toByteArray();

			outputStream.write(bb);
			outputStream.flush();
		} catch (Exception e)
		{
		}
	}


	public static void main(String[] args)
	{
		if (args.length != 3)
		{
			System.err.println("Invalid number of args. Terminating.");
			System.exit(1);
		}
		client_java_tcp client = new client_java_tcp(args[0], Integer.parseInt(args[1]), args[2]);
		client.connect();
		//server.run();
	}
}
