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
            address = loadimmediate(look);
            move();
            address = expr(address);
        }
        else if( look.tag == Tag.ID ) {
            Token temp = look;
            move();
            if( look.tag == '[' ) {
                move();
                address = loadarray(temp, look);
                move();
            }
            else {
                address = loadword(temp);
                address = expr(address);
            }     
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
                return addimmediate(operand1, look);
            }
            else if( look.tag == Tag.ID ) {
                Register operand2 = loadword(look);
                return add(operand1, operand2);
            }
        }
        if( look.tag == '-' ) {
            move();
            if( look.tag == Tag.NUM || look.tag == Tag.REAL ) {
                return subtimmediate(operand1, look);
            }
            else if( look.tag == Tag.ID ) {
                Register operand2 = loadword(look);
                return subt(operand1, operand2);
            }
        }
        if( look.tag == '*' ) {
            move();
            if( look.tag == Tag.NUM || look.tag == Tag.REAL ) {
                Register operand2 = loadimmediate(look);
                return mult(operand1, operand2);
            }
            else if( look.tag == Tag.ID ) {
                Register operand2 = loadword(look);
                return mult(operand1, operand2);
            }
        }
        if( look.tag == '/' ) {
            move();
            if( look.tag == Tag.NUM || look.tag == Tag.REAL ) {
                Register operand2 = loadimmediate(look);
                return div(operand1, operand2);
            }
            else if( look.tag == Tag.ID ) {
                Register operand2 = loadword(look);
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
            address = mult(address, temp);
        }
        else if( look.tag == Tag.NE ) {
            move();
            Register temp = rel();
            address = mult(address, temp);
        }
        return address;
    }
    
    private Register rel() {
        Register address;
        if(look.tag == Tag.NUM || look.tag == Tag.REAL) {
            address = loadimmediate(look);
        }
        else
            address = loadword(look);
        move();
        if( look.tag == '>' ) {
            move();
            Register temp = rel();
            address = subt(address, temp);
        }
        else if( look.tag == Tag.GE ) {
            move();
            Register temp = rel();
            Num num1 = new Num(1);
            address = addimmediate(address, num1);
            address = subt(address, temp);
        }
        else if( look.tag == '<' ) {
            move();
            Register temp = rel();
            address = subt(temp, address);
        }
        else if( look.tag == Tag.LE ) {
            move();
            Register temp = rel();
            Num num = new Num(1);
            temp = addimmediate(temp, num);
            address = subt(temp, address);
        }
        return address;
    }
    
    private Register loadimmediate(Token tok) {
        Register address;
        if( tok.tag == Tag.NUM )
            address = new Temporary(tok, Type.Int, usedtempreg++);
        else
            address = new Float(tok, Type.Float, usedfloatreg++);
        emit("\t" + "li" + "\t" + address.getAddress() + ",\t" + address.toString());
        return address;
    }
    
    private Register loadarray(Token var, Token index) {
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
    
    private Register loadword(Token tok) {
        Type type = getType(tok);
        Register address;
        if ( type == Type.Int   ||
             type == Type.Long  ||
             type == Type.Short ||
             type == Type.Byte   ) {
            address = new Temporary(tok, type, usedtempreg++);
        }
        else
            address = new Float(tok, type, usedfloatreg++);
        emit("\t" + "lw" + "\t" + address.getAddress() + ",\t" + "(" +
                address.toString() + ")");
        return address;
    }
    
    private Register addimmediate(Register reg1, Token tok) {
        Register output;
        Type type;
        if( tok.tag == Tag.NUM )
            type = Type.max(reg1.type, Type.Int);
        else
            type = Type.Float;
        if( type == Type.Int )
            output = new Temporary(new Word(reg1.toString() + "+" + tok.toString(), Tag.ID), type, reg1.getRegisterAddress());
        else
            output = new Float(new Word(reg1.toString() + "+" + tok.toString(), Tag.ID), type, reg1.getRegisterAddress());
        emit("\t" + "addi" + "\t" + output.getAddress() + ",\t" + 
                reg1.getAddress()+ ",\t" + tok.toString());
        return output;
    }
    
    private Register add(Register operand1, Register operand2) {
        Register output;
        Type type = Type.max(operand1.type, operand2.type);
        if( type == Type.Int ) {
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
    
    private Register subtimmediate(Register reg1, Token tok) {
        Register output;
        Type type;
        if( tok.tag == Tag.NUM )
            type = Type.max(reg1.type, Type.Int);
        else
            type = Type.Float;
        if( type == Type.Int )
            output = new Temporary(new Word(reg1.toString() + "-" + tok.toString(), Tag.ID), type, reg1.getRegisterAddress());
        else
            output = new Float(new Word(reg1.toString() + "-" + tok.toString(), Tag.ID), type, reg1.getRegisterAddress());
        emit("\t" + "addi" + "\t" + output.getAddress() + ",\t" + 
                reg1.getAddress()+ ",\t" + "-" + tok.toString());
        return output;
    }
    
    private Register subt(Register operand1, Register operand2) {
        Register output;
        Type type = Type.max(operand1.type, operand2.type);
        if( type == Type.Int ) {
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
        if( type == Type.Int ) {
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
        if( type == Type.Int ) {
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
