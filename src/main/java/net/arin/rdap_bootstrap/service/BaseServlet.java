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
import com.googlecode.ipv6.IPv6Network;
import net.arin.rdap_bootstrap.service.JsonBootstrapFile.ServiceUrls;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "RDAP Bootstrap Server", urlPatterns = { "/help", "/domain/*", "/nameserver/*", "/ip/*", "/entity/*", "/autnum/*" } )
public class BaseServlet extends HttpServlet
{
    protected Boolean matchSchemeOnRedirect = Boolean.FALSE;

    protected static final String MATCH_SCHEME_ON_REDIRECT = "rdap.match_scheme_on_redirect";

    public BaseServlet()
    {
    }

    protected DefaultBootstrap getDefaultBootstrap()
    {
        return (DefaultBootstrap) getServletContext().getAttribute( DefaultBootstrap.class.getName() );
    }

    protected AsBootstrap getAsBootstrap()
    {
        return (AsBootstrap) getServletContext().getAttribute( AsBootstrap.class.getName() );
    }

    protected IpV4Bootstrap getIpv4Bootstrap()
    {
        return (IpV4Bootstrap) getServletContext().getAttribute( IpV4Bootstrap.class.getName() );
    }

    protected IpV6Bootstrap getIpv6Bootstrap()
    {
        return (IpV6Bootstrap) getServletContext().getAttribute( IpV6Bootstrap.class.getName() );
    }

    protected DomainBootstrap getDomainBootstrap()
    {
        return (DomainBootstrap) getServletContext().getAttribute( DomainBootstrap.class.getName() );
    }

    protected EntityBootstrap getEntityBootstrap()
    {
        return (EntityBootstrap) getServletContext().getAttribute( EntityBootstrap.class.getName() );
    }

    @Override
    public void init( ServletConfig config ) throws ServletException
    {
        super.init( config );
        if( config != null )
        {
            config.getServletContext().log( "Starting " + this.getClass().getSimpleName() );
        }

        matchSchemeOnRedirect = Boolean.valueOf( System
            .getProperty( MATCH_SCHEME_ON_REDIRECT,
                matchSchemeOnRedirect.toString() ) );
    }

    protected void serve( BaseMaker baseMaker, DefaultBootstrap.Type defaultType,
                          String pathInfo, HttpServletRequest req, HttpServletResponse resp )
        throws IOException
    {
        try
        {
            ServiceUrls urls = baseMaker.makeBase( pathInfo );
            if ( urls == null && defaultType != null )
            {
                urls = getDefaultBootstrap().getServiceUrls( defaultType );
            }
            if ( urls == null )
            {
                resp.sendError( HttpServletResponse.SC_NOT_FOUND );
            }
            else
            {
                String redirectUrl = getRedirectUrl( req.getScheme(), req.getPathInfo(), urls );
                resp.sendRedirect( redirectUrl );
            }
        }
        catch ( Exception e )
        {
            resp.sendError( HttpServletResponse.SC_BAD_REQUEST, e.getMessage() );
        }

    }

    protected String getRedirectUrl( String scheme, String pathInfo, ServiceUrls urls )
    {
        String redirectUrl = null;
        if ( matchSchemeOnRedirect )
        {
            if ( scheme.equals( "https" ) && urls.getHttpsUrl() != null )
            {
                redirectUrl = urls.getHttpsUrl() + pathInfo;
            }
            else if ( scheme.equals( "http" ) && urls.getHttpUrl() != null )
            {
                redirectUrl = urls.getHttpUrl() + pathInfo;
            }
            else
            {
                redirectUrl = urls.getUrls().get( 0 ) + pathInfo;
            }
        }
        else
        {
            redirectUrl = urls.getHttpsUrl();
            if ( redirectUrl == null )
            {
                redirectUrl = urls.getHttpUrl();
            }
            if ( redirectUrl != null )
            {
                redirectUrl += pathInfo;
            }
        }
        return redirectUrl;
    }

