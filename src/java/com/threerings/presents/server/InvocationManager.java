//
// $Id: InvocationManager.java,v 1.1 2001/07/19 05:56:20 mdb Exp $

package com.threerings.cocktail.cher.server;

import com.threerings.cocktail.cher.Log;
import com.threerings.cocktail.cher.dobj.*;

/**
 * The invocation services provide client to server invocations (service
 * requests) and server to client invocations (responses and
 * notifications). Via this mechanism, the client can make requests of the
 * server, be notified of its response and the server can asynchronously
 * invoke code on the client.
 *
 * <p> Invocations are like remote procedure calls in that they are named
 * and take arguments. They are simple in that the arguments can only be
 * of a small set of supported types (the set of distributed object field
 * types) and there is no special facility provided for referencing
 * non-local objects (it is assumed that the distributed object facility
 * will already be in use for any objects that should be shared).
 *
 * <p> The server invocation manager listens for invocation requests from
 * the client and passes them on to the invocation provider registered for
 * the requested invocation type. It also provides a mechanism by which
 * responses and asynchronous notification invocations can be delivered to
 * the client.
 */
public class InvocationManager
    implements Subscriber
{
    public InvocationManager (DObjectManager omgr)
    {
        _omgr = omgr;

        // create the object on which we'll listen for invocation requests
        omgr.createObject(DObject.class, this, true);
    }

    public int getOid ()
    {
        return _invoid;
    }

    public void objectAvailable (DObject object)
    {
        // this must be our invocation object
        _invoid = object.getOid();
    }

    public void requestFailed (int oid, ObjectAccessException cause)
    {
        // if for some reason we were unable to create our invocation
        // object, we'll end up here
        Log.warning("Unable to create invocation object " +
                    "[reason=" + cause + "].");
        _invoid = -1;
    }

    public boolean handleEvent (DEvent event, DObject target)
    {
        // we shouldn't be getting non-message events, but check just to
        // be sure
        if (!(event instanceof MessageEvent)) {
            Log.warning("Got non-message event!? [evt=" + event + "].");
            return true;
        }

        MessageEvent mevt = (MessageEvent)event;
        // make sure the name is proper just for sanities sake

        return true;
    }

    protected DObjectManager _omgr;
    protected int _invoid;
}
