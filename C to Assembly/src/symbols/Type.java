package symbols;
import lexer.*;
public class Type extends Word {

   public int width = 0;          // width is used for storage allocation

   public Type(String s, int tag, int w) { super(s, tag); width = w; }

   /**
    * All basic types.
    */
   public static final Type
      Int    = new Type( "int",    Tag.BASIC, 2 ),
      Float  = new Type( "float",  Tag.BASIC, 4 ),
      Char   = new Type( "char",   Tag.BASIC, 1 ),
      Bool   = new Type( "bool",   Tag.BASIC, 1 ),
      Byte   = new Type( "byte",   Tag.BASIC, 1 ),
      Long   = new Type( "long",   Tag.BASIC, 4 ),
      Short  = new Type( "short",  Tag.BASIC, 2 ),
      Double = new Type( "double", Tag.BASIC, 8 );
   
   /**
    * Checks whether the type is numeric or not.
    * @param p Type to be checked.
    * @return {@code true} if numeric. Otherwise, {@code false}
    */
   public static boolean numeric(Type p) {
      if (p == Type.Char   || 
          p == Type.Int    || 
          p == Type.Float  || 
          p == Type.Byte   || 
          p == Type.Double ||
          p == Type.Long   ||
          p == Type.Short   ) 
          return true;
      else return false;
   }

   public static Type max(Type p1, Type p2 ) {
      if ( ! numeric(p1) || ! numeric(p2) ) return null;
      else if ( p1 == Type.Double || p2 == Type.Double ) return Type.Double;
      else if ( p1 == Type.Float  || p2 == Type.Float  ) return Type.Float;
      else if ( p1 == Type.Long   || p2 == Type.Long   ) return Type.Long;
      else if ( p1 == Type.Int    || p2 == Type.Int    ) return Type.Int;
      else if ( p1 == Type.Short  || p2 == Type.Short  ) return Type.Short;
      else return Type.Char;
   }
}
