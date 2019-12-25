import java.util.ArrayList;
import java.util.List;

public class Posting {

    private List<Integer> positions = new ArrayList<>();


    /* Constructor */
    public Posting() {

    }

    public Posting(List<Integer> positions) {
        this.positions.addAll(positions);
    }


    /* Methods */
    public void addPosition(int pos) {
        /*
        * @Func: add a position in the current document.
        * @Params: pos: the position in the document.
        * */
        this.positions.add(pos);
    }



    /* Getter */
    public int getDtf() {
        return this.positions.size();
    }

    public List<Integer> getPositions() {
        return this.positions;
    }


    /* override equals method */
    public boolean equals(Posting other) {
        if (this.positions.size() != other.getPositions().size()) {
            return false;
        }

        for (int i = 0; i < this.positions.size(); i++) {
            if (!this.positions.get(i).equals(other.getPositions().get(i))) {
                return false;
            }
        }

        return true;
    }
}
