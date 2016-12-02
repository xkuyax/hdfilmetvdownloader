package me.xkuyax.hdfilmetv.download.download;

import com.google.gson.Gson;
import me.xkuyax.hdfilmetv.download.*;
import me.xkuyax.hdfilmetv.download.utils.AllMovieListDownloader;
import org.apache.http.impl.client.CloseableHttpClient;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

public class BootstrapDownloader {

    public static int WEB_CRAWLER = 64;
    private static CacheDownloadHandler downloadHandler;

    public void run() throws Exception {
        Login login = new Login("http://hdfilme.tv", "hdfilme");
        CloseableHttpClient httpClient = login.run();
        BaseFileSupplier baseFileSupplier = () -> Paths.get("data");
        downloadHandler = new CacheDownloadHandler(httpClient, baseFileSupplier);
        AllMovieListDownloader movieListDownloader = new AllMovieListDownloader(downloadHandler);
        List<FilmInfo> filmInfos = movieListDownloader.downloadAllFilms();
        ForkJoinPool forkJoinPool = new ForkJoinPool(WEB_CRAWLER);
        forkJoinPool.submit(() -> {
            filmInfos.parallelStream().forEach(filmInfo -> {
                try {
                    String url = filmInfo.getUrl();
                    String[] split = url.split("\\/");
                    String file = split[split.length - 1];
                    List<VideoDownloadLink> downloadLinks = new HDFilmeDownloader(downloadHandler, url, "films/" + file + ".html")
                            .download();
                    filmInfo.setVideoDownloadLinks(downloadLinks);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }).get();
        Gson gson = new Gson();
        Files.write(Paths.get("out.json"), gson.toJson(Arrays.asList(filmInfos.get(0))).getBytes());
        System.out.println("FINISHED");
    }
}
