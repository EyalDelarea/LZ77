import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.BitSet;


public class Lz77EncoderDecoder {

    private static final int MIN_MATCH_LENGTH = 3;
    private static final int MAX_MATCH_LENGTH = 258;
    private static final int NOT_FOUND = -1;
    public int windowSize;
    public int searchBufferSize;
    public static int searchBufferIndex;
    public static int notNullIndex;
    private static int filePointer = 0;
    public static byte[] bytesArray;
    public static BitSet finalOutPut;
    private static int bitSetIndex = 0;
    ArrayList<DictionaryItem> dictionary = new ArrayList<>();
    private int tripletBitSize;

    /**
     * Constructor
     * @param searchBufferSize
     * @param windowSize
     */
    public Lz77EncoderDecoder(int searchBufferSize, int windowSize) {
        this.windowSize = windowSize;
        this.searchBufferSize = searchBufferSize;
        this.searchBufferIndex = windowSize - searchBufferSize;
        notNullIndex = searchBufferIndex;
        //log2 for how many bits to represent the numbers +8 bits for each char.
        tripletBitSize = (log2(searchBufferSize) + (log2(searchBufferIndex)) + 8);
        finalOutPut = new BitSet();

        //encode the size of triplet in the first 8 bits of the bitSet
        BitSet lengthBit = BitSet.valueOf(new long[]{tripletBitSize});
        for (int i = 0; i < 8; i++) {
            finalOutPut.set(bitSetIndex++, lengthBit.get(i));
        }
    }

