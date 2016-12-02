package me.xkuyax.hdfilmetv.download;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class FilmInfo {

    private final String title;
    private final String url;
    private final String imageUrl;
    private final String description;
    private final List<String> genres;
    private final int views;
    private final float rating;
    private List<VideoDownloadLink> videoDownloadLinks = new ArrayList<>();

}
