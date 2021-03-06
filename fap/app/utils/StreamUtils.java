package utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataHandler;
import javax.activation.DataSource;

import org.apache.cxf.jaxrs.ext.multipart.InputStreamDataSource;

public class StreamUtils {

	public static byte[] is2byteArray(InputStream is) throws IOException{
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		is2os(is, os);
		return os.toByteArray();
	}
	
	public static void is2os(InputStream is, OutputStream os) throws IOException{
        byte[] buffer = new byte[8092];
        int count = 0;
        while ((count = is.read(buffer)) > 0) {
           os.write(buffer, 0, count);
        }
        is.close();
	}
	
    public static DataHandler getDataHandler(InputStream inputStream, String mimetype) {
        DataSource dataSource = new InputStreamDataSource(inputStream, mimetype);
        DataHandler dataHandler = new DataHandler(dataSource);
        return dataHandler;
    }
}
