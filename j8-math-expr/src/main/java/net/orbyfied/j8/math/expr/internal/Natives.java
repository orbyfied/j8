package net.orbyfied.j8.math.expr.internal;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Locale;

public class Natives {

    private static final HashSet<String> loaded = new HashSet<>();

    public static void loadNativeFromResource(Class<?> ref, String name) {
        if (loaded.contains(name))
            return;

        try {
            // compile file name
            String arch;
            String pa = System.getProperty("os.arch").toLowerCase(Locale.ROOT);
            if (pa.contains("64")) arch = "x64";
            else arch = "x32";
            String os;
            String po = System.getProperty("os.name").toLowerCase(Locale.ROOT);
            if (po.contains("win")) os = "win";
            else if (po.contains("linux")) os = "linux";
            else if (po.contains("mac")) os = "mac";
            else throw new UnsupportedOperationException("unsupported OS");
            String fn = name + "-" + arch + "-" + os + ".so";
            String resName = "/natives/" + fn;

            // check for file
            final Path tempFile = Path.of(System.getProperty("user.home") + "/tmp/j-natives/" + fn);
            if (Files.exists(tempFile)) {
                System.load(tempFile.toAbsolutePath().toString());
                return;
            } else {
                Files.createDirectories(tempFile.getParent());
                Files.createFile(tempFile);
            }

            // extract into temporary file
            InputStream inputStream  = ref.getResourceAsStream(resName);
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
