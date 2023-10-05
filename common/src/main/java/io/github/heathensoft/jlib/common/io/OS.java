package io.github.heathensoft.jlib.common.io;


/**
 * @author Frederik Dahl
 * 06/10/2022
 */


public class OS {
    
    public enum NAME { WINDOWS, LINUX, MAC, SOLARIS, UNDEFINED }
    
    public static final NAME name;
    
    static { String system = System.getProperty("os.name").toLowerCase();
        if (system.contains("win")) {
            name = NAME.WINDOWS;
        } else if (system.contains("nix") || system.contains("nux") || system.contains("aix")) {
            name = NAME.LINUX;
        } else if (system.contains("mac")) {
            name = NAME.MAC;
        } else if (system.contains("sunos")) {
            name = NAME.SOLARIS;
        } else name = NAME.UNDEFINED;
    }

    public static void openBrowser(String url) throws Exception { // temporary copy-paste from stack overflow
        Runtime rt = Runtime.getRuntime();
        switch (name) {
            case WINDOWS -> rt.exec( "rundll32 url.dll,FileProtocolHandler " + url);
            case MAC -> rt.exec( "open " + url);
            case LINUX -> {
                String[] browsers = {"epiphany", "firefox", "mozilla", "konqueror",
                        "netscape","opera","links","lynx"};
                StringBuilder cmd = new StringBuilder();
                for (int i=0; i<browsers.length; i++)
                    cmd.append(i == 0 ? "" : " || ").
                            append(browsers[i]).append(" \"").
                            append(url).append("\" ");
                rt.exec(new String[] { "sh", "-c", cmd.toString() });
            }case SOLARIS, UNDEFINED -> throw new Exception("Unable to open browser in unknown OS");
        }
    }

}
