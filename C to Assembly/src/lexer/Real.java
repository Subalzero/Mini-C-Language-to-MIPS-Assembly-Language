package lexer;
public class Real extends Token {

	public final double value;
	public Real(double v) { super(Tag.REAL); value = v; }
        @Override
	public String toString() { return "" + value; }
}
