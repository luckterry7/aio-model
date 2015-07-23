package client;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CountDownLatch;

public class AsyncTimeClientHandler implements CompletionHandler<Void,AsyncTimeClientHandler>,Runnable{

	private String host;
	private int port;
	private AsynchronousSocketChannel socketChannel;
	private CountDownLatch latch;
	
	public AsyncTimeClientHandler(String host, int port) {
		this.host = host;
		this.port = port;
		try {
			socketChannel = AsynchronousSocketChannel.open();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		latch = new CountDownLatch(1);
		socketChannel.connect(new InetSocketAddress(host,port), this, this);
		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		try {
			socketChannel.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void completed(Void result, AsyncTimeClientHandler attachment) {
		byte[] bytes = "query time".getBytes();
		ByteBuffer writeBuffer = ByteBuffer.allocate(bytes.length);
		//把bytes放入缓存区
		writeBuffer.put(bytes);
		writeBuffer.flip();
		socketChannel.write(writeBuffer, writeBuffer, 
					new CompletionHandler<Integer,ByteBuffer>(){

						@Override
						public void completed(Integer result,
								ByteBuffer buffer) {
							if(buffer.hasRemaining()){
								socketChannel.write(buffer, buffer, this);
							}else{
								ByteBuffer readBuffer = ByteBuffer.allocate(1024);
								socketChannel.read(readBuffer, readBuffer, 
											new CompletionHandler<Integer,ByteBuffer>(){

												@Override
												public void completed(
														Integer result,
														ByteBuffer buffer) {
													buffer.flip();
													byte[] bytes = new byte[buffer.remaining()];
													buffer.get(bytes);
													String body;
													try {
														body = new String(bytes,"UTF-8");
														System.out
																.println("now is " + body);
														latch.countDown();
													} catch (UnsupportedEncodingException e) {
														e.printStackTrace();
													}
												}

												@Override
												public void failed(
														Throwable exc,
														ByteBuffer buffer) {
													try {
														socketChannel.close();
														latch.countDown();
													} catch (IOException e) {
														// TODO Auto-generated catch block
														e.printStackTrace();
													}
												}
									
								});
							}
						}

						@Override
						public void failed(Throwable exc, ByteBuffer buffer) {
							try {
								socketChannel.close();
								latch.countDown();
							} catch (IOException e) {
								e.printStackTrace();
							}
							
						}
			
		});
	}

	@Override
	public void failed(Throwable exc, AsyncTimeClientHandler attachment) {
		exc.printStackTrace();
		try {
			socketChannel.close();
			latch.countDown();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
