package asm;
import lexer.*;
import symbols.*;
import java.util.*;

public class Assembly {
    public String assembStr = "";
    private Token look;
    private LexInter lex;   
    public HashMap symbols = new HashMap();
    private int usedtempreg = 0;
    private int usedfloatreg = 0;
    
    public Assembly(LexInter l) { 
        lex = l;
    }
    
    private void move() { look = lex.scan(); }
    
    private void emit(String s) {
        assembStr += s + "\n";
        System.out.println(s);
    }
    
    public void program() {
        OUTER:
        for( move();; move() ) {
            switch( look.tag ) {
                case Tag.LABEL:
                    copylabel(look);
                    break;
                case Tag.END:
                    break OUTER;
                case Tag.ID:
                    assign(look);
                    break;
                case Tag.IFFALSE:
                    Register address = equality();
                    move();
                    Token label = look;
                    lesserThanZero(address, label);
                    break;
                case Tag.IF:
                    address = equality();
                    move();
                    label = look;
                    greaterThanZero(address, label);
                    break;
                case Tag.GOTO:
                    move();
                    jump(look);
                    break;
            }
        }
        exitstatement();
    }
    
    private void exitstatement() {
        emit("\t" + "li" + "\t" + "$v0" + ",\t" + 10);
        emit("\t" + "syscall");
    }
    
    private void copylabel(Token tok) {
        String str = tok.toString();
        move();
        if( look.tag != ':' ) emit(str);
        str += look.toString();
        emit(str);
    }
    
    private void resetAddresses() {
        usedtempreg = usedfloatreg = 0;
    }
    
    private Type getType(Token tok) {
        return (Type)symbols.get(tok);
    }
    
    private void assign(Token tok) {
        Register address = null;
        Token var = look;
        Token index = null;
        move();
        if( look.tag == '[' ){
            move();
            index = look;
            move();
            move();
        }
        move();
        if( look.tag == Tag.NUM || look.tag == Tag.REAL ) {
            address = loadimmediate((Num)look);
            move();
            address = expr(address);
        }
        else if( look.tag == Tag.ID ) {
            Token temp = look;
            move();
            if( look.tag == '[' ) {
                move();
                address = loadarray((Word)temp, (Num)look);
                move();
            }
            else {
                address = loadword((Word)temp);
                address = expr(address);
            }     
        }
        else if( look.tag == Tag.TRUE || look.tag == Tag.FALSE ) {
            int value = look.tag == Tag.TRUE ? 1 : 0;
            address = loadimmediate(new Num(value));
        }
        if(index != null) {
            storearray(var, index, address);
        }
        else 
            store(var, address);
        if ( look.tag == Tag.LABEL )
            copylabel(look);
        else if( look.tag == Tag.GOTO ) {
            move();
            jump(look);
        }
                    
    }
    
    private Register expr(Register operand1) {
        if( look.tag == '+' ){
            move();
            if( look.tag == Tag.NUM || look.tag == Tag.REAL ) {
                return addimmediate(operand1, (Num)look);
            }
            else if( look.tag == Tag.ID ) {
                Register operand2 = loadword((Word)look);
                return add(operand1, operand2);
            }
        }
        if( look.tag == '-' ) {
            move();
            if( look.tag == Tag.NUM || look.tag == Tag.REAL ) {
                return subtimmediate(operand1, (Num)look);
            }
            else if( look.tag == Tag.ID ) {
                Register operand2 = loadword((Word)look);
                return subt(operand1, operand2);
            }
        }
        if( look.tag == '*' ) {
            move();
            if( look.tag == Tag.NUM || look.tag == Tag.REAL ) {
                Register operand2 = loadimmediate((Num)look);
                return mult(operand1, operand2);
            }
            else if( look.tag == Tag.ID ) {
                Register operand2 = loadword((Word)look);
                return mult(operand1, operand2);
            }
        }
        if( look.tag == '/' ) {
            move();
            if( look.tag == Tag.NUM || look.tag == Tag.REAL ) {
                Register operand2 = loadimmediate((Num)look);
                return div(operand1, operand2);
            }
            else if( look.tag == Tag.ID ) {
                Register operand2 = loadword((Word)look);
                return div(operand1, operand2);
            }
        }
        return operand1;
    }
    
