import java.util.BitSet;

public class main {
    public static void main(String[] args) {
        String s = "C:\\Users\\eyald\\IdeaProjects\\Gzip\\src\\exampleInputs\\OnTheOrigin.txt";
        String t = "C:\\Users\\eyald\\Desktop\\smiley.bmp";
        String a
                = "C:\\Users\\eyald\\Desktop\\lz77.txt";
        Lz77EncoderDecoder l = new Lz77EncoderDecoder(8, 4096);
          // l.CompressLz(a);
           l.deCompress(a);


    }

}
