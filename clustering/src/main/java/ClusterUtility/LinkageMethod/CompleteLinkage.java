package ClusterUtility.LinkageMethod;

import ClusterUtility.DocumentVector;
import ClusterUtility.SimilarityMethod.Similarity;
import index.Index;

import java.util.Map;

public class CompleteLinkage extends Linkage {

    public CompleteLinkage(Index index, Similarity sim) {
        super(index, sim);
    }

    @Override
    public double score(Map<Integer, DocumentVector> documentVectors, DocumentVector newDocVec) {
        double maxSum = -Double.MAX_VALUE;
        for (DocumentVector docVec: documentVectors.values()) {
            maxSum = Math.max(maxSum, this.similarity.score(docVec, newDocVec));
        }
        return maxSum;
    }
}