    private Register equality() {
        Register address;
        move();
        address = rel();
        if( look.tag == Tag.EQ ) {
            move();
            Register temp = rel();
            address = subt(address, temp);
            address = andimmediate(address, new Num(2147483647));
            temp = loadimmediate(new Num(1));
            address = subt(temp, address);
        }
        else if( look.tag == Tag.NE ) {
            move();
            Register temp = rel();
            address = subt(address, temp);
            address = andimmediate(address, new Num(2147483647));
            temp = loadimmediate(new Num(1));
            address = subt(temp, address);
            Register neg = loadimmediate(new Num(-1));
            address = mult(address, neg);
        }
        return address;
    }
    
    private Register rel() {
        Register address;
        if(look.tag == Tag.NUM || look.tag == Tag.REAL) {
            address = loadimmediate((Num)look);
        }
        else
            address = loadword((Word)look);
        move();
        if( look.tag == '>' ) {
            move();
            Register temp = rel();
            address = subt(address, temp);
            address = addimmediate(address, new Num(-1));
            address = orimmediate(address, new Num(1));
            address = andimmediate(address, new Num(-2147483647));
        }
        else if( look.tag == Tag.GE ) {
            move();
            Register temp = rel();
            address = subt(address, temp);
            address = orimmediate(address, new Num(1));
            address = andimmediate(address, new Num(-2147483647));
        }
        else if( look.tag == '<' ) {
            move();
            Register temp = rel();
            address = subt(temp, address);
            address = addimmediate(address, new Num(-1));
            address = orimmediate(address, new Num(1));
            address = andimmediate(address, new Num(-2147483647));
        }
        else if( look.tag == Tag.LE ) {
            move();
            Register temp = rel();
            address = subt(temp, address);
            address = orimmediate(address, new Num(1));
            address = andimmediate(address, new Num(-2147483647));
        }
        return address;
    }
    
    private Register loadimmediate(Num num) {
        Register address;
        if( num.tag == Tag.NUM )
            address = new Temporary(num, Type.Int, usedtempreg++);
        else
            address = new Float(num, Type.Float, usedfloatreg++);
        emit("\t" + "li" + "\t" + address.getAddress() + ",\t" + address.toString());
        return address;
    }
    
    private Register loadarray(Word var, Num index) {
        Register address;
        Type type = getType(var);
        if( type == Type.Int   ||
            type == Type.Long  ||
            type == Type.Short ||
            type == Type.Byte   ) {
            Word word = new Word(var.toString() + "(" + index.toString() + ")", Tag.ID);
            address = new Temporary(word, type, usedtempreg++);
        }
        else {
            Word word = new Word(var.toString() + "(" + index.toString() + ")", Tag.ID);
            address = new Float(word, type, usedfloatreg++);
        }
        emit("\t" + "lw" + "\t" + address.getAddress() + ",\t" + var.toString() +
                "(" + index.toString() + ")");
        return address;
    }
    
    private Register loadword(Word word) {
        Type type = getType(word);
        Register address;
        if ( type == Type.Int   ||
             type == Type.Long  ||
             type == Type.Short ||
             type == Type.Byte   ) {
            address = new Temporary(word, type, usedtempreg++);
        }
        else
            address = new Float(word, type, usedfloatreg++);
        emit("\t" + "lw" + "\t" + address.getAddress() + ",\t" + "(" +
                address.toString() + ")");
        return address;
    }
    
    private Register addimmediate(Register operand1, Num operand2) {
        Register output;
        Type type;
        if( operand2.tag == Tag.NUM )
            type = Type.max(operand1.type, Type.Int);
        else
            type = Type.Float;
        if( type == Type.Int )
            output = new Temporary(new Word(operand1.toString() + "+" + operand2.toString(), Tag.ID), type, operand1.getRegisterAddress());
        else
            output = new Float(new Word(operand1.toString() + "+" + operand2.toString(), Tag.ID), type, operand1.getRegisterAddress());
        emit("\t" + "addi" + "\t" + output.getAddress() + ",\t" + 
                operand1.getAddress()+ ",\t" + operand2.toString());
        return output;
    }
    
    private Register add(Register operand1, Register operand2) {
        Register output;
        Type type = Type.max(operand1.type, operand2.type);
        if( type == Type.Int   ||
            type == Type.Long  ||
            type == Type.Short ||
            type == Type.Byte   ) {
            output = new Temporary(new Word(operand1.toString() + "+" + operand2.toString(), Tag.ID),
                type, operand1.getRegisterAddress());
        }
        else {
            output = new Float(new Word(operand1.toString() + "+" + operand2.toString(), Tag.ID),
                type, operand1.getRegisterAddress());
        }
        emit("\t" + "add" + "\t" + output.getAddress() + ",\t" + 
                operand1.getAddress()+ ",\t" + operand2.getAddress());
        return output;
    }
    
