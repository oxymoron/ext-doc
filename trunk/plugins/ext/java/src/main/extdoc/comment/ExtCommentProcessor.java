package extdoc.comment;

import extdoc.parser.Context;

/**
 * User: Andrey Zubkov
 * Date: 06.01.2009
 * Time: 0:55:57
 */
public class ExtCommentProcessor implements Preprocessor{
    public void process(Context context) {
        System.out.println("I'm EXT Preprocessor");
    }
}
