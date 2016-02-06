package boa.test.service;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import GnutellaOperations.DataConstruct;
import util.ProcessData;

public class Client {
	String id;
	int port;
	String ip;
	String myFilesLocation;
	int numberOfFiles;
	long sizeOfFiles;
	DatagramSocket clientSocket;
    InetAddress IPAddress;
	Scanner sc=new Scanner(System.in);
	HashMap<String,byte[]> neighborList = new HashMap<String,byte[]>();
	HashMap<Integer,String> requestId=new HashMap<Integer,String>();
	int maxNeighbors=2;
	int tcpPort;
	ProcessData processData=new ProcessData();
	DataConstruct dataConstruct=new DataConstruct();
	static ArrayList <String>messageCodes=new <String>ArrayList();
	
	public Client() throws IOException {
		messageCodes.add("Ping");messageCodes.add("Pong");messageCodes.add("Query");messageCodes.add("QueryHit");messageCodes.add("Get");messageCodes.add("Push");
		ip="127.0.0.1";
		System.out.println("Enter port:");
		port=sc.nextInt();
		System.out.println("Enter Client id:");
		id=sc.next();
		myFilesLocation="/home/ram/MyStuff/iowaStateMaterials/Distributed_And_Network_Programming/hw3/GNutella/";
		clientSocket = new DatagramSocket();
		new GnutellaMessageReciever().start();
		new FileSender(port).start();
		new PingMonitor().start();
	}
	