    private Register subtimmediate(Register operand1, Num operand2) {
        Register output;
        Type type;
        if( operand2.tag == Tag.NUM )
            type = Type.max(operand1.type, Type.Int);
        else
            type = Type.Float;
        if( type == Type.Int )
            output = new Temporary(new Word(operand1.toString() + "-" + operand1.toString(), Tag.ID), type, operand1.getRegisterAddress());
        else
            output = new Float(new Word(operand1.toString() + "-" + operand2.toString(), Tag.ID), type, operand1.getRegisterAddress());
        emit("\t" + "addi" + "\t" + output.getAddress() + ",\t" + 
                operand1.getAddress()+ ",\t" + "-" + operand2.toString());
        return output;
    }
    
    private Register andimmediate(Register operand1, Num operand2) {
        Register output;
        Type type;
        if( operand2.tag == Tag.NUM )
            type = Type.max(operand1.type, Type.Int);
        else
            type = Type.Float;
        if( type == Type.Int )
            output = new Temporary(new Word(operand1.toString() + "&" + operand2.toString(), Tag.ID), type, operand1.getRegisterAddress());
        else
            output = new Float(new Word(operand1.toString() + "&" + operand2.toString(), Tag.ID), type, operand1.getRegisterAddress());
        emit("\t" + "andi" + "\t" + output.getAddress() + ",\t" + 
                operand1.getAddress()+ ",\t" + operand2.toString());
        return output;
    }
    
    private Register orimmediate(Register operand1, Num operand2) {
        Register output;
        Type type;
        if( operand2.tag == Tag.NUM )
            type = Type.max(operand1.type, Type.Int);
        else
            type = Type.Float;
        if( type == Type.Int )
            output = new Temporary(new Word(operand1.toString() + "|" + operand2.toString(), Tag.ID), type, operand1.getRegisterAddress());
        else
            output = new Float(new Word(operand1.toString() + "|" + operand2.toString(), Tag.ID), type, operand1.getRegisterAddress());
        emit("\t" + "ori" + "\t" + output.getAddress() + ",\t" + 
                operand1.getAddress()+ ",\t" + operand2.toString());
        return output;
    }
    
    private Register xorimmediate(Register operand1, Num operand2) {
        Register output;
        Type type;
        if( operand2.tag == Tag.NUM )
            type = Type.max(operand1.type, Type.Int);
        else
            type = Type.Float;
        if( type == Type.Int )
            output = new Temporary(new Word(operand1.toString() + "^" + operand2.toString(), Tag.ID), type, operand1.getRegisterAddress());
        else
            output = new Float(new Word(operand1.toString() + "^" + operand2.toString(), Tag.ID), type, operand1.getRegisterAddress());
        emit("\t" + "ori" + "\t" + output.getAddress() + ",\t" + 
                operand1.getAddress()+ ",\t" + operand2.toString());
        return output;
    }
    
    private Register subt(Register operand1, Register operand2) {
        Register output;
        Type type = Type.max(operand1.type, operand2.type);
        if( type == Type.Int   ||
            type == Type.Long  ||
            type == Type.Short ||
            type == Type.Byte   ) {
            output = new Temporary(new Word(operand1.toString() + "-" + operand2.toString(), Tag.ID),
                type, operand1.getRegisterAddress());
        }
        else {
            output = new Float(new Word(operand1.toString() + "-" + operand2.toString(), Tag.ID),
                type, operand1.getRegisterAddress());
        }
        emit("\t" + "sub" + "\t" + output.getAddress() + ",\t" + 
                operand1.getAddress()+ ",\t" + operand2.getAddress());
        return output;
    }
    
    
    private Register mult(Register operand1, Register operand2) {
        Register output;
        Type type = Type.max(operand1.type, operand2.type);
        if( type == Type.Int   ||
            type == Type.Long  ||
            type == Type.Short ||
            type == Type.Byte   ) {
            output = new Temporary(new Word(operand1.toString() + "*" + operand2.toString(), Tag.ID),
                type, operand1.getRegisterAddress());
        }
        else {
            output = new Float(new Word(operand1.toString() + "*" + operand2.toString(), Tag.ID),
                type, operand1.getRegisterAddress());
        }
        emit("\t" + "mult" + "\t" + output.getAddress() + ",\t" + 
                operand1.getAddress()+ ",\t" + operand2.getAddress());
        return output;
    }
    
