package asm;
import lexer.*;
import symbols.*;

public abstract class Register {
    
    Token token;
    Type type;
    
    public Register(Token tok, Type typ) {
        token = tok;
        type = typ;
    }
    
    public abstract String getAddress();
    
    @Override
    public abstract String toString();
    
    public abstract int getRegisterAddress();
    
}
