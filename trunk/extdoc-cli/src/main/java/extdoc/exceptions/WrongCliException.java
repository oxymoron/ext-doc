package extdoc.exceptions;

/**
 * Created by IntelliJ IDEA.
 * User: oxymoron
 * Date: Jul 2, 2010
 * Time: 11:47:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class WrongCliException extends Exception{

    public WrongCliException(String s) {
        super(s);
    }

    public WrongCliException(Throwable throwable) {
        super(throwable);
    }
}
