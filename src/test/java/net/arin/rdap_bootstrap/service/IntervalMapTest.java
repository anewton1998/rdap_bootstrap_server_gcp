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

import net.ripe.ipresource.IpRange;
import net.ripe.ipresource.etree.IntervalMap;
import net.ripe.ipresource.etree.IpResourceIntervalStrategy;
import net.ripe.ipresource.etree.NestedIntervalMap;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

/**
 * Tests {@link net.ripe.ipresource.etree.IntervalMap}.
 */
public class IntervalMapTest
{

    @Test
    public void testContiguousClassAs()
    {
        IntervalMap<IpRange,String> map = new NestedIntervalMap<IpRange, String>( IpResourceIntervalStrategy.getInstance());
        IpRange net10 = IpRange.parse( "10.0.0.0/8" );
        IpRange net20 = IpRange.parse( "20.0.0.0/8" );
        IpRange net30 = IpRange.parse( "30.0.0.0/8" );

        map.put( net10, "net10" );
        map.put( net20, "net20" );
        map.put( net30, "net30" );

        // single IPs
        assertEquals( map.findExactOrFirstLessSpecific( IpRange.parse( "10.0.0.2/32" ) ), "net10" );
        assertEquals( map.findExactOrFirstLessSpecific( IpRange.parse( "20.0.1.2/32" ) ), "net20" );
        assertEquals( map.findExactOrFirstLessSpecific( IpRange.parse( "30.1.2.3/32" ) ), "net30" );

        // nets
        assertEquals( map.findExactOrFirstLessSpecific( IpRange.parse( "10.0.0.0/24" ) ), "net10" );
        assertEquals( map.findExactOrFirstLessSpecific( IpRange.parse( "20.0.0.0/24" ) ), "net20" );
        assertEquals( map.findExactOrFirstLessSpecific( IpRange.parse( "30.0.0.0/24" ) ), "net30" );
    }

}
