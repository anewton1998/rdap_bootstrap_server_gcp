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
import net.ripe.ipresource.IpRange;
import net.ripe.ipresource.Ipv4Address;
import net.ripe.ipresource.Ipv6Address;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "RDAP IP", urlPatterns = { "/ip/*" }, loadOnStartup = 2 )
public class IpServlet extends BaseServlet
{

    public IpServlet()
    {
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if( shouldService( req, resp ) )
        {
            String pathInfo = req.getPathInfo();
            serve( new MakeIpBase(), DefaultBootstrap.Type.IP, pathInfo, req, resp );
        }
    }

    public ServiceUrls makeIpBase( String pathInfo )
    {
        return new MakeIpBase().makeBase( pathInfo );
    }

    public class MakeIpBase implements BaseMaker
    {
        public ServiceUrls makeBase( String pathInfo )
        {
            // strip leading "/"
            pathInfo = pathInfo.substring( 1 );
            if ( pathInfo.indexOf( ":" ) == -1 ) // is not ipv6
            {
                if( pathInfo.indexOf( "/" ) == -1 )
                {
                    return getIpv4Bootstrap().getServiceUrls( Ipv4Address.parse( pathInfo ) );
                }
                //else
                return getIpv4Bootstrap().getServiceUrls( IpRange.parse( pathInfo ) );
            }
            // else
            if ( pathInfo.indexOf( "/" ) == -1 )
            {
                return getIpv6Bootstrap().getServiceUrls( Ipv6Address.parse( pathInfo ) );
            }
            //else
            return getIpv6Bootstrap().getServiceUrls( IpRange.parse( pathInfo ) );
        }
    }

}
