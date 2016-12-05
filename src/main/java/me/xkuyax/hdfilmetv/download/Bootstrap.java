package me.xkuyax.hdfilmetv.download;

import me.xkuyax.hdfilmetv.download.download.BootstrapDownloader;
import me.xkuyax.hdfilmetv.download.gui.BootstrapGui;

public class Bootstrap {

    public static void main(String[] args) throws Exception {
        if (args.length > 0) {
            String command = args[0];
            if (command.equalsIgnoreCase("download")) {
                new BootstrapDownloader().run();
            } else if (command.equalsIgnoreCase("gui")) {
                new BootstrapGui().run();
            }
        }
    }
}
