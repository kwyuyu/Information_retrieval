public class Lookup {

    private long offset;
    private int buffLength;
    private int ctf;
    private int tdf;


    /* Constructor */
    public Lookup(long offset, int buffLength, int ctf, int tdf) {
        this.offset = offset;
        this.buffLength = buffLength;
        this.ctf = ctf;
        this.tdf = tdf;
    }


    /* Getter */
    public long getOffset() {
        return this.offset;
    }

    public int getBuffLength() {
        return this.buffLength;
    }

    public int getCtf() {
        return this.ctf;
    }

    public int getTdf() {
        return this.tdf;
    }
}
