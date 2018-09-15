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

import org.junit.Test;

import javax.servlet.ServletContext;

import static junit.framework.TestCase.assertEquals;

public class DomainServletTest
{
    private DomainServlet makeDomainServlet() throws Exception
    {
        DomainServlet servlet = new DomainServlet() {
            @Override
            public ServletContext getServletContext() {
                return TestUtils.makeServletContext();
            }
        };
        servlet.init( null );
        return servlet;
    }

    @Test
    public void testMakeDomainBase() throws Exception
    {
        DomainServlet servlet = makeDomainServlet();

        assertEquals( TestUtils.INFO, servlet.makeDomainBase( "/example.INFO" ).getHttpUrl() );
        assertEquals( TestUtils.INFO, servlet.makeDomainBase( "/example.INFO." ).getHttpUrl() );
        assertEquals( TestUtils.ARIN,
                servlet.makeDomainBase( "/0.0.0.7.in-addr.arpa." ).getHttpUrl() );
        assertEquals( TestUtils.ARIN, servlet.makeDomainBase( "/0.0.0.7.in-addr.arpa" ).getHttpUrl() );
        assertEquals( TestUtils.ARIN, servlet.makeDomainBase( "/0.7.in-addr.arpa" ).getHttpUrl() );
        assertEquals( TestUtils.ARIN, servlet.makeDomainBase( "/7.in-addr.arpa" ).getHttpUrl() );
        assertEquals( TestUtils.ARIN, servlet.makeDomainBase( "/0.2.6.2.ip6.arpa" ).getHttpUrl() );
        //TODO renable when their server are put back in the bootstrap files
        //assertEquals( AFRINIC, servlet.makeDomainBase( "/0.c.2.ip6.arpa" ).getHttpUrl() );
        assertEquals( TestUtils.LACNIC, servlet.makeDomainBase( "/0.0.8.2.ip6.arpa" ).getHttpsUrl() );
    }


}
