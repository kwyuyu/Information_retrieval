package RetrievalModels;

import index.Index;
import index.Posting;
import index.PostingList;

import java.util.*;

public abstract class RetrievalModel {

    Index index;
    double collSize;

    /**
     * @param tf: term frequency
     * @param ctf: collection term frequency
     * @param docLen: document length
     * @return score for the given model
     */
    public abstract double scoring(int tf, int ctf, int docLen);

    /**
     *
     * @param query: query
     * @param k: return top k result
     * @return a list of the top k documents in descending order with respect to scores
     */
    public List<Map.Entry<String, Double>> retrieveQuery(String query, int k) {
        PriorityQueue<Map.Entry<String, Double>> result = new PriorityQueue<>(Map.Entry.<String, Double>comparingByValue());
        String[] queryTerms = query.split("\\s+");
        PostingList[] lists = new PostingList[queryTerms.length];

        // get the postingList for each query term
        for (int i = 0; i < queryTerms.length; i++) {
            lists[i] = this.index.getPostings(queryTerms[i]);
        }

        // calculate the score for each doc
        for (int doc = 1; doc <= this.index.getDocCount(); doc++) {
            Double curScore = 0.0;

            // go through each query term
            for (int i = 0; i < lists.length; i++) {
                PostingList p = lists[i];

                p.skipTo(doc);
                Posting post = p.getCurrentPosting();

                int tf = (post != null && post.getDocId() == doc ? post.getTermFreq() : 0);
                int docLen = this.index.getDocLength(doc);
                int ctf = this.index.getTermFreq(queryTerms[i]);

                curScore += this.scoring(tf, ctf, docLen);
            }

            result.add(new AbstractMap.SimpleEntry<String, Double>(this.index.getScene(doc), curScore));

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
}
