import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class file {

	public static void main(String[] args) throws IOException {
		/*
		String str = "Hello";
	    BufferedWriter writer = new BufferedWriter(new FileWriter("ggg.txt"));
	    writer.write(str);
	    writer.close();
	*/
		
		File file = new File("ggg.txt"); 
		BufferedReader br = new BufferedReader(new FileReader(file)); 
		String st; 
		while ((st = br.readLine()) != null) 
		    System.out.println(st); 
		
	}
	
}
