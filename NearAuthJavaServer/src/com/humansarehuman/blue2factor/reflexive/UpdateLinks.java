package com.humansarehuman.blue2factor.reflexive;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.lang3.StringUtils;
//import org.graalvm.polyglot.Context;

class UpdateLinks {
    static String base = "/Users/cjm10/git/B2fServer/B2FJavaServer/";
    private static String[] cssJsFileLocations = { base + "WebContent/resources/css",
            base + "WebContent/resources/js" };
    private static String jspPath = base + "WebContent/WEB-INF/pages";
    private static String[] otherFiles = {
            base + "/src/com/humansarehuman/blue2factor/constants/Constants.java"};
    private static String filePrefix = "b2F_";

    public static void main(String[] args) {
        int filesChanged = 0;
        for (String sfolder : cssJsFileLocations) {
            File folder = new File(sfolder);
            System.out.println("path: " + folder.getAbsolutePath());
            UpdateLinks ul = new UpdateLinks();
            for (final File fileEntry : folder.listFiles()) {
                if (fileEntry.getName().startsWith(filePrefix)) {
                    String[] fileNameArray = fileEntry.getName().split("\\.");
                    int arraySize = fileNameArray.length;
                    if (arraySize > 2) {
                        String suffix = fileNameArray[arraySize - 1];
                        if (suffix.equals("js") || suffix.equals("css")) {
                            System.out.println("found: " + fileEntry.getName());
                            String version = fileNameArray[arraySize - 2];
                            if (StringUtils.isNumeric(version)) {
                                filesChanged += ul.updateFile(fileEntry, fileNameArray,
                                        Integer.parseInt(version));
                            }

                        }
                    }
                }
            }
        }
        System.out.println("filenames changed in files: " + filesChanged);
    }

