package me.xkuyax.hdfilmetv.download.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import me.xkuyax.hdfilmetv.download.CacheDownloadHandler;
import me.xkuyax.hdfilmetv.download.FilmInfo;
import me.xkuyax.hdfilmetv.download.download.VideoSiteParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.IntStream;

@Data
public class AllMovieListDownloader {

    private final CacheDownloadHandler downloadHandler;
    private final String BASE_URL = "http://hdfilme.tv/movie-movies?order_f=id&order_d=desc&per_page=%s%";

    public List<FilmInfo> downloadAllFilms() throws Exception {
        List<FilmInfo> films = new ArrayList<>();
        FilmSiteInfo siteInfo = downloadSite(50);
        System.out.println(siteInfo);
        new ForkJoinPool(16).submit(() -> {
            IntStream.range(siteInfo.getCurrentSite(), siteInfo.getMaxSite() + 1).parallel().forEach(site -> {
                try {
                    FilmSiteInfo filmSiteInfo = downloadSite(site * 50);
                    films.addAll(filmSiteInfo.getFilmInfo());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }).get();
        return films;
    }

    private FilmSiteInfo downloadSite(int site) throws IOException {
        String url = BASE_URL.replaceAll("%s%", site + "");
        String html = downloadHandler.handleDownloadAsString(url, "moviesite-" + site + ".html");
        Document document = Jsoup.parse(html);
        int currentSite = 0;
        int maxSite = 0;
        for (Element element : document.select(".fa.fa-caret-down")) {
            String siteInfo = element.parent().text();
            if (!siteInfo.isEmpty()) {
                String[] split = siteInfo.split("\\/");
                try {
                    String current = split[split.length - 2].replaceAll("Seite ", "");
                    currentSite = Integer.parseInt(current);
                    maxSite = Integer.parseInt(split[split.length - 1]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        VideoSiteParser videoSiteParser = new VideoSiteParser(document);
        return new FilmSiteInfo(currentSite, maxSite, videoSiteParser.parse());
    }


    @Data
    @AllArgsConstructor
    private static class FilmSiteInfo {

        private int currentSite;
        private int maxSite;
        private List<FilmInfo> filmInfo;

    }
}
