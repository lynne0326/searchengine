/**
 * Created by lingyanjiang on 17/2/10.
 */
public class RetrievalModelBM25 extends RetrievalModel {

    double k1;
    double k3;
    double b;

    public RetrievalModelBM25(double k1, double k3, double b) {
        this.k1 = k1;
        this.k3 = k3;
        this.b = b;
    }

    @Override
    public String defaultQrySopName() {
        return "#sum";
    }
}
