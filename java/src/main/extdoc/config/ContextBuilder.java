package extdoc.config;

import extdoc.gen.syntax.Preprocessor;
import extdoc.jsdoc.util.StringUtils;
import extdoc.parser.Context;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * User: Andrey Zubkov
 * Date: 04.01.2009
 * Time: 13:49:02
 */
public class ContextBuilder {

    private Logger logger;

    private void fine(String pattern, String msg){
        logger.fine(MessageFormat.format(pattern, msg));
    }

    private void addFiles(Context context, String baseDir, String src,
                          String match, boolean skipHidden){
        File file = new File(baseDir, src);
        if (file.exists()){
            if (!(skipHidden && file.isHidden())){
                if (file.isDirectory()){
                    String[] children = file.list();
                    for(String child : children){
                        addFiles(context, file.getAbsolutePath(),
                                child, match, skipHidden);
                    }
                }else{
                    Pattern pattern =
                            Pattern.compile(StringUtils.wildcardToRegex(match));
                    if(pattern.matcher(src).matches()){
                        context.files.add(file);
                        fine("Source file to process: {0}", file.getName());
                    }
                }
            }else{
                fine("Skip hidden file or dir: {0}", src);
            }
        }else{
            // file does not exists
            logger.warning(
                    MessageFormat.format("File {0} not found", src));
        }
    }

    public Context build(Config config){
        logger = config.getLogger();
        logger.fine("Building context...");
        Context context = new Context();

        context.logger = config.getLogger();

        // get plugins for dynamic class loading
        List<String> plugins = config.getPlugins();
        List<URL> urls = new ArrayList<URL>();
        for(String plugin : plugins){
            try {
                urls.add(new URL("jar:file:" + plugin + "!/"));
            } catch (MalformedURLException e) {
                logger.warning(MessageFormat.format(
                        "Plug-in URL: {0} is not correct", plugin));
            }
        }
        ClassLoader cl =
                URLClassLoader.newInstance(urls.toArray(new URL[urls.size()]));        

        // prepare source files
        List<ConfigSource> sources = config.getSources();
        for(ConfigSource src : sources){
            addFiles(context, src.baseDir, src.src, src.match, src.skipHidden);
        }
        
        logger.info(MessageFormat.format("Total source files: {0}",
                context.files.size()));        

        context.startComment = config.getSyntax().getComment().getStart();
        context.endComment = config.getSyntax().getComment().getEnd();

        List<Preprocessor> preprocessors =
                config.getSyntax().getComment().getPreprocessor();
        for(Preprocessor p : preprocessors){
            String className = p.getClazz();
            try {
                context.preprocessors.add(
                    (extdoc.comment.Preprocessor)Class.forName(
                            className, true, cl).newInstance());
            } catch (ClassNotFoundException e) {
                logger.severe(MessageFormat.format(
                        "Class {0} not found", e.getMessage()));
            } catch (IllegalAccessException e) {
                logger.severe(e.getMessage());
            } catch (InstantiationException e) {
                logger.severe(e.getMessage());
            }

        }


        logger.fine("Context have been built successfully.");
        return context;
    }

}
