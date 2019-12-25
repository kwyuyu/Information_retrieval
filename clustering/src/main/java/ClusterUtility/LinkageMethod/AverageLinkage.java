package ClusterUtility.LinkageMethod;

import ClusterUtility.DocumentVector;
import ClusterUtility.SimilarityMethod.Similarity;
import index.Index;

import java.util.Map;

public class AverageLinkage extends Linkage {

    public AverageLinkage(Index index, Similarity sim) {
        super(index, sim);
    }

    @Override
    public double score(Map<Integer, DocumentVector> documentVectors, DocumentVector newDocVec) {
        double res = 0.0;
        for (DocumentVector docVec: documentVectors.values()) {
            res += this.similarity.score(docVec, newDocVec);
        }
        return res / documentVectors.size();
    }
}
