package RetrievalModels;

public class DirichletSmoothing implements RetrievalModel {

    private double mu;
    private double collSize;

    public DirichletSmoothing(double collSize, double mu) {
        this.mu = mu;
        this.collSize = collSize;
    }

    @Override
    public double scoring(int tf, int ctf, int docLen) {
        return Math.log((tf + this.mu * (ctf / this.collSize)) / (this.mu + docLen));
    }
}
