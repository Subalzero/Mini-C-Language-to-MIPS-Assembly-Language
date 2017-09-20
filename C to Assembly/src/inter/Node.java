package inter;
import lexer.*;
import parser.*;

public class Node {

   int lexline = 0;

   Node() { lexline = Lexer.line; }

   void error(String s) { throw new Error("near line "+lexline+": "+s); }

   static int labels = 0;

   public int newlabel() { return ++labels; }

   public void emitlabel(int i) { Parser.output += "L" + i + ":"; System.out.print("L" + i + ":"); }

   public void emit(String s) { Parser.output += "\t" + s + "\n"; System.out.println("\t" + s); }
}
