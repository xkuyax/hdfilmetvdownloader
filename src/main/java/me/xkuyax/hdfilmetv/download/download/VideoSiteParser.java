package me.xkuyax.hdfilmetv.download.download;

import lombok.Data;
import me.xkuyax.hdfilmetv.download.FilmInfo;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class VideoSiteParser {

    private final Document document;

    public List<FilmInfo> parse() {
        List<FilmInfo> film = new ArrayList<>();
        Elements element = document.select(".box-product.clearfix");
        for (Element filmInfo : element) {
            String url = filmInfo.select("div.box-product.clearfix > a").attr("href");
            System.out.println(parseId(url));
            String img = filmInfo.select("img").attr("src");
            String title = filmInfo.select(".title-product").text();
            String desc = filmInfo.select(".popover-content p").first().text();
            String fullText = filmInfo.select(".popover-content").text();
            String metaData = fullText.substring(desc.length() + 1);
            String[] split = metaData.split("IMDB Punkt: ");
            List<String> genres = Arrays.stream(split[0].replaceAll("Genre: ", "").split(" "))
                    .collect(Collectors.toList());
            float rating = Float.valueOf(split[1].replaceAll("[^\\d.]+|\\.(?!\\d)", ""));
            int views = Integer.parseInt(filmInfo.select("span.view-product").text().replaceAll("[^\\d.]", ""));
            url = url.replaceAll("info", "stream");
            film.add(new FilmInfo(title, url, img, desc, genres, views, rating));
        }
        return film;
    }

    private String parseId(String url) {
        String[] split = url.split("-");
        String id = split[split.length - 2];
        return id;
    }

}
