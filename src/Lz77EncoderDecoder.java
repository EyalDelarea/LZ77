import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;


public class Lz77EncoderDecoder {

    ArrayList<DictionaryItem> dictionary = new ArrayList<>();
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

    }

    /**
     * @param window function to find the best match for the current window values
     *               using dynamic programming logic
     */
    private void findBestMatch(Byte[] window, int index, DictionaryItem currentBestMatch) {

        if (index < 0) {
            //TODO better exit
            dictionary.add(currentBestMatch);
            pushNewWindowInput(window,currentBestMatch);
            return;
        }
        //if byte value is equal
        if (window[searchBufferSize].equals(window[index])) {
            int length = findCurrentMatchLength(window, index);
            DictionaryItem currentMatch = new DictionaryItem(window[searchBufferSize + length].byteValue(), Math.abs(searchBufferSize - index), length);
            //keep searching for other matches
            index--;
            currentBestMatch = findMaxLengthInMinDist(currentBestMatch, currentMatch);
            findBestMatch(window, index, currentBestMatch);

            //if index is null
        } else if (window[index] == null) {
            //we discovered new char
            if (currentBestMatch.getmatchDistance() == -1) {
                DictionaryItem item = new DictionaryItem(window[searchBufferSize].byteValue(), 0, 0);
                dictionary.add(item);
            } else {
                //add the match we found earlier
                dictionary.add(currentBestMatch);
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
        while ((window[searchBufferIndex + temp].equals(window[index + temp]))
                && (searchBufferSize + temp < window.length)) {
            counter++;
            temp++;
        }
        return counter;
    }


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

        int firstIndex = searchBufferSize;
        int amountOfJumps = item.getLength() + 1;

        // finding the first not null element in window
        for (int i = 0; i < window.length; i++) {
            if (window[i] != null) {
                firstIndex = i;
                break;
            }
        }
        for (int jumps = 0; jumps < amountOfJumps; jumps++) {
            Byte temp = window[window.length - 1];
            if (firstIndex >= 0) {
                for (int i = window.length - 1; i >= firstIndex; i--) {
                    if (i > 0) {
                        Byte temp2 = window[i - 1];
                        window[i - 1] = temp;
                        temp = temp2;
                    }
                }
                if (filePointer >= bytesArray.length) {
                    //read all file already,push -1
                    window[window.length - 1] = -1;
                } else {
                    //push next char to windwo
                    window[window.length - 1] = bytesArray[filePointer];
                    filePointer++;
                }
            }
            if (firstIndex <= 0) {
                firstIndex = 0;
            } else {
                firstIndex--;
            }
        }
    }

    public void deCompress(ArrayList<DictionaryItem> dictionary) {
        while (!dictionary.isEmpty()) {
            System.out.println(dictionary.get(0));
        }
    }


}
