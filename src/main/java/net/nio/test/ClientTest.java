package net.nio.test;

import java.io.IOException;

import net.nio.NioChannel;
import net.nio.NioConnector;

public class ClientTest {

	public static void main(String[] args) throws IOException, Exception {
		NioConnector connector = new NioConnector(new ClientHandler());
		NioChannel channel = connector.connect("127.0.0.1",1234);
		connector.finishConnect();
		Thread.sleep(2000);
		connector.close();
		
	}

}
