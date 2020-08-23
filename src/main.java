
public class main {
    public static void main(String[] args) {


        //Please change your base path
        final String basePath = "C:\\Users\\eyald\\Desktop\\";

        final String fileToCompressPath = basePath+"Romeo and Juliet  Entire Play.txt"; //file to compress
        final String compressOutPutPath = basePath+"CompressedFile";                    //where to save compress
        final String finalOutPutPath = basePath+"originalFile";                         //where to save decompress
        final int searchBufferSize = 64;
        final int windowSize = 2048;


        Lz77EncoderDecoder l = new Lz77EncoderDecoder(searchBufferSize, windowSize);


        l.CompressLz(fileToCompressPath, compressOutPutPath);
        l.deCompress(compressOutPutPath, finalOutPutPath);


    }
}
