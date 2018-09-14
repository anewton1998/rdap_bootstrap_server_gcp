/*
 * Copyright (C) 2013, 2015 American Registry for Internet Numbers (ARIN)
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
import java.io.InputStream;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @version $Rev$, $Date$
 */
public class BaseServletTest
{
    private static final String ARIN = "http://rdap.arin.net/registry";
    private static final String LACNIC = "https://rdap.lacnic.net/rdap";
    private static final String IANA = "http://rdap.iana.org";
    private static final String APNIC = "https://rdap.apnic.net";
    private static final String RIPE = "http://rdap.db.ripe.net";
    private static final String AFRINIC = "http://rdap.rd.me.afrinic.net/whois/AFRINIC";
    private static final String INFO = "http://rdg.afilias.info/rdap";


    private GcsResources makeGcsResourceMock()
    {
        GcsResources mock = mock( GcsResources.class );
        InputStream ipv6 = getClass().getResourceAsStream( "/ipv6.json" );
        when( mock.getInputStream( GcsResources.BootFile.V6 ) ).thenReturn( ipv6 );
        InputStream ipv4 = getClass().getResourceAsStream( "/ipv4.json" );
        when( mock.getInputStream( GcsResources.BootFile.V4 ) ).thenReturn( ipv4 );
        InputStream asn = getClass().getResourceAsStream( "/asn.json" );
        when( mock.getInputStream( GcsResources.BootFile.AS ) ).thenReturn( asn );
        InputStream dns = getClass().getResourceAsStream( "/dns.json" );
        when( mock.getInputStream( GcsResources.BootFile.DOMAIN ) ).thenReturn( dns );
        InputStream entity = getClass().getResourceAsStream( "/entity.json" );
        when( mock.getInputStream( GcsResources.BootFile.ENTITY ) ).thenReturn( entity );
        InputStream dIS = getClass().getResourceAsStream( "/default.json" );
        when( mock.getInputStream( GcsResources.BootFile.DEFAULT ) ).thenReturn( dIS );
        return mock;
    }

    private ServletContext makeServletContext()
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

    private BaseServlet makeRedirectServlet() throws Exception
    {
        BaseServlet servlet = new BaseServlet() {
            @Override
            public ServletContext getServletContext() {
                return makeServletContext();
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

        BaseServlet servlet = makeRedirectServlet();

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

        BaseServlet servlet = makeRedirectServlet();

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

        BaseServlet servlet = makeRedirectServlet();

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

        BaseServlet servlet = makeRedirectServlet();

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

        BaseServlet servlet = makeRedirectServlet();

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

        BaseServlet servlet = makeRedirectServlet();

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

        BaseServlet servlet = makeRedirectServlet();

        assertEquals( "https://example.com/bar", servlet.getRedirectUrl( "http", "/bar", urls ) );
        assertEquals( "https://example.com/bar", servlet.getRedirectUrl( "https", "/bar", urls ) );

        System
            .clearProperty( BaseServlet.MATCH_SCHEME_ON_REDIRECT );
    }

    @Test
    public void testMakeAutNumInt() throws Exception
    {
        BaseServlet servlet = makeRedirectServlet();

        assertEquals( ARIN, servlet.makeAutnumBase( "/autnum/10" ).getHttpUrl() );
        //TODO re-enable when their servers are put back in the bootstrap files
        //assertEquals( "http://rdap.db.ripe.net", asBootstrap.getServiceUrls( "7" ).getHttpUrl() );
        //assertEquals( RIPE, servlet.makeAutnumBase( "/autnum/42222" ).getHttpUrl() );
    }

    @Test
    public void testMakeIpBase() throws Exception
    {
        BaseServlet servlet = makeRedirectServlet();

        assertEquals( ARIN, servlet.makeIpBase( "/ip/7.0.0.0/8" ).getHttpUrl() );
        assertEquals( ARIN, servlet.makeIpBase( "/ip/7.0.0.0/16" ).getHttpUrl() );
        assertEquals( LACNIC, servlet.makeIpBase( "/ip/191.0.1.0/24" ).getHttpsUrl() );
        assertEquals( ARIN,
            servlet.makeIpBase( "/ip/2620:0000:0000:0000:0000:0000:0000:0000" ).getHttpUrl() );
        //TODO renable when their server are put back in the bootstrap files
        //assertEquals( AFRINIC, servlet.makeIpBase( "/ip/2c00:0000::/12" ).getHttpUrl() );
        assertEquals( LACNIC, servlet.makeIpBase( "/ip/2800:0000::/12" ).getHttpsUrl() );
        //TODO renable when their server are put back in the bootstrap files
        //assertEquals( IANA, servlet.makeIpBase( "/ip/2001:0000::1" ).getHttpUrl() );

        assertEquals( LACNIC, servlet.makeIpBase( "/ip/191.0.1.1/32" ).getHttpsUrl() );
        assertEquals( LACNIC, servlet.makeIpBase( "/ip/191.0.1.1" ).getHttpsUrl() );
    }

    @Test
    public void testMakeDomainBase() throws Exception
    {
        BaseServlet servlet = makeRedirectServlet();

        assertEquals( INFO, servlet.makeDomainBase( "/domain/example.INFO" ).getHttpUrl() );
        assertEquals( INFO, servlet.makeDomainBase( "/domain/example.INFO." ).getHttpUrl() );
        assertEquals( ARIN,
            servlet.makeDomainBase( "/domain/0.0.0.7.in-addr.arpa." ).getHttpUrl() );
        assertEquals( ARIN, servlet.makeDomainBase( "/domain/0.0.0.7.in-addr.arpa" ).getHttpUrl() );
        assertEquals( ARIN, servlet.makeDomainBase( "/domain/0.7.in-addr.arpa" ).getHttpUrl() );
        assertEquals( ARIN, servlet.makeDomainBase( "/domain/7.in-addr.arpa" ).getHttpUrl() );
        assertEquals( ARIN, servlet.makeDomainBase( "/domain/0.2.6.2.ip6.arpa" ).getHttpUrl() );
        //TODO renable when their server are put back in the bootstrap files
        //assertEquals( AFRINIC, servlet.makeDomainBase( "/domain/0.c.2.ip6.arpa" ).getHttpUrl() );
        assertEquals( LACNIC, servlet.makeDomainBase( "/domain/0.0.8.2.ip6.arpa" ).getHttpsUrl() );
    }

    @Test
    public void testMakeNameserverBase() throws Exception
    {
        BaseServlet servlet = makeRedirectServlet();

        assertEquals( INFO,
            servlet.makeNameserverBase( "/nameserver/ns1.example.INFO" ).getHttpUrl() );
        assertEquals( INFO,
            servlet.makeNameserverBase( "/nameserver/ns1.example.INFO." ).getHttpUrl() );
    }

    @Test
    public void testMakeEntityBase() throws Exception
    {
        BaseServlet servlet = makeRedirectServlet();

        assertEquals( ARIN, servlet.makeEntityBase( "/entity/ABC123-ARIN" ).getHttpUrl() );
        assertEquals( RIPE, servlet.makeEntityBase( "/entity/ABC123-RIPE" ).getHttpUrl() );
        assertEquals( APNIC, servlet.makeEntityBase( "/entity/ABC123-AP" ).getHttpsUrl() );
        assertEquals( LACNIC, servlet.makeEntityBase( "/entity/ABC123-LACNIC" ).getHttpsUrl() );
        //TODO renable when their server are put back in the bootstrap files
        //assertEquals( AFRINIC, servlet.makeEntityBase( "/entity/ABC123-AFRINIC" ).getHttpUrl() );
    }
}
