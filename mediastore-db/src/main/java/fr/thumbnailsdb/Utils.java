package fr.thumbnailsdb;

import fr.thumbnailsdb.utils.FileCounter;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    private static final String IMAGE_PATTERN = "(.+(\\.(?i)(jpg|jpeg|JPG|JPEG|png|gif|bmp))$)";
    private static final String VIDEO_PATTERN = "(.+(\\.(?i)(avi|flv|mpg|mp4|wmv))$)";

    private static Pattern imagePattern = Pattern.compile(IMAGE_PATTERN);
    private static Matcher imageMatcher;
    private static Pattern videoPattern = Pattern.compile(VIDEO_PATTERN);
    private static Matcher videoMatcher;


    /**
     * Validate image with regular expression
     *
     * @param image image for validation
     * @return true valid image, false invalid image
     */
    public static boolean isValideImageName(final String image) {
        imageMatcher = imagePattern.matcher(image);
        return imageMatcher.matches();
    }


    public static int countFilesInFolder(String root) {
        FileCounter fileProcessor = new FileCounter();
        try {
            Files.walkFileTree(Paths.get(root), fileProcessor);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileProcessor.getTotal();
    }

    public static boolean isValideFile(File fd) {
        return fd.isFile();
    }

    public static boolean isValideVideoName(final String video) {
        videoMatcher = videoPattern.matcher(video);
        return videoMatcher.matches();
    }

    public static ByteArrayInputStream readFileToMemory(File f) {
        try {
            return new ByteArrayInputStream(FileUtils.readFileToByteArray(f));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String fileToDirectory(String n) {
        if (n == null) return null;

        int folderIndex = n.lastIndexOf('/');
        if (folderIndex < 0) {
            //it's probably a windows path
            folderIndex = n.lastIndexOf('\\');
        }
        if (folderIndex == -1)  {
            //something went wrong
            System.err.println("Utils.fileToDirectory ERROR for path " + n);
            folderIndex=0;
        }
        return n.substring(0, folderIndex);//file.getParent(); // to get the parent dir name
    }


    public static int folderSize(String folder) {

        String[] folders = new File(folder).list();
        if (folders != null) {
            int total = 0;
            for (String s : folders) {
                if (isValideImageName(s) || isValideVideoName(s)) {
                    total++;
                }
            }
            return total;
        } else {
            return 0;
        }
    }

    public static int[] toIntArray(byte[] d) {
        int[] idata = null;
        if (d != null) {
            ObjectInputStream oi = null;
            try {
                oi = new ObjectInputStream(new ByteArrayInputStream(d));
                idata = (int[]) oi.readObject();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return idata;
    }

    public static byte[] toByteArray(int[] data) {
        ByteArrayOutputStream ba = new ByteArrayOutputStream();
        ObjectOutputStream oi = null;
        try {
            oi = new ObjectOutputStream(ba);
            oi.writeObject(data);
            oi.close();
            return ba.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static String byteArrayToBase64Img(byte[] data) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BufferedImage dest = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
//
        //we check we have a correct number of entries in the array, to avoid NPE
        int[] tab = toIntArray(data);
        //TODO : check if we really need to build an image
        // or we can simply use the int[]
        if (tab != null) {
            dest.setRGB(0, 0, 10, 10, tab, 0, 10);
            try {
                ImageIO.write(dest, "JPEG", out);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return Base64.encodeBase64String(out.toByteArray());
        } else {
            return null;
        }
    }

    public static int[] base64ImgToIntArray(String base) {
        byte[] tmp = Base64.decodeBase64(base);

        int[] target = new int[10 * 10];
        ByteArrayInputStream bi = new ByteArrayInputStream(tmp);

        try {
            BufferedImage image = ImageIO.read(bi);
            if (image != null) {
                image.getRGB(0, 0, 10, 10, target, 0, 10);
            }    else {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return target;
    }


}
