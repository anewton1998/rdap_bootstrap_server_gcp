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

public class AutnumServletTest
{
    private AutnumServlet makeAutnumServlet() throws Exception
    {
        AutnumServlet servlet = new AutnumServlet() {
            @Override
            public ServletContext getServletContext() {
                return TestUtils.makeServletContext();
            }
        };
        servlet.init( null );
        return servlet;
    }

    @Test
    public void testMakeAutNumInt() throws Exception
    {
        AutnumServlet servlet = makeAutnumServlet();

        assertEquals( TestUtils.ARIN, servlet.makeAutnumBase( "/10" ).getHttpUrl() );
        //TODO re-enable when their servers are put back in the bootstrap files
        //assertEquals( "http://rdap.db.ripe.net", asBootstrap.getServiceUrls( "7" ).getHttpUrl() );
        //assertEquals( RIPE, servlet.makeAutnumBase( "/autnum/42222" ).getHttpUrl() );
    }

}
