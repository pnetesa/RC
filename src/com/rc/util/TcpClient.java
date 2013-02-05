package com.rc.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import android.util.Log;

public class TcpClient {
	
	public static final String TAG = TcpClient.class.getSimpleName();
	
	private Socket mSocket;
	private InputStream mInput;
	private OutputStream mOutput;
	
	private ExecutorService mWriteExec = Executors.newSingleThreadExecutor();
	
	public TcpClient(String host, int port) throws UnknownHostException, IOException {
		
		InetSocketAddress address = new InetSocketAddress(host, port);
		mSocket = new Socket();
		mSocket.connect(address, 5000); // 5 seconds for conn-timeout
		mInput = mSocket.getInputStream();
		mOutput = mSocket.getOutputStream();
	}
	
	public void start(LinkedBlockingQueue<byte[]> inputQueue) {
		
		try {
			byte[] buffer = new byte[mSocket.getReceiveBufferSize()];
			
			while (true) {
				
				int bytesRead = mInput.read(buffer);
				if (bytesRead < 0)
					break; // Connection broken
				
				byte[] result = buffer;
				
				if (bytesRead < buffer.length) {
					result = new byte[bytesRead];
					System.arraycopy(buffer, 0, result, 0, bytesRead);
				}
				
				inputQueue.put(result);
			}
		} catch (IOException e) {
			Log.w(TAG, e.getLocalizedMessage(), e);
		} catch (InterruptedException e) {
			Log.i(TAG, e.getLocalizedMessage(), e);
		}
		
	}
	
	public void write(final byte[] bytes) {
		
		mWriteExec.execute(new Runnable() {
			
			@Override
			public void run() {
				
				try {
					mOutput.write(bytes);
				} catch (IOException e) {
					Log.w(TAG, e.getLocalizedMessage(), e);
				}
			}
		});
		
	}
	
	public void stop() {
		
		mWriteExec.shutdown();
		
		try {
			mSocket.close();
		} catch (IOException e) {
			Log.w(TAG, e.getLocalizedMessage(), e);
		}
	}
}
