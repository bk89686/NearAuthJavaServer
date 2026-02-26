package com.humansarehuman.blue2factor.reflexive;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

class Minify {
    private String compressorPath = UpdateLinks.base
            + "WebContent/resources/yuicompressor-2.4.8.jar";

    void minify(String currFile, String minFile) {
        System.out.println("java -jar " + compressorPath + " " + currFile + " -o " + minFile);
        executeCommand("java -jar " + compressorPath + " " + currFile + " -o " + minFile);
        if (!waitForFile(minFile)) {
            System.out.println("could not execute minify on " + currFile);
        }
    }

    private boolean waitForFile(String filePathString) {
        File f = new File(filePathString);
        boolean fileExists = false;
        int i = 0;
        while (!fileExists && i < 10) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
            if (f.exists()) {
                fileExists = true;
            }
            i++;
        }
        return fileExists;
    }

    private String executeCommand(String command) {

        StringBuffer output = new StringBuffer();

        Process p;
        try {
            p = Runtime.getRuntime().exec(command);
            p.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line = "";
            while ((line = reader.readLine()) != null) {
                output.append(line + "\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return output.toString();

    }
}
