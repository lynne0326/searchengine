/**
 * Created by lingyanjiang on 17/1/29.
 */
public class RetrievalModelRankedBoolean extends RetrievalModel {
    @Override
    public String defaultQrySopName () {
        return new String ("#or");
    }
}
