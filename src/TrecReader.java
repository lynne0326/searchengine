import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lingyanjiang on 17/3/14.
 */
public class TrecReader {

    /**
     * This method read trec_eval output file and extract top k docs
     * @param filePath
     * @param k
     * @return
     */
    public static Map<String, ScoreList> readPartialRes(String filePath, int k) {
        Map<String, ScoreList> map = new HashMap<>();
        //Extract top k result
        try(BufferedReader bufIn = new BufferedReader(new FileReader(new File(filePath)))) {
            String line = null;
            while ((line = bufIn.readLine()) != null && k > 0) {
                String [] splited = line.split("\\s+");
                //Invalid output
                if (splited.length < 5) continue;
                String externalId = splited[2].trim();

                String query = splited[0].trim();
                if (!map.containsKey(query)) map.put(query, new ScoreList());

                int docId = Idx.getInternalDocid(externalId);
                double score = Double.parseDouble(splited[4].trim());
                map.get(query).add(docId, score);
                k--;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }
}
