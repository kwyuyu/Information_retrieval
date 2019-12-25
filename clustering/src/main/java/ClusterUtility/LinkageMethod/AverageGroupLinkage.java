package ClusterUtility.LinkageMethod;

import ClusterUtility.DocumentVector;
import ClusterUtility.SimilarityMethod.Similarity;
import index.Index;

import java.util.Map;

public class AverageGroupLinkage extends Linkage {

    public AverageGroupLinkage(Index index, Similarity sim) {
        super(index, sim);
    }

    @Override
    public double score(Map<Integer, DocumentVector> documentVectors, DocumentVector newDocVec) {
        return this.similarity.score(documentVectors.get(0), newDocVec);
    }
}
