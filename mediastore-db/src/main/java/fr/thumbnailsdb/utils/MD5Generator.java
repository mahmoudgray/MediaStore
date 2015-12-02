package fr.thumbnailsdb.utils;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.*;

/**
 * Created by mohannad on 02/12/15.
 */
public class MD5Generator {

    public String generateMD5(File f) throws IOException {
        InputStream fis = new BufferedInputStream(new FileInputStream(f));
        String s = this.generateMD5(fis);
        fis.close();
        return s;
    }
    public String generateMD5(InputStream fi) throws IOException {
        byte[] buffer = DigestUtils.md5(fi);
        String s = DigestUtils.md5Hex(buffer);
        return s;
    }

}
