package extdoc.config;

import extdoc.gen.syntax.Syntax;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * User: Andrey Zubkov
 * Date: 27.12.2008
 * Time: 0:58:48
 */
public class Config {

    private static final boolean DEFAULT_SKIPHIDDEN = true;
    private static final String DEFAULT_MATCH = "*.js";
    private static final String DEFAULT_DIR = ".";

    private String project = null;

    private String output = null;

    private List<ConfigSource> sources = new ArrayList<ConfigSource>();

    private List<String> plugins = new ArrayList<String>();

    private Logger logger = null;

    private Syntax syntax = null;

    public boolean hasProjectFile(){
        return project!=null;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getProject() {
        return project;
    }

    public void addSource(ConfigSource source){
        sources.add(source);
    }

    public void addSource(String baseDir, String src,
                          String match, Boolean skipHidden){
        addSource(new ConfigSource(
                baseDir,
                src,
                match!=null?match:DEFAULT_MATCH,
                skipHidden!=null?skipHidden:DEFAULT_SKIPHIDDEN));
    }

    public void addSource(String source){
        addSource(new ConfigSource(DEFAULT_DIR, source,
                DEFAULT_MATCH, DEFAULT_SKIPHIDDEN));
    }

    public List<ConfigSource> getSources(){
        return sources;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public String getOutput() {
        return output;
    }

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public List<String> getPlugins() {
        return plugins;
    }

    public void addPlugin(String plugin) {
        plugins.add(plugin);
    }

    public Syntax getSyntax() {
        return syntax;
    }

    public void setSyntax(Syntax syntax) {
        this.syntax = syntax;
    }
}
