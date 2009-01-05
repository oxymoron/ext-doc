package extdoc.config;

import extdoc.gen.syntax.Syntax;
import org.apache.commons.io.IOUtils;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
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

    private File syntaxFile;

    public SyntaxFileReader(File syntaxFile) {
        this.syntaxFile = syntaxFile;
    }

    void read(Config config){
        Logger logger = config.getLogger();
        logger.fine(MessageFormat.format("Processing syntax file: {0}", 
                syntaxFile.getName()));
        try {
            InputStream in = new FileInputStream(syntaxFile);
            try {
                JAXBContext jaxbContext =
                        JAXBContext.newInstance("extdoc.gen.syntax");
                Unmarshaller unmarshaller =
                                jaxbContext.createUnmarshaller();
                SchemaFactory sf = SchemaFactory.newInstance(
                            javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
                Schema schema =
                        sf.newSchema(ClassLoader.getSystemClassLoader()
                                    .getResource("schema/syntax.xsd"));
                unmarshaller.setSchema(schema);
                Syntax syntax =(Syntax) unmarshaller.unmarshal(in);
                config.setSyntax(syntax);
            }catch (JAXBException e) {
                logger.severe("Error while parsing: "
                        + e.getLinkedException().getMessage());
                
            } catch (SAXException e) {
                logger.severe("Error while parsing syntax schema: "
                        + e.getMessage());                
            } finally{
                IOUtils.closeQuietly(in);
                logger.fine("Syntax file processed fine.");
            }            
        } catch (FileNotFoundException e) {
            logger.severe(MessageFormat.format(
                        "Syntax file not found: {0}", e.getMessage()));
        }

        
    }

}
