import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by lingyanjiang on 17/1/31.
 */
public class QryIopNear extends QryIop {

    int distance;

    /**
     * Constructor for distance as arg
     * @param distance
     */
    public QryIopNear(int distance) {
        this.distance = distance;
    }

    /**
     * Default Constructor
     */
    public QryIopNear() {

    }

    /**
     * This method is to initiate posting lists
     * @throws IOException
     */
    @Override
    protected void evaluate() throws IOException {
        this.invertedList = new InvList();

        if (args.size() == 0) {
            return;
        }

        // Flag control out most matching loop
        boolean matchFound = false;

        // Find matching Document Id
        while (!matchFound) {
            // Get the docid of the first query argument.
            Qry q_0 = this.args.get(0);
            // If matching not found
            if (!q_0.docIteratorHasMatch(null)) {
                return;
            }
            // Get first matching document
            int docid_0 = q_0.docIteratorGetMatch();
            // Control inner loop of doc position match
            boolean docMatchFound = true;

            for (int i = 1; i < this.args.size(); i++) {
                Qry q_i = this.args.get(i);
                //Iterate till all args matching
                q_i.docIteratorAdvanceTo(docid_0);

                if (!q_i.docIteratorHasMatch(null)) {
                    // If any argument is exhausted
                    return;
                }
                int docid_i = q_i.docIteratorGetMatch();
                if (docid_0 != docid_i) {
                    // If not match try next
                    q_0.docIteratorAdvanceTo(docid_i);
                    matchFound = false;
                    docMatchFound = false;
                    break;
                }
            }

            //Flag control the inner position loop
            boolean canContinue = true;
            // Find matching loc using greedy
            if (docMatchFound) {
                // Record cur locs in positions
                int[] curPos = new int[this.args.size()];
                // Store posting positions iterators in list
                List<Iterator<Integer>> list = new ArrayList<>();
                //Add posting iterators to the list
                for (int i = 0; i < this.args.size(); i++) {
                    list.add(((QryIop) this.args.get(i)).docIteratorGetMatchPosting().positions.iterator());
                }
                //Initiate iterators and positions
                for (int i = 0; i < curPos.length; i++) {
                    if (list.get(i).hasNext()) {
                        curPos[i] = list.get(i).next().intValue();
                    } else {
                        //If exhausted break
                        canContinue = false;
                        break;
                    }
                }
                // Count will serve as term frequency
                int count = 0;
                // Positions will be inserted after iteration
                ArrayList<Integer> positions = new ArrayList<>();

                // Loop until at least one iterate not to the end
                while (canContinue) {
                    int minPos = curPos[0];
                    int minPosInd = 0;
                    int maxPos = curPos[0];
                    boolean flag = true;
                    for (int i = 0; i < list.size(); i++) {
                        //update min and min args index
                        minPos = Math.min(minPos, curPos[i]);
                        maxPos = Math.max(maxPos, curPos[i]);
                        if (minPos == curPos[i]) {
                            minPosInd = i;
                        }
                        if (i > 0) {
                            if (curPos[i] - curPos[i - 1] < 0 || curPos[i] - curPos[i - 1] > distance) {
                                flag = false;
                            }
                        }
                    }
                    if (!flag) {
                        //advance minimum one
                        if (list.get(minPosInd).hasNext()) {
                            curPos[minPosInd] = list.get(minPosInd).next().intValue();
                        } else {
                            //If till the end break the loop
                            canContinue = false;
                        }
                    } else {
                        // If found match advance all locs and add count
                        count++;
                        positions.add(maxPos);
                        for (int i = 0; i < curPos.length; i++) {
                            if (list.get(i).hasNext()) {
                                curPos[i] = list.get(i).next().intValue();
                            } else {
                                //till the end
                                canContinue = false;
                                break;
                            }
                        }
                    }
                }

                //If count not null append to posting lists
                if (count != 0) {
                    this.invertedList.appendPosting(docid_0, positions);
                    matchFound = false;
                }
                //Advance all pointers of term doc id
                advanceAll(docid_0);
            }
        }
    }

    /**
     * This method is to advance all documents
     * @param docId
     */
    private void advanceAll(int docId) {
        for (int i = 0; i < this.args.size(); i++) {
            this.args.get(i).docIteratorAdvancePast(docId);
        }
    }

}
