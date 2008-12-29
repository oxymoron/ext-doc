package extdoc.config;

import org.apache.commons.io.IOUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.logging.Logger;

/**
 * User: Andrey Zubkov
 * Date: 28.12.2008
 * Time: 20:59:00
 */
public class SyntaxFileReader {

    private String syntaxFileName;

    public SyntaxFileReader(String syntaxFileName) {
        this.syntaxFileName = syntaxFileName;
    }

    void read(Config config){
        Logger logger = config.getLogger();
        logger.fine(MessageFormat.format("Processing syntax file: {0}", 
                syntaxFileName));
        File syntaxFile = new File(syntaxFileName);
        try {
            InputStream in = new FileInputStream(syntaxFile);
            try {
                JAXBContext jaxbContext =
                        JAXBContext.newInstance("extdoc.gen.syntax");
                Unmarshaller unmarshaller =
                                jaxbContext.createUnmarshaller();

            }catch (JAXBException e) {
                logger.severe("Error while parsing: " + e.getMessage());
            }finally{
                IOUtils.closeQuietly(in);
            }            
        } catch (FileNotFoundException e) {
            logger.severe(MessageFormat.format(
                        "Syntax file not found: {0}", e.getMessage()));
        }

        
    }

}
