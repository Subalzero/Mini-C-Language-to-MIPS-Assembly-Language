package lexer;
import java.io.*;
import java.util.*;
import symbols.*;
import reader.*;
public class Lexer {
    
   public static int line = 1;           //counts newlines. Useful for error handling.
   
   private char peek = ' ';              //container for next character.
   
   private HashMap words = new HashMap();
   
   private ReadFile r = new ReadFile();           //reads the "test.txt" file.
   
   /**
    * Reserves Reserved words into the HashMap.
    * @param w 
    */
   private void reserve(Word w) { words.put(w.lexeme, w); }

   /**
    * Constructor.
    */
   public Lexer() {
      
      reserve( new Word("if",       Tag.IF)       );
      reserve( new Word("else",     Tag.ELSE)     );
      reserve( new Word("while",    Tag.WHILE)    );
      reserve( new Word("do",       Tag.DO)       );
      reserve( new Word("break",    Tag.BREAK)    );
      reserve( new Word("continue", Tag.CONTINUE) );

      reserve( Word.True );  reserve( Word.False );

      reserve( Type.Int  );  reserve( Type.Char   );
      reserve( Type.Bool );  reserve( Type.Float  );
      reserve( Type.Byte );  reserve( Type.Short  );
      reserve( Type.Long );  reserve( Type.Double );
   }

   /**
    * Reads the next character.
    * @throws IOException 
    */
   private void readch() throws IOException { peek = r.nextch(); }
   
   /**
    * Checks if the next character is equal to the character
    * in the parameter.
    * @param c The character to be checked.
    * @return {@code true} if char is equal. Otherwise {@code false}.
    * @throws IOException 
    */
   private boolean readch(char c) throws IOException {
      readch();
      if( peek != c ) return false;
      peek = ' ';
      return true;
   }
   
   /**
    * Scans the string of words; then, it returns the token
    * of each word scanned.
    * @return Token type.
    * @throws IOException 
    */
   public Token scan() throws IOException {
      for( ; ; readch() ) {
         if( peek == ' ' || peek == '\t' ) continue;
         else if( peek == '\n' ) line = line + 1;
         else if( peek == '/' ) {
             char temp = peek;
             if( readch('/') ) {
                 while( peek != '\n' )
                     readch();
             }
             else return new Token(temp);
         }
         else break;
      }
      switch( peek ) {
      case '&':
         if( readch('&') ) return Word.and;  else return new Token('&');
      case '|':
         if( readch('|') ) return Word.or;   else return new Token('|');
      case '=':
         if( readch('=') ) return Word.eq;   else return new Token('=');
      case '!':
         if( readch('=') ) return Word.ne;   else return new Token('!');
      case '<':
         if( readch('=') ) return Word.le;   else return new Token('<');
      case '>':
         if( readch('=') ) return Word.ge;   else return new Token('>');
      }
      if( Character.isDigit(peek) ) {
         int v = 0;
         do {
            v = 10*v + Character.digit(peek, 10); readch();
         } while( Character.isDigit(peek) );
         if( peek != '.' ) return new Num(v);
         float x = v; float d = 10;
         for(;;) {
            readch();
            if( ! Character.isDigit(peek) ) break;
            x = x + Character.digit(peek, 10) / d; d = d*10;
         }
         return new Real(x);
      }
      if( Character.isLetter(peek) ) {
         StringBuilder b = new StringBuilder();
         do {
            b.append(peek); readch();
         } while( Character.isLetterOrDigit(peek) );
         String s = b.toString();
         Word w = (Word)words.get(s);
         if( w != null ) return w;
         w = new Word(s, Tag.ID);
         words.put(s, w);
         return w;
      } 
      Token tok = new Token(peek); peek = ' ';
      return tok;
   }
}
