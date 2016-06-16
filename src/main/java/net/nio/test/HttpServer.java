package net.nio.test;

import java.io.IOException;

import net.nio.NioAcceptor;

public class HttpServer {

	public static void main(String[] args) throws IOException {
		NioAcceptor acceptor = new NioAcceptor(new HttpHandler());
		acceptor.bind(1234);

	}

}
