package com.github.alexthe666.iceandfire.fabric.networktest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.AtomicMoveNotSupportedException;

/** Private file coordination only; gameplay assertions use actual native TCP connections. */
public final class NetworkTestFiles {
    private NetworkTestFiles() {}
    public static Path directory() { return Path.of(System.getProperty("iaf.network.dir")); }
    public static void write(String name, String value) {
        try {
            Files.createDirectories(directory());
            Path file = directory().resolve(name), temp = directory().resolve(name + ".tmp");
            Files.writeString(temp, value);
            try { Files.move(temp, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
            catch (AtomicMoveNotSupportedException error) { Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING); }
        } catch (IOException error) { throw new IllegalStateException("Cannot write network test state " + name, error); }
    }
    public static String read(String name) {
        try { Path file = directory().resolve(name); return Files.exists(file) ? Files.readString(file).trim() : ""; }
        catch (IOException error) { throw new IllegalStateException("Cannot read network test state " + name, error); }
    }
}
