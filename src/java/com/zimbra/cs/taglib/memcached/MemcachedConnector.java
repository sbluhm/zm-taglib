/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2013, 2014, 2016 Synacor, Inc.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation,
 * version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program.
 * If not, see <https://www.gnu.org/licenses/>.
 * ***** END LICENSE BLOCK *****
 */

package com.zimbra.cs.taglib.memcached;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.memcached.ZimbraMemcachedClient;

import net.spy.memcached.DefaultHashAlgorithm;

public class MemcachedConnector {

    private static ZimbraMemcachedClient sTheClient = new ZimbraMemcachedClient();

    /**
     * Returns the one and only memcached client object.
     * @return
     */
    public static ZimbraMemcachedClient getClient() {
        return sTheClient;
    }

    /**
     * Startup the memcached connection.  Establish the memcached connection(s) if configured.
     * @throws ServiceException
     */
    public static void startup() throws ServiceException {
        reloadConfig();
    }

    /**
     * Are we currently connected to the memcached servers?
     * @return
     */
    public static boolean isConnected() {
        return sTheClient.isConnected();
    }

    /**
     * Reload the memcached client configuration.  Connect to the servers if configured with a
     * non-empty server list.  Any old connections are flushed and disconnected.
     * @throws ServiceException
     */
    public static void reloadConfig() throws ServiceException {
        String[] serverList = null;
        boolean useBinaryProtocol = false;
        String hashAlgorithm =  DefaultHashAlgorithm.KETAMA_HASH.toString();
        int expirySeconds = 86400;
        long timeoutMillis = 10000;
        try {
            Context initCtx = new InitialContext();
            Context envCtx = (Context) initCtx.lookup("java:comp/env");

            serverList = ((String) envCtx.lookup("memcachedServers")).split("\\s+");
            useBinaryProtocol = (Boolean) envCtx.lookup("memcachedClientBinaryProtocolEnabled");
            hashAlgorithm = (String) envCtx.lookup("memcachedClientHashAlgorithm");
            expirySeconds = (Integer) envCtx.lookup("memcachedClientExpiry");
            timeoutMillis = (Integer) envCtx.lookup("memcachedClientTimeout");
        } catch (NamingException ne) {
            //REDO to throw the error or print stack trace??
            ne.printStackTrace();
        } catch (Exception e) {
            //REDO to throw the error or print stack trace??
            e.printStackTrace();
        }
        sTheClient.connect(serverList, useBinaryProtocol, hashAlgorithm, expirySeconds, timeoutMillis);
    }

    /**
     * Shutdown the memcached connection.
     * @throws ServiceException
     */
    public static void shutdown() throws ServiceException {
        sTheClient.disconnect(30000);
        sTheClient = null;
    }
}
