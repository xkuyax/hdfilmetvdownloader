package me.xkuyax.hdfilmetv.download.metadata;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class MetaDataManager {

    private static final Gson gson = new Gson();
    private List<MetaDataVideoEntry> videoEntries = new ArrayList<>();
    private List<SeenVideo> seenVideos = new ArrayList<>();

    public MetaDataManager(boolean save) {

    }

}
