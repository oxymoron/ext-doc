package org.extdoc;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.text.MessageFormat;
import java.util.regex.Pattern;
import java.util.List;

/**
 * User: Andrey Zubkov
 * Date: 26.01.2009
 * Time: 12:12:10
 */
public class Parser {

    private Context context = null;
    private TagReader tagReader = null;
    private List<Source> sources = null;
    private List<CommentPreprocessor> preprocessors = null;
    private String commentStartsWith = null;
    private String commentEndsWith = null;


    private enum State {CODE, COMMENT}
    
    private static Log log = LogFactory.getLog(Parser.class);

    public void setContext(Context context) {
        this.context = context;
    }

    public void setTagReader(TagReader tagReader) {
        this.tagReader = tagReader;
    }

    public void setSources(List<Source> sources) {
        this.sources = sources;
    }

    public void setPreprocessors(List<CommentPreprocessor> preprocessors) {
        this.preprocessors = preprocessors;
    }

    public void setCommentStartsWith(String commentStartsWith) {
        this.commentStartsWith = commentStartsWith;
    }

    public void setCommentEndsWith(String commentEndsWith) {
        this.commentEndsWith = commentEndsWith;
    }

    /**
     * Recursively adds files to the context for further processing
     * @param baseDir Base directory
     * @param src Path inside base directory
     * @param match Wildcard to match file names (ex. *.js)
     * @param skipHidden True to skip hidden files and directories
     */
    private void addFiles(String baseDir, String src,
                          String match, boolean skipHidden){
        File file = new File(baseDir, src);
        if (file.exists()){
            if (!(skipHidden && file.isHidden())){
                if (file.isDirectory()){
                    String[] children = file.list();
                    for(String child : children){
                        addFiles(file.getAbsolutePath(), child,
                                match, skipHidden);
                    }
                }else{
                    Pattern pattern =
                            Pattern.compile(StringUtils.wildcardToRegex(match));
                    if(pattern.matcher(src).matches()){
                        context.addSourceFile(file);
                        log.debug(MessageFormat.format(
                                "Source file to process: {0}", file.getName()));
                    }
                }
            }else{
                log.debug(MessageFormat.format(
                        "Skip hidden file or dir: {0}", src));
            }
        }else{
            log.warn(MessageFormat.format("File {0} not found", src));    
        }
    }

    private void processBlock(){

        // here comment and code come with start comment sequence or
        // end comment sequence at the end. Let's cut it.
        StringBuilder code = context.getCodeBuffer();
        StringBuilder comment = context.getCommentBuffer();
        int commentLength = comment.length();
        int codeLength = code.length();
        int startsWithLength = commentStartsWith.length();
        int endsWithLength = commentEndsWith.length();
        comment.setLength(
            commentLength>=endsWithLength?commentLength-endsWithLength:0);
        code.setLength(
            codeLength>=startsWithLength?codeLength-startsWithLength:0);

        // preprocess comment
        for(CommentPreprocessor preprocessor : preprocessors){
            preprocessor.process(context);            
        }

        // read tags
        context.setCurrentTags(tagReader.read(context));

    }

    private void processCurrentFile(){
        log.debug(MessageFormat.format("Processing: {0}",
                context.getCurrentFile().getName()));
        try {
            BufferedReader reader =
                new BufferedReader(new FileReader(context.getCurrentFile()));
            try{
                int numRead;
                char ch;
                State state = State.CODE;
                context.resetPositionInFile();
                StringBuilder code = context.getCodeBuffer();
                StringBuilder comment = context.getCommentBuffer();
                while((numRead=reader.read())!=-1){
                    context.incrementPositionInFile();
                    ch =(char)numRead;
                    switch (state){
                        case CODE:
                            context.appendToCodeBuffer(ch);
                            if (StringUtils.endsWith(code, commentStartsWith)){
                                state = State.COMMENT;

                                // process COMMENT+CODE (with start/end markers)
                                processBlock();

                                // clear buffers
                                context.resetBuffers();
                            }
                        break;
                        case COMMENT:
                            context.appendToCommentBuffer(ch);
                            if (StringUtils.endsWith(comment, commentEndsWith)){
                                state = State.CODE;
                            }
                        break;
                    }
                }
                if (state == State.CODE){
                    processBlock();
                }else{
                    log.warn(MessageFormat.format("Unexpected end of " +
                        "file {0} (Comment block started, but not finished).",
                        context.getCurrentFile().getName()));
                }
            } catch (IOException e) {
                log.warn(e.getMessage());
            }finally {
                // clear buffers
                context.resetBuffers();
                IOUtils.closeQuietly(reader);
            }
        } catch (FileNotFoundException e) {
            log.warn(MessageFormat.format("File not found: {0}",
                context.getCurrentFile().getName()));
        }
    }


    public void parse() {

        // add files specified in application context
        for(Source src : sources){
            addFiles(new File("").getAbsolutePath(), src.getSrc(),
                    src.getMatch(), src.getSkipHidden());
        }

        // process files one by one
        for(File file : context.getSourceFiles()){
            context.setCurrentFile(file);
            processCurrentFile();
        }

    }
}
