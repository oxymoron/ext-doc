package extdoc.config;

/**
 * User: Andrey Zubkov
 * Date: 28.12.2008
 * Time: 19:02:41
 */
public class ConfigSource {

    public String baseDir = null;
    public String src = null;
    public String match = null;
    public Boolean skipHidden = null;
    
    public ConfigSource(String baseDir, String src,
                        String match, Boolean skipHidden) {
        this.baseDir = baseDir;
        this.src = src;
        this.match = match;
        this.skipHidden = skipHidden;
    }
}
