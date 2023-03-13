
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.sql.Timestamp;
import java.util.Scanner;

import java.io.BufferedReader;
public class Main {
	
	public static boolean contain(String stop[],String word) {
		boolean flag = true;
		for (int i = 0; i < stop.length; i++) {
			if(stop[i].equalsIgnoreCase(word)) {
				flag = false;
				break;
				
			}
		}
		return flag;
	}
	public static String[] WordsReader(String directory) {
		BufferedReader br = null;
		String text = "";
		try {
		    try {
		        br = new BufferedReader(new FileReader(directory));
		        String line;
		        while ((line = br.readLine()) != null) {
		        	text += line + " ";
		        	
		        	
		        }

		    } finally {
		        br.close();
		    }
		} catch (Exception e) {
		    throw new RuntimeException("Error while reading");
		}
		String Words[]= text.split(" ");
		return Words;
	}
	

	public static void main(String[] args) throws FileNotFoundException {
		
		
		HashedDictionary  hs2 = new HashedDictionary();
		String DELIMITERS = "[-+=" +" " +        //space
		        "\r\n " +    //carriage return line fit

				"1234567890" + //numbers

				"’'\"" +       // apostrophe

				"(){}<>\\[\\]" + // brackets

				":" +        // colon

				"," +        // comma

				"‒–—―" +     // dashes

				"…" +        // ellipsis

				"!" +        // exclamation mark

				"." +        // full stop/period

				"«»" +       // guillemets

				"-‐" +       // hyphen

				"?" +        // question mark

				"‘’“”" +     // quotation marks

				";" +        // semicolon

				"/" +        // slash/stroke

				"⁄" +        // solidus

				"␠" +        // space?   

				"·" +        // interpunct

				"&" +        // ampersand

				"@" +        // at sign

				"*" +        // asterisk

				"\\" +       // backslash

				"•" +        // bullet

				"^" +        // caret

				"¤¢$€£¥₩₪" + // currency

				"†‡" +       // dagger

				"°" +        // degree

				"¡" +        // inverted exclamation point

				"¿" +        // inverted question mark

				"¬" +        // negation

				"#" +        // number sign (hashtag)

				"№" +        // numero sign ()

				"%‰‱" +      // percent and related signs

				"¶" +        // pilcrow

				"′" +        // prime

				"§" +        // section sign

				"~" +        // tilde/swung dash

				"¨" +        // umlaut/diaeresis

				"_" +        // underscore/understrike

				"|¦" +       // vertical/pipe/broken bar

				"⁂" +        // asterism

				"☞" +        // index/fist

				"∴" +        // therefore sign

				"‽" +        // interrobang

				"※" +           // reference mark

		        "]";

				
		Scanner girdi = new Scanner(System.in);
		//System.out.println("enter word");
		//String c = girdi.nextLine();
		System.out.println("hash function selection");
		System.out.println("1-ssf");
		System.out.println("2-paf");
		
		hs2.hf = girdi.nextInt();
		System.out.println("collision technique selection");
		System.out.println("1-linear");
		System.out.println("2-double");
		hs2.ct = girdi.nextInt();
		
		
		String Words[]= WordsReader("stop_words_en.txt");
		
		
		
		Timestamp indexing = new Timestamp(System.currentTimeMillis());
		
		File klasor = new File("bbc");
		
		String altklasörler[] = klasor.list();
		
		for (int i = 0; i < altklasörler.length; i++) {
			File altklasör = new File("bbc/" + altklasörler[i]);
			String dosyalar[] = altklasör.list();
			
			
			for (int j = 0; j < dosyalar.length; j++) {
				HashedDictionary hs = new HashedDictionary();
				hs.hf = hs2.hf;
				hs.ct = hs2.ct;

				Scanner txt = new Scanner(new File("bbc/" + altklasörler[i] + "/" + dosyalar[j]));

				while (txt.hasNextLine()) {

					String[] arrOfStr = txt.nextLine().toLowerCase().split(DELIMITERS);
					for (String string : arrOfStr) {
						if (contain(Words,string)) {
							if (hs.contains(string))
								hs.put(string, (int) hs.getValue(string) + 1);
							else
								hs.put(string, 1);
						}

					}

				}
				
				hs.Transferring(hs2, altklasörler[i]+"_" + dosyalar[j]);

			}
		}
		
		Timestamp indexing2 = new Timestamp(System.currentTimeMillis());
		
		
		double sumSearchTime = 0;
		double minSearchTime = Integer.MAX_VALUE;
		double maxSearchTime = 0;
		double diff=0;

		String Searchs[]= WordsReader("search.txt");
		
		for (int i = 0; i < Searchs.length; i++) {
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			
			
			hs2.Search(Searchs[i]);
			System.out.println();
			
			
			Timestamp timestamp2 = new Timestamp(System.currentTimeMillis());
			diff = (double) (timestamp2.getTime()-timestamp.getTime());
			sumSearchTime += timestamp2.getTime()-timestamp.getTime();
			if(diff < minSearchTime)
				minSearchTime = diff;
			if(diff > maxSearchTime)
				maxSearchTime = diff;
	
		}
		
		sumSearchTime = sumSearchTime/1000;
		System.out.println("Minimum search time :" + minSearchTime);
		System.out.println("Maximum search time :" + maxSearchTime);
		System.out.println("Average search time :" + sumSearchTime);
		
		System.out.println("Collision count: "+hs2.collision_count);
		
		
		System.out.println(indexing);
		System.out.println(indexing2);

		
	}

}
