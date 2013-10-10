import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;

/**
 * Created with IntelliJ IDEA.
 * User: Fricken Hamster
 * Date: 10/9/13
 * Time: 11:24 AM
 */
public class client_java_udp
{
	
	private DatagramSocket socket;
	private InetAddress host;
	private int port;
	private byte netbit;

	public static final byte CONNECT = 0x01;
	public static final byte ACCEPTED = 0x02;
	public static final byte PRINTALL = 0x03;
	public static final byte SEND = 0x04;
	public static final byte MESSAGE = 0x05;
	public static final byte SIZE = 0x09;
	public static final byte ACK = 0x08;
	
	public client_java_udp(InetAddress host, int port, String username)
	{
		this.host = host;
		this.port = port;
		netbit = 0x00;
	}
	
	public void connect()
	{
		try
		{
			socket = new DatagramSocket(port);
			sendSizePacket(2);
		} catch (SocketException e)
		{
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
	}
	public int sendSizePacket(int size)
	{
		try
		{
			ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(byteOut);
			int nb = netbit;
			out.writeByte(SIZE);
			out.writeByte(nb);
			out.writeByte(size);
			byte[] bb = byteOut.toByteArray();
			DatagramPacket packet = new DatagramPacket(byteOut.toByteArray(), bb.length, host, 22415);
			socket.send(packet);
			netbit++;
			System.out.println("size sent");
			return nb;
		} catch (IOException e)
		{
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
		return -1;
	}

	public static void main(String[] args)
	{
		if (args.length != 3)
		{
			System.err.println("Invalid number of args. Terminating.");
			System.exit(0);
		}
		client_java_udp client = null;
		try
		{
			client = new client_java_udp(InetAddress.getByName(args[0]), Integer.parseInt(args[1]), args[2]);
		} catch (UnknownHostException e)
		{
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
		client.connect();
	}
}
