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
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import rpc.RpcAcceptor;
import rpc.RpcProcessor;
import rpc.RpcRequest;
import rpc.RpcResponse;

public class NioRpcAcceptor implements RpcAcceptor {

	private String host;
	private int port;

	private Selector selector;
	private ServerSocketChannel serverChannel;
	private RpcProcessor processor;
	protected ConcurrentHashMap<String, SocketChannel> channels = new ConcurrentHashMap<>();

	public void setProcessor(RpcProcessor processor) {
		this.processor = processor;
	}

	public void init() throws IOException {
		// 获得一个ServerSocket通道
		serverChannel = ServerSocketChannel.open();
		// 设置通道为非阻塞
		serverChannel.configureBlocking(false);
		// 将该通道对应的ServerSocket绑定到port端口
		serverChannel.bind(new InetSocketAddress(host, port));
		// 获得一个通道管理器
		this.selector = Selector.open();
		// 将通道管理器和该通道绑定，并为该通道注册SelectionKey.OP_ACCEPT事件,注册该事件后，
		// 当该事件到达时，selector.select()会返回，如果该事件没到达selector.select()会一直阻塞。
		serverChannel.register(selector, SelectionKey.OP_ACCEPT);
		System.out.println("...server start");

		// 取出response
		processor.getExecutorService().execute(new Runnable() {
			public void run() {
				while (true) {
					try {
						RpcResponse response = processor.getResponses().take();
						if (response != null) {
							write(response);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
		
	}

	public void start() {
		processor.getExecutorService().execute(new Runnable() {
			public void run() {
				try {
					start0();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	private void start0() throws IOException {
		this.init();
		// 轮询访问selector
		while (true) {
			// 当注册的事件到达时，方法返回；否则,该方法会一直阻塞
			int k = selector.select();
			if (k == 0)
				continue;
			// 获得selector中选中的项的迭代器，选中的项为注册的事件
			Iterator<SelectionKey> ite = this.selector.selectedKeys().iterator();
			while (ite.hasNext()) {
				SelectionKey key = (SelectionKey) ite.next();
				// 删除已选的key,以防重复处理
				ite.remove();
				// 客户端请求连接事件
				if (key.isAcceptable()) {
					// System.out.println("isAcceptable");

					ServerSocketChannel server = (ServerSocketChannel) key.channel();
					// 获得和客户端连接的通道
					SocketChannel channel = server.accept();
					// 设置成非阻塞
					channel.configureBlocking(false);
					// 在和客户端连接成功之后，为了可以接收到客户端的信息，需要给通道设置读的权限。
					channel.register(this.selector, SelectionKey.OP_READ);

					// 获得了可读的事件
				} else if (key.isReadable()) {
					read(key);
				}

			}

		}
	}

	public void read(SelectionKey key) throws IOException  {
		// 服务器可读取消息:得到事件发生的Socket通道
		SocketChannel channel = (SocketChannel) key.channel();
		// 创建读取的缓冲区
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		try {
			channel.read(buffer);
		} catch (IOException e1) {
		//	e1.printStackTrace();
			//System.err.println("客户端关闭");
			return;
		}
		byte[] data = buffer.array();

		ByteArrayInputStream in = new ByteArrayInputStream(data);
		ObjectInputStream ois = new ObjectInputStream(in);
		RpcRequest request = null;
		try {
			request = (RpcRequest) ois.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		if (request != null) {

			channels.put(request.getRequestId(), channel);
			processor.ExcuteRemotecall(request);

		}

	}

	public void stop() throws IOException {
		serverChannel.close();
	}

	public void setHost(String host) {
		this.host = host;
	}

	public void setport(int port) {
		this.port = port;
	}

	@Override
	public void write(RpcResponse response) throws IOException {

		SocketChannel channel = channels.get(response.getRequestId());
		channels.remove(response.getRequestId());
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(out);
		oos.writeObject(response);
		byte[] b = out.toByteArray();
		ByteBuffer outBuffer = ByteBuffer.wrap(b);
		channel.write(outBuffer);// 将结果回送给客户端

	}

}
