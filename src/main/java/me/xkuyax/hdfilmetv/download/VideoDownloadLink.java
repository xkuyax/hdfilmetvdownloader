package me.xkuyax.hdfilmetv.download;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VideoDownloadLink {

    private String fileName;
    private String url;
    private QualityLevel qualityLevel;

}