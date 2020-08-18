import java.io.FileNotFoundException;

public class main
{
    public static void main(String[] args)
    {
        String s = "C:\\Users\\owner\\Desktop\\דחיסה\\gzip\\exmp.txt";
        Lz77EncoderDecoder l = new Lz77EncoderDecoder();
        l.CompressLz(s);

    }
}
