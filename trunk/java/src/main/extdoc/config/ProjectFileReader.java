package extdoc.config;

import extdoc.gen.project.*;
import org.apache.commons.io.IOUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.List;
import java.util.logging.Logger;

/**
 * User: Andrey Zubkov
 * Date: 27.12.2008
 * Time: 0:55:41
 */
public class ProjectFileReader{

    public void read(Config config) {

        Logger logger = config.getLogger();
        // check if project file name specified in command line options
        if(config.hasProjectFile()){
            logger.fine("Processing project file...");                        

            File projectFile = new File(config.getProject());
            try {
                InputStream in = new FileInputStream(projectFile);
                try{
                    // unmarshall
                    JAXBContext jaxbContext =
                            JAXBContext.newInstance("extdoc.gen.project");
                    Unmarshaller unmarshaller =
                            jaxbContext.createUnmarshaller();
                    Project project =(Project) unmarshaller.unmarshal(in);

                    // read sources
                    Sources sources = project.getSources();
                    if(sources!=null){
                        List<Source> source = sources.getSource();
                        if(source!=null){
                            for(Source src : source){
                                logger.fine(MessageFormat.format(
                                        "Source added: {0}",src.getSrc()));
                                config.addSource(src.getSrc(),src.getMatch(),
                                        src.isSkipHidden());
                            }
                        }
                    }

                    // read plugins
                    Plugins plugins = project.getPlugins();
                    if (plugins!=null){
                        List<Plugin> plugin = plugins.getPlugin();
                        if(plugin!=null){
                            for(Plugin plg: plugin){
                                logger.fine(MessageFormat.format(
                                        "Plugin added: {0}",plg.getFile()));
                                config.addPlugin(plg.getFile());
                            }
                        }
                    }

                    // read syntax file
                    Syntax syntax = project.getSyntax();
                    if(syntax!=null){
                        new SyntaxFileReader(syntax.getFile()).read(config);    
                    }

                config.getLogger().fine("Project file processed.");
                } catch (JAXBException e) {
                    logger.severe("Error while parsing: " + e.getMessage());
                }finally{
                    IOUtils.closeQuietly(in);
                }
            }catch (FileNotFoundException e) {
                logger.severe(MessageFormat.format(
                        "Project file not found: {0}", e.getMessage()));
            }
        }else{
            logger.fine(
                "Project XML file is not specified in command line options");
        }
    }
}
