public class DictionaryItem {

    private int matchDistance;
    private int length;
    private byte value;
    private boolean basic;
    private static final int MIN_MATCH_LENGTH = 3;
    private static final int MAX_MATCH_LENGTH = 258;


    public DictionaryItem() {
        this.matchDistance = -1;
        this.length = 0;
    }

    public DictionaryItem(byte value, int matchDistance, int length,boolean basic) {
        this.value = value;
        this.matchDistance = matchDistance;
        this.length = length;
        this.basic=basic;
    }

    public boolean isBasic() {
        return this.basic = (this.length < MIN_MATCH_LENGTH || this.length > MAX_MATCH_LENGTH);
    }

    public void setBasic(boolean bool) {
        this.basic = bool;
    }

    public boolean getBasic() {
        return this.basic;
    }


    public int getmatchDistance() {
        return matchDistance;
    }

    public void setmatchDistance(int matchDistance) {
        this.matchDistance = matchDistance;
    }

    public int getLength() {
        return length;
    }

    public byte getValue() {
        return value;
    }

    public void setLength(int length) {
        this.length = length;
    }


}

