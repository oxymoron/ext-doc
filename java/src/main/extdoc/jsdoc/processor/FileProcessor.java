package extdoc.jsdoc.processor;

import extdoc.jsdoc.docs.*;
import extdoc.jsdoc.schema.Doc;
import extdoc.jsdoc.schema.Source;
import extdoc.jsdoc.tags.Comment;
import org.w3c.dom.Document;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.util.ArrayList;
import java.util.List;


/**
 * User: Andrey Zubkov
 * Date: 25.10.2008
 * Time: 4:41:12
 */
public class FileProcessor{

    public List<DocClass> classes = new ArrayList<DocClass>();
    public List<DocCfg> cfgs = new ArrayList<DocCfg>();
    public List<DocProperty> properties = new ArrayList<DocProperty>();
    public List<DocMethod> methods = new ArrayList<DocMethod>();
    public List<DocEvent> events = new ArrayList<DocEvent>();

    final static String OUT_FILE_EXTENSION = "html";
    private String className;
    private String currFile;


    private void processComment(String content, String extraLine){
        Comment comment = new Comment(content);        
        
    }

    private enum State {CODE, COMMENT}
    private enum ExtraState {SKIP, SPACE, READ}   

    private static final String START_COMMENT = "/**";
    private static final String END_COMMENT = "*/";

    /**
     * Checks if StringBuilder ends with string
     */
    private boolean endsWith(StringBuilder sb, String str){
        int len = sb.length();
        int strLen = str.length();        
        return (len>=strLen && sb.substring(len-strLen).equals(str));
    }

    /**
     * Checks if char is white space in terms of extra line of code after
     * comments
     * @param ch character
     * @return true if space or new line or * or / or ' etc...
     */
    private boolean isWhite(char ch){
        return !Character.isLetterOrDigit(ch);
    }

    /**
     * Processes one file with state machine
     * @param fileName Source Code file name
     */
    private void processFile(String fileName){
        try {
            File file = new File(fileName);
            currFile = file.getName();
            BufferedReader reader = new BufferedReader(new FileReader(file));
            int numRead;
            State state = State.CODE;
            ExtraState extraState = ExtraState.SKIP;            
            StringBuilder buffer = new StringBuilder();
            StringBuilder extraBuffer = new StringBuilder();            
            String comment=null;
            char ch;
            while((numRead=reader.read())!=-1){
                ch =(char)numRead;
                buffer.append(ch);
                switch(state){
                    case CODE:
                        switch (extraState){
                            case SKIP:
                                break;
                            case SPACE:
                                if (isWhite(ch)){
                                    break;
                                }
                                extraState = ExtraState.READ;
                                /* fall through */
                            case READ:
                                if (isWhite(ch)){
                                    extraState = ExtraState.SKIP;
                                    break;
                                }
                                extraBuffer.append(ch);
                                break;
                        }                                            
                        if (endsWith(buffer, START_COMMENT)){
                            if (comment!=null){
                                // comment is null before the first comment starts
                                // so we do not process it
                                processComment(comment, extraBuffer.toString());
                            }
                            extraBuffer.setLength(0);
                            buffer.setLength(0);
                            state = State.COMMENT;
                        }
                        break;
                    case COMMENT:
                       if (endsWith(buffer, END_COMMENT)){
                            comment = buffer.substring(0, buffer.length()-END_COMMENT.length());
                            buffer.setLength(0);
                            state = State.CODE;
                            extraState = ExtraState.SPACE;
                        }
                        break;
                }
            }
            processComment(comment, extraBuffer.toString());
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void populateTree(){
        for(DocClass docClass: classes){
            for(DocClass cls: classes){
                if(docClass.className.equals(cls.parentClass)){
                    docClass.subClasses.add(cls);
                }
            }
            for(DocCfg cfg: cfgs){
                if(docClass.className.equals(cfg.className)){
                    docClass.cfgs.add(cfg);
                }
            }
            for(DocProperty property: properties){
                if(docClass.className.equals(property.className)){
                    docClass.properties.add(property);
                }
            }
            for(DocMethod method: methods){
                if(docClass.className.equals(method.className)){
                    docClass.methods.add(method);
                }
            }
            for(DocEvent event: events){
                if(docClass.className.equals(event.className)){
                    docClass.events.add(event);
                }
            }
        }
    }

    public void process(String fileName){
        try {
            File xmlFile = new File(fileName);
            FileInputStream fileInputStream = new FileInputStream(xmlFile);
            JAXBContext jaxbContext = JAXBContext.newInstance("extdoc.jsdoc.schema");
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            Doc doc = (Doc) unmarshaller.unmarshal(fileInputStream);
            Source source = doc.getSource();
            List<extdoc.jsdoc.schema.File> files = source.getFile();
            for(extdoc.jsdoc.schema.File file: files){
                processFile(xmlFile.getParent()+ File.separator +file.getSrc());
            }
            fileInputStream.close();
            populateTree();
        } catch (JAXBException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private void createResources(String folderName){
       File file = new File(folderName);
       file.mkdirs();
    }

    public void saveToFolder(String folderName, String templateFileName) {
        createResources(folderName);
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance("extdoc.jsdoc.docs");
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            builderFactory.setNamespaceAware(true);

            TransformerFactory factory = TransformerFactory.newInstance();
            Templates transformation = factory.newTemplates (new StreamSource(templateFileName)) ;
            Transformer transformer = transformation.newTransformer();

            for(DocClass docClass: classes){
                String targetFileName = folderName+File.separator+docClass.className+".html";
                Document doc = builderFactory.newDocumentBuilder().newDocument();
                marshaller.marshal(docClass, doc);
                marshaller.marshal(docClass, new File(targetFileName+"_"));
                Result fileResult = new StreamResult(new File(targetFileName));
                transformer.transform(new DOMSource(doc), fileResult);
            }
        } catch (JAXBException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }
}
