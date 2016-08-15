package info.smart_tools.smartactors.actors.mailing;


import info.smart_tools.smartactors.actors.mailing.email.MessageAttributeSetters;
import info.smart_tools.smartactors.actors.mailing.email.MessagePartCreators;
import info.smart_tools.smartactors.actors.mailing.email.SMTPMessageAdaptor;
import info.smart_tools.smartactors.actors.mailing.exception.AttributeSetterException;
import info.smart_tools.smartactors.actors.mailing.exception.MailingActorException;
import info.smart_tools.smartactors.actors.mailing.wrapper.MailingMessage;
import info.smart_tools.smartactors.core.field.Field;
import info.smart_tools.smartactors.core.ifield.IField;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import me.normanmaurer.niosmtp.delivery.Authentication;
import me.normanmaurer.niosmtp.delivery.SMTPDeliveryAgent;
import me.normanmaurer.niosmtp.delivery.SMTPDeliveryEnvelope;
import me.normanmaurer.niosmtp.delivery.impl.AuthenticationImpl;
import me.normanmaurer.niosmtp.delivery.impl.SMTPDeliveryAgentConfigImpl;
import me.normanmaurer.niosmtp.delivery.impl.SMTPDeliveryEnvelopeImpl;
import me.normanmaurer.niosmtp.transport.SMTPClientTransport;
import me.normanmaurer.niosmtp.transport.netty.NettyLMTPClientTransportFactory;
import me.normanmaurer.niosmtp.transport.netty.NettySMTPClientTransportFactory;

import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import javax.net.ssl.SSLContext;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Actor for sending emails
 */
public class MailingActor {
    private SMTPDeliveryAgent deliveryAgent;
    private SMTPDeliveryAgentConfigImpl deliveryAgentConfig = new SMTPDeliveryAgentConfigImpl();
    private InetSocketAddress serverHost;
    private URI serverUri;
    private IObject mailingContext;

    private static Field serverURI_ActorParams_F;
    private static Field senderAddress_ActorParams_F;
    private static Field userName_ActorParams_F;
    private static Field password_ActorParams_F;
    private static Field authenticationMode_ActorParams_F;
    private static Field SSLProtocol_ActorParams_F;
    private static Field senderAddress_Context_F;


    // Functions creating client transport, depending on server URI scheme
    private static Map<String, Function<IObject, SMTPClientTransport>> transportCreators
            = new HashMap<String, Function<IObject, SMTPClientTransport>>() {{
        put("smtp", params -> NettySMTPClientTransportFactory.createNio().createPlain());
        put("smtps", params -> NettySMTPClientTransportFactory.createNio().createSMTPS(createSSLContext(params)));
        put("lmtp", params -> NettyLMTPClientTransportFactory.createNio().createPlain());
    }};

    /**
     * Constructor.
     *
     * @param params actor parameters. Expected following fields:
     *      <ul>
     *               <li>"username" - username used for authentication on SMTP server.</li>
     *               <li>"password" - password used for authentication on SMTP server.</li>
     *               <li>"authenticationMode" - use "Login" for login/password authentication.</li>
     *               <li>"server" - URI of SMTP/LMTP server. Supported schemes are: "smtp", "smtps" and "lmtp".</li>
     *               <li>"sslProtocol" - name of SSL protocol for SMTPS transport.</li>
     *               <li>"senderAddress" - e-mail address used to send mail from.</li>
     *      </ul>
     */
    public MailingActor(final IObject params) throws MailingActorException {
        try {
            //Fields initialize
            serverURI_ActorParams_F = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "server");
            senderAddress_Context_F = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "senderAddress");
            senderAddress_ActorParams_F = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "senderAddress");
            userName_ActorParams_F = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "username");
            password_ActorParams_F = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "password");
            authenticationMode_ActorParams_F = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "authenticationMode");
            SSLProtocol_ActorParams_F = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "sslProtocol");
            senderAddress_Context_F = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "senderAddress");

            mailingContext = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()));

            serverUri = new URI(serverURI_ActorParams_F.in(params, String.class));
            serverHost = new InetSocketAddress(serverUri.getHost(), serverUri.getPort());

            senderAddress_Context_F.out(
                    mailingContext,
                    senderAddress_ActorParams_F.in(params, String.class));

            deliveryAgent = createAgent(params);

            Authentication authentication = new AuthenticationImpl(
                    userName_ActorParams_F.in(params, String.class),
                    password_ActorParams_F.in(params, String.class),
                    Authentication.AuthMode.valueOf(authenticationMode_ActorParams_F.in(params, String.class)));
            deliveryAgentConfig.setAuthentication(authentication);
        } catch (ReadValueException | ChangeValueException | InvalidArgumentException e) {
            throw new MailingActorException("Params object is not correct", e);
        } catch (URISyntaxException e) {
            throw new MailingActorException("Failed to create URI", e);
        } catch (ResolutionException e) {
            throw new MailingActorException("Failed to resolve fields", e);
        }
    }

    /**
     * Creates SSL context for encrypted client transport
     *
     * @param actorParams parameters of mailing actor. Expected all fields of actor's constructor parameters related to
     *                    creation of SSL context (for now only "sslProtocol").
     * @return created SSL context
     */
    private static SSLContext createSSLContext(final IObject actorParams) {
        try {
            SSLContext sslContext = SSLContext.getInstance(SSLProtocol_ActorParams_F.in(actorParams, String.class));
            sslContext.init(null, null, null);
            return sslContext;
        } catch (ReadValueException | NoSuchAlgorithmException | KeyManagementException | InvalidArgumentException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates delivery agent with transport type specified by server URI scheme.
     *
     * @param actorParams parameters of mailing actor
     * @return created delivery agent
     */
    private SMTPDeliveryAgent createAgent(final IObject actorParams) {
        return new SMTPDeliveryAgent(transportCreators.get(serverUri.getScheme()).apply(actorParams));
    }

    /**
     * Handler for sending emails
     * @param message the wrapper for message
     */
    public void sendMailHandler(final MailingMessage message) {
        try {
            List<String> recipients = message.getSendToMessage();
            SMTPMessageAdaptor smtpMessage = new SMTPMessageAdaptor(SMTPMessageAdaptor.createMimeMessage());

            setMessageAttributes(
                    smtpMessage,
                    message.getMessageAttributesMessage(),
                    recipients);

            MessagePartCreators.addAllPartsTo(
                    smtpMessage,
                    mailingContext,
                    message.getMessagePartsMessage()
            );

            SMTPDeliveryEnvelope deliveryEnvelope = new SMTPDeliveryEnvelopeImpl(
                    senderAddress_Context_F.in(mailingContext, String.class), recipients, smtpMessage);

            deliveryAgent.deliver(serverHost, deliveryAgentConfig, deliveryEnvelope);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setMessageAttributes(final SMTPMessageAdaptor smtpMessage, final IObject attributes, final List<String> recipients)
            throws Exception {
        smtpMessage.getMimeMessage().setFrom(
                new InternetAddress(senderAddress_Context_F.in(mailingContext, String.class)));
        for (String recipient : recipients) {
            smtpMessage.getMimeMessage().addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
        }

        MessageAttributeSetters.applyAll(
                attributes,
                mailingContext, smtpMessage);
    }
}