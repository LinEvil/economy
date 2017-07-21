package com.mengcraft.economy.lib;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;

/**
 * Created on 17-6-26.
 */
@Data
@EqualsAndHashCode(of = {"repository", "group", "artifact", "version"})
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class MavenLibrary extends Library {

    private final String repository;
    private final String group;
    private final String artifact;
    private final String version;
    private File file;
    private List<Library> sublist;

    @Override
    public String toString() {
        return ("MavenLibrary(repository='" + repository + '\'' +
                ", group='" + group + '\'' +
                ", artifact='" + artifact + '\'' +
                ", version='" + version + '\'' +
                ")");
    }

    @Override
    public File getFile() {
        if (file == null) {
            file = new File("lib", group.replace(".", File.separator)
                    + File.separator
                    + artifact
                    + File.separator
                    + artifact + '-' + version + ".jar");
        }
        return file;
    }

    @SneakyThrows
    @Override
    public List<Library> getSublist() {
        if (sublist == null) {
            File xml = new File(getFile().getParentFile(), getFile().getName() + ".pom");
            Node pom = XMLHelper.getDocumentBy(xml).getFirstChild();

            Element all = XMLHelper.getElementBy(pom, "dependencies");
            if (all == null) return (sublist = ImmutableList.of());

            Element p = XMLHelper.getElementBy(pom, "properties");

            Builder<Library> b = ImmutableList.builder();

            for (Element depend : XMLHelper.getElementListBy(all, "dependency")) {
                String scope = XMLHelper.getElementValue(depend, "scope");
                if (scope == null || scope.equals("compile")) {
                    String version = XMLHelper.getElementValue(depend, "version");
                    if (version == null) throw new NullPointerException();
                    // TODO
                    if (version.startsWith("${")) {
                        val sub = version.substring(2, version.length() - 1);
                        version = XMLHelper.getElementValue(p, sub);
                    }
                    b.add(new MavenLibrary(repository,
                            XMLHelper.getElementValue(depend, "groupId"),
                            XMLHelper.getElementValue(depend, "artifactId"),
                            version
                    ));
                }
            }
            sublist = b.build();
        }
        return sublist;
    }

    @SneakyThrows
    public void init() {
        if (!(getFile().getParentFile().isDirectory() || getFile().getParentFile().mkdirs())) {
            throw new IOException("mkdir");
        }

        val url = repository
                + '/'
                + group.replace('.', '/')
                + '/'
                + artifact
                + '/'
                + version
                + '/'
                + artifact + '-' + version;

        Files.copy(new URL(url + ".jar").openStream(),
                getFile().toPath(),
                StandardCopyOption.REPLACE_EXISTING);

        Files.copy(new URL(url + ".jar.md5").openStream(),
                new File(getFile().getParentFile(), getFile().getName() + ".md5").toPath(),
                StandardCopyOption.REPLACE_EXISTING);

        Files.copy(new URL(url + ".pom").openStream(),
                new File(getFile().getParentFile(), getFile().getName() + ".pom").toPath(),
                StandardCopyOption.REPLACE_EXISTING);
    }

    @SneakyThrows
    public boolean isLoadable() {
        if (getFile().isFile()) {
            val check = new File(file.getParentFile(), file.getName() + ".md5");
            if (check.isFile()) {
                val buf = ByteBuffer.allocate(1 << 16);
                FileChannel channel = FileChannel.open(file.toPath());
                while (!(channel.read(buf) == -1)) {
                    buf.flip();
                    MD5.update(buf);
                    buf.compact();
                }
                return Files.newBufferedReader(check.toPath()).readLine().equals(MD5.digest());
            }
        }
        return false;
    }

    public enum Repository {

        CENTRAL("http://central.maven.org/maven2");

        private final String repository;

        Repository(String repository) {
            this.repository = repository;
        }
    }

    public static MavenLibrary of(String description) {
        return of(Repository.CENTRAL.repository, description);
    }

    public static MavenLibrary of(String repository, String description) {
        val split = description.split(":");
        if (!(split.length == 3)) throw new IllegalArgumentException(description);
        val itr = Arrays.asList(split).iterator();
        return new MavenLibrary(repository, itr.next(), itr.next(), itr.next());
    }

}
