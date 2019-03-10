import java.io.BufferedReader;
import java.io.IOException;
//import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.File;
import java.io.FileReader;

/* Distributed Hash Table - CHORD
 * Author - Ashish Paralkar
 * Code for a node in the table
 * 
 * IP addresses going from 
 * 172.18.0.10 to 172.18.0.25 in the LAN - can also pick another range by creating another subnet - no hard IPs
 * also 172.18.0.26 is reference table for node joins and leaves
 * 
 * class C -
 * main() for the join, init fingers, then call to helper thread to accept conns for 
 * files or other new node joins, followed by loop for this node to write/query files 
 * */

public class Node {
	static int[] finger = new int[4]; //where the fingers point to by default
	//finger[i] = id+2^i 

	static int[] finger_succ = new int[4]; //ids of successors in ring
	static String[] finger_IP = new String[4]; //their IPs
	static int id;	//id of this node
	static String ip; //IP of this node

	public static void main(String[] args) throws IOException, InterruptedException {
		ip = Inet4Address.getLocalHost().getHostAddress();
		id = hashcal(ip);
		System.out.println("node IP is "+ip);
		System.out.println("node Identifier is "+id);
		
		//initialize finger
		for(int i = 0; i<4; i++) {
			finger[i] = (int) (id + Math.pow(2, i)) % 16;
			System.out.println(finger[i]);
			finger_succ[i] = -1;
		}
		
		//implement the join, with this node as the newjoiner
		Socket s = new Socket();
		s.connect(new InetSocketAddress("172.18.0.26", 1980), 100); //connect to the external table
		//the external table has 
		//been given hard IP that lies outside of the subnet of the DHT 
		
		PrintWriter o = new PrintWriter(s.getOutputStream(), true);
		System.out.println("about to join");
		o.println("join");
		o.println(ip);
		o.println(Integer.toString(id));
		Thread.sleep(75); //was 100ms if any problem arises take back to 100ms
		BufferedReader BRj = new BufferedReader(new InputStreamReader(s.getInputStream()));
		int tempf[] = new int[5]; //fingers of previously existing node
		String tempip[] = new String[5]; //successorIP of previously existing node
		//int temps[] = new int[5];
		
		String first = BRj.readLine();
		//System.out.println("received from table "+first);
		if (first.equals("1")) {
			
			//System.out.println("received finger table from existing");
			for(int j=0;j<5;j++) {
				tempf[j] = Integer.parseInt(BRj.readLine());
				tempip[j] = BRj.readLine();
				//temps[j] = hashcal(tempip[j]);
			}
			/*
			System.out.println("received is:");
			for(int j=0;j<5;j++) {
				System.out.println(tempf[j]+" "+tempip[j]);
			}*/
			
			int j = 0;
			
		    
		    //populate finger of this node using above info
			for(int x=0;x<4;x++) {	//for iterating over this nodes table
				j = 0;	//j for iterating over received node table
				while(j<5) {
					if( Math.floorMod(tempf[(j+1)%5]-tempf[j], 16) > Math.floorMod(finger[x]-tempf[j], 16)  ) {
						int tid = hashcal(tempip[j]); 
						
						if(Math.floorMod(tid - tempf[j], 16) < Math.floorMod(finger[x]-tempf[j], 16)) {
							finger_IP[x] = tempip[(j+1)%5];
							finger_succ[x] = hashcal(tempip[(j+1)%5]);	//this should take care of the join
						}
						else {
							finger_IP[x] = tempip[j];
							finger_succ[x] = hashcal(tempip[j]);	//this should take care of the join
						}
						break;	//entry created
					}
					else {	//need to see another interval from received table
						j++;
					}
				}
			}
		}
		
		else {	//you are the first and only node!
			for(int x=0;x<4;x++) {
				finger_IP[x] = ip;
				finger_succ[x] = id;
			}
		}
		s.close();
		
		new HelperThread().start(); //accept all incoming connections
		
		BufferedReader BR = new BufferedReader(new InputStreamReader(System.in));
		String inp;
		String infos[] = new String[6];
		String infoq[] = new String[6];
		
		while ( true ) {	//infinite loop to let the user do mutiple actions at a node - query,store files, print own finger table or to leave
			System.out.println("enter 'q' for query or 's' for store or 'l' to leave ");
			inp = BR.readLine();
			switch(inp) {
			case "s":
				System.out.println("enter filename of file to be stored");
				inp = BR.readLine();
				infos[0] = ip;
				infos[1] = Integer.toString(id);
				infos[2] = "write";
				infos[3] = inp;	//name
				infos[4] = Integer.toString(Math.floorMod(inp.hashCode(), 16));
				System.out.println("file hashes to "+infos[4]);
				System.out.println("enter file content");		
				infos[5] = BR.readLine(); //actual content
				
				
				if(inp.hashCode()%16 == id ) {
					System.out.println("this file will be stored here itself!");
					//code to write file
					BufferedWriter writer = new BufferedWriter(new FileWriter(infos[3]));
				    writer.write(infos[5]);
				    writer.close();
				}
				else{ //more code needs to go here
					Node.filesend(infos); 
				}
				break;
				
			case "q":
				System.out.println("enter name of file to be queried");
				inp = BR.readLine();
				infoq[0] = ip;
				infoq[1] = Integer.toString(id);
				infoq[2] = "query";
				infoq[3] = inp;	//name
				infoq[4] = Integer.toString(inp.hashCode()%16);
				System.out.println("file hashes to "+infoq[4]);
				infoq[5] = Node.ip;
				
				if(inp.hashCode()%16 == id ) {
					System.out.println("this file should be present right here\n Checking if it is present");
					File file = new File(infoq[3]); 
					if (file.exists()) {
						System.out.println("printing file contents");
						BufferedReader bb = new BufferedReader(new FileReader(file)); 
						String st; 
						while ((st = bb.readLine()) != null) 
						    System.out.println(st);
						bb.close();
						
					}
					
					//code to check and display contents of file here itself
				}
				else{
					Node.filesend(infoq); //call the send function and preserve senders address!
				}
				break;
				
			case "l":
				s = new Socket();
				s.connect(new InetSocketAddress("172.18.0.26", 1980), 100); //connect to the external table
				o = new PrintWriter(s.getOutputStream(), true);
				System.out.println("about to leave");
				o.println("leave");
				o.println(ip); //send own IP
				o.println(Integer.toString(id)); //send own id
				
				o.println(Node.finger_IP[0]); //send IP of this departing node's immediate successor
				break;
			
			case "f":
				System.out.println("finger table:");
				for(int x=0;x<4;x++) {
					System.out.println(finger[x]+"   "+finger_succ[x]);
				}
				break;
				
			default:
				System.out.println("enter either 's' or 'q' or 'l' or 'f'");
				break;
			}
		}
	}
	
