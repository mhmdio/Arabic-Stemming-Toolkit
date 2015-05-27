package app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Vector;


public class Algo3Stemmer{
    
    // for the get_root function
    private static Vector staticFiles ;
    static String  pathToStemmerFiles = new StringBuffer ( System.getProperty ( "user.dir" ) + System.getProperty ( "file.separator" ) + "algo3_Files" + System.getProperty ( "file.separator" ) ).toString ( );

    private static boolean rootFound = false;
    private static boolean stopwordFound = false;
    private static boolean fromSuffixes = false;
    private static String[][] stemmedDocument = new String[10000][3];
    private static int wordNumber            = 0;
    private static int stopwordNumber        = 0;
    private static int stemmedWordsNumber    = 0;
    private static Vector listStemmedWords = new Vector( );
    private static Vector listRootsFound = new Vector( );
    private static Vector listNotStemmedWords = new Vector( );
    private static Vector listStopwordsFound = new Vector( );
    private static Vector listOriginalStopword = new Vector( );
    private static boolean rootNotFound = false;
    private static Vector wordsNotStemmed = new Vector( );
    static int number = 0;
    private static String[][] possibleRoots;
    private static String roots="";
    

    /** Creates new form ASASinterface */
    public Algo3Stemmer() {
        initComponents();
    }

