package lexer;

import java.util.Objects;

public class Word extends Token {

   public String lexeme = "";
   public Word(String s, int tag) { super(tag); lexeme = s; }
   @Override
   public String toString() { return lexeme; }

    /**
     * Reserved words
     */
    public static final Word

      and = new Word( "&&", Tag.AND ),  or = new Word( "||", Tag.OR ),
      eq  = new Word( "==", Tag.EQ  ),  ne = new Word( "!=", Tag.NE ),
      le  = new Word( "<=", Tag.LE  ),  ge = new Word( ">=", Tag.GE ),

      minus  = new Word( "minus", Tag.MINUS ),
      True   = new Word( "true",  Tag.TRUE  ),
      False  = new Word( "false", Tag.FALSE ),
      temp   = new Word( "t",     Tag.TEMP  );

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.lexeme);
        return hash;
    }
    
   @Override
    public boolean equals(Object o) {
        if( o instanceof Word ) {
            Word w = (Word) o;
            return(w.lexeme.equals(lexeme) && w.tag == this.tag);
        }
        return false;
    }
}
