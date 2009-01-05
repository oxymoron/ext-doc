package extdoc.parser.impl;

import extdoc.jsdoc.util.StringUtils;
import extdoc.parser.Context;
import extdoc.parser.Parser;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.text.MessageFormat;
import java.util.logging.Logger;

/**
 * User: Andrey Zubkov
 * Date: 27.12.2008
 * Time: 0:36:00
 */
public class FileParser implements Parser {

    private Context context = null;

    private Logger logger = null;

    private enum State {CODE, COMMENT}

    public FileParser(Context context) {
        this.context = context;
        logger = context.logger;
    }

    private void processBlock(){

        // here comment and code come with start comment sequence or
        // end comment sequence at the end. Let's cut it.
        int commentLength = context.comment.length();
        int codeLength = context.code.length();
        int startCommentLength = context.startComment.length();
        int endCommentLength = context.endComment.length();
        context.comment.setLength(
            commentLength>=endCommentLength?commentLength-endCommentLength:0);
        context.code.setLength(
            codeLength>=startCommentLength?codeLength-startCommentLength:0);
        
    }

    private void processCurrentFile(){
        logger.fine(MessageFormat.format("Processing: {0}",
                context.currentFile.getName()));
        try {
            BufferedReader reader =
                new BufferedReader(new FileReader(context.currentFile));
            try{
                int numRead;
                char ch;
                State state = State.CODE;
                context.position = 0;
                while((numRead=reader.read())!=-1){
                    context.position++;
                    ch =(char)numRead;                    
                    switch (state){
                        case CODE:
                            context.code.append(ch);
                            if (StringUtils.endsWith(
                                    context.code, context.startComment)){
                                state = State.COMMENT;

                                // process COMMENT+CODE (with start/end markers)
                                processBlock();

                                // clear buffers
                                context.code.setLength(0);
                                context.comment.setLength(0);
                            }
                        break;
                        case COMMENT:
                            context.comment.append(ch);
                            if (StringUtils.endsWith(
                                    context.comment, context.endComment)){
                                state = State.CODE;    
                            }
                        break;
                    }
                }
                if (state == State.CODE){
                    processBlock();
                }else{
                    logger.warning(MessageFormat.format("Unexpected end of " +
                        "file {0} (Comment block started, but not finished).",
                        context.currentFile.getName()));
                }
            } catch (IOException e) {
                logger.warning(e.getMessage());
            }finally {
                // clear buffers
                context.code.setLength(0);
                context.comment.setLength(0);
                IOUtils.closeQuietly(reader);
            }
        } catch (FileNotFoundException e) {
            logger.warning(MessageFormat.format("File not found: {0}",
                context.currentFile.getName()));
        }
    }

    public void parse() {
        for(File file : context.files){
            context.currentFile = file;
            processCurrentFile();
        }
    }
}
