package info.smart_tools.smartactors.core.iresponse_sender;

import info.smart_tools.smartactors.core.ichannel_handler.IChannelHandler;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iresponse.IResponse;

/**
 * Interface for response senders
 */
public interface IResponseSender {
    /**
     * Method for sending response to client
     * @param responseObject Object with content of the response
     * @param environment Environment of the message processor
     * @param ctx Channel handler from endpoint
     * @throws ResolutionException
     */
    void send(final IResponse responseObject, final IObject environment, final IChannelHandler ctx)
            throws ResolutionException;
}