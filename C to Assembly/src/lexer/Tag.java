package lexer;

public class Tag {
    
    /**
     * Do not instantiate this class.
     */
    private Tag() {}

   public final static int
      AND   = 256,  BASIC   = 257,  BREAK = 258,  CONTINUE = 259,   DO   = 260, 
      ELSE  = 261,  EQ      = 262,  FALSE = 263,  GE       = 264,   ID   = 265, 
      IF    = 266,  INDEX   = 267,  LE    = 268,  MINUS    = 269,   NE   = 270, 
      NUM   = 271,  OR      = 272,  REAL  = 273,  TEMP     = 274,   TRUE = 275, 
      WHILE = 276,  GOTO    = 277,  LABEL = 278,  IFFALSE  = 279,   END  = 280;
}
