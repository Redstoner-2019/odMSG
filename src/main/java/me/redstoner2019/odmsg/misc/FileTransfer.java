package me.redstoner2019.odmsg.misc;

import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class FileTransfer {
    private static int SIZE = 8192;
    public static void downloadFile(String savePath, Socket socket){
        System.out.println("Downloading");

        File file = new File(savePath);
        if(!file.exists()) {
            file.getParentFile().mkdirs();
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            InputStream inputStream = socket.getInputStream();

            FileOutputStream fileoutputStream = new FileOutputStream(file);

            byte[] data = inputStream.readNBytes(SIZE);

            while (data.length > 0) {
                fileoutputStream.write(data);
                data = inputStream.readNBytes(Math.min(inputStream.available(),SIZE));
            }

            fileoutputStream.close();
            System.out.println("Done");

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static void uploadFile(File file, Socket socket, String type){
        JSONObject object = new JSONObject();

        List<byte[]> data = new ArrayList<>();
        long bytesToRead = 0;

        try {
            FileInputStream inputStream = new FileInputStream(file);
            byte[] bytes = inputStream.readNBytes(SIZE);

            while (bytes.length > 0) {
                data.add(bytes);
                bytesToRead+=bytes.length;
                bytes = inputStream.readNBytes(SIZE);
            }

            JSONObject dataObject = new JSONObject();


            dataObject.put("file-size",bytesToRead);
            dataObject.put("filename",file.getName());
            dataObject.put("type",type);

            object.put("header","file-upload");
            object.put("data",dataObject);

            System.out.println(object.toString());

            OutputStream outputStream = socket.getOutputStream();

            new ObjectOutputStream(outputStream).writeObject(object.toString());

            for(byte[] byteData : data){
                outputStream.write(byteData);
            }


        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
