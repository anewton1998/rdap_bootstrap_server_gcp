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

import com.googlecode.ipv6.IPv6Address;
import net.arin.rdap_bootstrap.service.DefaultBootstrap.Type;
import net.arin.rdap_bootstrap.service.JsonBootstrapFile.ServiceUrls;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;

@WebServlet(name = "RDAP Domain", urlPatterns = { "/domain/*" }, loadOnStartup = 2 )
public class DomainServlet extends BaseServlet
{
    public DomainServlet()
    {
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if( shouldService( req, resp ) )
        {
            String pathInfo = req.getPathInfo();
            serve( new MakeDomainBase(), Type.DOMAIN, pathInfo, req, resp );
        }
    }


    public class MakeDomainBase implements BaseMaker
    {
        public ServiceUrls makeBase( String pathInfo )
        {
            // strip leading "/domain/"
            pathInfo = pathInfo.substring( 8 );
            // strip possible trailing period
            if ( pathInfo.endsWith( "." ) )
            {
                pathInfo = pathInfo.substring( 0, pathInfo.length() - 1 );
            }
            if ( pathInfo.endsWith( ".in-addr.arpa" ) )
            {
                final int BITS_PER_WORD = 8, MIVAR = 1;
                final String DELIMITER = ".";

                String[] words = new String[4];
                Arrays.fill( words, "0" );

                final String[] _split = pathInfo.split( "\\." );
                int n = _split.length - 2;

                String s = "", _s = "";
                for ( int i = n - 1, j = 1; i >= 0; i--, j++ )
                {
                    _s += _split[i];
                    if ( j % MIVAR == 0 )
                    {
                        words[j / MIVAR - 1] = _s;
                        _s = "";
                    }
                }

                for ( int i = 0; i < words.length - 1; i++ )
                {// todos menos el
                    s += words[i] + DELIMITER;
                }
                s += words[words.length - 1];
                s += "/" + BITS_PER_WORD * n;

                return getIpv4Bootstrap().getServiceUrls( s );

            }
            else if ( pathInfo.endsWith( ".ip6.arpa" ) )
            {
                String[] labels = pathInfo.split( "\\." );
                byte[] bytes = new byte[16];
                Arrays.fill( bytes, ( byte ) 0 );
                int labelIdx = labels.length - 3;
                int byteIdx = 0;
                int idxJump = 1;
                while ( labelIdx > 0 )
                {
                    char ch = labels[labelIdx].charAt( 0 );
                    byte value = 0;
                    if ( ch >= '0' && ch <= '9' )
                    {
                        value = ( byte ) ( ch - '0' );
                    }
                    else if ( ch >= 'A' && ch <= 'F' )
                    {
                        value = ( byte ) ( ch - ( 'A' - 0xaL ) );
                    }
                    else if ( ch >= 'a' && ch <= 'f' )
                    {
                        value = ( byte ) ( ch - ( 'a' - 0xaL ) );
                    }
                    if ( idxJump % 2 == 1 )
                    {
                        bytes[byteIdx] = ( byte ) ( value << 4 );
                    }
                    else
                    {
                        bytes[byteIdx] = ( byte ) ( bytes[byteIdx] + value );
                    }
                    labelIdx--;
                    idxJump++;
                    if ( idxJump % 2 == 1 )
                    {
                        byteIdx++;
                    }
                }
                return getIpv6Bootstrap().getServiceUrls( IPv6Address.fromByteArray( bytes ) );
            }
            // else
            String[] labels = pathInfo.split( "\\." );
            return getDomainBootstrap().getServiceUrls( labels[labels.length - 1] );
        }

    }
}
