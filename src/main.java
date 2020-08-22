
public class main {
    public static void main(String[] args) {
        String s = "C:\\Users\\eyald\\IdeaProjects\\Gzip\\src\\exampleInputs\\OnTheOrigin.txt";
        String t = "C:\\Users\\eyald\\Desktop\\smiley.bmp";
        String a
                = "C:\\Users\\eyald\\Desktop\\Red_Flowers.bmp";
        String d = "C:\\Users\\eyald\\Desktop\\CompressedFile";
        String e = "C:\\Users\\eyald\\Desktop\\originalFile";


        Lz77EncoderDecoder l = new Lz77EncoderDecoder(8, 1024);
        l.CompressLz(a, d);
        l.deCompress(d, e);


    }
}
