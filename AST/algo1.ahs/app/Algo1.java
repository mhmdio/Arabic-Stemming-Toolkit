package app;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
//import java.util.Iterator;
import java.util.Scanner;

import ui.GUI;
import arabic.ArabicNormalizer;
import arabic.ArabicStemmer;
//import org.omg.CORBA.FREE_MEM;
import javax.swing.text.BadLocationException;

/**
 * @author mhmd yahya almusaddar
 * @author mhmd@alaqsa.edu.ps
 * @version 0.8 - 8th version 20/1/2014
 * 
 */

public class Algo1 {

	/**
	 * @param args
	 */

	// Global Variables
	public static int datasetSize;
	public static int stopsetSize;
	public static int stopWordCounter;

	public static void main(String[] args) {

		System.out.println("-----------------Start-------------------");
		System.out
				.println(">> Convert Dataset files and Stop-word lists to UTF8 Encoding");
		System.out.println("----------------------------------------------");

		System.out.println("--------------readStopWordFile---------------");
		ArrayList<String> stopWordList = readStopWordFile(GUI.STOP_PATH);
		System.out.println("--------------StopWordFile = " + stopsetSize);
		System.out.println("----------------------------------------------");

		System.out.println("--------------readDataSetFile---------------");
		final ArrayList<String> dataSetList = readDataSetFile(GUI.DATASET_PATH);
		System.out.println("--------------DataSetFile = " + datasetSize);
		System.out.println("----------------------------------------------");

		ArrayList<String> freeDataSetList = removeStopWords(stopWordList,
				dataSetList);
		System.out.println("----------------------------------------------");

		String normalizedWord = normalizeArabicDataSet(freeDataSetList);
		System.out.println("----------------------------------------------");

		// StringBuilder output = stemmingArabicTokens(normalizedWord);
		// System.out.println("-----------------Stop-------------------");

		// finish algorithm and write the stemmed word to output text UTF8 file.
		try {
			StringBuilder output = stemmingArabicTokens(normalizedWord);
			System.out.println("-----------------Stop-------------------");
			writeOutput(output);
		} catch (Exception e) {
			e.printStackTrace();
		} 

		// // better output
		// String[] lines = normalizedWord.split("\n");
		// for (int i = 0; i < lines.length; i++) {
		// String[] tokens = lines[i].split("\\s");
		// for (int j = 0; j < tokens.length; j++) {
		// String t = tokens[j];
		// String stemmed = stemArabicWord(t);
		// System.out.printf("%s %15s %15s\n",freeDataSetList.get(j),t,stemmed
		// );
		// }
		//
		// }

	}

	/**
	 * @param output
	 * @throws UnsupportedEncodingException
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void writeOutput(StringBuilder output)
			throws UnsupportedEncodingException, FileNotFoundException,
			IOException {
		Writer out = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(
						"output/output_algorithm1.txt"), GUI.ENCODING));
		try {
			String stemmedString = String.valueOf(output);
			out.write(stemmedString.toString());
		} finally {
			out.close();
		}
	}

	// exclude words from stemming process
	private static boolean excludeWords(String word) {

		File file = new File(GUI.ARABIZED_PATH);
		Scanner scanner;
		try {
			scanner = new Scanner(file);

			// now read the file line by line...
			int lineNum = 0;
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				lineNum++;
				if (line.equals(word)) {
					// System.out.println("Arabized word on line " +lineNum);
					try {
						GUI.doc.insertString(GUI.doc.getLength(),
								"\nArabized word on line " + lineNum, null);
					} catch (BadLocationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return true;
				}

			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;

	}

	/**
	 * @param normalizedWord
	 * @throws FileNotFoundException
	 */
	public static StringBuilder stemmingArabicTokens(String normalizedWord)
			throws FileNotFoundException {
		String[] lines = normalizedWord.split("\n");
		StringBuilder sbuf = new StringBuilder();
		// System.out.println("Normalized\tStemmed ");
		for (int i = 0; i < lines.length; i++) {
			String[] tokens = lines[i].split("\\s");
			for (int j = 0; j < tokens.length; j++) {
				String t = tokens[j];
				String stemmed;
				if (excludeWords(t)) {
					stemmed = t;
				} else {
					stemmed = stemArabicWord(t);
				}

				// System.out.printf("%s %15s\n",stemmed, t);
				sbuf.append(stemmed).append(" ");
			}
			sbuf.append("\n");
		}
		return sbuf;

	}

