package org.superbiz.moviefun.blobstore;

import org.apache.tika.Tika;
import org.apache.tika.io.IOUtils;

import java.io.*;
import java.util.Optional;

public class FileStore implements BlobStore {


    @Override
    public void put(Blob blob) throws IOException {
        File newFile = new File(blob.name);
        newFile.delete();
        newFile.getParentFile().mkdirs();
        newFile.createNewFile();

        try (FileOutputStream outputStream = new FileOutputStream(newFile)) {
            outputStream.write(IOUtils.toByteArray(blob.inputStream));
        }
    }

    @Override
    public Optional<Blob> get(String name) throws IOException {
        File file = new File(name);
        Blob blob = new Blob(name, new FileInputStream(file), new Tika().detect(name));

        return Optional.of(blob);
    }

    @Override
    public void deleteAll() {

    }
}
