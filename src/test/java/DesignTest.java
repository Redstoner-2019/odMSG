import me.redstoner2019.odmsg.misc.Util;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

public class DesignTest {
    public static void main(String[] args) throws IOException {
        for (int i = 1; i < 2; i++) {
            System.out.println(i);
        }
        /*JSONObject data = new JSONObject();
        File saveLocation = new File("save.json");

        JSONObject segmentData = new JSONObject();

        JSONObject segment = new JSONObject();

        segment.put("min",0);
        segment.put("max",9);

        segmentData.put("1",segment);

        segment = new JSONObject();

        segment.put("min",10);
        segment.put("max",19);

        segmentData.put("2",segment);

        data.put("segment-data",segmentData);

        if(!saveLocation.exists()) saveLocation.createNewFile();
        Util.writeJsonToFile(data,saveLocation);*/
    }
}
