package com.rc.util;

import java.io.IOException;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import android.content.Context;
import android.hardware.usb.UsbManager;
/*
public class UsbConnector {
	
	private UsbManager mUsbManager;
	private UsbSerialDriver mUsbDriver;
	
	public UsbConnector(Context context) {
		mUsbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
	}
	
	public void connect() {
		mUsbDriver = UsbSerialProber.acquire(mUsbManager);
		if (mUsbDriver == null)
			throw new UsbConnectorException("no serial device");
		
		try {
			mUsbDriver.open();
		} catch (IOException e) {
			throw new UsbConnectorException(e);
		}
	}
	
	public void disconnect() {
		checkDriver();
		
		try {
			mUsbDriver.close();
		} catch (IOException e) {
			throw new UsbConnectorException(e);
		}
		mUsbDriver = null;
	}
	
	public byte[] read(int bufSize) {
		checkDriver();
		
		byte[] buffer = new byte[bufSize];
		
		int bytesRead;
		try {
			bytesRead = mUsbDriver.read(buffer, 100);
		} catch (IOException e) {
			throw new UsbConnectorException(e);
		}
		
		if (bytesRead < bufSize) {
			byte[] result = new byte[bytesRead];
			System.arraycopy(buffer, 0, result, 0, bytesRead);
			buffer = result;
		}
			
		return buffer;
	}
	
	public int write(byte... bytes) {
		checkDriver();
		checkWriteArgs(bytes);
		
		try {
			return mUsbDriver.write(bytes, 10);
		} catch (IOException e) {
			throw new UsbConnectorException(e);
		}
	}
	
	private void checkDriver() {
		if (mUsbDriver == null)
			throw new UsbConnectorException("usb connection is not established");
	}

	private void checkWriteArgs(byte[] bytes) {
		if (bytes == null || bytes.length == 0)
			throw new UsbConnectorException("write arguments missing");
	}
}
*/

public class UsbConnector {
	
	private UsbDriver mUsbDriver;

	public UsbConnector(Context context) {
	}
	
	class UsbDriver {
		
		public int read(byte[] buffer, int timeoutMillis) {
			byte[] buff = new byte[] { 80, 13, 10, 86, 13, 10 };
			System.arraycopy(buff, 0, buffer, 0, buff.length);
			return buff.length;
		}
		
		public int write(byte[] bytes, int timeout) {
			return bytes.length;
		}
	}
	
	public void connect() {
		mUsbDriver = new UsbDriver();
		
		if (mUsbDriver == null)
			throw new UsbConnectorException("no serial device");
	}
	
	public void disconnect() {
		checkDriver();
		mUsbDriver = null;
	}
	
	public byte[] read(int bufSize, byte... writeArgs) {
		checkDriver();
		
		byte[] buffer = new byte[bufSize];
		int bytesRead = mUsbDriver.read(buffer, 100);
		if (bytesRead < bufSize) {
			byte[] result = new byte[bytesRead];
			System.arraycopy(buffer, 0, result, 0, bytesRead);
			buffer = result;
		}
			
		return buffer;
	}
	
	public int write(byte... bytes) {
		checkDriver();
		checkWriteArgs(bytes);
		
		return mUsbDriver.write(bytes, 10);
	}
	
	private void checkDriver() {
		if (mUsbDriver == null)
			throw new UsbConnectorException("usb connection is not established");
	}

	private void checkWriteArgs(byte[] bytes) {
		if (bytes == null || bytes.length == 0)
			throw new UsbConnectorException("write arguments missing");
	}
}

@SuppressWarnings("serial")
class UsbConnectorException extends RuntimeException {
	
	public UsbConnectorException(String msg) {
		super(msg);
	}
	
	public UsbConnectorException(Throwable throwable) {
		super(throwable);
	}
}