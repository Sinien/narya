//
// $Id: Log.java,v 1.4 2001/07/20 23:23:50 mdb Exp $

package com.threerings.cocktail.cher;

/**
 * A placeholder class that contains a reference to the log object used by
 * the Cher services.
 */
public class Log
{
    public static com.samskivert.util.Log log =
	new com.samskivert.util.Log("cocktail.cher");

    /** Convenience function. */
    public static void debug (String message)
    {
	log.debug(message);
    }

    /** Convenience function. */
    public static void info (String message)
    {
	log.info(message);
    }

    /** Convenience function. */
    public static void warning (String message)
    {
	log.warning(message);
    }

    /** Convenience function. */
    public static void logStackTrace (Throwable t)
    {
	log.logStackTrace(com.samskivert.util.Log.WARNING, t);
    }
}
