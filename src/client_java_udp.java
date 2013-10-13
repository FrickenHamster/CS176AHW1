import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Fricken Hamster
 * Date: 10/9/13
 * Time: 11:24 AM
 */
public class client_java_udp
{
	private String username;
	private String identifier;
	
	private DatagramSocket socket;
	private InetAddress host;
	private int port;
	private byte netbit;
	
	public static final byte CONNECT = 0x01;
	public static final byte ACCEPTED = 0x02;
	public static final byte PRINTALL = 0x03;
	public static final byte SEND = 0x04;
	public static final byte CONTENT = 0x05;
	public static final byte SIZE = 0x09;
	public static final byte ACK = 0x08;
	
	public client_java_udp(InetAddress host, int port, String username)
	{
		if (port < 1024 || port > 49151)
		{
			System.err.println("Invalid port. Terminating.");
			System.exit(1);
		}
		this.host = host;
		this.port = port;
		this.username = username;
		netbit = 0x00;
	}
	
	public void connect()
	{
		sendConnect();
		while (!waitWelcome()){};
		
		enterCommand();
	}
	public void sendSizePacket(int size, int nb)
	{
		try
		{
			ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(byteOut);
			out.writeByte(SIZE);
			out.writeByte(nb);
			out.writeShort(size);
			byte[] bb = byteOut.toByteArray();
			DatagramPacket packet = new DatagramPacket(byteOut.toByteArray(), bb.length, host, port);
			int timesSent = 0;
			do
			{
				if (timesSent == 3)
				{
					System.err.println("Failed to send message. Terminating.");
					System.exit(1);
				}
				timesSent++;
				socket.send(packet);
//				System.out.println("size sent");
			}while(!waitAck(nb));
			
		} catch (IOException e)
		{
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
	}
	
	public void sendConnect()
	{
		try
		{
			int nb = netbit;
			ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(byteOut);
			out.writeByte(CONNECT);
			out.writeByte(nb + 1);
			out.writeUTF(username);
			byte[] bb = byteOut.toByteArray();
			DatagramPacket packet = new DatagramPacket(byteOut.toByteArray(), bb.length, host, port);

			socket = new DatagramSocket();
			
			sendSizePacket(bb.length, nb);

			int timesSent = 0;
			do
			{
				if (timesSent == 3)
				{
					System.err.println("Failed to send message. Terminating.");
					System.exit(1);
				}
				timesSent++;
				socket.send(packet);
//				System.out.println("sent connect" + (nb + 1));
			}while(!waitAck(nb + 1));
			incNetbit(2);
			
		} catch (IOException e)
		{
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
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
			waitPrintAll();
			enterCommand();
		} else if (command.equals("exit"))
		{
			System.exit(0);
		} else
		{
			enterCommand();
		}
	}
	
	public void sendMessage(String msg)
	{
		try
		{
			int nb = netbit;
			ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(byteOut);
			out.writeByte(SEND);
			out.writeByte(nb + 1);
			out.writeUTF(identifier);
			out.writeUTF(msg +'\n');
			byte[] bb = byteOut.toByteArray();
			DatagramPacket packet = new DatagramPacket(byteOut.toByteArray(), bb.length, host, port);

			socket = new DatagramSocket();

			sendSizePacket(bb.length, nb);

			int timesSent = 0;
			do
			{
				if (timesSent == 3)
				{
					System.err.println("Failed to send message. Terminating.");
					System.exit(1);
				}
				timesSent++;
				socket.send(packet);
//				System.out.println("sent message" + (nb + 1));
			}while(!waitAck(nb + 1));
			incNetbit(2);

		} catch (IOException e)
		{
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
	}
	
	public void sendPrintAll()
	{
		try
		{
			int nb = netbit;
			ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(byteOut);
			out.writeByte(PRINTALL);
			out.writeByte(nb + 1);
			byte[] bb = byteOut.toByteArray();
			DatagramPacket packet = new DatagramPacket(byteOut.toByteArray(), bb.length, host, port);
			socket = new DatagramSocket();
			sendSizePacket(bb.length, nb);

			int timesSent = 0;
			do
			{
				if (timesSent == 3)
				{
					System.err.println("Failed to send message. Terminating.");
					System.exit(1);
				}
				timesSent++;
				socket.send(packet);
//				System.out.println("sent connect" + (nb + 1));
			}while(!waitAck(nb + 1));
			incNetbit(2);

		} catch (IOException e)
		{
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
		
		
	}
	
	public void waitPrintAll()
	{
		try
		{
			byte[] sizeByte = new byte[16];
			int packetSize;
			while (true)
			{
//				System.out.println("listening for size");
				DatagramPacket sizePacket = new DatagramPacket(sizeByte, sizeByte.length);
				socket.receive(sizePacket);
				DataInputStream in = new DataInputStream( new ByteArrayInputStream(sizePacket.getData()));
				int id = in.readByte();

				if (id == SIZE)
				{
					int netbit = in.readByte();
					packetSize = in.readShort();
					sendACK(netbit);
//					System.out.println("recieved size," + netbit );
					break;
				}
			}

			while (true)
			{
				byte[] packetByte = new byte[packetSize];
				DatagramPacket packet = new DatagramPacket(packetByte, packetByte.length);
				socket.receive(packet);
				DataInputStream in = new DataInputStream( new ByteArrayInputStream(packet.getData()));
				int id = in.readByte();
				int packetnb = in.readByte();
				switch(id)
				{
					case CONTENT:
//						System.out.println("received printall from:" + packet.getAddress() + "," + packetnb);
						System.out.println(in.readUTF());
						sendACK(packetnb);
						return;
					
					default:
						System.err.println("Invalid packet from server. Terminating.");
						System.exit(1);
						break;
				}
			}

		} catch (SocketTimeoutException e)
		{
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		} catch (IOException e)
		{
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
	}
	
	public boolean waitWelcome()
	{
		try
		{
			byte[] sizeByte = new byte[16];
			int packetSize;
			while (true)
			{
//				System.out.println("listening for size");
				DatagramPacket sizePacket = new DatagramPacket(sizeByte, sizeByte.length);
				socket.receive(sizePacket);
				DataInputStream in = new DataInputStream( new ByteArrayInputStream(sizePacket.getData()));
				int id = in.readByte();

				if (id == SIZE)
				{
					int netbit = in.readByte();
					packetSize = in.readShort();
					sendACK(netbit);
//					System.out.println("recieved size," + netbit );
					break;
				}
			}
			
			while (true)
			{
				byte[] packetByte = new byte[packetSize];
				DatagramPacket packet = new DatagramPacket(packetByte, packetByte.length);
				socket.receive(packet);
				DataInputStream in = new DataInputStream( new ByteArrayInputStream(packet.getData()));
				int id = in.readByte();
				int packetnb = in.readByte();
				switch(id)
				{
					case ACCEPTED:
//						System.out.println("received accepted from:" + packet.getAddress() + "," + packetnb);
						identifier = in.readUTF();
						sendACK(packetnb);
						return false;
					
					case CONTENT:
//						System.out.println("received message from:" + packet.getAddress() + "," + packetnb);
						String msg = in.readUTF();
						sendACK(packetnb);
						System.out.println(msg);
						return true;
				}
			}

		} catch (SocketTimeoutException e)
		{
			System.err.println("Failed to receive message. Terminating.");
			System.exit(1);
		} catch (IOException e)
		{
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
		
		return false;
	}
	
	public boolean waitAck(int nb)
	{
		try
		{
			while(true)
			{
				byte[] recieveByte = new byte[2];
				DatagramPacket packet = new DatagramPacket(recieveByte, recieveByte.length);
				socket.setSoTimeout(500);
				socket.receive(packet);
				
				DataInputStream in = new DataInputStream( new ByteArrayInputStream(packet.getData()));
				int id = in.readByte();
				if (id == ACK)
				{
					int packetnb = in.readByte();
					if (packetnb != nb)
						return false;
//					System.out.println("recieved ACK:" + packetnb);
					return true;
				}
			}
		}
		catch (SocketTimeoutException e)
		{
//			System.out.println("ack timeout:" + nb);
			return false;
		}
		catch (IOException e)
		{
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
		return false;
	}

	public void sendACK(int nb)
	{
		try
		{
			ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(byteOut);
			out.writeByte(ACK);
			out.writeByte(nb);
			byte[] bb = byteOut.toByteArray();
			DatagramPacket packet = new DatagramPacket(byteOut.toByteArray(), bb.length, host, port);
			socket.send(packet);
//			System.out.println("sent ack:" + nb);
			netbit = (byte)nb;
			incNetbit(1);
		} catch (IOException e)
		{
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
	}


	public void incNetbit(int nn)
	{
		netbit += nn;
		if (netbit > 100)
			netbit -= 100;
	}
	
	public static void main(String[] args)
	{
		if (args.length != 3)
		{
			System.err.println("Invalid number of args. Terminating.");
			System.exit(1);
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
