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

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import net.ripe.ipresource.IpRange;
import net.ripe.ipresource.Ipv4Address;
import org.junit.Test;

import java.io.InputStream;

/**
 * @version $Rev$, $Date$
 */
public class IpV4BootstrapTest
{
    @Test
    public void testAllocations() throws Exception
    {
        InputStream inputStream = getClass().getResourceAsStream( "/ipv4.json" );
        GcsResources mock = mock( GcsResources.class );
        when( mock.getInputStream( GcsResources.BootFile.V4 ) ).thenReturn( inputStream );
        IpV4Bootstrap v4 = new IpV4Bootstrap();
        v4.loadData( mock );

        assertEquals( "https://rdap.apnic.net", v4.getServiceUrls( Ipv4Address.parse( "1.0.0.0" ) ).getHttpsUrl() );
        //TODO renable when their server are put back in the bootstrap files
        //assertEquals( "http://rdap.iana.org", v4.getServiceUrls( 0 ).getHttpUrl() );
        assertEquals( "https://rdap.apnic.net", v4.getServiceUrls( Ipv4Address.parse( "27.0.0.0" ) ).getHttpsUrl() );
        assertEquals( "https://rdap.db.ripe.net", v4.getServiceUrls( Ipv4Address.parse( "31.0.0.0" ) ).getHttpsUrl() );
        assertEquals( "http://rdap.afrinic.net/rdap", v4.getServiceUrls( Ipv4Address.parse( "41.0.0.0" ) ).getHttpUrl() );
        assertEquals( "https://rdap.lacnic.net/rdap", v4.getServiceUrls( Ipv4Address.parse( "177.0.0.0" ) ).getHttpsUrl() );
        assertEquals( "https://rdap.db.ripe.net", v4.getServiceUrls( Ipv4Address.parse( "188.0.0.0" ) ).getHttpsUrl() );
        assertEquals( "https://rdap.lacnic.net/rdap", v4.getServiceUrls( Ipv4Address.parse( "191.0.0.0" ) ).getHttpsUrl() );

        // Testing for full prefixes
        assertEquals( "https://rdap.lacnic.net/rdap",
            v4.getServiceUrls( IpRange.parse( "177.0.0.0/8" ) ).getHttpsUrl() );

        // Testing for host addresses
        assertEquals( "https://rdap.lacnic.net/rdap",
            v4.getServiceUrls( IpRange.parse( "177.0.0.1/32" ) ).getHttpsUrl() );
        assertEquals( "https://rdap.lacnic.net/rdap",
            v4.getServiceUrls( Ipv4Address.parse( "177.0.0.1" ) ).getHttpsUrl() );
    }
}
