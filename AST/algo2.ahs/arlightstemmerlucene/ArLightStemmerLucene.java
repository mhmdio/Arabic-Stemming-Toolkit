/*
 	
 	Arabic Light Stemmer: This program stems affixes for the Arabic words.
 	
 	
 	This program is free software; you can redistribute it and/or
 	modify it under the terms of the GNU General Public License
 	as published by the Free Software Foundation; either version 2
 	of the License, or (at your option) any later version.
 	
 	This program is distributed in the hope that it will be useful,
 	but WITHOUT ANY WARRANTY; without even the implied warranty of
 	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 	GNU General Public License for more details.
 	
 	You should have received a copy of the GNU General Public License
 	along with this program; if not, write to the Free Software
 	Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 	
 	
 	Implemented by Motaz K. Saad (motaz.saad@gmail.com)
 	
 */

package arlightstemmerlucene;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import lucene.ArabicNormalizer;
import lucene.ArabicStemmer;
import myfileutil.myFileHandler;

/**
 *
 * @author Motaz
 */
public class ArLightStemmerLucene {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException, IOException {
        /*
        String words [] = { "ذاهبون" , "المسافرون" , "الجمعية", "زهرة" , "المرحلة" , "الجامعية",         "الاولى", "وطنية", "الوطنية"};
        for (int i = 0; i < words.length; i++) {
            String resut = lightStem(words[i]);
            System.out.println(resut);            
        }*/
        myfileutil.myFileHandler fr = new myFileHandler();
        //String fin = "/home/msaad/apps/stem/sample-ar";
        //String fout = "/home/msaad/apps/stem/sample-ar.out";
        
        String fin = args[0];
        String fout = args[1];
        
        //String s = fr.myReadFileUtf8(f);
        String s = fr.readEntilerFile(fin);
        //System.out.print(s);
        
        String[] lines = s.split("\n");
        StringBuilder sbuf = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            String[] tokens = lines[i].split("\\s");
            for (int j = 0; j < tokens.length; j++) {
                String t = tokens[j];
                String resut = lightStem(t);
                //System.out.print(resut);    
                sbuf.append(resut).append(" ");

            }
            sbuf.append("\n");
        }
        
        fr.writeFileUTF(sbuf.toString(),fout);
                
    }
    
    public static String lightStem(String string) {
        ArabicNormalizer arabicNorm = new ArabicNormalizer();
        char[] c = string.toCharArray();
        int len = c.length;
        len = arabicNorm.normalize(c, len);
        char[] normalizedWord = new char[len];
        System.arraycopy(c, 0, normalizedWord, 0, len);



        ArabicStemmer araLightStemmer = new ArabicStemmer();
        len = araLightStemmer.stem(normalizedWord, len);
        char[] lightWord = new char[len];
        System.arraycopy(normalizedWord, 0, lightWord, 0, len);
       

        StringBuilder sbuf = new StringBuilder();
        sbuf.append(lightWord);


        String result = sbuf.toString();
        return result;
    }
    
}
