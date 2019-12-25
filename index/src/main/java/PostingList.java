
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PostingList {

    private Map<Integer, Posting> postings = new HashMap<>(); // docId: Posting

    /* Constructor */
    public PostingList() {

    }


    /* Methods */
    public void addDocIDPos(int docId, int pos) {
        /*
        * @Func: add a position to a specific document.
        * @Params: docId: document id
        *          pos: position at the document
        * */
        if (!this.postings.containsKey(docId)) {
            this.postings.put(docId, new Posting());
        }

        this.postings.get(docId).addPosition(pos);
    }

    public void addPost(int docId, Posting post) {
        /*
        * @Func: add a Posting object to the specific document.
        * @Params: docId: document id
        *          post: Posting that will be added
        * */
        this.postings.put(docId, post);
    }

    public boolean containsId(int docId) {
        return this.postings.containsKey(docId);
    }




    /* Getter */
    public int getTdf() {
        return this.postings.size();
    }

    public int getDtf(int docId) {
        return this.postings.get(docId).getDtf();
    }

    public Map<Integer, Posting> getPostings() {
        return this.postings;
    }

    public Set<Integer> getAllDocIds() {
        return this.postings.keySet();
    }

    public Posting getOnePosting(int docId) {
        return this.postings.get(docId);
    }




    /* override equals method */
    public boolean equals(PostingList other) {
        Map<Integer, Posting> otherPostings = other.getPostings();

        for (int docId: this.postings.keySet()) {
            if (!this.postings.get(docId).equals(otherPostings.get(docId))) {
                return false;
            }
        }

        return true;
    }
}
