import java.io.*;
import java.nio.file.Files;

public class main {
    public static void main(String[] args) {
        String s = "C:\\Users\\eyald\\Desktop\\ExampleInputs\\LZ77.txt";
        String t = "C:\\Users\\eyald\\Desktop\\ExampleInputs\\OnTheOrigin.txt";
        Lz77EncoderDecoder l = new Lz77EncoderDecoder();
        l.CompressLz(t);
    }
}
