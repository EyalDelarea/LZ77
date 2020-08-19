import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;


public class Lz77EncoderDecoder {

    private static final int MIN_MATCH_LENGTH = 3;
    private static final int MAX_MATCH_LENGTH = 258;
    ArrayList<basicDictionaryItem> dictionary = new ArrayList<>();
    public final int windowSize = 16;
    public final int searchBufferSize = 8;
    static int filePointer = 0;
    static byte[] bytesArray;

    public void CompressLz(String input_path) {

        //read all bytes from file
        File sa = new File(input_path);
        try {
            bytesArray = Files.readAllBytes(sa.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        Byte[] window = new Byte[windowSize];

        initWindow(window);
        /**
         * look ahead buffer condition !=null
         */
        do {
            DictionaryItem currentBestMatch = new DictionaryItem();
            findBestMatch(window, searchBufferSize - 1, currentBestMatch);
        }
        //meaning index 8 is -1
        while (window[searchBufferSize] != -1);


        printDic(dictionary);

    }

    /**
     * @param window function to find the best match for the current window values
     *               using dynamic programming logic
     */
    private void findBestMatch(Byte[] window, int index, DictionaryItem currentBestMatch) {

        if (index < 0) {
            int length = currentBestMatch.getLength();
            boolean min = length < MIN_MATCH_LENGTH;
            boolean max = length > MAX_MATCH_LENGTH;


            if (min || max) { //basic object
                basicDictionaryItem basic = new basicDictionaryItem(currentBestMatch.getValue());
                dictionary.add(basic);
                //TODO push basic
             //   pushNewWindowInput(window, basic);
            } else { //not basic
                dictionary.add(currentBestMatch);
                pushNewWindowInput(window, currentBestMatch);
            }


            return;
        }
        //if byte value is equal
        if (window[searchBufferSize].equals(window[index])) {
            int length = findCurrentMatchLength(window, index);
            DictionaryItem currentMatch = new DictionaryItem(window[searchBufferSize + length], Math.abs(searchBufferSize - index), length);
            //keep searching for other matches
            index--;
            currentBestMatch = findMaxLengthInMinDist(currentBestMatch, currentMatch);
            findBestMatch(window, index, currentBestMatch);

            //if index value is null
        } else if (window[index] == null) {

            if (currentBestMatch.getmatchDistance() == -1) { //we discovered new char
                basicDictionaryItem basicCurrentMatch = new basicDictionaryItem(window[searchBufferSize + currentBestMatch.getLength()]);
                dictionary.add(basicCurrentMatch);
            } else {
                //add the match we found earlier
                //but check if it hold the conditions
                if (currentBestMatch.getLength() < MIN_MATCH_LENGTH ||
                        currentBestMatch.getLength() > MAX_MATCH_LENGTH) {
                    basicDictionaryItem basicCurrentMatch = new basicDictionaryItem(window[searchBufferSize]);
                    dictionary.add(basicCurrentMatch);
                } else {
                    dictionary.add(currentBestMatch);
                }

            }
            pushNewWindowInput(window, currentBestMatch);
            return;

            //no match found reducing index to keep search for match
            //in search buffer
        } else {
            index--;
            findBestMatch(window, index, currentBestMatch);
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
     * @param window the current window we will look on
     * @param index  the byte index for the current match
     * @return the matches byte sequence
     */
    private int findCurrentMatchLength(Byte[] window, int index) {
        int searchBufferIndex = searchBufferSize + 1;
        int counter = 1;
        int temp = 0;
        index++;
        //advance two pointer until there's no match between the chars
        while ((searchBufferSize + temp < window.length) && (searchBufferIndex + temp < window.length)
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
     * @param window byte[] array
     */
    private void initWindow(Byte[] window) {
        for (int i = searchBufferSize; i < window.length; i++) {
            window[i] = bytesArray[filePointer];
            filePointer++;
        }
    }


    /**
     * @param window bytes window
     * @param item   coded item to jump the amount of the length.
     */
    private void pushNewWindowInput(Byte[] window, DictionaryItem item) {
//TODO fix basic window push
        int length = item.getLength();
        boolean min = length < MIN_MATCH_LENGTH;
        boolean max = length > MAX_MATCH_LENGTH;
        int firstIndex = searchBufferSize;
        int amountOfJumps;

        // finding the first not null element in window
        for (int i = 0; i < window.length; i++) {
            if (window[i] != null) {
                firstIndex = i;
                break;
            }
        }

        if (min || max) {
            amountOfJumps = 1;
        }else{
            amountOfJumps = item.getLength() + 1;
        }

        //loop how many jumps
        for (int jumps = 0; jumps < amountOfJumps; jumps++) {

        }
    }


    public void deCompress(ArrayList<DictionaryItem> dictionary) {
        while (!dictionary.isEmpty()) {
            System.out.println(dictionary.get(0));
        }
    }

    public static void printDic(ArrayList<basicDictionaryItem> dictionary) {
        //print dictionary
        for (int i = 0; i < dictionary.size(); i++) {
            if (dictionary.get(i) instanceof DictionaryItem) {
                System.out.print("<" + (char) (dictionary.get(i).getValue()) +
                        "" + "," + ((DictionaryItem) dictionary.get(i)).getLength() + ","
                        + ((DictionaryItem) dictionary.get(i)).getmatchDistance() + ">");
            } else {
                System.out.print((char) (dictionary.get(i).getValue()));
            }
        }
    }
}