	/**
	 * @param normalizedWord
	 */
	private static String stemArabicWord(String string) {
		char[] normalizedWord = string.toCharArray();
		ArabicStemmer as = new ArabicStemmer();
		int len = normalizedWord.length;
		len = as.stem(normalizedWord, len);
		char[] lightWord = new char[len];
		System.arraycopy(normalizedWord, 0, lightWord, 0, len);

		StringBuilder sbuf = new StringBuilder();
		sbuf.append(lightWord);

		String result = sbuf.toString();
		return result;
	}

	/**
	 * @param freeDataSetList
	 */
	public static String normalizeArabicDataSet(
			ArrayList<String> freeDataSetList) {

		String dataset = freeDataSetList.toString().replaceAll(",|\\[|\\]", "");

		// Advance removing of punctuation marks
		dataset = dataset.replaceAll("\\p{Punct}", "");
		dataset = dataset.replaceAll("،", "");

		// Remove Numbers and English words
		dataset = dataset.replaceAll("[0-9A-Za-z$-]", "");

		ArabicNormalizer an = new ArabicNormalizer();
		char[] c = dataset.toCharArray();
		int len = c.length;
		len = an.normalize(c, len);
		char[] normalizedWord = new char[len];
		System.arraycopy(c, 0, normalizedWord, 0, len);

		// System.out.println("Normalized Text : ");
		String characterObjectToString = String.valueOf(normalizedWord);

		// Remove empty lines before normalizing, Here is better
		characterObjectToString = characterObjectToString.replaceAll("\\s+",
				" ");

		// System.out.println(characterObjectToString);
		return characterObjectToString;
	}

	/**
	 * @param stopWordList
	 * @param dataSetList
	 */
	public static ArrayList<String> removeStopWords(
			ArrayList<String> stopWordList, ArrayList<String> dataSetList) {
		stopWordCounter = 0;
		// System.out.println("Matched stop-words : ");
		for (String stop : stopWordList) {
			if (dataSetList.contains(stop)) {
				stopWordCounter++;
				// System.out.print(stop + ", ");
				dataSetList.remove(stop);
			}
		}

		// System.out.println("");
		// System.out.println("----------------------------------------------");
		// System.out.println("Removing " + '"'+ stopWordCounter +'"' +
		// " stop-words");
		// System.out.println("New Dataset size : " + datasetSize +" words");

		return dataSetList;
	}

	/**
	 * @param String
	 *            File Path
	 * @return ArrayList<String>
	 */
	public static ArrayList<String> readStopWordFile(String filePath) {
		ArrayList<String> result = new ArrayList<>();
		String str;
		// String comment = "#";
		try {
			File fileDir = new File(filePath);

			BufferedReader in = new BufferedReader(new InputStreamReader(
					new FileInputStream(fileDir), GUI.ENCODING));

			while ((str = in.readLine()) != null) {
				result.add(str.trim());
			}

			stopsetSize = result.size();
			// System.out.println("Stop-words size : " + stopsetSize);
			in.close();
		} catch (UnsupportedEncodingException e) {
			System.out.println(e.getMessage());
		} catch (IOException e) {
			System.out.println(e.getMessage());
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return result;
	}

	/**
	 * @param String
	 *            File Path
	 * @return ArrayList<String>
	 */
	public static ArrayList<String> readDataSetFile(String filePath) {

		Scanner s;
		ArrayList<String> list = new ArrayList<>();
		try {
			s = new Scanner(new File(filePath), GUI.ENCODING);
			while (s.hasNext()) {
				list.add(s.next());
			}
			s.close();
			datasetSize = list.size();
			// System.out.println("Dataset list : ");
			// System.out.println(list.toString().replaceAll(",|\\[|\\]",""));
			// System.out.println("Dataset size : " + datasetSize + " words");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return list;
	}
}
