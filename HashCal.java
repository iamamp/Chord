import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class HashCal {

	public static void main(String[] args) throws InterruptedException, IOException {
		// TODO Auto-generated method stub
		System.out.println(Inet4Address.getLocalHost().getHostAddress());
		
		String[] inp = new String[16];
		inp[0] = "172.18.0.10";
		inp[1] = "172.18.0.11";
		inp[2] = "172.18.0.12";
		inp[3] = "172.18.0.13";
		inp[4] = "172.18.0.14";
		inp[5] = "172.18.0.15";
		inp[6] = "172.18.0.16";
		inp[7] = "172.18.0.17";
		inp[8] = "172.18.0.18";
		inp[9] = "172.18.0.19";
		inp[10] = "172.18.0.20";
		inp[11] = "172.18.0.21";
		inp[12] = "172.18.0.22";
		inp[13] = "172.18.0.23";
		inp[14] = "172.18.0.24";
		inp[15] = "172.18.0.25";
		
		
		
		for(int j=0;j<16;j++) {
			String[] spl = inp[j].split("\\.");
			System.out.print(spl[3]+"  ");
			System.out.println((Integer.parseInt(spl[3])+10)%16);
		}
		
		/*
		System.out.println(Math.floorMod(-5, 16)); //11
		System.out.println();
		/*
		String a = "X|Y";
		System.out.println((a.split("\\|"))[1]);
		//String[] parts = myIP.split("\\.");
		*/
		/*
		System.out.println(Inet4Address.getLocalHost().getHostAddress());
		Socket socket = new Socket();
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		while(br.readLine()!="exit") {
			try {
				socket = new Socket();
				socket.connect(new InetSocketAddress("192.168.99.1", 1980), 10);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
					}
		Thread.sleep(30000);
		*/
		/*
		System.out.println(10 + (11-10 + (2^0))%16);
		System.out.println(10 + (11-10 + (2^1))%16);
		System.out.println(10 + (11-10 + (2^2))%16);
		System.out.println(10 + (11-10 + (2^3))%16);
		System.out.println();
		
		System.out.println(10 + (11-10 + 1)%16);
		System.out.println(10 + (11-10 + 2)%16);
		System.out.println(10 + (11-10 + 4)%16);
		System.out.println(10 + (11-10 + 8)%16);
		*/
	}
	
	
}
