package org.superbiz.moviefun.albums;

import org.apache.tika.io.IOUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.superbiz.moviefun.blobstore.Blob;
import org.superbiz.moviefun.blobstore.BlobStore;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;
import static org.springframework.util.MimeTypeUtils.IMAGE_JPEG_VALUE;

@Controller
@RequestMapping("/albums")
public class AlbumsController {

    private final AlbumsBean albumsBean;

    private final BlobStore blobStore;

    public AlbumsController(AlbumsBean albumsBean, BlobStore blobStore) {
        this.albumsBean = albumsBean;
        this.blobStore = blobStore;
    }


    @GetMapping
    public String index(Map<String, Object> model) {
        model.put("albums", albumsBean.getAlbums());
        return "albums";
    }

    @GetMapping("/{albumId}")
    public String details(@PathVariable long albumId, Map<String, Object> model) {
        model.put("album", albumsBean.find(albumId));
        return "albumDetails";
    }

    @PostMapping("/{albumId}/cover")
    public String uploadCover(@PathVariable long albumId, @RequestParam("file") MultipartFile uploadedFile) throws IOException {
           Blob blob = new Blob(
                   getCoverFile(albumId),
                   uploadedFile.getInputStream(),
                   uploadedFile.getContentType());
            blobStore.put(blob);

        return format("redirect:/albums/%d", albumId);
    }

    @GetMapping("/{albumId}/cover")
    public HttpEntity<byte[]> getCover(@PathVariable long albumId) throws IOException {
        Optional<Blob> optionalBlob = blobStore.get(getCoverFile(albumId));
        Blob blob = optionalBlob.orElseGet(this::getDefaultCover);
        byte[] imageBytes = IOUtils.toByteArray(blob.inputStream);
        HttpHeaders headers = createImageHttpHeaders(blob.contentType, imageBytes);
        return new HttpEntity<>(imageBytes, headers);
    }

    @DeleteMapping("/covers")
    public String deleteCovers() {
        blobStore.deleteAll();
        return "redirect:/albums";
    }


    private Blob getDefaultCover() {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream input = classLoader.getResourceAsStream("default-cover.jpg");

        return new Blob("default-cover", input, IMAGE_JPEG_VALUE);
    }

    private HttpHeaders createImageHttpHeaders(String contentType, byte[] imageBytes) throws IOException {
//        String contentType = new Tika().detect(coverFilePath);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentLength(imageBytes.length);
        return headers;
    }

    private String getCoverFile(@PathVariable long albumId) {
        String coverFileName = format("covers/%d", albumId);
        return coverFileName;
    }
}
