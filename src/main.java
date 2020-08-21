import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.BitSet;

public class main {
    public static void main(String[] args) {
        String s = "C:\\Users\\eyald\\IdeaProjects\\Gzip\\src\\exampleInputs\\OnTheOrigin.txt";
        String t = "C:\\Users\\eyald\\Desktop\\smiley.bmp";
        String a
                = "C:\\Users\\eyald\\Desktop\\lz77.txt";
        String genesis
                = "C:\\Users\\eyald\\Desktop\\genesis.txt";

        String c
                = "C:\\Users\\eyald\\Desktop\\CompressFile";
        Lz77EncoderDecoder l = new Lz77EncoderDecoder(8, 2048);
         l.CompressLz(genesis);
          l.deCompress(c);



    }
}
