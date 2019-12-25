package ClusterUtility;

import ClusterUtility.LinkageMethod.AverageGroupLinkage;
import ClusterUtility.LinkageMethod.Linkage;
import index.Index;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Cluster {

    private int clusterId;
    private Index index;
    private Linkage linkage;
    private Map<Integer, DocumentVector> documentVectors;
    private DocumentVector centroid;

    public Cluster(int clusterId, Index index, Linkage linkage) {
        this.clusterId = clusterId;
        this.index = index;
        this.linkage = linkage;
        this.documentVectors = new HashMap<>();
        this.centroid = new DocumentVector();
    }

    public int getClusterId() {
        return this.clusterId;
    }

    public List<Integer> getDocumentIds() {
        return new ArrayList<>(this.documentVectors.keySet());
    }

    public void addDocumentVector(int docId, DocumentVector docVec) {
        this.centroid.multiply(this.documentVectors.size());
        this.centroid.add(docVec);
        this.documentVectors.put(docId, docVec);
        this.centroid.divide(this.documentVectors.size());
    }

    public double score(DocumentVector dv) {
        if (this.linkage instanceof AverageGroupLinkage) {
            Map<Integer, DocumentVector> input = new HashMap<>();
            input.put(0, this.centroid);
            return this.linkage.score(input, dv);
        }
        return this.linkage.score(this.documentVectors, dv);
    }
}
