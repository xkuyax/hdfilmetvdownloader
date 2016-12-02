package me.xkuyax.hdfilmetv.download.download;

import lombok.Data;
import me.xkuyax.hdfilmetv.download.CacheDownloadHandler;
import me.xkuyax.hdfilmetv.download.VideoDownloadLink;

import java.util.ArrayList;
import java.util.List;

@Data
public class HDFilmeDownloader {

    private final CacheDownloadHandler downloadHandler;
    private final String url;
    private final String file;

    public List<VideoDownloadLink> download() throws Exception {
        String html = downloadHandler.handleDownloadAsString(url, file);
        String[] lines = html.split("\n");
        List<VideoDownloadLink> links = new ArrayList<>();
        for (String line : lines) {
            if (line.contains("var kF")) {
                String link = line.substring(line.indexOf('"'), line.lastIndexOf('"'));
                link = link.replaceAll("\"\\+\"", "").substring(1);
                VideoDownloadLinkParser videoDownloadLinkParser = new VideoDownloadLinkParser(downloadHandler, link, file + "_download.html");
                links.addAll(videoDownloadLinkParser.parse());
            }
        }
        return links;
    }
}
