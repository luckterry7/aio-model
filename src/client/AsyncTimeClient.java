package client;

public class AsyncTimeClient {
	public static void main(String[] args) {
		new Thread(new AsyncTimeClientHandler("127.0.0.1",8080),"AIO-AsyncTimeClientHandler-001").start();
	}
}