	public static void join(String ip,int id) {
		//this node will be informed of the join of a new node
		int x = 0;
		System.out.println("previously:");
		for(x=0;x<4;x++) {
			System.out.println(finger[x]+" "+finger_succ[x]);
		}
		for(x=0;x<4;x++) {
			if( Math.floorMod(finger_succ[x] - finger[x],16) > Math.floorMod(id - finger[x],16) ) {
				finger_succ[x] = id;
				finger_IP[x] = ip;
			}
		}
		System.out.println("now:");
		for(x=0;x<4;x++) {
			System.out.println(finger[x]+" "+finger_succ[x]);
		}
	}
	
	public static void leave(String ip,int id, String sip) {
		int x = 0;
		System.out.println("previously:");
		for(x=0;x<4;x++) {
			System.out.println(finger[x]+" "+finger_succ[x]);
		}
		for(x=0;x<4;x++) {
			if( finger_IP[x].equals(ip) ) {
				finger_succ[x] = Node.hashcal(sip);
				finger_IP[x] = sip; //set new successor as this node is leaving
			}
		}
		System.out.println("now:");
		for(x=0;x<4;x++) {
			System.out.println(finger[x]+" "+finger_succ[x]);
		}
	}
	
	public static int hashcal(String inp) {
		String[] spl = inp.split("\\.");
		return //Integer.parseInt((spl[3])+10)%16;
				((Integer.parseInt(spl[3])+10)%16);
	}
	
