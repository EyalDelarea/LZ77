
public class main {
    public static void main(String[] args) {
        String s = "C:\\Users\\eyald\\IdeaProjects\\Gzip\\src\\exampleInputs\\OnTheOrigin.txt";
        String t = "C:\\Users\\eyald\\Desktop\\smiley.bmp";
        String a
                = "C:\\Users\\eyald\\Desktop\\lz77.txt";
        String d = "C:\\Users\\eyald\\Desktop\\CompressedFile";
        String e = "C:\\Users\\eyald\\Desktop\\originalFile";


        Lz77EncoderDecoder l = new Lz77EncoderDecoder(16, 2048);
        l.CompressLz(s, d);
        l.deCompress(d, e);


    }
}