    private static void initComponents()
    {
        pathToStemmerFiles = new StringBuffer(System.getProperty("user.dir") + System.getProperty("file.separator") + "algo3_Files" + System.getProperty("file.separator")).toString();
        rootFound = false;
        stopwordFound = false;
        fromSuffixes = false;
        stemmedDocument = new String[10000][3];
        wordNumber = 0;
        stopwordNumber = 0;
        stemmedWordsNumber = 0;
        listStemmedWords = new Vector();
        listRootsFound = new Vector();
        listNotStemmedWords = new Vector();
        listStopwordsFound = new Vector();
        listOriginalStopword = new Vector();
        rootNotFound = false;
        wordsNotStemmed = new Vector();
        number = 0;
    }
        // check and remove any prefixes
    private static String checkForPrefixes ( String word )
    {
        //System.out.println("Enter checkForPrefix");
        staticFiles = new Vector ( );
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "definite_article.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "duplicate.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "first_waw.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "first_yah.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "last_alif.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "last_hamza.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "last_maksoura.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "last_yah.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "mid_waw.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "mid_yah.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "prefixes.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "punctuation.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "quad_roots.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "stopwords.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "suffixes.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "tri_patt.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "tri_roots.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "diacritics.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "strange.txt" ).toString ( ) ) )
            {
            // the vector was successfully created
            //System.out.println( "read in files successfully" );
        }
        //System.out.println("Start checkForPrefix");

            
        String prefix = "";
        String modifiedWord = word;
        Vector prefixes = ( Vector ) staticFiles.elementAt ( 10 );
         //System.out.println("for checkForPrefix");
        // for every prefix in the list
        for ( int i = 0; i < prefixes.size ( ); i++ )
        {
            prefix = ( String ) prefixes.elementAt ( i );
            // if the prefix was found
            if ( prefix.regionMatches ( 0, modifiedWord, 0, prefix.length ( ) ) )
            {
                modifiedWord = modifiedWord.substring ( prefix.length ( ) );

                // check to see if the word is a stopword
                if ( checkStopwords( modifiedWord ) )
                    return modifiedWord;

                // check to see if the word is a root of three or four letters
                // if the word has only two letters, test to see if one was removed
                if ( modifiedWord.length ( ) == 2 )
                    modifiedWord = isTwoLetters ( modifiedWord );
                else if ( modifiedWord.length ( ) == 3 && !rootFound )
                    modifiedWord = isThreeLetters ( modifiedWord );
                else if ( modifiedWord.length ( ) == 4 )
                    isFourLetters ( modifiedWord );

                // if the root hasn't been found, check for patterns
                if ( !rootFound && modifiedWord.length ( ) > 2 )
                    modifiedWord = checkPatterns ( modifiedWord );

                // if the root STILL hasn't been found
                if ( !rootFound && !stopwordFound && !fromSuffixes)
                {
                    // check for suffixes
                    modifiedWord = checkForSuffixes ( modifiedWord );
                }

                if ( stopwordFound )
                    return modifiedWord;

                // if the root was found, return the modified word
                if ( rootFound && !stopwordFound )
                {
                    return modifiedWord;
                }
            }
        }
        return word;
    }
    
    
    private static boolean checkStopwords ( String currentWord )
    {   
        staticFiles = new Vector ( );
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "definite_article.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "duplicate.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "first_waw.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "first_yah.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "last_alif.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "last_hamza.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "last_maksoura.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "last_yah.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "mid_waw.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "mid_yah.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "prefixes.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "punctuation.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "quad_roots.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "stopwords.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "suffixes.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "tri_patt.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "tri_roots.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "diacritics.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "strange.txt" ).toString ( ) ) )
            {
            // the vector was successfully created
            //System.out.println( "read in files successfully" );
        }
        
        Vector v = ( Vector ) staticFiles.elementAt ( 13 );

        if ( stopwordFound = v.contains ( currentWord ) )
        {
            stemmedDocument[wordNumber][1] = currentWord;
            stemmedDocument[wordNumber][2] = "STOPWORD";
            stopwordNumber ++;
            listStopwordsFound.addElement( currentWord );
            listOriginalStopword.addElement( stemmedDocument[wordNumber][0] );

        }
        return stopwordFound;
    }
    
    
    private static String isTwoLetters ( String word )
    {
        // if the word consists of two letters, then this could be either
        // - because it is a root consisting of two letters (though I can't think of any!)
        // - because a letter was deleted as it is duplicated or a weak middle or last letter.
        staticFiles = new Vector ( );
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "definite_article.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "duplicate.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "first_waw.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "first_yah.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "last_alif.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "last_hamza.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "last_maksoura.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "last_yah.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "mid_waw.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "mid_yah.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "prefixes.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "punctuation.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "quad_roots.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "stopwords.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "suffixes.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "tri_patt.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "tri_roots.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "diacritics.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "strange.txt" ).toString ( ) ) )
            {
            // the vector was successfully created
            //System.out.println( "read in files successfully" );
        }
        
        word = duplicate ( word );

        // check if the last letter was weak
        if ( !rootFound )
            word = lastWeak ( word );

        // check if the first letter was weak
        if ( !rootFound )
            word = firstWeak ( word );

        // check if the middle letter was weak
        if ( !rootFound )
            word = middleWeak ( word );

    return word;
    }
    
    
    // if the word consists of three letters
    private static String isThreeLetters ( String word )
    {   
        staticFiles = new Vector ( );
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "definite_article.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "duplicate.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "first_waw.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "first_yah.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "last_alif.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "last_hamza.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "last_maksoura.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "last_yah.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "mid_waw.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "mid_yah.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "prefixes.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "punctuation.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "quad_roots.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "stopwords.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "suffixes.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "tri_patt.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "tri_roots.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "diacritics.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "strange.txt" ).toString ( ) ) )
            {
            // the vector was successfully created
            //System.out.println( "read in files successfully" );
        }
        
        StringBuffer modifiedWord = new StringBuffer ( word );
        String root = "";
        // if the first letter is a 'ا', 'ؤ'  or 'ئ'
        // then change it to a 'أ'
        if ( word.length ( ) > 0 )
        {
            if ( word.charAt ( 0 ) == '\u0627' || word.charAt ( 0 ) == '\u0624' || word.charAt ( 0 ) == '\u0626' )
            {
                modifiedWord.setLength ( 0 );
                modifiedWord.append ( '\u0623' );
                modifiedWord.append ( word.substring ( 1 ) );
                root = modifiedWord.toString ( );
            }

            // if the last letter is a weak letter or a hamza
            // then remove it and check for last weak letters
            if ( word.charAt ( 2 ) == '\u0648' || word.charAt ( 2 ) == '\u064a' || word.charAt ( 2 ) == '\u0627' ||
                 word.charAt ( 2 ) == '\u0649' || word.charAt ( 2 ) == '\u0621' || word.charAt ( 2 ) == '\u0626' )
            {
                root = word.substring ( 0, 2 );
                root = lastWeak ( root );
                if ( rootFound )
                {
                    return root;
                }
            }

            // if the second letter is a weak letter or a hamza
            // then remove it
            if ( word.charAt ( 1 ) == '\u0648' || word.charAt ( 1 ) == '\u064a' || word.charAt ( 1 ) == '\u0627' || word.charAt ( 1 ) == '\u0626' )
            {
                root = word.substring ( 0, 1 );
                root = root + word.substring ( 2 );

                root = middleWeak ( root );
                if ( rootFound )
                {
                    return root;
                }
            }

            // if the second letter has a hamza, and it's not on a alif
            // then it must be returned to the alif
            if ( word.charAt ( 1 ) == '\u0624' || word.charAt ( 1 ) == '\u0626' )
            {
                if ( word.charAt ( 2 ) == '\u0645' || word.charAt ( 2 ) == '\u0632' || word.charAt ( 2 ) == '\u0631' )
                {
                    root = word.substring ( 0, 1 );
                    root = root + '\u0627';
                    root = root+ word.substring ( 2 );
                }
                else
                {
                    root = word.substring ( 0, 1 );
                    root = root + '\u0623';
                    root = root + word.substring ( 2 );
                }
            }

            // if the last letter is a shadda, remove it and
            // duplicate the last letter
            if ( word.charAt ( 2 ) == '\u0651')
            {
                root = word.substring ( 0, 1 );
                root = root + word.substring ( 1, 2 );
            }
        }

        // if word is a root, then rootFound is true
        if ( root.length ( ) == 0 )
        {
            if ( ( ( Vector ) staticFiles.elementAt ( 16 ) ) .contains ( word ) )
            {
                rootFound = true;
                stemmedDocument[wordNumber][1] = word;
                stemmedDocument[wordNumber][2] = "ROOT";
                stemmedWordsNumber ++;
                listStemmedWords.addElement ( stemmedDocument[wordNumber][0] );
                listRootsFound.addElement ( word );
                if ( rootNotFound )
                {
                    for ( int i = 0; i < number; i++ )
                        wordsNotStemmed.removeElement ( wordsNotStemmed.lastElement ( ) );
                    rootNotFound = false;
                }
                return word;
            }
        }
        // check for the root that we just derived
        else if ( ( ( Vector ) staticFiles.elementAt ( 16 ) ) .contains ( root ) )
        {
            rootFound = true;
            stemmedDocument[wordNumber][1] = root;
            stemmedDocument[wordNumber][2] = "ROOT";
            stemmedWordsNumber ++;
            listStemmedWords.addElement ( stemmedDocument[wordNumber][0] );
            listRootsFound.addElement ( word );
            if ( rootNotFound )
            {
                for ( int i = 0; i < number; i++ )
                    wordsNotStemmed.removeElement ( wordsNotStemmed.lastElement ( ) );
                rootNotFound = false;
            }
            return root;
        }

        if ( root.length ( ) == 3 )
        {
            possibleRoots[number][1] = root;
            possibleRoots[number][0] = stemmedDocument[wordNumber][0];
            number++;
        }
        else
        {
  //            possibleRoots[number][1] = word;
    //        possibleRoots[number][0] = stemmedDocument[wordNumber][0];
            number++;
        }
        return word;
    }
    
    
    // if the word has four letters
    private static void isFourLetters ( String word )
    {   
        staticFiles = new Vector ( );
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "definite_article.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "duplicate.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "first_waw.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "first_yah.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "last_alif.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "last_hamza.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "last_maksoura.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "last_yah.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "mid_waw.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "mid_yah.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "prefixes.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "punctuation.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "quad_roots.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "stopwords.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "suffixes.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "tri_patt.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "tri_roots.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "diacritics.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "strange.txt" ).toString ( ) ) )
            {
            // the vector was successfully created
            //System.out.println( "read in files successfully" );
        }
        
        // if word is a root, then rootFound is true
        if( ( ( Vector ) staticFiles.elementAt ( 12 ) ) .contains ( word ) )
        {
            rootFound = true;

            stemmedDocument[wordNumber][1] = word;
            stemmedDocument[wordNumber][2] = "ROOT";
            stemmedWordsNumber ++;
            listStemmedWords.addElement ( stemmedDocument[wordNumber][0] );
            listRootsFound.addElement ( word );
        }
    }
    
    // check if the word matches any of the patterns
    private static String checkPatterns ( String word )
    {   
        staticFiles = new Vector ( );
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "definite_article.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "duplicate.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "first_waw.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "first_yah.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "last_alif.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "last_hamza.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "last_maksoura.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "last_yah.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "mid_waw.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "mid_yah.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "prefixes.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "punctuation.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "quad_roots.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "stopwords.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "suffixes.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "tri_patt.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "tri_roots.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "diacritics.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "strange.txt" ).toString ( ) ) )
            {
            // the vector was successfully created
            //System.out.println( "read in files successfully" );
        }
        
        staticFiles = new Vector ( );
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "definite_article.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "duplicate.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "first_waw.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "first_yah.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "last_alif.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "last_hamza.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "last_maksoura.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "last_yah.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "mid_waw.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "mid_yah.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "prefixes.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "punctuation.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "quad_roots.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "stopwords.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "suffixes.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "tri_patt.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "tri_roots.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "diacritics.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "strange.txt" ).toString ( ) ) )
            {
            // the vector was successfully created
            //System.out.println( "read in files successfully" );
        }
        StringBuffer root = new StringBuffer ( "" );
        // if the first letter is a hamza, change it to an alif
        if ( word.length ( ) > 0 )
            if ( word.charAt ( 0 ) == '\u0623' || word.charAt ( 0 ) == '\u0625' || word.charAt ( 0 ) == '\u0622' )
            {
                root.append ( "j" );
                root.setCharAt ( 0, '\u0627' );
                root.append ( word.substring ( 1 ) );
                word = root.toString ( );
            }

        // try and find a pattern that matches the word
        Vector patterns = ( Vector ) staticFiles.elementAt ( 15 );
        int numberSameLetters = 0;
        String pattern = "";
        String modifiedWord = "";

        // for every pattern
        for( int i = 0; i < patterns.size ( ); i++ )
        {
            pattern = ( String ) patterns.elementAt ( i );
            root.setLength ( 0 );
            // if the length of the words are the same
            if ( pattern.length ( ) == word.length ( ) )
            {
                numberSameLetters = 0;
                // find out how many letters are the same at the same index
                // so long as they're not a fa, ain, or lam
                for ( int j = 0; j < word.length ( ); j++ )
                    if ( pattern.charAt ( j ) == word.charAt ( j ) &&
                         pattern.charAt ( j ) != '\u0641'          &&
                         pattern.charAt ( j ) != '\u0639'          &&
                         pattern.charAt ( j ) != '\u0644'            )
                        numberSameLetters ++;

                // test to see if the word matches the pattern افعلا
                if ( word.length ( ) == 6 && word.charAt ( 3 ) == word.charAt ( 5 ) && numberSameLetters == 2 )
                {
                    root.append ( word.charAt ( 1 ) );
                    root.append ( word.charAt ( 2 ) );
                    root.append ( word.charAt ( 3 ) );
                    modifiedWord = root.toString ( );
                    modifiedWord = isThreeLetters ( modifiedWord );
                    if ( rootFound )
                        return modifiedWord;
                    else
                        root.setLength ( 0 );
                }


                // if the word matches the pattern, get the root
                if ( word.length ( ) - 3 <= numberSameLetters )
                {
                    // derive the root from the word by matching it with the pattern
                    for ( int j = 0; j < word.length ( ); j++ )
                        if ( pattern.charAt ( j ) == '\u0641' ||
                             pattern.charAt ( j ) == '\u0639' ||
                             pattern.charAt ( j ) == '\u0644'   )
                            root.append ( word.charAt ( j ) );

                    modifiedWord = root.toString ( );
                    modifiedWord = isThreeLetters ( modifiedWord );

                    if ( rootFound )
                    {
                        word = modifiedWord;
                        return word;
                    }
                }
            }
        }
        return word;
    }
    
    
    // METHOD CHECKFORSUFFIXES
    private static String checkForSuffixes ( String word )
    {   
        staticFiles = new Vector ( );
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "definite_article.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "duplicate.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "first_waw.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "first_yah.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "last_alif.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "last_hamza.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "last_maksoura.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "last_yah.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "mid_waw.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "mid_yah.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "prefixes.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "punctuation.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "quad_roots.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "stopwords.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "suffixes.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "tri_patt.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "tri_roots.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "diacritics.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "strange.txt" ).toString ( ) ) )
            {
            // the vector was successfully created
            //System.out.println( "read in files successfully" );
        }
        
        String suffix = "";
        String modifiedWord = word;
        Vector suffixes = ( Vector ) staticFiles.elementAt ( 14 );
        fromSuffixes = true;

        // for every suffix in the list
        for ( int i = 0; i < suffixes.size ( ); i++ )
        {
            suffix = ( String ) suffixes.elementAt ( i );

            // if the suffix was found
            if( suffix.regionMatches ( 0, modifiedWord, modifiedWord.length ( ) - suffix.length ( ), suffix.length ( ) ) )
            {
                modifiedWord = modifiedWord.substring ( 0, modifiedWord.length ( ) - suffix.length ( ) );

                // check to see if the word is a stopword
                if ( checkStopwords ( modifiedWord ) )
                {
                    fromSuffixes = false;
                    return modifiedWord;
                }

                // check to see if the word is a root of three or four letters
                // if the word has only two letters, test to see if one was removed
                if ( modifiedWord.length ( ) == 2 )
                {
                    modifiedWord = isTwoLetters ( modifiedWord );
                }
                else if ( modifiedWord.length ( ) == 3 )
                {
                    modifiedWord = isThreeLetters ( modifiedWord );
                }
                else if ( modifiedWord.length ( ) == 4 )
                {
                    isFourLetters ( modifiedWord );
                }

                // if the root hasn't been found, check for patterns
                if ( !rootFound && modifiedWord.length( ) > 2 )
                {
                    modifiedWord = checkPatterns( modifiedWord );
                }

                if ( stopwordFound )
                {
                    fromSuffixes = false;
                    return modifiedWord;
                }

                // if the root was found, return the modified word
                if ( rootFound )
                {
                    fromSuffixes = false;
                    return modifiedWord;
                }
            }
        }
        fromSuffixes = false;
        return word;
    }
    
    
    // handle duplicate letters in the word
    private static String duplicate ( String word )
    {   
        staticFiles = new Vector ( );
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "definite_article.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "duplicate.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "first_waw.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "first_yah.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "last_alif.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "last_hamza.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "last_maksoura.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "last_yah.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "mid_waw.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "mid_yah.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "prefixes.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "punctuation.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "quad_roots.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "stopwords.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "suffixes.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "tri_patt.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "tri_roots.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "diacritics.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "strange.txt" ).toString ( ) ) )
            {
            // the vector was successfully created
           //System.out.println( "read in files successfully" );
        }
        
        // check if a letter was duplicated
        if ( ( ( Vector ) staticFiles.elementAt ( 1 ) ).contains ( word ) )
        {
            // if so, then return the deleted duplicate letter
            word = word + word.substring ( 1 );

            // root was found, so set variable
            rootFound = true;

            stemmedDocument[wordNumber][1] = word;
            stemmedDocument[wordNumber][2] = "ROOT";
            stemmedWordsNumber ++;
            listStemmedWords.addElement ( stemmedDocument[wordNumber][0] );
            listRootsFound.addElement ( word );

            return word;
        }
        return word;
    }
    
    // check if the last letter of the word is a weak letter
    private static String lastWeak ( String word )
    {   
        staticFiles = new Vector ( );
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "definite_article.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "duplicate.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "first_waw.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "first_yah.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "last_alif.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "last_hamza.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "last_maksoura.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "last_yah.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "mid_waw.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "mid_yah.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "prefixes.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "punctuation.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "quad_roots.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "stopwords.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "suffixes.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "tri_patt.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "tri_roots.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "diacritics.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "strange.txt" ).toString ( ) ) )
            {
            // the vector was successfully created
            //System.out.println( "read in files successfully" );
        }
        
        StringBuffer stemmedWord = new StringBuffer ( "" );
        // check if the last letter was an alif
        if ( ( ( Vector )staticFiles.elementAt ( 4 ) ).contains ( word ) )
        {
            stemmedWord.append ( word );
            stemmedWord.append ( "\u0627" );
            word = stemmedWord.toString ( );
            stemmedWord.setLength ( 0 );

            // root was found, so set variable
            rootFound = true;

            stemmedDocument[wordNumber][1] = word;
            stemmedDocument[wordNumber][2] = "ROOT";
            stemmedWordsNumber ++;
            listStemmedWords.addElement ( stemmedDocument[wordNumber][0] );
            listRootsFound.addElement ( word );

            return word;
        }
        // check if the last letter was an hamza
        else if ( ( ( Vector ) staticFiles.elementAt ( 5 ) ) .contains ( word ) )
        {
            stemmedWord.append ( word );
            stemmedWord.append ( "\u0623" );
            word = stemmedWord.toString ( );
            stemmedWord.setLength ( 0 );

            // root was found, so set variable
            rootFound = true;

            stemmedDocument[wordNumber][1] = word;
            stemmedDocument[wordNumber][2] = "ROOT";
            stemmedWordsNumber ++;
            listStemmedWords.addElement ( stemmedDocument[wordNumber][0] );
            listRootsFound.addElement ( word );

            return word;
        }
        // check if the last letter was an maksoura
        else if ( ( ( Vector ) staticFiles.elementAt ( 6 ) ) .contains ( word ) )
        {
            stemmedWord.append ( word );
            stemmedWord.append ( "\u0649" );
            word = stemmedWord.toString ( );
            stemmedWord.setLength ( 0 );

            // root was found, so set variable
            rootFound = true;

            stemmedDocument[wordNumber][1] = word;
            stemmedDocument[wordNumber][2] = "ROOT";
            stemmedWordsNumber ++;
            listStemmedWords.addElement ( stemmedDocument[wordNumber][0] );
            listRootsFound.addElement ( word );

            return word;
        }
        // check if the last letter was an yah
        else if ( ( ( Vector ) staticFiles.elementAt ( 7 ) ).contains ( word ) )
        {
            stemmedWord.append ( word );
            stemmedWord.append ( "\u064a" );
            word = stemmedWord.toString ( );
            stemmedWord.setLength ( 0 );

            // root was found, so set variable
            rootFound = true;

            stemmedDocument[wordNumber][1] = word;
            stemmedDocument[wordNumber][2] = "ROOT";
            stemmedWordsNumber ++;
            listStemmedWords.addElement ( stemmedDocument[wordNumber][0] );
            listRootsFound.addElement ( word );

            return word;
        }
        return word;
    }

    //--------------------------------------------------------------------------

    // check if the first letter is a weak letter
    private static String firstWeak ( String word )
    {   
        staticFiles = new Vector ( );
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "definite_article.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "duplicate.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "first_waw.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "first_yah.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "last_alif.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "last_hamza.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "last_maksoura.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "last_yah.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "mid_waw.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "mid_yah.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "prefixes.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "punctuation.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "quad_roots.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "stopwords.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "suffixes.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "tri_patt.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "tri_roots.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "diacritics.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "strange.txt" ).toString ( ) ) )
            {
            // the vector was successfully created
            //System.out.println( "read in files successfully" );
        }
        
        StringBuffer stemmedWord = new StringBuffer ( "" );
        // check if the firs letter was a waw
        if( ( ( Vector ) staticFiles.elementAt ( 2 ) ) .contains ( word ) )
        {
            stemmedWord.append ( "\u0648" );
            stemmedWord.append ( word );
            word = stemmedWord.toString ( );
            stemmedWord.setLength ( 0 );

            // root was found, so set variable
            rootFound = true;

            stemmedDocument[wordNumber][1] = word;
            stemmedDocument[wordNumber][2] = "ROOT";
            stemmedWordsNumber ++;
            listStemmedWords.addElement ( stemmedDocument[wordNumber][0] );
            listRootsFound.addElement ( word );

            return word;
        }
        // check if the first letter was a yah
        else if ( ( ( Vector ) staticFiles.elementAt ( 3 ) ) .contains ( word ) )
        {
            stemmedWord.append ( "\u064a" );
            stemmedWord.append ( word );
            word = stemmedWord.toString ( );
            stemmedWord.setLength ( 0 );

            // root was found, so set variable
            rootFound = true;

            stemmedDocument[wordNumber][1] = word;
            stemmedDocument[wordNumber][2] = "ROOT";
            stemmedWordsNumber ++;
            listStemmedWords.addElement ( stemmedDocument[wordNumber][0] );
            listRootsFound.addElement ( word );

            return word;
        }
    return word;
    }

    //--------------------------------------------------------------------------

    // check if the middle letter of the root is weak
    private static String middleWeak ( String word )
    {   
        staticFiles = new Vector ( );
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "definite_article.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "duplicate.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "first_waw.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "first_yah.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "last_alif.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "last_hamza.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "last_maksoura.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "last_yah.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "mid_waw.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "mid_yah.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "prefixes.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "punctuation.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "quad_roots.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "stopwords.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "suffixes.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "tri_patt.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "tri_roots.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "diacritics.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "strange.txt" ).toString ( ) ) )
            {
            // the vector was successfully created
            //System.out.println( "read in files successfully" );
        }
        
        StringBuffer stemmedWord = new StringBuffer ( "j" );
        // check if the middle letter is a waw
        if ( ( ( Vector ) staticFiles.elementAt ( 8 ) ) .contains ( word ) )
        {
            // return the waw to the word
            stemmedWord.setCharAt ( 0, word.charAt ( 0 ) );
            stemmedWord.append ( "\u0648" );
            stemmedWord.append ( word.substring ( 1 ) );
            word = stemmedWord.toString ( );
            stemmedWord.setLength ( 0 );

            // root was found, so set variable
            rootFound = true;

            stemmedDocument[wordNumber][1] = word;
            stemmedDocument[wordNumber][2] = "ROOT";
            stemmedWordsNumber ++;
            listStemmedWords.addElement ( stemmedDocument[wordNumber][0] );
            listRootsFound.addElement ( word );

            return word;
        }
        // check if the middle letter is a yah
        else if ( ( ( Vector ) staticFiles.elementAt ( 9 ) ) .contains ( word ) )
        {
            // return the waw to the word
            stemmedWord.setCharAt ( 0, word.charAt ( 0 ) );
            stemmedWord.append ( "\u064a" );
            stemmedWord.append ( word.substring ( 1 ) );
            word = stemmedWord.toString ( );
            stemmedWord.setLength ( 0 );

            // root was found, so set variable
            rootFound = true;

            stemmedDocument[wordNumber][1] = word;
            stemmedDocument[wordNumber][2] = "ROOT";
            stemmedWordsNumber ++;
            listStemmedWords.addElement ( stemmedDocument[wordNumber][0] );
            listRootsFound.addElement ( word );

            return word;
        }
        return word;
    }
    
    public  String[][] returnPossibleRoots ( )
    {
        return possibleRoots;
    }
    
    // read in the contents of a file, put it into a vector, and add that vector
    // to the vector composed of vectors containing the static files
    protected static boolean addVectorFromFile ( String fileName )
    {   
        
        boolean returnValue;
        try
        {
            // the vector we are going to fill
            Vector vectorFromFile = new Vector ( );

            // create a buffered reader
            File file = new File ( fileName );
            FileInputStream fileInputStream = new FileInputStream ( file );
            InputStreamReader inputStreamReader = new InputStreamReader ( fileInputStream, "UTF-16" );

            //If the bufferedReader is not big enough for a file, I should change the size of it here
            BufferedReader bufferedReader = new BufferedReader ( inputStreamReader, 20000 );

            // read in the text a line at a time
            String part;
            StringBuffer word = new StringBuffer ( );
            while ( ( part = bufferedReader.readLine ( ) ) != null )
            {
                // add spaces at the end of the line
                part = part + "  ";

                // for each line
                for ( int index = 0; index < part.length ( ) - 1; index ++ )
                {
                    // if the character is not a space, append it to a word
                    if ( ! ( Character.isWhitespace ( part.charAt ( index ) ) ) )
                    {
                        word.append ( part.charAt ( index ) );
                    }
                    // otherwise, if the word contains some characters, add it
                    // to the vector
                    else
                    {
                        if ( word.length ( ) != 0 )
                        {
                            vectorFromFile.addElement ( word.toString ( ) );
                            word.setLength ( 0 );
                        }
                    }
                }
            }

            // trim the vector
            vectorFromFile.trimToSize ( );

            // destroy the buffered reader
            bufferedReader.close ( );
   	        fileInputStream.close ( );

            // add the vector to the vector composed of vectors containing the
            // static files
            staticFiles.addElement ( vectorFromFile );
            returnValue = true;
        }
        catch ( Exception exception )
        {
            //JOptionPane.showMessageDialog ( arabicStemmerGUI, "Could not open '" + fileName + "'.", " Error ", JOptionPane.ERROR_MESSAGE );
            returnValue = false;
        }
        return returnValue;
    }
