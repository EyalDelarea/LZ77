public class DictionaryItem extends basicDictionaryItem {

    private int matchDistance;
    private int length;


    public DictionaryItem() {
        this.matchDistance=-1;
        this.length=0;
    }

    public DictionaryItem(byte value, int matchDistance, int length) {
        this.value = value;
        this.matchDistance = matchDistance;
        this.length = length;
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

    public void setLength(int length) {
        this.length = length;
    }
}
