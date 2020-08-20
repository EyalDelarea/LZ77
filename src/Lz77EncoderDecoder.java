import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;


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
    ArrayList<DictionaryItem> dictionary = new ArrayList<>();

    public Lz77EncoderDecoder(int searchBufferSize, int windowSize) {
        this.windowSize = windowSize;
        this.searchBufferSize = searchBufferSize;
        this.searchBufferIndex = windowSize - searchBufferSize;
        notNullIndex = searchBufferIndex;
    }

    public void CompressLz(String input_path) {

        //read all bytes from file
        File sa = new File(input_path);
        try {
            bytesArray = Files.readAllBytes(sa.toPath());
        } catch (IOException e) {
            e.printStackTrace();
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


        printDic(dictionary);

    }

    /**
     * Searching for the longest match in the window , using recursion
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
                basicPush(window);
            } else {
                //not basic
                dictionary.add(currentBestMatch);
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
                basicPush(window);
            } else {
                //add the match we found earlier
                //but check if it hold the conditions
                boolean shortLengthObject = isShortObject(currentBestMatch);
                if (shortLengthObject) {
                    DictionaryItem basicCurrentMatch = new DictionaryItem(window[searchBufferIndex], 0, 0, true);
                    dictionary.add(basicCurrentMatch);
                    basicPush(window);
                } else {
                    dictionary.add(currentBestMatch);
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

        for (int i = (searchBufferIndex); i < window.length; i++) {
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


    public static void printDic(ArrayList<DictionaryItem> dictionary) {
        //print dictionary
        for (int i = 0; i < dictionary.size(); i++) {
            if (!dictionary.get(i).isBasic()) {
                System.out.print("<" + (char) (dictionary.get(i).getValue()) +
                        "" + "," + (dictionary.get(i)).getLength() + ","
                        + (dictionary.get(i)).getmatchDistance() + ">");
            } else {
                System.out.print((char) (dictionary.get(i).getValue()));
            }
        }
    }
}