    protected boolean shouldService( HttpServletRequest req, HttpServletResponse resp ) throws IOException {
        if( req == null )
        {
            resp.sendError( HttpServletResponse.SC_BAD_REQUEST, "No valid request given." );
            return false;
        }
        //else
        if( req.getPathInfo() == null )
        {
            resp.sendError( HttpServletResponse.SC_BAD_REQUEST, "No path information given." );
            return false;
        }
        //else
        return true;
    }
/*

    @Override
    protected void service( HttpServletRequest req, HttpServletResponse resp )
        throws ServletException, IOException
    {
        if( req == null )
        {
            resp.sendError( HttpServletResponse.SC_BAD_REQUEST, "No valid request given." );
        }
        else if( req.getPathInfo() == null )
        {
            resp.sendError( HttpServletResponse.SC_BAD_REQUEST, "No path information given." );
        }
        else
        {
            String pathInfo = req.getPathInfo();
            if ( pathInfo.startsWith( "/domain/" ) )
            {
                serve( new MakeDomainBase(), Type.DOMAIN, pathInfo, req, resp );
            }
            else if ( pathInfo.startsWith( "/nameserver/" ) )
            {
                serve( new MakeNameserverBase(), Type.NAMESERVER, pathInfo, req,
                    resp );
            }
            else if ( pathInfo.startsWith( "/ip/" ) )
            {
                serve( new MakeIpBase(), Type.IP, pathInfo, req, resp );
            }
            else if ( pathInfo.startsWith( "/entity/" ) )
            {
                serve( new MakeEntityBase(), Type.ENTITY, pathInfo, req, resp );
            }
            else if ( pathInfo.startsWith( "/autnum/" ) )
            {
                serve( new MakeAutnumBase(), Type.AUTNUM, pathInfo, req, resp );
            }
            else if ( pathInfo.startsWith( "/help" ) )
            {
                resp.setContentType( "application/rdap+json" );
                makeHelp( resp.getOutputStream() );
            }
            else
            {
                resp.sendError( HttpServletResponse.SC_NOT_FOUND, "Unknown RDAP Query Type: " + pathInfo );
            }
        }
    }
*/

    public interface BaseMaker
    {
        public ServiceUrls makeBase( String pathInfo );
    }

    public ServiceUrls makeAutnumBase( String pathInfo )
    {
        return new MakeAutnumBase().makeBase( pathInfo );
    }

    public class MakeAutnumBase implements BaseMaker
    {
        public ServiceUrls makeBase( String pathInfo )
        {
            return getAsBootstrap().getServiceUrls( pathInfo.split( "/" )[2] );
        }
    }

    public ServiceUrls makeNameserverBase( String pathInfo )
    {
        return new MakeNameserverBase().makeBase( pathInfo );
    }

    public class MakeNameserverBase implements BaseMaker
    {
        public ServiceUrls makeBase( String pathInfo )
        {
            // strip leading "/nameserver/"
            pathInfo = pathInfo.substring( 12 );
            // strip possible trailing period
            if ( pathInfo.endsWith( "." ) )
            {
                pathInfo = pathInfo.substring( 0, pathInfo.length() - 1 );
            }
            String[] labels = pathInfo.split( "\\." );
            return getDomainBootstrap().getServiceUrls( labels[labels.length - 1] );
        }
    }

    public ServiceUrls makeEntityBase( String pathInfo )
    {
        return new MakeEntityBase().makeBase( pathInfo );
    }

    public class MakeEntityBase implements BaseMaker
    {
        public ServiceUrls makeBase( String pathInfo )
        {
            int i = pathInfo.lastIndexOf( '-' );
            if ( i != -1 && i + 1 < pathInfo.length() )
            {
                return getEntityBootstrap().getServiceUrls( pathInfo.substring( i + 1 ) );
            }
            // else
            return null;
        }

    }
}