	public static void filesend(String[] info) throws IOException, InterruptedException {
		PrintWriter out = null;
		Socket socket = null;
		
		int i;
		int loc = 1;	//index of appropriate successor
		int hash = Integer.parseInt(info[4]); 
		int sender_id = Integer.parseInt(info[1]); //VERY ESSENTIAL!
		
		if(info[2].equals("query")) {
			System.out.println("mode is query");
			System.out.println("initiated by "+info[5]);
		}
		else {
			System.out.println("mode is write");
		}
		
		//check if file belongs here or needs to be passed on
		//3 such cases for that:
		//id and hash exact match
		//distance between this node and sender > distance betn this node and hash
		//the lower down entries of the table point to this node itself
		
		int t1 = Math.floorMod(Node.id - sender_id, 16);
		int t2 = Math.floorMod(Node.id - hash, 16);
		
		for(i=0;i<3;i++) { //locate the right finger
			if( Math.floorMod(Node.finger[i+1] - Node.finger[i], 16) > Math.floorMod(hash - Node.finger[i], 16) ) {
				loc = i;
				System.out.println("located finger is "+loc);
				break;
			}//has to be true for atleast one if the i's from 0 to 2
		}
		
		if(hash == Node.id || t1 > t2 || Node.finger_succ[loc] == Node.id) { //done this is the right place
			if(info[2].equals("write")) {
				//code to write file HERE
				System.out.println("a file will be written here");
				BufferedWriter writer = new BufferedWriter(new FileWriter(info[3]));
			    writer.write(info[5]);
			    writer.close();
			}
			
			else {
				//code to query and read file HERE
				System.out.println("looking for file "+info[3]);
				File file = new File(info[3]); 
				String st = "file not found"; //default
				
				socket = new Socket();
				socket.connect(new InetSocketAddress(info[5], 1980), 100);
				out = new PrintWriter(socket.getOutputStream(), true);
				
				
				if (file.exists()) {
					
					System.out.println("printing file contents");
					BufferedReader bb = new BufferedReader(new FileReader(file)); 
					String s = bb.readLine();
					System.out.println(s);
					bb.close();
					
					out.println("response");
					out.println(s);	 //send string st to original node that requested it
					
				}
				else {
					out.println("response");
					out.println(st);
				}
				socket.close();
				
			}
		}
		
		else {//an actual need for forwarding
			//forward
			socket = new Socket();
			socket.connect(new InetSocketAddress(finger_IP[loc], 1980), 100);
			out = new PrintWriter(socket.getOutputStream(), true);
			out.println(info[0]);
			//out.println(info[1]);
			out.println(Integer.toString(Node.id)); //append your id instead
			out.println(info[2]);
			out.println(info[3]);
			out.println(info[4]);
			out.println(info[5]);
			socket.close();			
		}
	}
}

class ConnThread extends Thread {
    Socket socket;
    
    public ConnThread(Socket clientSocket) {
        this.socket = clientSocket;
        //System.out.println("inside thread constr");
    }
    
    public void run() {
    	BufferedReader BR;
    	PrintWriter out;
    	String[] info = new String[6]; //size is 6 but loop runs till 5
    	
		try {
			BR = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);	//use only for request case
			//out.println(Inet4Address.getLocalHost().getHostAddress());
			
			//new node wants to join, and this existing node must update its table
			String first = BR.readLine();
			//i think next 10 lines may not be used
			if(first.equals("join")) {	//line 185
				//System.out.println("a new node is about to join");
				String ip = BR.readLine();
				//int id = Integer.parseInt(BR.readLine());
				int id = Node.hashcal(ip);
				Node.join(ip, id);	//not sending back anything
				socket.close();
			}
			
			//this existing node has been determined as successor of new joining node
			//and wants to borrow a finger table from this node to populate its own table
			else if (first.equals("request")) {
				//System.out.println("a new node is requesting me for finger table");
				for (int i=0; i<4; i++) {	//sending out finger and succ IPs
					out.println(Integer.toString(Node.finger[i]));
					out.println(Node.finger_IP[i]);
				}
				socket.close();
			}
			
			//this node got notified about another node wanting to leave and must update its own table
			else if(first.equals("leave")) {
				String ip = BR.readLine(); //leavers ip
				int id = Integer.parseInt(BR.readLine()); //leavers id
				String SIP = BR.readLine(); //leaver's successor's IP
				Node.leave(ip, id, SIP);
			}
			
			//this node queried for a file and is getting back a response from node where file is located
			else if(first.equals("response")) {
				String con = BR.readLine();
				System.out.println(con);
				socket.close();
			}
			
			//node is an intermediate node whose job is to forward file with content or file query request
			else { //file query or write case
				int i = 0;
				info[0] = first;
				for(i=1;i<6;i++) { //6th is lines but depends on mode being "write"
					info[i] = BR.readLine(); //get ip,id,***mode***,filename,hash
				}
				/*
				if(info[2].equals("write")) {
					info[5] = BR.readLine();
				}*/
				System.out.println("calling send function");
				socket.close();
				Node.filesend(info);
			}
			
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}

//ServerSocket code to accept connections in new threads
class HelperThread extends Thread {
	public void run() {
		ServerSocket serverSocket = null;
		Socket socket = null;
		try {
            serverSocket = new ServerSocket(1980);	//leave the socket is never close warning as it is..
            System.out.println("ready to accept at 1980!");
        } catch (IOException ex) {
            Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
        }
		while(true) {
            try { 
                socket = serverSocket.accept();
            } catch (IOException ex) {
                Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            new ConnThread(socket).start();
        }
	}
}
 