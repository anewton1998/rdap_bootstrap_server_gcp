/*
 * Copyright (C) 2013-2018 American Registry for Internet Numbers (ARIN)
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 *
 */
package net.arin.rdap_bootstrap.service;

import net.arin.rdap_bootstrap.service.JsonBootstrapFile.ServiceUrls;
import org.junit.Test;

import javax.servlet.ServletContext;

import static junit.framework.Assert.assertEquals;

/**
 * @version $Rev$, $Date$
 */
public class BaseServletTest
{


    private BaseServlet makeBaseServlet() throws Exception
    {
        BaseServlet servlet = new BaseServlet() {
            @Override
            public ServletContext getServletContext() {
                return TestUtils.makeServletContext();
            }
        };
        servlet.init( null );
        return servlet;
    }

    @Test
    public void testGetRedirectUrlDefault() throws Exception
    {
        System
            .clearProperty( BaseServlet.MATCH_SCHEME_ON_REDIRECT );

        ServiceUrls urls = new ServiceUrls();
        urls.addUrl( "http://example.com" );
        urls.addUrl( "https://example.com" );

        BaseServlet servlet = makeBaseServlet();

        assertEquals( "https://example.com/bar", servlet.getRedirectUrl( "http", "/bar", urls ) );
        assertEquals( "https://example.com/bar", servlet.getRedirectUrl( "https", "/bar", urls ) );
    }

    @Test
    public void testGetRedirectUrlDefaultOnlyHttp() throws Exception
    {
        System
            .clearProperty( BaseServlet.MATCH_SCHEME_ON_REDIRECT );

        ServiceUrls urls = new ServiceUrls();
        urls.addUrl( "http://example.com" );

        BaseServlet servlet = makeBaseServlet();

        assertEquals( "http://example.com/bar", servlet.getRedirectUrl( "http", "/bar", urls ) );
        assertEquals( "http://example.com/bar", servlet.getRedirectUrl( "https", "/bar", urls ) );
    }

    @Test
    public void testGetRedirectUrlDefaultOnlyHttps() throws Exception
    {
        System
            .clearProperty( BaseServlet.MATCH_SCHEME_ON_REDIRECT );

        ServiceUrls urls = new ServiceUrls();
        urls.addUrl( "https://example.com" );

        BaseServlet servlet = makeBaseServlet();

        assertEquals( "https://example.com/bar", servlet.getRedirectUrl( "http", "/bar", urls ) );
        assertEquals( "https://example.com/bar", servlet.getRedirectUrl( "https", "/bar", urls ) );
    }

    @Test
    public void testGetRedirectUrlFalse() throws Exception
    {
        System
            .clearProperty( BaseServlet.MATCH_SCHEME_ON_REDIRECT );
        System.setProperty( BaseServlet.MATCH_SCHEME_ON_REDIRECT,
            "False" );

        ServiceUrls urls = new ServiceUrls();
        urls.addUrl( "http://example.com" );
        urls.addUrl( "https://example.com" );

        BaseServlet servlet = makeBaseServlet();

        assertEquals( "https://example.com/bar", servlet.getRedirectUrl( "http", "/bar", urls ) );
        assertEquals( "https://example.com/bar", servlet.getRedirectUrl( "https", "/bar", urls ) );

        System
            .clearProperty( BaseServlet.MATCH_SCHEME_ON_REDIRECT );
    }

    @Test
    public void testGetRedirectUrlTrue() throws Exception
    {
        System
            .clearProperty( BaseServlet.MATCH_SCHEME_ON_REDIRECT );
        System.setProperty( BaseServlet.MATCH_SCHEME_ON_REDIRECT,
            "true" );

        ServiceUrls urls = new ServiceUrls();
        urls.addUrl( "http://example.com" );
        urls.addUrl( "https://example.com" );

        BaseServlet servlet = makeBaseServlet();

        assertEquals( "http://example.com/bar", servlet.getRedirectUrl( "http", "/bar", urls ) );
        assertEquals( "https://example.com/bar", servlet.getRedirectUrl( "https", "/bar", urls ) );

        System
            .clearProperty( BaseServlet.MATCH_SCHEME_ON_REDIRECT );
    }

    @Test
    public void testGetRedirectUrlTrueOnlyHttp() throws Exception
    {
        System
            .clearProperty( BaseServlet.MATCH_SCHEME_ON_REDIRECT );
        System.setProperty( BaseServlet.MATCH_SCHEME_ON_REDIRECT,
            "true" );

        ServiceUrls urls = new ServiceUrls();
        urls.addUrl( "http://example.com" );

        BaseServlet servlet = makeBaseServlet();

        assertEquals( "http://example.com/bar", servlet.getRedirectUrl( "http", "/bar", urls ) );
        assertEquals( "http://example.com/bar", servlet.getRedirectUrl( "https", "/bar", urls ) );

        System
            .clearProperty( BaseServlet.MATCH_SCHEME_ON_REDIRECT );
    }

    @Test
    public void testGetRedirectUrlTrueOnlyHttps() throws Exception
    {
        System
            .clearProperty( BaseServlet.MATCH_SCHEME_ON_REDIRECT );
        System.setProperty( BaseServlet.MATCH_SCHEME_ON_REDIRECT,
            "true" );

        ServiceUrls urls = new ServiceUrls();
        urls.addUrl( "https://example.com" );

        BaseServlet servlet = makeBaseServlet();

        assertEquals( "https://example.com/bar", servlet.getRedirectUrl( "http", "/bar", urls ) );
        assertEquals( "https://example.com/bar", servlet.getRedirectUrl( "https", "/bar", urls ) );

        System
            .clearProperty( BaseServlet.MATCH_SCHEME_ON_REDIRECT );
    }

}
