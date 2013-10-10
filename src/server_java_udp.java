import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * Created with IntelliJ IDEA.
 * User: Fricken Hamster
 * Date: 10/9/13
 * Time: 11:34 AM
 */
public class server_java_udp
{
	private DatagramSocket socket;
	private int port;

	public static final byte CONNECT = 0x01;
	public static final byte ACCEPTED = 0x02;
	public static final byte PRINTALL = 0x03;
	public static final byte SEND = 0x04;
	public static final byte MESSAGE = 0x05;
	public static final byte SIZE = 0x09;
	public static final byte ACK = 0x08;
	
	public server_java_udp(int port)
	{
		this.port = port;
	}
	
	public void start()
	{
		try
		{
			socket = new DatagramSocket(port);
			byte[] sizeByte = new byte[16];
			while (true)
			{
				System.out.println("listening");
				DatagramPacket sizePacket = new DatagramPacket(sizeByte, sizeByte.length);
				socket.receive(sizePacket);
				DataInputStream in = new DataInputStream( new ByteArrayInputStream(sizePacket.getData()));
				int id = in.readByte();
				int netbit = in.readByte();
				System.out.println("recieved size");
				if (id == SIZE)
				{
					sendACK(netbit, sizePacket.getAddress(), sizePacket.getPort());
				}
				int packetSize = in.readShort();
				
				byte[] packetByte = new byte[packetSize];
				DatagramPacket packet = new DatagramPacket(packetByte, packetByte.length);
				
			}
		} catch (SocketException e)
		{
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		} catch (IOException e)
		{
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
	}
	
	public void sendACK(int netbit, InetAddress address, int port)
	{
		try
		{
			ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(byteOut);
			out.writeByte(ACK);
			out.writeByte(netbit);
			byte[] bb = byteOut.toByteArray();
			DatagramPacket packet = new DatagramPacket(byteOut.toByteArray(), bb.length, address, port);
			socket.send(packet);
		} catch (IOException e)
		{
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
	}


	public static void main(String[] args)
	{
		if (args.length != 1)
		{
			System.out.println("Invalid number of args. Terminating.");
			System.exit(0);
		}
		server_java_udp server = new server_java_udp(Integer.parseInt(args[0]));
		server.start();
	}
}
