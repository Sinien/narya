//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.threerings.crowd.server;

import java.util.Iterator;

import com.threerings.util.Name;

import com.threerings.presents.dobj.RootDObjectManager;
import com.threerings.presents.net.AuthRequest;
import com.threerings.presents.server.ClientFactory;
import com.threerings.presents.server.ClientResolver;
import com.threerings.presents.server.InvocationManager;
import com.threerings.presents.server.PresentsClient;
import com.threerings.presents.server.PresentsServer;

import com.threerings.crowd.Log;
import com.threerings.crowd.chat.server.ChatProvider;
import com.threerings.crowd.data.BodyObject;

/**
 * The crowd server extends the presents server by configuring it to use the
 * extensions provided by the crowd layer to support crowd services.
 */
public class CrowdServer extends PresentsServer
{
    /** The place registry. */
    public static PlaceRegistry plreg;

    /** Our chat provider. */
    public static ChatProvider chatprov;

    /**
     * Initializes all of the server services and prepares for operation.
     */
    public void init ()
        throws Exception
    {
        // do the presents server initialization
        super.init();

        // configure the client manager to use our bits
        clmgr.setClientFactory(new ClientFactory() {
            public PresentsClient createClient (AuthRequest areq) {
                return new CrowdClient();
            }
            public ClientResolver createClientResolver (Name username) {
                return new CrowdClientResolver();
            }
        });

        // configure the dobject manager with our access controller
        omgr.setDefaultAccessController(CrowdObjectAccess.DEFAULT);

        // create our body locator
        _lookup = createBodyLocator();

        // create our place registry
        plreg = createPlaceRegistry(invmgr, omgr);

        // initialize the body services
        BodyProvider.init(invmgr);

        // initialize the chat services
        chatprov = createChatProvider();
        chatprov.init(invmgr, omgr);
    }

    /**
     * Allow derived instances to create a custom {@link PlaceRegistry}.
     */
    protected PlaceRegistry createPlaceRegistry (
        InvocationManager invmgr, RootDObjectManager omgr)
    {
        return new PlaceRegistry(invmgr, omgr);
    }

    /**
     * Creates the {@link ChatProvider} we'll use to handle chat (or a
     * derivation).
     */
    protected ChatProvider createChatProvider ()
    {
        return new ChatProvider();
    }

    /**
     * Allow derived instances to create a custom {@link BodyLocator}. If the
     * system opts not to use {@link BodyObject#username} as a user's visible
     * name, it will need to provide a custom {@link BodyLocator}.
     */
    protected BodyLocator createBodyLocator ()
    {
        return new BodyLocator() {
            public BodyObject get (Name visibleName) {
                // by default visibleName is username
                return (BodyObject)clmgr.getClientObject(visibleName);
            }
        };
    }

    /**
     * Enumerates the body objects for all active users on the server.
     * This should only be called from the dobjmgr thread.  The caller had
     * best be certain they know what they're doing, since this should
     * only be necessary for use in rather special circumstances.
     */
    public static Iterator enumerateBodies ()
    {
        return clmgr.enumerateClientObjects();
    }

    /**
     * Looks up the {@link BodyObject} for the user with the specified visible
     * name, returns null if they are not online. This should only be called
     * from the dobjmgr thread.
     */
    public static BodyObject lookupBody (Name visibleName)
    {
        return _lookup.get(visibleName);
    }

    public static void main (String[] args)
    {
        CrowdServer server = new CrowdServer();
        try {
            server.init();
            server.run();
        } catch (Exception e) {
            Log.warning("Unable to initialize server.");
            Log.logStackTrace(e);
        }
    }

    /** An interface that allows server extensions to reconfigure the body
     * lookup process. See {@link #lookupBody}, {@link #createBodyLocator}. */
    protected static interface BodyLocator
    {
        /** Returns the body object for the user with the specified visible
         * name, or null if they are not online. This will only be called from
         * the dobjmgr thread. */
        public BodyObject get (Name visibleName);
    }

    /** Used to look up {@link BodyObject} instance for online users. See
     * {@link #lookupBody}. */
    protected static BodyLocator _lookup;

    /** The config key for our list of invocation provider mappings. */
    protected final static String PROVIDERS_KEY = "providers";
}
