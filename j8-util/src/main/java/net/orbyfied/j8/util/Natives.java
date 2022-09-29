package net.orbyfied.j8.util;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Locale;

public class Natives {

    private static final HashSet<String> loaded = new HashSet<>();

    public static void loadNativeFromResource(Class<?> ref, String name, String version, boolean depOs) {
        if (loaded.contains(name))
            return;

        try {
            // compile name
            String arch = System.getProperty("os.arch");
            String os   = "";
            if (depOs) {
                os = "-" + System.getProperty("os.name");
            }
            String ext;
            String osn = System.getProperty("os.name").toLowerCase(Locale.ROOT);
            if (osn.contains("windows"))
                ext = ".dll";
            else if (osn.contains("mac"))
                ext = ".dylib";
            else
                ext = ".so";
            String fn = name + "-" + version + "-" + arch + os + ext;
            String resName = "/natives/" + fn;

            // check for file
            final Path tempFile = Path.of(System.getProperty("user.home") + "/tmp/j-natives/" + name + "/" + version + "/" + fn);
            if (Files.exists(tempFile)) {
                System.load(tempFile.toAbsolutePath().toString());
                return;
            } else {
                Files.createDirectories(tempFile.getParent());
                Files.createFile(tempFile);
            }

            // extract into temporary file
            InputStream  inputStream  = ref.getResourceAsStream(resName);
            if (inputStream == null)
                throw new IllegalArgumentException("Could not find resource '" + resName + "'");
            OutputStream outputStream = Files.newOutputStream(tempFile);
            inputStream.transferTo(outputStream);
            inputStream.close();
            outputStream.close();

            // load libary
            System.load(tempFile.toAbsolutePath().toString());
            loaded.add(name);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
