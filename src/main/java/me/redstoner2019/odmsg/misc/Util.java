package me.redstoner2019.odmsg.misc;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;

public class Util {
    public static void writeStringToFile(String str, File file){
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);
            byte[] strToBytes = str.getBytes();
            outputStream.write(strToBytes);

            outputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static void writeJsonToFile(JSONObject o, File file) {
        writeStringToFile(prettyJSON(o.toString()),file);
    }
    public static String readFile(File path) {
        byte[] encoded = null;
        try {
            encoded = Files.readAllBytes(path.toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new String(encoded, Charset.defaultCharset());
    }
    public static String prettyJSON(String uglyJsonString) {
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            Object jsonObject = objectMapper.readValue(uglyJsonString, Object.class);
            String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObject);
            return prettyJson;
        }catch (Exception e){
            return null;
        }
    }
    public static String stacktraceToString(StackTraceElement[] ste){
        String s = "";
        for(StackTraceElement st : ste) s+= st.toString();
        return s;
    }
}
