import java.util.ArrayList;
import java.util.List;

/**
 * Created by lingyanjiang on 17/2/15.
 * This class is sub QrySop class with weights appended
 */
public abstract class QrySopWeighted extends QrySop{

    List<Double> weights = new ArrayList<>();

    public void appendWeights(double weight) {
        weights.add(weight);
    }

}