    private Register div(Register operand1, Register operand2) {
        Register output;
        Type type = Type.max(operand1.type, operand2.type);
        if( type == Type.Int   ||
            type == Type.Long  ||
            type == Type.Short ||
            type == Type.Byte   ) {
            output = new Temporary(new Word(operand1.toString() + "/" + operand2.toString(), Tag.ID),
                type, operand1.getRegisterAddress());
        }
        else {
            output = new Float(new Word(operand1.toString() + "/" + operand2.toString(), Tag.ID),
                type, operand1.getRegisterAddress());
        }
        emit("\t" + "div" + "\t" + output.getAddress() + ",\t" + 
                operand1.getAddress()+ ",\t" + operand2.getAddress());
        return output;
    }
    
    private Register and(Register operand1, Register operand2) {
        Register output;
        Type type = Type.max(operand1.type, operand2.type);
        if( type == Type.Int   ||
            type == Type.Long  ||
            type == Type.Short ||
            type == Type.Byte   ) {
            output = new Temporary(new Word(operand1.toString() + "&" + operand2.toString(), Tag.ID),
                type, operand1.getRegisterAddress());
        }
        else {
            output = new Float(new Word(operand1.toString() + "&" + operand2.toString(), Tag.ID),
                type, operand1.getRegisterAddress());
        }
        emit("\t" + "and" + "\t" + output.getAddress() + ",\t" + 
                operand1.getAddress()+ ",\t" + operand2.getAddress());
        return output;
    }
    
    private Register or(Register operand1, Register operand2) {
        Register output;
        Type type = Type.max(operand1.type, operand2.type);
        if( type == Type.Int   ||
            type == Type.Long  ||
            type == Type.Short ||
            type == Type.Byte   ) {
            output = new Temporary(new Word(operand1.toString() + "|" + operand2.toString(), Tag.ID),
                type, operand1.getRegisterAddress());
        }
        else {
            output = new Float(new Word(operand1.toString() + "|" + operand2.toString(), Tag.ID),
                type, operand1.getRegisterAddress());
        }
        emit("\t" + "or" + "\t" + output.getAddress() + ",\t" + 
                operand1.getAddress()+ ",\t" + operand2.getAddress());
        return output;
    }
    
    private Register xor(Register operand1, Register operand2) {
        Register output;
        Type type = Type.max(operand1.type, operand2.type);
        if( type == Type.Int   ||
            type == Type.Long  ||
            type == Type.Short ||
            type == Type.Byte   ) {
            output = new Temporary(new Word(operand1.toString() + "^" + operand2.toString(), Tag.ID),
                type, operand1.getRegisterAddress());
        }
        else {
            output = new Float(new Word(operand1.toString() + "^" + operand2.toString(), Tag.ID),
                type, operand1.getRegisterAddress());
        }
        emit("\t" + "xor" + "\t" + output.getAddress() + ",\t" + 
                operand1.getAddress()+ ",\t" + operand2.getAddress());
        return output;
    }
    
    private void lesserThanZero(Register address, Token label) {
        emit("\t" + "bltz" + "\t" + address.getAddress() + ",\t" +
                label.toString());
        resetAddresses();
    }
    
    private void greaterThanZero(Register address, Token label) {
        emit("\t" + "bgtz" + "\t" + address.getAddress() + ",\t" +
                label.toString());
        resetAddresses();
    }
    
    private void jump(Token label) {
        emit("\t" + "j" + "\t" + label.toString());
    }
    
    private void store(Token tok, Register address) {
        emit("\t" + "move" + "\t" + tok.toString() + ",\t" + 
                address.getAddress());
        resetAddresses();
    }
    
    private void store(Register address1, Register address2) {
        emit("\t" + "move" + "\t" + address1.toString() + ",\t" + 
                address2.getAddress());
        resetAddresses();
    }
    
    private void storearray(Token offset, Token index, Register address) {
        emit("\t" + "move" + "\t" + offset.toString() + "(" +
                index.toString() + "),\t" + address.getAddress());
    }
}