    /**
     *
     * @param fileToCompress
     */
    public void CompressLz(String fileToCompress,String outPutPath) {

        //read all bytes from file
        File sa = new File(fileToCompress);
        try {
            bytesArray = Files.readAllBytes(sa.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        //handling end case where the file is too short
        if (bytesArray.length < searchBufferSize) {
            System.out.println("Your file is too short to be compress!" +
                    "\nIt's just not worth it...");
            System.exit(1);
        }

        //create the window
        Byte[] window = initWindow(windowSize);

        // look ahead buffer condition !=null
        do {
            DictionaryItem currentBestMatch = new DictionaryItem();
            findBestMatch(window, searchBufferIndex - 1, currentBestMatch);
        }
        //meaning index 8 is -1
        while (window[searchBufferIndex] != NOT_FOUND);

        //out the compressFile
        createFileAndWriteObject(outPutPath);
    }

    /**
     *
     * @param compressFile
     * @param outputDecompress
     */
    public void deCompress(String compressFile,String outputDecompress) {

        //create file
        try {
            File compressedFile = new File(outputDecompress);
            if (compressedFile.createNewFile()) {
                System.out.println("File created: " + compressedFile.getName());
            } else {
                System.out.println("File already exists.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //write to file
        try {
            FileOutputStream fileOut =
                    new FileOutputStream(outputDecompress);
            ArrayList<Byte> decompressed = new ArrayList<Byte>();
            //reset our bitsetIndex to read
            bitSetIndex = 0;
            //Read the bitset from the file
            BitSet set = new BitSet();

            try {
                FileInputStream fileIn = new FileInputStream(compressFile);
                ObjectInputStream objectIn = new ObjectInputStream(fileIn);
                set = (BitSet) objectIn.readObject();
                System.out.println("The Object has been read from the file");
                objectIn.close();
            } catch (IOException | ClassNotFoundException i) {
                i.printStackTrace();
            }


            //first 8 bits represent the length of the triplets
            int tripletSize = 0;
            while (bitSetIndex < 8) {
                if (set.get(bitSetIndex)) {
                    tripletSize += Math.pow(2, bitSetIndex);
                }
                bitSetIndex++;
            }


            while (bitSetIndex < set.length()) {
                //decode one triplet
                //char
                int bValue = 0;
                for (int i = 0; i < 8; i++) {
                    if (set.get(bitSetIndex)) {
                        bValue += Math.pow(2, i);
                    }
                    bitSetIndex++;
                }
                byte oValue = (byte) bValue;

                int length = 0;
                //length
                for (int i = 0; i < (log2(searchBufferSize)); i++) {
                    if (set.get(bitSetIndex)) {
                        length += Math.pow(2, i);
                    }
                    bitSetIndex++;
                }
                //match distance
                int distance = 0;
                for (int i = 0; i < (log2(searchBufferIndex)); i++) {
                    if (set.get(bitSetIndex)) {
                        distance += Math.pow(2, i);
                    }
                    bitSetIndex++;
                }
                boolean isBasic = ((length > MAX_MATCH_LENGTH) || (length < MIN_MATCH_LENGTH));
                DictionaryItem triplet = new DictionaryItem(oValue, distance, length, isBasic);
                //parse triplet
                if (!isBasic) {
                    //run matchDistance backwards in arraylist
                    //copy amount of length
                    //write the char
                    int lastIndex = (decompressed.size()); //last element

                    //copy length amount of chars
                    for (int i = 0; i < length; i++) {
                        decompressed.add(decompressed.get(lastIndex - distance));
                        fileOut.write(decompressed.get(lastIndex - distance));
                        lastIndex = decompressed.size();
                    }
                    //insert the char

                }
                decompressed.add(oValue);
                fileOut.write(oValue);
            }
            fileOut.close();
        } catch (IOException i) {
            i.printStackTrace();
        }
    }


    /**
     * Searching for the longest match in the window , using recursion
     * Once found the best match in the file,add it to dictionary and
     * push the window.
     *
     * @param window           current window
     * @param index            index to start look from
     * @param currentBestMatch the current item who claims the throne.
     */
    private void findBestMatch(Byte[] window, int index, DictionaryItem currentBestMatch) {
        //Stop condition - if index is below zero , meaning we went through the entire window
        //and won't better match.
        if (index < 0) {
            //validate length
            if (currentBestMatch.isBasic()) {
                //basic object
                DictionaryItem basic = new DictionaryItem(window[searchBufferIndex],
                        0, 0, true);
                dictionary.add(basic);
                encodeToBitSet(basic);
                basicPush(window);
            } else {
                //not basic
                dictionary.add(currentBestMatch);
                encodeToBitSet(currentBestMatch);
                pushNewWindowInput(window, currentBestMatch);
            }
            return;
        }

        //Byte value is the same,we found a match
        if (window[searchBufferIndex].equals(window[index])) {
            //check for length
            int length = findCurrentMatchLength(window, index);
            //create new object
            DictionaryItem currentMatch = null;
            if (length < MIN_MATCH_LENGTH || length > MAX_MATCH_LENGTH) {
                currentMatch = new DictionaryItem(window[searchBufferIndex], 0, 0, true);
            } else {
                currentMatch = new DictionaryItem(window[searchBufferIndex + length], Math.abs(searchBufferIndex - index), length, false);
            }


            //Continue the recursion
            index--;
            currentBestMatch = findMaxLengthInMinDist(currentBestMatch, currentMatch);
            findBestMatch(window, index, currentBestMatch);
        }
        //if index value is null
        else if (window[index] == null) {
            //if the distance is in it's default value
            if (currentBestMatch.getmatchDistance() == NOT_FOUND) {
                DictionaryItem basicCurrentMatch = new DictionaryItem(window[searchBufferIndex], 0, 0, true);
                dictionary.add(basicCurrentMatch);
                encodeToBitSet(basicCurrentMatch);
                basicPush(window);
            } else {
                //add the match we found earlier
                //but check if it hold the conditions
                boolean shortLengthObject = isShortObject(currentBestMatch);
                if (shortLengthObject) {
                    DictionaryItem basicCurrentMatch = new DictionaryItem(window[searchBufferIndex], 0, 0, true);
                    dictionary.add(basicCurrentMatch);
                    encodeToBitSet(basicCurrentMatch);
                    basicPush(window);
                } else {
                    dictionary.add(currentBestMatch);
                    encodeToBitSet(currentBestMatch);
                    pushNewWindowInput(window, currentBestMatch);
                }
            }
            return;
        } else {
            //no match found reducing index to keep search for match
            //in search buffer
            index--;
            findBestMatch(window, index, currentBestMatch);
        }

    }

    /**
     * Encode dictionary item into bitset object
     * <Char,Length,Distance>
     *
     * @param item dictionary item
     *             And writing it to the BitSet object which is static.
     */
    private void encodeToBitSet(DictionaryItem item) {

        //encode item to bit set
        // <char,copy,length>
        // <8bits,log2,log2>

        byte ch = item.getValue();
        BitSet chBit = BitSet.valueOf(new byte[]{ch});

        //for char
        for (int i = 0; i < 8; i++) {
            finalOutPut.set(bitSetIndex++, chBit.get(i));
        }

        int length = item.getLength();
        BitSet lengthBit = BitSet.valueOf(new long[]{length});
        //for char
        for (int i = 0; i < log2(searchBufferSize); i++) {
            finalOutPut.set(bitSetIndex++, lengthBit.get(i));
        }

        int copy = item.getmatchDistance();
        BitSet copyBit = BitSet.valueOf(new long[]{copy});
        //for char
        for (int i = 0; i < log2(searchBufferIndex); i++) {
            finalOutPut.set(bitSetIndex++, copyBit.get(i));
        }
    }


    /**
     * @param a first dictionary item
     * @param b second dictionary item
     * @return the item with the highest length in minimum match distance
     */
    private DictionaryItem findMaxLengthInMinDist(DictionaryItem a, DictionaryItem b) {
        if (a.getLength() > b.getLength())
            return a;
        else if (b.getLength() > a.getLength())
            return b;
        else {
            if (a.getmatchDistance() > b.getmatchDistance()) {
                return b;
            } else
                return a;
        }
    }

    /**
     * Function to check if a dictionary item length is valid
     * and worth coding
     *
     * @param item current item
     * @return boolean value
     */
    private boolean isShortObject(DictionaryItem item) {
        //Check for length limits
        int length = item.getLength();
        boolean min = length < MIN_MATCH_LENGTH;
        boolean max = length > MAX_MATCH_LENGTH;

        return min || max;
    }

    /**
     * @param window the current window we will look on
     * @param index  the byte index for the current match
     * @return the matches byte sequence
     */
    private int findCurrentMatchLength(Byte[] window, int index) {
        int searchBufferIndex = Lz77EncoderDecoder.searchBufferIndex + 1;
        int counter = 1;
        int temp = 0;
        index++;
        //advance two pointer until there's no match between the chars
        while ((Lz77EncoderDecoder.searchBufferIndex + temp < window.length) && (searchBufferIndex + temp < window.length)
                && (window[searchBufferIndex + temp].equals(window[index + temp]))) {
            counter++;
            temp++;
        }
        //crash fix for length bigger then window size /2
        if (counter >= (windowSize - searchBufferIndex))
            return windowSize - searchBufferIndex - 1;

        return counter;
    }

    /**
     * initialize window
     *
     * @param size byte[] array length
     */
    private Byte[] initWindow(int size) {
        Byte[] window = new Byte[size];

        for (int i = (searchBufferIndex); (i < window.length) && (filePointer < bytesArray.length); i++) {
            window[i] = bytesArray[filePointer];
            filePointer++;
        }
        return window;
    }


    /**
     * @param window bytes window
     * @param item   coded item to jump the amount of the length.
     */
    private void pushNewWindowInput(Byte[] window, DictionaryItem item) {
        int amountOfJumps = item.getLength() + 1;
        //loop how many jumps
        for (int jumps = 0; jumps < amountOfJumps; jumps++) {
            basicPush(window);
        }
    }

    /**
     * Function to push the entire window by one index
     * And then reads a new char for the byte array of the file
     *
     * @param window out window
     * @return updated firstIndex
     */
    public void basicPush(Byte[] window) {

        //push each byte
        Byte temp = window[window.length - 1];
        if (notNullIndex >= 0) {
            for (int i = window.length - 1; i >= notNullIndex; i--) {
                if (i > 0) {
                    Byte temp2 = window[i - 1];
                    window[i - 1] = temp;
                    temp = temp2;
                }
            }
            //read for the file new char
            if (filePointer >= bytesArray.length) {
                //read all file already,push -1
                window[window.length - 1] = -1;
            } else {
                //push next char to window
                window[window.length - 1] = bytesArray[filePointer];
                filePointer++;
            }
        }
        //update notNullIndex
        //if we finished with null ,update to 0;
        if (notNullIndex <= 0) {
            notNullIndex = 0;
        } else {
            --notNullIndex;
        }

    }


    /**
     * helper function to calculate log2 of N
     *
     * @param N number
     * @return round up the score as int.
     */
    public static int log2(int N) {
        // calculate log2 N indirectly
        // using log() method
        return ((int) (Math.log(N) / Math.log(2))) + 1;
    }

    /**
     * Create a file in the @param path
     * and writing to it @finalOutPut object which is BitSet  object
     * @param path where to create the compress file.
     */
    public static void createFileAndWriteObject(String path) {

        //create file
        try {
            File compressedFile = new File(path);
            if (compressedFile.createNewFile()) {
                System.out.println("File created: " + compressedFile.getName());
            } else {
                System.out.println("File already exists.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //write to file
        try {
            FileOutputStream fileOut =
                    new FileOutputStream(path);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(finalOutPut);
            out.close();
            fileOut.close();
        } catch (IOException i) {
            i.printStackTrace();
        }


    }


}
