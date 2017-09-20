package lexer;
import java.util.*;
import parser.*;

public class LexInter {
    
    public static int line = 1;
    private char peek = ' ';
    private HashMap words = new HashMap();
    private int next = 0;
    
    private void reserve(Word w) { words.put(w.lexeme, w); }
    
    public LexInter() {
        
        reserve( new Word("goto",    Tag.GOTO)   );
        reserve( new Word("if",      Tag.IF)     );
        reserve( new Word("iffalse", Tag.IFFALSE));
        reserve( new Word("minus",   Tag.MINUS)  );
        reserve( new Word("end",     Tag.END)    );
        reserve( Word.True  );
        reserve( Word.False );
    }
    
    private void readch() { 
        peek = Parser.output.charAt(next++); 
    }
    
    private boolean readch(char c) {
        readch();
        if ( peek != c ) return false;
        peek = ' ';
        return true;
    }
    
    public Token scan() {
        OUTER:
        for (;; readch()) {
            switch (peek) {
                case ' ':
                case '\t':
                    continue;
                case '\n':
                    line = line + 1;
                    break;
                default:
                    break OUTER;
            }
        }
        switch( peek ) {
            case '=':
                if ( readch('=') ) return Word.eq; else return new Token('=');
            case '!':
                if ( readch('=') ) return Word.ne; else return new Token('!');
            case '>':
                if ( readch('=') ) return Word.ge; else return new Token('>');
            case '<':
                if ( readch('=') ) return Word.le; else return new Token('<');
        }
        if ( Character.isDigit(peek) ) {
            int v = 0;
            do {
                v = 10 * v + Character.digit(peek, 10); readch();
            } while( Character.isDigit(peek) );
            if ( peek != '.' ) return new Num(v);
            float x = v; float d = 10;
            for ( ; ; ) {
                readch();
                if ( ! Character.isDigit(peek) ) break;
                x = x + Character.digit(peek, 10) / d; d = d * 10;
            }
            return new Real(x);
        }
        if ( Character.isLetter(peek) ) {
            StringBuilder b = new StringBuilder();
            if ( peek == 'L') {             //checks if object is LABEL type.
                b.append(peek);
                readch();
                if ( Character.isDigit(peek) ) {
                    do {
                        b.append(peek); readch();
                    } while( Character.isDigit(peek) );
                    if ( ! Character.isLetter(peek) ) {
                        String s = b.toString();
                        return new Word(s, Tag.LABEL);
                    }
                }
            }
            while ( Character.isLetterOrDigit(peek) ) {
                b.append(peek); readch();
            }
            if ( peek == ':' ) {                     //checks LABEL
                String s = b.toString();
                return new Word(s, Tag.LABEL);
            }
            String s = b.toString();
            Word w = (Word)words.get(s);
            if ( w != null ) return w;              //checks if word is a reserved word.
            w = new Word(s, Tag.ID);
            words.put(w.lexeme, w);
            return w;                               //returns as identifier type otherwise.
        }
        Token tok = new Token(peek); peek = ' ';
        return tok;                  //tokenize other characters.
    }
}
