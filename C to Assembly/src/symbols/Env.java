package symbols;
import java.util.*; import lexer.*; import inter.*;

public class Env {

	private HashMap map;
	protected Env prev;

	public Env(Env n) { map = new HashMap(); prev = n; }

	public void put(Token w, Id i) { map.put(w, i); }

	public Id get(Token w) {
		for( Env e = this; e != null; e = e.prev ) {
			Id found = (Id)(e.map.get(w));
			if( found != null ) return found;
		}
		return null;
	}
}
