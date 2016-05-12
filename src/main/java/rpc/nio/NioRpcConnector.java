package rpc.nio;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import rpc.RpcConnector;
import rpc.RpcRequest;
import rpc.RpcResponse;

public class NioRpcConnector implements RpcConnector {
	private String host;
	private int port;
	private Selector selector;
	private SocketChannel channel;

	boolean stop =false;
	public void setHost(String host) {
		this.host = host;
	}

	public void setport(int port) {
		this.port = port;
	}

	public void init() throws IOException {
		// 获得一个Socket通道
		channel = SocketChannel.open();
		// 设置通道为非阻塞
		channel.configureBlocking(false);
		// 获得一个通道管理器
		this.selector = Selector.open();

		// 客户端连接服务器,其实方法执行并没有实现连接，需要在listen（）方法中调
		// 用channel.finishConnect();才能完成连接
		channel.connect(new InetSocketAddress(host, port));
		// 将通道管理器和该通道绑定，并为该通道注册SelectionKey.OP_CONNECT事件,注册该事件后，
		// 当该事件到达时，selector.select()会返回，如果该事件没到达selector.select()会一直阻塞。
		channel.register(selector, SelectionKey.OP_CONNECT);
	}

	public void start(){
		
		try {
			init();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	@Override
	public RpcResponse invoke(RpcRequest requst) throws IOException{
		System.out.println("call....");
		RpcResponse response =null;
		while(!stop){
		// 选择一组可以进行I/O操作的事件，放在selector中,该方法会阻塞
			int k = selector.select();
			if (k == 0)	continue;
		// 获得selector中选中的项的迭代器
		Iterator<SelectionKey> ite = this.selector.selectedKeys().iterator();
		while (ite.hasNext()) {
			SelectionKey key = (SelectionKey) ite.next();
			// 删除已选的key,以防重复处理
			ite.remove();
			// 连接事件发生
			if (key.isConnectable()) {
				SocketChannel channel = (SocketChannel) key.channel();
				// 如果正在连接，则完成连接
				if (channel.isConnectionPending()) {
					channel.finishConnect();

				}
				// 设置成非阻塞
				channel.configureBlocking(false);
				
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				ObjectOutputStream oos= new ObjectOutputStream(out);
				
				oos.writeObject(requst);
				byte [] b =out.toByteArray();
				
				ByteBuffer outBuffer = ByteBuffer.wrap(b);
				channel.write(outBuffer);
				
				// 在和服务端连接成功之后，为了可以接收到服务端的信息，需要给通道设置读的权限。
				channel.register(this.selector, SelectionKey.OP_READ);

				// 获得了可读的事件
			} else if (key.isReadable()) {
				response = read(key);
			}

		}}
		return response;

	}

	public RpcResponse read(SelectionKey key) throws IOException {
		// 和服务端的read方法一样
		// 服务器可读取消息:得到事件发生的Socket通道
		SocketChannel channel = (SocketChannel) key.channel();
		// 创建读取的缓冲区
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		channel.read(buffer);
		byte[] data = buffer.array();
		ByteArrayInputStream in = new ByteArrayInputStream(data);
		ObjectInputStream ois = new ObjectInputStream(in);
		RpcResponse response = null;
		try {
			response = (RpcResponse) ois.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		stop = true;
		return response;
	}


}
