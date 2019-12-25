package ClusterUtility.SimilarityMethod;

import ClusterUtility.DocumentVector;

public interface Similarity {

    public double score(DocumentVector dv1, DocumentVector dv2);
}