    private int updateFile(File file, String[] fileNameArray, int version) {
        String oldFileName = file.getName();
        String newFileName = getNewFileName(file, fileNameArray, version);
        String path = file.getParent();
        int changes = replaceFileNamesInJsps(oldFileName, newFileName);
        changes += replaceFileNamesElsewhere(oldFileName, newFileName);

        try {
//            if (oldFileName.startsWith("b2F_2Fa_")) {
//                changes += handleBrowserifyFiles(file, fileNameArray, version, oldFileName,
//                        newFileName, path);
//            } else {
            String minFileName = newFileName.replace(".js", ".min.js").replace(".css", ".min.css");
            renameFile(oldFileName, newFileName, path);
            Minify minify = new Minify();
            String oldMinFileName = oldFileName.replace(".js", ".min.js").replace(".css",
                    ".min.css");

            File oldMinFile = new File(path + File.separatorChar + oldMinFileName);
            oldMinFile.delete();

            minify.minify(path + File.separatorChar + newFileName,
                    path + File.separatorChar + minFileName);
            changes += replaceFileNamesInJsps(oldMinFileName, minFileName);
            changes += replaceFileNamesElsewhere(oldMinFileName, minFileName);

//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return changes;
    }

//    @SuppressWarnings("unused")
//    private Context context;

//    @SuppressWarnings("unused")
//    private int handleBrowserifyFiles(File file, String[] fileNameArray, int version,
//            String oldFileName, String newFileName, String path)
//            throws IOException, ScriptException {
//        int changes = 0;
//        if (!oldFileName.contains("browserify")) {
//            context = Context.create("js");
//            String newBrowserifyFileName = getNewBrowserifyFileName(file, fileNameArray, version);
////            File fileValidatorBundle = new File(getClass().getClassLoader()
////                    .getResource("/usr/local/lib/node_modules/browserify/bin/cmd.js").getFile());
////            context.eval(Source.newBuilder("js", fileValidatorBundle).build());
////            System.out.println(path + File.separatorChar + oldFileName);
////            ScriptEngineManager mgr = new ScriptEngineManager();
////            ScriptEngine engine = mgr.getEngineByName("JavaScript");
////
////            Object result = engine.eval("/usr/local/lib/node_modules/browserify/bin/cmd.js "); // +
////                                                                                               // path
////            // + File.separatorChar + oldFileName + " -o " + newBrowserifyFileName);
////            System.out.println("Result returned by Javascript is: " + result);
//            String[] args = new String[] { "/usr/local/lib/node_modules/browserify/bin/cmd.js",
//                    path + File.separatorChar + oldFileName, "-o", newBrowserifyFileName };
//            Process proc = Runtime.getRuntime()
//                    .exec("/usr/local/lib/node_modules/browserify/bin/cmd.js");// /Users/cjm10/git/B2fServer/B2FJavaServer/WebContent/resources/Js/b2F_2Fa_r.1.0.0.162.js
//                                                                               // -o
//                                                                               // b2F_2Fa_r.1.0.0.160_browserify.js");
//            printOutput(proc);
//            // Runtime.getRuntime().exec(args);
//
//            String minFileName = newFileName.replace(".js", ".min.js");
//            renameFile(oldFileName, newFileName, path);
//
//            Minify minify = new Minify();
//            minify.minify(path + File.separatorChar + newBrowserifyFileName,
//                    path + File.separatorChar + minFileName);
//            String oldMinFileName = getBrowserifyOldFileName(oldFileName);
//            File oldMinFile = new File(path + File.separatorChar + oldMinFileName);
//            oldMinFile.delete();
//            try {
//                Files.copy(new File(path + File.separatorChar + minFileName).toPath(),
//                        new File(path + File.separatorChar + "b2f.latest.js").toPath(),
//                        StandardCopyOption.REPLACE_EXISTING);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            changes += replaceFileNamesInJsps(oldMinFileName, minFileName);
//            changes += replaceFileNamesElsewhere(oldMinFileName, minFileName);
//        } else {
//            System.out.println("should delete: " + oldFileName);
//        }
//        return changes;
//    }

    @SuppressWarnings("unused")
    private void printOutput(Process proc) throws IOException {
        BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

        // Read the output from the command
        System.out.println("Here is the standard output of the command:\n");
        String s = null;
        while ((s = stdInput.readLine()) != null) {
            System.out.println(s);
        }

        // Read any errors from the attempted command
        System.out.println("Here is the standard error of the command (if any):\n");
        while ((s = stdError.readLine()) != null) {
            System.out.println(s);
        }
    }

    @SuppressWarnings("unused")
    private String getBrowserifyOldFileName(String oldFileName) {
        oldFileName.replace(".js", ".min.js").replace(".css", ".min.css");
        String[] fileNameArray = oldFileName.split("//.");
        String oldBrowserifyFileName = "";
        for (int i = 0; i < fileNameArray.length; i++) {
            if (i == 0) {
                oldBrowserifyFileName += fileNameArray[i] + "_browserify.";
            } else if (i <= fileNameArray.length - 2) {
                oldBrowserifyFileName += fileNameArray[i] + ".";
            } else {
                oldBrowserifyFileName += fileNameArray[i];
            }
        }
        return oldBrowserifyFileName;
    }

    @SuppressWarnings("unused")
    private String getNewBrowserifyFileName(File file, String[] fileNameArray, int version) {
        String newFileName = "";
        for (int i = 0; i < fileNameArray.length; i++) {
            if (i == 0) {
                newFileName += fileNameArray[i] + "_browserify.";
            } else if (i < fileNameArray.length - 2) {
                newFileName += fileNameArray[i] + ".";
            } else if (i == fileNameArray.length - 2) {
                newFileName += Integer.toString((version + 1)) + ".";
            } else {
                newFileName += fileNameArray[i];
            }
        }
        return newFileName;
    }

    private String getNewFileName(File file, String[] fileNameArray, int version) {
        String newFileName = "";
        for (int i = 0; i < fileNameArray.length; i++) {
            if (i < fileNameArray.length - 2) {
                newFileName += fileNameArray[i] + ".";
            } else if (i == fileNameArray.length - 2) {
                newFileName += Integer.toString((version + 1)) + ".";
            } else {
                newFileName += fileNameArray[i];
            }
        }
        return newFileName;
    }

    private void renameFile(String oldFileString, String newFileString, String filePath)
            throws IOException {
        File oldFile = new File(filePath + File.separatorChar + oldFileString);
        File newFile = new File(filePath + File.separatorChar + newFileString);
        if (newFile.exists())
            throw new java.io.IOException("file exists");

        boolean success = oldFile.renameTo(newFile);
        // movingFile(oldFileString, newFile.getParent(), newFile.getName());
        if (!success) {
            System.out.println("failed to rename " + oldFileString + " to " + newFileString);
        } else {
            System.out.println("Renamed " + oldFileString + " to " + newFileString);
        }
    }

//    public void movingFile(String oldFileString, String newPath, String newName)
//            throws IOException {
//        Path fileToMovePath = Files.createFile(Paths.get(oldFileString));
//        Path targetPath = Paths.get("src/main/resources/");
//
//        Files.move(fileToMovePath, targetPath.resolve(fileToMovePath.getFileName()));
//    }

    private int replaceFileNamesInJsps(String oldFileName, String newFileName) {
        File folder = new File(jspPath);
        int updates = 0;
        for (final File fileEntry : folder.listFiles()) {
            Path path = fileEntry.toPath();
            Charset charset = StandardCharsets.UTF_8;
            try {
                String content = new String(Files.readAllBytes(path), charset);
                String newContent = content.replaceAll(oldFileName, newFileName);
                if (!content.equals(newContent)) {
                    updates++;
                    System.out.println("updating " + fileEntry.getName());
                    Files.write(path, newContent.getBytes(charset));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return updates;
    }

    private int replaceFileNamesElsewhere(String oldFileName, String newFileName) {
        Charset charset = StandardCharsets.UTF_8;
        int updates = 0;
        for (String otherFile : otherFiles) {
            File currFile = new File(otherFile);
            Path path = currFile.toPath();
            try {
                String content = new String(Files.readAllBytes(path), charset);
                String newContent = content.replaceAll(oldFileName, newFileName);
                if (!content.equals(newContent)) {
                    updates++;
                    System.out.println("updating " + currFile.getName());
                    Files.write(path, newContent.getBytes(charset));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return updates;
    }
}
