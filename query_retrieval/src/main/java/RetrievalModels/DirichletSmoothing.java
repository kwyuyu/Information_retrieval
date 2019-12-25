package RetrievalModels;

import index.Index;

public class DirichletSmoothing extends RetrievalModel {

    private double mu;

    public DirichletSmoothing(Index index, double mu) {
        this.index = index;
        this.mu = mu;
        this.collSize = this.index.getCollectionSize();
    }

    @Override
    public double scoring(int tf, int ctf, int docLen) {
        return Math.log((tf + this.mu * (ctf / this.collSize)) / (this.mu + docLen));
    }
}
