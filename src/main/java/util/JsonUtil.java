package util;
//<editor-fold desc="Imports">
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
//</editor-fold>

public class JsonUtil {
    //<editor-fold desc="Fields">
    private static final Gson gson = new Gson();
    //</editor-fold>
    //<editor-fold desc="Public Methods">
    public static <T> T fromJson(BufferedReader reader, Class<T> clazz) throws IOException {
        StringBuilder json = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            json.append(line);
        }

        return gson.fromJson(json.toString(), clazz);
    }

    public static String toJson(Object object) {
        return gson.toJson(object);
    }
    //</editor-fold>
}
