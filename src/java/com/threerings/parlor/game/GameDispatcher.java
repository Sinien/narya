//
// $Id: GameDispatcher.java,v 1.4 2004/06/22 13:55:25 mdb Exp $

package com.threerings.parlor.game;

import com.threerings.parlor.game.GameMarshaller;
import com.threerings.parlor.game.GameService;
import com.threerings.presents.client.Client;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.server.InvocationDispatcher;
import com.threerings.presents.server.InvocationException;

/**
 * Dispatches requests to the {@link GameProvider}.
 */
public class GameDispatcher extends InvocationDispatcher
{
    /**
     * Creates a dispatcher that may be registered to dispatch invocation
     * service requests for the specified provider.
     */
    public GameDispatcher (GameProvider provider)
    {
        this.provider = provider;
    }

    // documentation inherited
    public InvocationMarshaller createMarshaller ()
    {
        return new GameMarshaller();
    }

    // documentation inherited
    public void dispatchRequest (
        ClientObject source, int methodId, Object[] args)
        throws InvocationException
    {
        switch (methodId) {
        case GameMarshaller.PLAYER_READY:
            ((GameProvider)provider).playerReady(
                source
                
            );
            return;

        case GameMarshaller.START_PARTY_GAME:
            ((GameProvider)provider).startPartyGame(
                source
                
            );
            return;

        default:
            super.dispatchRequest(source, methodId, args);
        }
    }
}
