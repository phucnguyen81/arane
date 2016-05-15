package lou.arane.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.xml.ws.Holder;

/**
 * Create commonly used objects
 *
 * @author LOU
 */
public class New {

    public static <T> Holder<T> holder() {
        return new Holder<>();
    }

    public static BufferedReader reader(InputStream stream) {
        return new BufferedReader(new InputStreamReader(stream, IO.CHARSET));
    }

    public static BufferedReader reader(Path path) {
        try {
            return Files.newBufferedReader(path, IO.CHARSET);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
