import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;


public class Lz77EncoderDecoder {

    public final int windowSize = 16;
    public final int searchBufferSize = 8;
    ArrayList<DictionaryItem> dictionary = new ArrayList<>();

    public void CompressLz(String input_path) {

        BufferedReader buffer = null;
        try {
            buffer = new BufferedReader(new FileReader(input_path));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Byte[] window = new Byte[windowSize];

        initWindow(window, buffer);
        /**
         * look ahead buffer condition !=null
         */
        while (window[searchBufferSize] != null) {
            //for (int i = searchBufferSize - 1; i >= 0; i--)
            int i = searchBufferSize - 1;
            do {
                DictionaryItem currentBestMatch = new DictionaryItem();
                findBestMatch(window, buffer, searchBufferSize - 1, currentBestMatch);
            }
            while (window[i] != null);

        }
    }

    /**
     * @param window function to find the best match for the current window values
     *               using dynamic programming logic
     */
    private void findBestMatch(Byte[] window, BufferedReader buffer, int index, DictionaryItem currentBestMatch) {
        if (window[searchBufferSize].equals(window[index])) {

            int length = findCurrentMatchLength(window, window[index], index);
            DictionaryItem item = new DictionaryItem(window[searchBufferSize + length].byteValue(), Math.abs(searchBufferSize - index), length);
            index--;
            currentBestMatch = findMaxLengthInMinDist(currentBestMatch, item);
            findBestMatch(window, buffer, index, currentBestMatch);
        } else if (window[index] == null) {
            if (currentBestMatch.getmatchDistance() == -1) {
                DictionaryItem item = new DictionaryItem(window[searchBufferSize].byteValue(), 0, 0);
                dictionary.add(item);
            } else
                dictionary.add(currentBestMatch);
            pushNewWindowInput(window, buffer, currentBestMatch);
            return;
        } else {
            index--;
            findBestMatch(window, buffer, index, currentBestMatch);
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
     * @param aByte  the current byte we check
     * @param index  the byte index for the current match
     * @return the matches byte sequence
     */
    private int findCurrentMatchLength(Byte[] window, Byte aByte, int index) {
        int length = 1;
        for (int i = searchBufferSize + 1; i < window.length; i++) {
            for (int j = index + 1; j < window.length; j++) {
                if (window[i].equals(window[j])) {
                    length++;
                } else
                    return length;
            }
        }
        return length;
    }

    private void initWindow(Byte[] window, BufferedReader buffer) {
        for (int i = 0; i < searchBufferSize; i++) {
            window[i] = null;
        }
        for (int i = searchBufferSize; i < window.length; i++) {
            try {
                window[i] = Byte.valueOf((byte) buffer.read());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     *
     * @param window bytes window
     * @param buffer input buffer
     * @param item coded item to jump the amount of the length.
     *
     */
    private void pushNewWindowInput(Byte[] window, BufferedReader buffer, DictionaryItem item) {
        int firstIndex = searchBufferSize;
        Byte temp = window[window.length-1];

        /**
         finding the first not null element in window
         */
        for (int i = 0; i < window.length; i++) {
            if (window[i] != null) {
                firstIndex = i;
                break;
            }
        }

        for (int i = window.length-1; i >= firstIndex - (item.getLength() + 1); i--) {
            if (i > 0) {
                //
                Byte temp2 =window[i-1];
                //push
                window[i - 1] = temp;
                temp = temp2;
            }
        }
        //read a new byte from the bufferReader
        try {
            window[window.length - 1] = Byte.valueOf((byte) buffer.read());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deCompress(ArrayList<DictionaryItem> dictionary) {
        while (!dictionary.isEmpty()) {
            System.out.println(dictionary.get(0));
        }
    }


}
