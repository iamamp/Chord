/* 
 * Distributed Hash Table - CHORD
 * Author - Ashish Paralkar
 * Code for the external reference table for leaves and joins
 */ 


import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.PrintWriter;

public class Table {
	//static ArrayList<String> a = new ArrayList<String>();
	static HashMap<Integer,String> h = new HashMap<Integer,String>();
	public static void main(String[] args) throws IOException, InterruptedException {
		new HThread().start();
	}
	
	public static int hashcal(String inp) {
		String[] spl = inp.split("\\.");
		return //Integer.parseInt((spl[3])+10)%16;
				((Integer.parseInt(spl[3])+10)%16);
	}
}

class CThread extends Thread {
	Socket socket;	//for the joining/leaving node
	
	public CThread(Socket clientSocket) {
		this.socket = clientSocket;
        //System.out.println("inside thread constr");
    }
	
	public void run() {
		//System.out.println("inside run");
    	BufferedReader BR,BRex;
    	PrintWriter out,outex; //for existing nodes
    	
    	
		try {
			String IP; //node that wants to join or leave
	    	String id;
			String action;
			BR = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);
			System.out.println(action = BR.readLine());	//will be either join or leave
			System.out.println(IP = BR.readLine());	//joiner's/leaver's IP
			System.out.println(id = BR.readLine()); //joiner's/leaver's id
			
			if (action.equals("join")) {
				System.out.println("inside action = join");
				//update existing nodes first as mentioned on page 5 and 6 of DHT paper
				
				Socket sout = null;	//for existing nodes
				
				if (Table.h.size() > 0) {
					Iterator it = Table.h.entrySet().iterator();
					System.out.println("inside tablesize > 0");
					while (it.hasNext()) {
						Map.Entry pair = (Map.Entry)it.next();
				        sout = new Socket();
				        System.out.println("informing "+pair.getValue());
				        sout.connect(new InetSocketAddress(pair.getValue().toString(), 1980), 100);
						outex = new PrintWriter(sout.getOutputStream(), true);
						outex.println("join");
						outex.println(IP);
						//outex.println(id);
						outex.close();
						sout.close();
				    }
					
					//pick a node that'll help the new joiner update its own table
					sout = new Socket();	//again to existing node's Serversocket
					
					int min = 100;
					int sid = 0; //the actual successor who will hand over finger table
					it = Table.h.entrySet().iterator();
				    while (it.hasNext()) {
				        Map.Entry pair = (Map.Entry)it.next();
			        	if (Math.floorMod((Integer)pair.getKey() - Integer.parseInt(id), 16) < min) {
							sid = (Integer)pair.getKey();
							min = Math.floorMod((Integer)pair.getKey() - Integer.parseInt(id), 16);
						}
				    }
				    
					System.out.println("getting from "+Table.h.get(sid));
			        sout.connect(new InetSocketAddress(Table.h.get(sid), 1980), 100);
			        outex = new PrintWriter(sout.getOutputStream(), true);
			        BRex = new BufferedReader(new InputStreamReader(sout.getInputStream()));
			        String f[] = new String[4];
			        String fs[] = new String[4];
			        outex = new PrintWriter(sout.getOutputStream(), true);
					outex.println("request");
			        
			        for(int i =0;i<4;i++) {
			        	f[i] = BRex.readLine();	//finger index
			        	fs[i] = BRex.readLine(); //succ IP
			        }
			        System.out.println("received from existing:");
			        for(int i =0;i<4;i++) {
			        	System.out.println(f[i]+" "+fs[i]);
			        }
			        
			        //total 11 strings sent
			        //now connect back to the node thats about to join
			        out.println("1"); //to show not the first node
			        
			        out.println(Integer.toString(sid));
			        out.println(Table.h.get(sid));
			        for(int i =0;i<4;i++) {
			        	out.println(f[i]);
			        	out.println(fs[i]);	//the newcoming node should be able to populate
			        						//its own finger table from this info
			        }
				}
				else {
					System.out.println("inside tablesize = 0");
					out.println("0");
				}
				socket.close();
				//add this node to the table
				Table.h.put(Integer.parseInt(id),IP);
			}
			
			else {
				//the leave case
				System.out.println("node "+id+" wants to leave!");
				String succIP = BR.readLine();
				
				Socket sout = null;	//for existing nodes
				
				if (Table.h.size() > 0) {
					Iterator it = Table.h.entrySet().iterator();
					System.out.println("inside tablesize > 0");
					while (it.hasNext()) {
						Map.Entry pair = (Map.Entry)it.next();
				        sout = new Socket();
				        System.out.println("informing "+pair.getValue());
				        sout.connect(new InetSocketAddress(pair.getValue().toString(), 1980), 100);
						outex = new PrintWriter(sout.getOutputStream(), true);
						outex.println("leave");
						outex.println(IP); //node thats about to leave
						
						outex.println(id); //id of node thats about to leave
						outex.println(succIP); //send leaving node's successor's IP
						outex.close();
						sout.close();
				    }
				}
				//else {
					Table.h.remove(Integer.parseInt(id)); //no updates needed, 
					//this was the only node and now its gone, so remove it from the Map too
				//}
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
	}
}

class HThread extends Thread {
	public void run() {
		ServerSocket serverSocket = null;
		Socket socket = null;
		try {
            serverSocket = new ServerSocket(1980);
            System.out.println("ready to accept at 1980!");
        } catch (IOException ex) {
            Logger.getLogger(Table.class.getName()).log(Level.SEVERE, null, ex);
        }
		while(true) {
            try { 
                socket = serverSocket.accept();
            } catch (IOException ex) {
                Logger.getLogger(Table.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            new CThread(socket).start();
        }
	}
}


