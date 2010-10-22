//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2010 Three Rings Design, Inc., All Rights Reserved
// http://code.google.com/p/narya/
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

package com.threerings.util;

import java.util.HashMap;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import com.google.common.collect.Maps;

import com.samskivert.util.StringUtil;

import static com.threerings.NaryaLog.log;

/**
 * The message manager provides a thin wrapper around Java's built-in localization support,
 * supporting a policy of dividing up localization resources into logical units, all of the
 * translations for which are contained in a single messages file.
 *
 * <p> The message manager assumes that the locale remains constant for the duration of its
 * operation. If the locale were to change during the operation of the client, a call to
 * {@link #setLocale} should be made to inform the message manager of the new locale (which will
 * clear the message bundle cache).
 */
public class MessageManager
{
    /**
     * The name of the global resource bundle (which other bundles revert to if they can't locate
     * a message within themselves). It must be named <code>global.properties</code> and live at
     * the top of the bundle hierarchy.
     */
    public static final String GLOBAL_BUNDLE = "global";

    /**
     * Constructs a message manager with the supplied resource prefix and the default locale. The
     * prefix will be prepended to the path of all resource bundles prior to their resolution. For
     * example, if a prefix of <code>rsrc.messages</code> was provided and a message bundle with
     * the name <code>game.chess</code> was later requested, the message manager would attempt to
     * load a resource bundle with the path <code>rsrc.messages.game.chess</code> and would
     * eventually search for a file in the classpath with the path
     * <code>rsrc/messages/game/chess.properties</code>.
     *
     * <p> See the documentation for {@link ResourceBundle#getBundle(String,Locale,ClassLoader)}
     * for a more detailed explanation of how resource bundle paths are resolved.
     */
    public MessageManager (String resourcePrefix)
    {
        // keep the prefix
        _prefix = resourcePrefix;

        // use the default locale
        _locale = Locale.getDefault();
        log.debug("Using locale: " + _locale + ".");

        // make sure the prefix ends with a dot
        if (!_prefix.endsWith(".")) {
            _prefix += ".";
        }

        // load up the global bundle
        _global = getBundle(GLOBAL_BUNDLE);
    }

    /**
     * Get the locale that is being used to translate messages. This may be useful if using
     * standard translations, for example new SimpleDateFormat("EEEE", getLocale()) to get the
     * name of a weekday that matches the language being used for all other client translations.
     */
    public Locale getLocale ()
    {
        return _locale;
    }

    /**
     * Sets the locale to the specified locale. Subsequent message bundles fetched via the message
     * manager will use the new locale. The message bundle cache will also be cleared.
     */
    public void setLocale (Locale locale)
    {
        _locale = locale;
        _cache.clear();
    }

    /**
     * Sets the appropriate resource prefix for where to find subsequent message bundles.
     */
    public void setPrefix (String resourcePrefix)
    {
        _prefix = resourcePrefix;

        // Need to reget the global bundle at the new prefix location.
        _global = getBundle(GLOBAL_BUNDLE);
    }

    /**
     * Allows a custom classloader to be configured for locating translation resources.
     */
    public void setClassLoader (ClassLoader loader)
    {
        _loader = loader;
    }

    /**
     * Fetches the message bundle for the specified path. If no bundle can be located with the
     * specified path, a special bundle is returned that returns the untranslated message
     * identifiers instead of an associated translation. This is done so that error code to handle
     * a failed bundle load need not be replicated wherever bundles are used. Instead an error
     * will be logged and the requesting service can continue to function in an impaired state.
     */
    public MessageBundle getBundle (String path)
    {
        // first look in the cache
        MessageBundle bundle = _cache.get(path);
        if (bundle != null) {
            return bundle;
        }

        // if it's not cached, we'll need to resolve it
        ResourceBundle rbundle = loadBundle(_prefix + path);

        // if the resource bundle contains a special resource, we'll interpret that as a derivation
        // of MessageBundle to instantiate for handling that class
        MessageBundle customBundle = null;
        if (rbundle != null) {
            String mbclass = null;
            try {
                mbclass = rbundle.getString(MBUNDLE_CLASS_KEY).trim();
                if (!StringUtil.isBlank(mbclass)) {
                    customBundle = (MessageBundle)Class.forName(mbclass).newInstance();
                }

            } catch (MissingResourceException mre) {
                // nothing to worry about

            } catch (Throwable t) {
                log.warning("Failure instantiating custom message bundle", "mbclass", mbclass,
                            "error", t);
            }
        }

        // initialize our message bundle, cache it and return it (if we couldn't resolve the
        // bundle, the message bundle will cope with its null resource bundle)
        bundle = createBundle(path, rbundle, customBundle);
        _cache.put(path, bundle);
        return bundle;
    }

    /**
     * Returns the bundle to use for the given path and resource bundle. If customBundle is
     * non-null, it's an instance of the bundle class specified by the bundle itself and should be
     * used as part of the created bundle.
     */
    protected MessageBundle createBundle (String path, ResourceBundle rbundle,
        MessageBundle customBundle)
    {
        // if there was no custom class, or we failed to instantiate the custom class, use a
        // standard message bundle
        if (customBundle == null) {
            customBundle = new MessageBundle();
        }
        initBundle(customBundle, path, rbundle);
        return customBundle;
    }

    /**
     * Initializes the given bundle with this manager and the given path and resource bundle.
     */
    protected void initBundle (MessageBundle bundle, String path, ResourceBundle rbundle)
    {
        bundle.init(this, path, rbundle, _global);
    }

    /**
     * Loads a bundle from the given path, or returns null if it can't be found.
     */
    protected ResourceBundle loadBundle (String path)
    {
        try {
            if (_loader != null) {
                return ResourceBundle.getBundle(path, _locale, _loader);
            }
            return ResourceBundle.getBundle(path, _locale);
        } catch (MissingResourceException mre) {
            log.warning("Unable to resolve resource bundle", "path", path, "locale", _locale,
                mre);
            return null;
        }
    }

    /** The prefix we prepend to resource paths prior to loading. */
    protected String _prefix;

    /** The locale for which we're obtaining message bundles. */
    protected Locale _locale;

    /** A custom class loader that we use to load resource bundles. */
    protected ClassLoader _loader;

    /** A cache of instantiated message bundles. */
    protected HashMap<String, MessageBundle> _cache = Maps.newHashMap();

    /** Our top-level message bundle, from which others obtain messages if
     * they can't find them within themselves. */
    protected MessageBundle _global;

    /** A key that can contain the classname of a custom message bundle
     * class to be used to handle messages for a particular bundle. */
    protected static final String MBUNDLE_CLASS_KEY = "msgbundle_class";
}