// stem the word
    public String stemWord ( String word )
    {   
        initComponents();
        staticFiles = new Vector ( );
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "definite_article.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "duplicate.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "first_waw.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "first_yah.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "last_alif.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "last_hamza.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "last_maksoura.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "last_yah.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "mid_waw.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "mid_yah.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "prefixes.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "punctuation.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "quad_roots.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "stopwords.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "suffixes.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "tri_patt.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "tri_roots.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "diacritics.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "strange.txt" ).toString ( ) ) )
            {
            // the vector was successfully created
            //System.out.println( "read in files successfully" );
        }
        
        // check if the word consists of two letters
        // and find it's root
        if ( word.length ( ) == 2 )
            word = isTwoLetters ( word );

        // if the word consists of three letters
        if( word.length ( ) == 3 && !rootFound )
            // check if it's a root
            word = isThreeLetters ( word );

        // if the word consists of four letters
        if( word.length ( ) == 4 )
            // check if it's a root
            isFourLetters ( word );

        // if the root hasn't yet been found
        if( !rootFound )
        {
            // check if the word is a pattern
            word = checkPatterns ( word );
        }

        // if the root still hasn't been found
        if ( !rootFound )
        {
            // check for a definite article, and remove it
            word = checkDefiniteArticle ( word );
        }

        // if the root still hasn't been found
        if ( !rootFound && !stopwordFound )
        {
            // check for the prefix waw
            word = checkPrefixWaw ( word );
        }

        // if the root STILL hasnt' been found
        if ( !rootFound && !stopwordFound )
        {
            // check for suffixes
            word = checkForSuffixes ( word );
        }

        // if the root STILL hasn't been found
        if ( !rootFound && !stopwordFound )
        {
            // check for prefixes
            word = checkForPrefixes ( word );
        }
        return word;
    }
    
    // check and remove the definite article
    private static String checkDefiniteArticle ( String word )
    {   
        staticFiles = new Vector ( );
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "definite_article.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "duplicate.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "first_waw.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "first_yah.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "last_alif.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "last_hamza.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "last_maksoura.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "last_yah.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "mid_waw.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "mid_yah.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "prefixes.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "punctuation.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "quad_roots.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "stopwords.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "suffixes.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "tri_patt.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "tri_roots.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "diacritics.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "strange.txt" ).toString ( ) ) )
            {
            // the vector was successfully created
            //System.out.println( "read in files successfully" );
        }
        
        // looking through the vector of definite articles
        // search through each definite article, and try and
        // find a match
        String definiteArticle = "";
        String modifiedWord = "";
        Vector definiteArticles = ( Vector ) staticFiles.elementAt ( 0 );

        // for every definite article in the list
        for ( int i = 0; i < definiteArticles.size ( ); i++ )
        {
            definiteArticle = ( String ) definiteArticles.elementAt ( i );
            // if the definite article was found
            if ( definiteArticle.regionMatches ( 0, word, 0, definiteArticle.length ( ) ) )
            {
                // remove the definite article
                modifiedWord = word.substring ( definiteArticle.length ( ), word.length ( ) );

                // check to see if the word is a stopword
                if ( checkStopwords ( modifiedWord ) )
                    return modifiedWord;

                // check to see if the word is a root of three or four letters
                // if the word has only two letters, test to see if one was removed
                if ( modifiedWord.length ( ) == 2 )
                    modifiedWord = isTwoLetters ( modifiedWord );
                else if ( modifiedWord.length ( ) == 3 && !rootFound )
                    modifiedWord = isThreeLetters ( modifiedWord );
                else if ( modifiedWord.length ( ) == 4 )
                    isFourLetters ( modifiedWord );

                // if the root hasn't been found, check for patterns
                if ( !rootFound && modifiedWord.length ( ) > 2 )
                    modifiedWord = checkPatterns ( modifiedWord );

                // if the root STILL hasnt' been found
                if ( !rootFound && !stopwordFound )
                {
                    // check for suffixes
                    modifiedWord = checkForSuffixes ( modifiedWord );
                }

                // if the root STILL hasn't been found
                if ( !rootFound && !stopwordFound )
                {
                    // check for prefixes
                    modifiedWord = checkForPrefixes ( modifiedWord );
                }


                if ( stopwordFound )
                    return modifiedWord;


                // if the root was found, return the modified word
                if ( rootFound && !stopwordFound )
                {
                    return modifiedWord;
                }
            }
        }
        if ( modifiedWord.length ( ) > 3 )
            return modifiedWord;
        return word;
    }
    
    // check and remove the special prefix (waw)
    private static String checkPrefixWaw ( String word )
    {   
        staticFiles = new Vector ( );
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "definite_article.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "duplicate.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "first_waw.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "first_yah.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "last_alif.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "last_hamza.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "last_maksoura.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "last_yah.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "mid_waw.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "mid_yah.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "prefixes.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "punctuation.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "quad_roots.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "stopwords.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "suffixes.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "tri_patt.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "tri_roots.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "diacritics.txt" ).toString ( ) ) )
        if ( addVectorFromFile ( new StringBuffer ( pathToStemmerFiles + "strange.txt" ).toString ( ) ) )
            {
            // the vector was successfully created
            //System.out.println( "read in files successfully" );
        }
        
        String modifiedWord = "";

        if ( word.length ( ) > 3 && word.charAt ( 0 ) == '\u0648' )
        {
            modifiedWord = word.substring ( 1 );

            // check to see if the word is a stopword
            if ( checkStopwords ( modifiedWord ) )
                return modifiedWord;

            // check to see if the word is a root of three or four letters
            // if the word has only two letters, test to see if one was removed
            if ( modifiedWord.length ( ) == 2 )
                modifiedWord = isTwoLetters( modifiedWord );
            else if ( modifiedWord.length ( ) == 3 && !rootFound )
                modifiedWord = isThreeLetters( modifiedWord );
            else if ( modifiedWord.length ( ) == 4 )
                isFourLetters ( modifiedWord );

            // if the root hasn't been found, check for patterns
            if ( !rootFound && modifiedWord.length ( ) > 2 )
                modifiedWord = checkPatterns ( modifiedWord );

            // if the root STILL hasnt' been found
            if ( !rootFound && !stopwordFound )
            {
                // check for suffixes
                modifiedWord = checkForSuffixes ( modifiedWord );
            }

            // iIf the root STILL hasn't been found
            if ( !rootFound && !stopwordFound )
            {
                // check for prefixes
                modifiedWord = checkForPrefixes ( modifiedWord );
            }

            if ( stopwordFound )
                return modifiedWord;

            if ( rootFound && !stopwordFound )
            {
                return modifiedWord;
            }
        }
        return word;
    }
    
    /*
     * This function is not done yet.
     * private static String remove_tashkel(String Arabic_word)
    {   
        Arabic_word = Arabic_word.replace("َ","");
        Arabic_word = Arabic_word.replace("ً","");
        Arabic_word = Arabic_word.replace("ُ","");
        Arabic_word = Arabic_word.replace("ٌ","");
        Arabic_word = Arabic_word.replace("ِ","");
        Arabic_word = Arabic_word.replace("ٍ","");
        
        return Arabic_word;
    }*/

} // main class end