package app;
/**
 * @author mhmd yahya almusaddar
 * @author mhmd@alaqsa.edu.ps
 */
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {
	
	public static void main(String [] args) {
		Algo3Stemmer Stemmer=new Algo3Stemmer(); 
		Scanner s;
		ArrayList<String> list = new ArrayList<>();
		try {
			s = new Scanner(new File("resources/dataset.txt"),"UTF-8");
			while (s.hasNext()){
				list.add(s.next());
			}
			s.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		java.util.Iterator<String> itr= list .iterator();
		while(itr.hasNext()) {
			String element = itr.next();
			//System.out.println("Word: " + element + "  Root: " + Stemmer.stemWord(element));
			System.out.println(Stemmer.stemWord(element));
			System.out.println(list.size());
	}
}
}