	public boolean isFirstNodeInNutellaNetwork() {
		System.out.println("R you the first node in Gnuteela network?(Y/N)");
		if(sc.next().equalsIgnoreCase("Y")) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public int findNumberOfFiles() {
		return new File(myFilesLocation+id+"/").list().length-3;
	}
	
	public long folderSize() {
	    long length = 0;
	    for (File file : new File(myFilesLocation+id+"/").listFiles()) {
	        if (file.isFile())
	            length += file.length();
	        else
	            length += folderSize();
	    }
	    return length;
	}
	
	public void sendPing(String ip,int port,byte[] message) throws IOException {
        int decodedId=Integer.parseInt(processData.toBinary((int)message[0])+processData.toBinary((int)message[1]),2);
        String decodedIp=(int)message[5]+"."+(int)message[6]+"."+(int)message[7]+"."+(int)message[8];
        int decodedPort=Integer.parseInt(processData.toBinary((int)message[9])+processData.toBinary((int)message[10]),2);
        requestId.put(decodedId,decodedIp+"#"+decodedPort);
	    IPAddress = InetAddress.getByName(ip);
	    DatagramPacket pingPacket = new DatagramPacket(message, message.length, IPAddress, port);
	    clientSocket.send(pingPacket);
	    System.out.println("ping sent to"+port);
	}
	public void sendPong(String ip,int port,byte[] message) throws IOException {
		IPAddress = InetAddress.getByName(ip);
	    DatagramPacket pingPacket = new DatagramPacket(message, message.length, IPAddress, port);
	    clientSocket.send(pingPacket);
	    System.out.println("pong sent to "+port);
	}
	
	public void sendQuery(byte[] message) throws IOException {
		int decodedId=Integer.parseInt(processData.toBinary((int)message[1])+processData.toBinary((int)message[0]),2);
        String decodedIp=(int)message[5]+"."+(int)message[6]+"."+(int)message[7]+"."+(int)message[8];
        int decodedPort=Integer.parseInt(processData.toBinary((int)message[9])+processData.toBinary((int)message[10]),2);
		
		requestId.put(decodedId,decodedIp+"#"+decodedPort);
		byte[] fileBytes=new byte[message.length-11];
		System.arraycopy(message, 11, fileBytes, 0, message.length-11);
		String file=new String(fileBytes);
		if(!searchInLocalDisk(file)) {
			String[] ip=this.ip.split("\\.");
			for(int i=0,j=5;i<ip.length;i++,j++) {
				message[j]=(byte)Integer.parseInt(ip[i]);
			}
			StringBuilder hexPort=processData.toHex(this.port,4);
			for(int i=0,j=9;i<hexPort.length();i+=2,j++) {
				message[j]=(byte)Integer.parseInt(hexPort.substring(i, i+2),16);
			}
			int ttl=Integer.parseInt(processData.toBinary((int)message[3]),2);
			
			if(ttl>=0) {
				Iterator it = neighborList.entrySet().iterator();
				while (it.hasNext()) {
						Map.Entry pairs = (Map.Entry)it.next();
						byte[] neighborData=(byte[])pairs.getValue();
						int nport=Integer.parseInt(processData.toBinary((int)neighborData[9])+processData.toBinary((int)neighborData[10]),2);
				        IPAddress = InetAddress.getByName((int)neighborData[5]+"."+(int)neighborData[6]+"."+(int)neighborData[7]+"."+(int)neighborData[8]);
					    DatagramPacket pingPacket = new DatagramPacket(message, message.length, IPAddress, nport);
					    clientSocket.send(pingPacket);
					    System.out.println("query sent to "+nport);					        
				}
			}
		}
		else {
			String[] ipport=requestId.get(decodedId).split("#");
			sendQueryHit(dataConstruct.constructQueryHitData(decodedId,file,this.ip,this.port,5),ipport[0],ipport[1]);
		}
	}
	
	public void sendQueryHit(byte[] message,String ip,String port) throws IOException {
		IPAddress = InetAddress.getByName(ip);
	    DatagramPacket queryHitPacket = new DatagramPacket(message, message.length, IPAddress, Integer.parseInt(port));
	    clientSocket.send(queryHitPacket);
	    System.out.println("query hit sent to "+port);
	}
	
	public boolean searchInLocalDisk(String fileName) throws IOException{
		return new File(myFilesLocation+id+"/", fileName.trim()).exists();
	}
	
	public void displayNeighborList() {
		System.out.println("-------Neighbor client list-------");
		System.out.println("------------------------------------------------------------");
		Iterator it = neighborList.entrySet().iterator();
		 int i=1;
		 while (it.hasNext()) {
		        Map.Entry pairs = (Map.Entry)it.next();
		        System.out.println(i+"."+pairs.getKey()+"\t"+pairs.getValue());
		        i++;
		 }
		 System.out.println("------------------------------------------------------------");
	}
	
	class GnutellaMessageReciever extends Thread {
		public void run() {
			DatagramSocket serverSocket;
			try {
				serverSocket = new DatagramSocket(port);
				byte[] receiveData = new byte[1024];
				while(true) {
					DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
					serverSocket.receive(receivePacket);
					byte[] message = new byte[receivePacket.getLength()];
				    System.arraycopy(receivePacket.getData(), 0, message, 0, message.length);
				    
					int ttl=Integer.parseInt(processData.toBinary((int)message[3]),2);
					message[3]=(byte)(ttl-1);
					int hop=Integer.parseInt(processData.toBinary((int)message[4]),2);
					message[4]=(byte)(hop+1);
					int decodedId=Integer.parseInt(processData.toBinary((int)message[0])+processData.toBinary((int)message[1]),2);
					int decodedType=Integer.parseInt(processData.toBinary((int)message[2]),2);
					int decodedPort=Integer.parseInt(processData.toBinary((int)message[9])+processData.toBinary((int)message[10]),2);
					String decodedIp=(int)message[5]+"."+(int)message[6]+"."+(int)message[7]+"."+(int)message[8];
			        
					if(!requestId.containsKey(decodedId))  {
						System.out.println(messageCodes.get(decodedType)+" recieved from "+decodedPort);
						if(decodedType==2) {
							sendQuery(message);
						}
						else if(decodedType==3) {
							int queryId=Integer.parseInt(processData.toBinary((int)message[11])+processData.toBinary((int)message[12]),2);
							String ipport=requestId.get(queryId);
							String[] decIpPort=ipport.split("#");
							if(ipport.equalsIgnoreCase(ip+"#"+port)) {
								byte[] fileBytes=new byte[message.length-13];
								System.arraycopy(message, 13, fileBytes, 0, message.length-13);
								String file=new String(fileBytes);
								System.out.println("Requested file: "+file+" found in IP: "+decodedIp+" Port:"+decodedPort);
								try {
									Socket client = new Socket(decodedIp, decodedPort);
							        OutputStream outToServer = client.getOutputStream();
							        DataOutputStream out = new DataOutputStream(outToServer);
							        
							        
							        byte[] getByte=new byte[11+file.length()];
									System.arraycopy(dataConstruct.getHeader(4,5), 0, getByte, 0, 5);
								    String[] hexip=ip.split("\\.");
									for(int i=0,j=5;i<hexip.length;i++,j++) {
										getByte[j]=(byte)Integer.parseInt(hexip[i]);
									}
									StringBuilder hexPort=processData.toHex(port,4);
									for(int i=0,j=9;i<hexPort.length();i+=2,j++) {
										getByte[j]=(byte)Integer.parseInt(hexPort.substring(i, i+2),16);
									}
									System.arraycopy(fileBytes, 0, getByte, 11, fileBytes.length);
							        out.write(getByte);
							        System.out.println("Get sent to "+decodedPort);
							        InputStream inFromServer = client.getInputStream();
							        DataInputStream in = new DataInputStream(inFromServer);
							        byte[] pushByte=new byte[1024];							        
							        try {
							        	in.readFully(pushByte);
							        }
							        catch(EOFException e){
							        	
							        }
							        
							        int pushType=Integer.parseInt(processData.toBinary((int)pushByte[2]),2);
									int pushPort=Integer.parseInt(processData.toBinary((int)pushByte[9])+processData.toBinary((int)pushByte[10]),2);
									int fileLength=Integer.parseInt(processData.toBinary((int)pushByte[11])+processData.toBinary((int)pushByte[12])+processData.toBinary((int)pushByte[13])+processData.toBinary((int)pushByte[14]),2);
							        if(pushType==5) {
							        	System.out.println("Push recieved from "+pushPort);
							        	File f = new File(myFilesLocation+id+"/"+file);
							        	FileWriter fw = new FileWriter(f.getAbsoluteFile());
										BufferedWriter bw = new BufferedWriter(fw);
										byte[] fileContent=new byte[fileLength];
										System.arraycopy(pushByte, 15, fileContent, 0, fileContent.length);
										bw.write(new String(fileContent));
										bw.close();
							        	client.close();
							        }
							      }catch(IOException e)
							      {
							         e.printStackTrace();
							      }
							}
							else {
								sendQueryHit(message,decIpPort[0],decIpPort[1]);
							}
						}
						else {
							if(decodedType==0) {
								requestId.put(decodedId,decodedIp+"#"+decodedPort);
								sendPong(decodedIp,decodedPort,dataConstruct.constructPingPongData(false,ip,port,findNumberOfFiles(),folderSize()));
								Iterator it = neighborList.entrySet().iterator();
								if(ttl>0) {
									while (it.hasNext()) {
										Map.Entry pairs = (Map.Entry)it.next();
									    byte[] neighborData=(byte[])pairs.getValue();
									    String neighborIp=(int)neighborData[5]+"."+(int)neighborData[6]+"."+(int)neighborData[7]+"."+(int)neighborData[8];
									    int neighborPort=Integer.parseInt(processData.toBinary((int)neighborData[9])+processData.toBinary((int)neighborData[10]),2);
									    sendPing(neighborIp,neighborPort,message);					        
									}
								}
							}
							if(neighborList.size()<maxNeighbors) {
								neighborList.put(decodedIp+decodedPort, message);
							}
							displayNeighborList();
						}
					}
				}
			}
			catch (IOException e) {
				e.printStackTrace();
			}
        }
	}
	
	class FileSender extends Thread {
		ServerSocket serverSocket;
		   
		public FileSender(int port) throws IOException {
		      serverSocket = new ServerSocket(port);
		}

		public void run() {
		      while(true) {
		         try {
		            Socket server = serverSocket.accept();
		            DataInputStream in = new DataInputStream(server.getInputStream());
		            byte[] message=new byte[16];
		            in.readFully(message);
		            int decodedType=Integer.parseInt(processData.toBinary((int)message[2]),2);
					int decodedPort=Integer.parseInt(processData.toBinary((int)message[9])+processData.toBinary((int)message[10]),2);
					byte[] filename=new byte[message.length-11];
					for(int i=11,j=0;i<message.length;i++,j++) {
						filename[j]=message[i];
					}
					String decFilename=new String(filename);
					
		            if(decodedType==4) {
		            	System.out.println("Get recieved from "+decodedPort);
		            	BufferedReader br = new BufferedReader(new FileReader(myFilesLocation+id+"/"+decFilename));
		            	String sCurrentLine,fileContent="";
		            	while ((sCurrentLine = br.readLine()) != null) {
		            		fileContent+=sCurrentLine;
		        		}
		            	DataOutputStream out = new DataOutputStream(server.getOutputStream());
		            	
		            	byte[] pushByte=new byte[15+fileContent.length()];
		        		System.arraycopy(dataConstruct.getHeader(5,5), 0, pushByte, 0, 5);
		        		String[] hexIp=ip.split("\\.");
		        		for(int i=0,j=5;i<hexIp.length;i++,j++) {
		        			pushByte[j]=(byte)Integer.parseInt(hexIp[i]);;
		        		}
		        		StringBuilder hexPort=processData.toHex(port,4);
		        		for(int i=0,j=9;i<hexPort.length();i+=2,j++) {
		        			pushByte[j]=(byte)Integer.parseInt(hexPort.substring(i, i+2),16);
		        		}
		        		
		        		StringBuilder fileLength=processData.toHex(fileContent.length(),8);
		        		for(int i=0,j=11;i<fileLength.length();i+=2,j++) {
		        			pushByte[j]=(byte)Integer.parseInt(fileLength.substring(i, i+2),16);
		        		}
		        		
		        		for(int i=0,j=15;i<fileContent.length();i++,j++) {
		        			pushByte[j]=(byte)fileContent.charAt(i);
		        		}
		        		
		            	out.write(pushByte);
			            System.out.println("Push sent to "+decodedPort);
			        }
		            server.close();
		         }
		         catch(IOException e) {
		            e.printStackTrace();
		            break;
		         }
		      }
		   }
	}
	
	class PeriodicPingAgent extends Thread {
		public void run() {
			while(true) {
				try {
					sleep(5000);
					Iterator it = neighborList.entrySet().iterator();
					while (it.hasNext()) {
						Map.Entry pairs = (Map.Entry)it.next();
					    byte[] neighborData=(byte[])pairs.getValue();
					    String neighborIp=(int)neighborData[5]+"."+(int)neighborData[6]+"."+(int)neighborData[7]+"."+(int)neighborData[8];
					    int neighborPort=Integer.parseInt(processData.toBinary((int)neighborData[9])+processData.toBinary((int)neighborData[10]),2);
					    sendPing(neighborIp,neighborPort,dataConstruct.constructPingPongData(true,ip,port,findNumberOfFiles(),folderSize()));
						System.out.println("Periodic Ping sent to "+neighborPort);					        
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	class PingMonitor extends Thread {
		
		public void run() {
			while(true) {
				HashMap tmpNeighborList = (HashMap)neighborList.clone();
				Iterator it = tmpNeighborList.entrySet().iterator();
			    while (it.hasNext()) {
			        Map.Entry pairs = (Map.Entry)it.next();
			        byte[] tmp=(byte[])pairs.getValue();
			        long time=Long.parseLong(processData.toBinary((int)tmp[19])+processData.toBinary((int)tmp[20])+processData.toBinary((int)tmp[21])+processData.toBinary((int)tmp[22])+processData.toBinary((int)tmp[23])+processData.toBinary((int)tmp[24])+processData.toBinary((int)tmp[25])+processData.toBinary((int)tmp[26]),2);
			        if(System.currentTimeMillis()-time>10000) {
			        	neighborList.remove(pairs.getKey());
			        	String tmpIp=(int)tmp[5]+"."+(int)tmp[6]+"."+(int)tmp[7]+"."+(int)tmp[8];
					    int tmpPort=Integer.parseInt(processData.toBinary((int)tmp[9])+processData.toBinary((int)tmp[10]),2);
						System.out.println("No ping/pong has been recieved from neighbour with Ip :"+tmpIp+" port: "+tmpPort+". Neighbour removed from the neighbour list");
			        	System.out.println("Updated Neighbour List Below");
			        	displayNeighborList();
			       }
			    }
				try {
					sleep(5000);
				} catch (InterruptedException e) {
				}
			}
		}
	}
	
	public static void main(String args[]) throws IOException {
		Client c=new Client();
	    if(!(c.isFirstNodeInNutellaNetwork())) {
			System.out.println("Enter ip and port of well known root");
			String rootip=c.sc.next();
			int rootport=c.sc.nextInt();
			c.sendPing(rootip,rootport,c.dataConstruct.constructPingPongData(true,c.ip,c.port,c.findNumberOfFiles(),c.folderSize()));
		}
	    c.new PeriodicPingAgent().start();
	    System.out.println("Do you want to search for any file?Y/N");
		if(c.sc.next().equalsIgnoreCase("Y")) {
			System.out.println("Enter name of the file : ");
			c.sendQuery(c.dataConstruct.constructQueryData(c.sc.next(),5,c.ip,c.port));
		}
	}
}
