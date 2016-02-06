package boa.test.GnutellaOperations;

import java.util.Random;

import util.ProcessData;

public class DataConstruct {
	ProcessData processData=new ProcessData();
	
	public byte[] getHeader(int typeOfPayload,int ttl) {
		byte[] b=new byte[6];
		StringBuilder descriptorid=processData.toHex(new Random().nextInt(9999),4);
		for(int i=0,j=0;i<descriptorid.length();i+=2,j++) {
			b[j]=(byte)Integer.parseInt(descriptorid.substring(i, i+2),16);
		}
		b[2]=(byte)typeOfPayload;
		b[3]=(byte)ttl;
		b[4]=0;
	    return b;
	}
	
	public byte[] constructPingPongData(boolean isPing,String ip,int port,int noOfFiles,long folderSize) {
		byte[] pingByte=new byte[27];
		
		if(isPing) {
	    	System.arraycopy(getHeader(0,2), 0, pingByte, 0, 5);
	    }
	    else {
	    	System.arraycopy(getHeader(1,2), 0, pingByte, 0, 5);
	    }
		String[] hesIp=ip.split("\\.");
		for(int i=0,j=5;i<hesIp.length;i++,j++) {
			pingByte[j]=(byte)Integer.parseInt(hesIp[i]);
		}
		StringBuilder hexPort=processData.toHex(port,4);
		for(int i=0,j=9;i<hexPort.length();i+=2,j++) {
			pingByte[j]=(byte)Integer.parseInt(hexPort.substring(i, i+2),16);
		}
		
		StringBuilder numberOfFiles=processData.toHex(noOfFiles,8);
		for(int i=0,j=11;i<numberOfFiles.length();i+=2,j++) {
			pingByte[j]=(byte)Integer.parseInt(numberOfFiles.substring(i, i+2),16);
		}
		StringBuilder sizeOfFiles=processData.toHex((int)folderSize,8);
		for(int i=0,j=15;i<sizeOfFiles.length();i+=2,j++) {
			pingByte[j]=(byte)Integer.parseInt(sizeOfFiles.substring(i, i+2),16);
		}
		long time=System.currentTimeMillis();
		StringBuilder currentTime=processData.toHex(time,16);
		for(int i=0,j=19;i<currentTime.length();i+=2,j++) {
			pingByte[j]=(byte)Integer.parseInt(currentTime.substring(i, i+2),16);
		}
	   
		return pingByte;
	}
	
	public byte[] constructQueryData(String fileName,int ttl,String ip,int port) {
		byte[] queryByte=new byte[11+fileName.length()];
		System.arraycopy(getHeader(2,ttl-1), 0, queryByte, 0, 5);
		String[] hexIp=ip.split("\\.");
		for(int i=0,j=5;i<hexIp.length;i++,j++) {
			queryByte[j]=(byte)Integer.parseInt(hexIp[i]);
		}
		StringBuilder hexPort=processData.toHex(port,4);
		for(int i=0,j=9;i<hexPort.length();i+=2,j++) {
			queryByte[j]=(byte)Integer.parseInt(hexPort.substring(i, i+2),16);
		}
		System.arraycopy(fileName.getBytes(), 0, queryByte, 11, fileName.length());
		return queryByte;
	}
	
	public byte[] constructQueryHitData(int queryId,String file,String ip,int port,int ttl) {
		byte[] queryByte=new byte[13+file.length()];
		System.arraycopy(getHeader(3,ttl-1), 0, queryByte, 0, 5);
		String[] hexIp=ip.split("\\.");
		for(int i=0,j=5;i<hexIp.length;i++,j++) {
			queryByte[j]=(byte)Integer.parseInt(hexIp[i]);
		}
		StringBuilder hexPort=processData.toHex(port,4);
		for(int i=0,j=9;i<hexPort.length();i+=2,j++) {
			queryByte[j]=(byte)Integer.parseInt(hexPort.substring(i, i+2),16);
		}
		StringBuilder hexQueryid=processData.toHex(queryId,4);
		for(int i=0,j=11;i<hexQueryid.length();i+=2,j++) {
			queryByte[j]=(byte)Integer.parseInt(hexQueryid.substring(i, i+2),16);
		}
		for(int i=0,j=13;i<file.length();i++,j++) {
			queryByte[j]=(byte)file.charAt(i);
		}
		return queryByte;
	}
}
