import java.io.*;
import java.net.*;

/**
 * Created with IntelliJ IDEA.
 * User: Fricken Hamster
 * Date: 10/3/13
 * Time: 11:30 AM
 */
public class client_java_tcp
{
	
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
	public static final byte MESSAGE = 0x05;
	
	public client_java_tcp(String host, int port, String userName)
	{
		this.host = host;
		this.port = port;
		this.username = userName;
	}
	
	public void connect()
	{
		try
		{
			socket = new Socket(host, port);
			
			outputStream = new DataOutputStream(socket.getOutputStream());
			inputStream = new DataInputStream(new BufferedInputStream( socket.getInputStream()));
			
			sendConnect();
			while(true)
			{
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
						case ACCEPTED:
						{
							identifier = packetStream.readUTF();
							break;
						}
						case MESSAGE:
						{
							System.out.println(packetStream.readUTF());
							break;
						}
					}
				}
			}
		} catch (UnknownHostException e)
		{
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		} catch (IOException e)
		{
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
	}

	public void sendConnect()
	{
		try
		{
			ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(byteOut);
			out.writeByte(CONNECT);
			out.writeUTF(username);
			byte[] bb = byteOut.toByteArray();
			sendByteArray(bb);
			System.out.println("sent connect packet");
		} catch (IOException e)
		{
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
	}
	
	public void sendPrintAll()
	{
		try
		{
			ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(byteOut);
			out.writeByte(PRINTALL);
			byte[] bb = byteOut.toByteArray();
			sendByteArray(bb);
			System.out.println("sent printall packet");
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
			out.writeUTF(msg);
			byte[] bb = byteOut.toByteArray();
			sendByteArray(bb);
			System.out.println("sent connect packet");
		} catch (IOException e)
		{
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
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


	public static void main(String[] args)
	{
		client_java_tcp client = new client_java_tcp(args[0], Integer.parseInt(args[1]), args[2]);
		client.connect();
		//server.run();
	}
}
