package reader;
import java.io.*;
public class ReadFile {
    private FileReader f;
    private BufferedReader b;
    private String inside;
    private int next;               //character index
    
    public ReadFile() {
        next = 0;
        inside = "";
        try {
            f = new FileReader("test.txt");
            b = new BufferedReader(f);
            String currentString;
            for ( ; (currentString = b.readLine()) != null; inside += "\n")
                inside += currentString;
            System.out.println("<Input Source Code>");
            showStr();
        }
        catch(FileNotFoundException  e) { e.printStackTrace(); }
        catch(IOException ex) { ex.printStackTrace(); }
    }
    
    public void showStr() {
        System.out.println(inside);
    }
    
    public char nextch() {
        if(next >= inside.length())
            return '\0';
        return inside.charAt(next++);
    }
}
