package me.xkuyax.hdfilmetv.download;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@RequiredArgsConstructor
@Getter
public class CacheDownloadHandler {

    private final CloseableHttpClient httpClient;
    private final BaseFileSupplier fileSupplier;
    @Setter
    private boolean debug = true;

    public byte[] handleDownload(String url, String file) throws IOException {
        return handleDownload(new HttpGet(url), file);
    }

    public String handleDownloadAsString(String url, String file) throws IOException {
        return new String(handleDownload(url, file));
    }

    public byte[] handleDownload(HttpUriRequest request, String file) throws IOException {
        Path cache = fileSupplier.get().resolve(file.trim());
        if (Files.exists(cache)) {
            return Files.readAllBytes(cache);
        }
        if (debug) {
            System.out.println("Downloading " + request.getURI() + " to " + file);
        }
        try (CloseableHttpResponse httpResponse = httpClient.execute(request)) {
            HttpEntity httpEntity = httpResponse.getEntity();
            byte[] data = EntityUtils.toByteArray(httpEntity);
            write(cache, data);
            if (debug) {
                System.out.println("Downloaded " + request.getURI() + " to " + cache);
            }
            return data;
        } catch (Exception e) {
            if (debug) {
                System.out.println(request + " is invalid");
            }
            return new byte[0];
        }
    }

    public void handleDownloadSilent(String url, String file) throws IOException {
        handleDownloadSilent(new HttpGet(url), file);
    }

    public void handleDownloadSilent(HttpUriRequest request, String file) throws IOException {
        Path cache = fileSupplier.get().resolve(file.trim());
        if (!Files.exists(cache)) {
            try (CloseableHttpResponse httpResponse = httpClient.execute(request)) {
                HttpEntity httpEntity = httpResponse.getEntity();
                byte[] data = EntityUtils.toByteArray(httpEntity);
                write(cache, data);
                if (debug) {
                    System.out.println("Downloaded " + request.getURI() + " to " + file);
                }
            }
        }
    }

    private void write(Path cache, byte[] data) throws IOException {
        Files.createDirectories(cache.getParent());
        Files.write(cache, data);
    }
}
