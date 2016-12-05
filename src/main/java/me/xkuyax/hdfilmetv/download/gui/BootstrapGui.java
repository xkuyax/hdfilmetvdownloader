package me.xkuyax.hdfilmetv.download.gui;

import javax.swing.*;

public class BootstrapGui {

    public void run() {
        JFrame frame = new JFrame("HDFilme.tv - Downloader");
        DownloadFrame downloadFrame = new DownloadFrame();
        frame.setSize(1280, 720);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setContentPane(downloadFrame.getPanel1());
        frame.setVisible(true);
    }
}