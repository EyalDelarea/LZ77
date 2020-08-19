import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;

public class main {
    public static void main(String[] args) {
        String s = "C:\\Users\\eyald\\IdeaProjects\\Gzip\\src\\exampleInputs";
        String t = "C:\\Users\\eyald\\Desktop\\lz77.txt";
        Lz77EncoderDecoder l = new Lz77EncoderDecoder();
        l.CompressLz(t);


    }

}
