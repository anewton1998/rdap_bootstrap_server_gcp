/*
 * Copyright (C) 2013-2015 American Registry for Internet Numbers (ARIN)
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

import java.util.HashMap;
import java.util.Set;

import net.arin.rdap_bootstrap.service.JsonBootstrapFile.ServiceUrls;
import net.ripe.ipresource.IpRange;
import net.ripe.ipresource.Ipv4Address;
import net.ripe.ipresource.UniqueIpResource;
import net.ripe.ipresource.etree.IntervalMap;
import net.ripe.ipresource.etree.IpResourceIntervalStrategy;
import net.ripe.ipresource.etree.NestedIntervalMap;

/**
 * @version $Rev$, $Date$
 */
public class IpV4Bootstrap implements JsonBootstrapFile.Handler
{
    private volatile IntervalMap<IpRange,ServiceUrls> allocations = new NestedIntervalMap<>( IpResourceIntervalStrategy.getInstance() );
    private IntervalMap<IpRange,ServiceUrls> _allocations;

    private ServiceUrls serviceUrls;
    private String publication;
    private String description;

    public void loadData( GcsResources gcsResources )
    {
        JsonBootstrapFile bsFile = new JsonBootstrapFile();
        bsFile.loadData( gcsResources.getInputStream( GcsResources.BootFile.V4 ), this );
    }

    @Override
    public void startServices()
    {
        _allocations = new NestedIntervalMap<>( IpResourceIntervalStrategy.getInstance() );
    }

    @Override
    public void endServices()
    {
        allocations = _allocations;
    }

    @Override
    public void startService()
    {
        serviceUrls = new ServiceUrls();
    }

    @Override
    public void endService()
    {
        // nothing to do
    }

    @Override
    public void addServiceEntry( String entry )
    {
        _allocations.put( IpRange.parse( entry ), serviceUrls );
    }

    @Override
    public void addServiceUrl( String url )
    {
        serviceUrls.addUrl( url );
    }

    public ServiceUrls getServiceUrls( IpRange ipRange )
    {
        return allocations.findExactOrFirstLessSpecific( ipRange );
    }

    public ServiceUrls getServiceUrls( Ipv4Address ipv4Address )
    {
        return allocations.findExactOrFirstLessSpecific( IpRange.range( ipv4Address, ipv4Address ) );
    }

    @Override
    public void setPublication( String publication )
    {
        this.publication = publication;
    }

    public String getPublication()
    {
        return publication;
    }

    public String getDescription()
    {
        return description;
    }

    @Override
    public void setDescription( String description )
    {
        this.description = description;
    }
}
