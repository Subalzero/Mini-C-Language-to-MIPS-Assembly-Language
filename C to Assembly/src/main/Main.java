package main;
import lexer.LexInter;
import java.io.*; import lexer.*; import parser.*;import asm.*;

public class Main {

	public static void main(String[] args) throws IOException {
                LexInter li = new LexInter();
                Assembly assemb = new Assembly(li);
		Lexer lex = new Lexer();
		Parser parse = new Parser(lex, assemb);
                
                System.out.println("<Intermediate code>");
		parse.program();
		System.out.write('\n');
                System.out.println();
                System.out.println("<Assembly code>");
                assemb.program();
	}
}
