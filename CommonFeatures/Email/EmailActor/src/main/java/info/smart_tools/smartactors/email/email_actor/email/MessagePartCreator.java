package info.smart_tools.smartactors.email.email_actor.email;

import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;

import javax.mail.MessagingException;

@FunctionalInterface
public interface MessagePartCreator {
    /**
     * Add part to a message. Part may be a text of an e-ail or attached file.
     *
     * @param smtpMessage e-mail message
     * @param partDescription IObject containing parameters of a message part
     * @throws MessagingException exception
     * @throws ReadValueException exception
     * @throws ChangeValueException exception
     */
    void addPartTo(SMTPMessageAdaptor smtpMessage, IObject context, IObject partDescription)
            throws MessagingException, ReadValueException, ChangeValueException;
}
