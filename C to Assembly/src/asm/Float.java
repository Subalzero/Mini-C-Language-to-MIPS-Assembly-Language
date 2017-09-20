package asm;
import lexer.*;
import symbols.*;

public class Float extends Register {
    
    private int address;
    
    public Float(Token token, Type type, int address) {
        super(token, type);
        this.address = address;
    }
    
    @Override
    public String getAddress() {
        return "" + "$f" + address;
    }
    
    @Override
    public int getRegisterAddress() {
        return address;
    }
    
    @Override
    public String toString() {
        return token.toString();
    }
}
