package RetrievalModels;

import index.Index;
import index.Posting;
import index.PostingList;

import java.util.*;

public class BM25Model {

    private double k1;
    private double k2;
    private double b;
    private double avgDocLen;
    private int numDocs;
    private Index index;

    public BM25Model(Index index, double k1, double k2, double b) {
        this.k1 = k1;
        this.k2 = k2;
        this.b = b;
        this.index = index;
        this.avgDocLen = this.index.getAverageDocLength();
        this.numDocs = this.index.getDocCount();
    }

    /**
     *
     * @return a list of the top k documents in descending order with respect to scores.
     * key = sceneId, value = score
     * Does document at a time retrieval using the BM25 model
     */
    public List<Map.Entry<String, Double>> retrieveQuery(String query, int k) {
        PriorityQueue<Map.Entry<String, Double>> result = new PriorityQueue<>(Map.Entry.<String, Double>comparingByValue());
        String[] queryTerms = query.split("\\s+");
        Map<String, Integer> qTerms = new HashMap<>();
        List<String> terms = new ArrayList<>();
        List<PostingList> lists = new ArrayList<>();

        // get the postingList for each query term
        for (String term: queryTerms) {
            qTerms.put(term, qTerms.getOrDefault(term, 0) + 1);
            terms.add(term);
            lists.add(this.index.getPostings(term));
        }

        // calculate the score for each doc
        for (int doc = 1; doc <= this.numDocs; doc++) {
            Double curScore = 0.0;
            boolean scored = false;

            // go through each query term
            for (int i = 0; i < lists.size(); i++) {
                PostingList p = lists.get(i);
                String term = terms.get(i);

                p.skipTo(doc);
                Posting post = p.getCurrentPosting();

                if (post != null && post.getDocId() == doc) {
                    int tf = post.getTermFreq();
                    int dtf = this.index.getDocFreq(term);
                    int docLen = this.index.getDocLength(doc);
                    int qtf = qTerms.get(term);
                    curScore += this.scoring(tf, dtf, docLen, qtf);
                    scored = true;
                }
            }

            // do not score not exist doc
            if (scored) {
                result.add(new AbstractMap.SimpleEntry<String, Double>(this.index.getScene(doc), curScore));
            }

            // maintain only k results
            if (result.size() > k) {
                result.poll();
            }
        }

        // reverse the queue
        ArrayList<Map.Entry<String, Double>> scores = new ArrayList<>();
        scores.addAll(result);
        scores.sort(Map.Entry.<String, Double>comparingByValue(Comparator.reverseOrder()));
        return scores;
    }

    public double scoring(int tf, int tdf, int docLen, int qtf) {
        double K = this.k1 * ((1 - this.b) + this.b * (docLen / this.avgDocLen));
        double idfPart = Math.log((this.numDocs - tdf + 0.5) / (tdf + 0.5));
        double docPart = ((this.k1 + 1) * tf) / (K + tf);
        double qPart = ((this.k2 + 1) * qtf) / (this.k2 + qtf);
        return idfPart * docPart * qPart;
    }
}
