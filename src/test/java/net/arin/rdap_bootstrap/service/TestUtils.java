/*
 * Copyright (C) 2018 American Registry for Internet Numbers (ARIN)
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

import javax.servlet.ServletContext;
import java.io.InputStream;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestUtils
{

    static final String ARIN = "http://rdap.arin.net/registry";
    static final String LACNIC = "https://rdap.lacnic.net/rdap";
    static final String IANA = "http://rdap.iana.org";
    static final String APNIC = "https://rdap.apnic.net";
    static final String RIPE = "http://rdap.db.ripe.net";
    static final String AFRINIC = "http://rdap.rd.me.afrinic.net/whois/AFRINIC";
    static final String INFO = "http://rdg.afilias.info/rdap";

    private static GcsResources makeGcsResourceMock()
    {
        GcsResources mock = mock( GcsResources.class );
        InputStream ipv6 = BaseServlet.class.getResourceAsStream( "/ipv6.json" );
        when( mock.getInputStream( GcsResources.BootFile.V6 ) ).thenReturn( ipv6 );
        InputStream ipv4 = BaseServlet.class.getResourceAsStream( "/ipv4.json" );
        when( mock.getInputStream( GcsResources.BootFile.V4 ) ).thenReturn( ipv4 );
        InputStream asn = BaseServlet.class.getResourceAsStream( "/asn.json" );
        when( mock.getInputStream( GcsResources.BootFile.AS ) ).thenReturn( asn );
        InputStream dns = BaseServlet.class.getResourceAsStream( "/dns.json" );
        when( mock.getInputStream( GcsResources.BootFile.DOMAIN ) ).thenReturn( dns );
        InputStream entity = BaseServlet.class.getResourceAsStream( "/entity.json" );
        when( mock.getInputStream( GcsResources.BootFile.ENTITY ) ).thenReturn( entity );
        InputStream dIS = BaseServlet.class.getResourceAsStream( "/default.json" );
        when( mock.getInputStream( GcsResources.BootFile.DEFAULT ) ).thenReturn( dIS );
        return mock;
    }

    static ServletContext makeServletContext()
    {
        GcsResources gcsResources = makeGcsResourceMock();

        AsBootstrap asBootstrap = new AsBootstrap();
        IpV6Bootstrap ipV6Bootstrap = new IpV6Bootstrap();
        IpV4Bootstrap ipV4Bootstrap = new IpV4Bootstrap();
        DomainBootstrap domainBootstrap = new DomainBootstrap();
        DefaultBootstrap defaultBootstrap = new DefaultBootstrap();
        EntityBootstrap entityBootstrap = new EntityBootstrap();

        asBootstrap.loadData( gcsResources );
        ipV4Bootstrap.loadData( gcsResources );
        ipV6Bootstrap.loadData( gcsResources );
        domainBootstrap.loadData( gcsResources );
        entityBootstrap.loadData( gcsResources );
        defaultBootstrap.loadData( gcsResources );

        ServletContext mock = mock( ServletContext.class );
        when( mock.getAttribute( AsBootstrap.class.getName() ) ).thenReturn( asBootstrap );
        when( mock.getAttribute( IpV4Bootstrap.class.getName() ) ).thenReturn( ipV4Bootstrap );
        when( mock.getAttribute( IpV6Bootstrap.class.getName() ) ).thenReturn( ipV6Bootstrap );
        when( mock.getAttribute( DomainBootstrap.class.getName() ) ).thenReturn( domainBootstrap );
        when( mock.getAttribute( DefaultBootstrap.class.getName() ) ).thenReturn( domainBootstrap );
        when( mock.getAttribute( EntityBootstrap.class.getName() ) ).thenReturn( entityBootstrap );

        return mock;
    }
}
