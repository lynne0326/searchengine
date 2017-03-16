/**
 * Created by lingyanjiang on 17/2/14.
 */
public class RetrievalModelIndri extends RetrievalModel{
    double mu;
    double lambda;

    RetrievalModelIndri(double mu, double lambda) {
        this.mu = mu;
        this.lambda = lambda;
    }

    @Override
    public String defaultQrySopName() {
        return "#and";
    }
}
