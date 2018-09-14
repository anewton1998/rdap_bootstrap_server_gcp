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

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.arin.rdap_bootstrap.json.Notice;
import net.arin.rdap_bootstrap.json.Response;
import net.arin.rdap_bootstrap.service.DefaultBootstrap.Type;
import net.arin.rdap_bootstrap.service.JsonBootstrapFile.ServiceUrls;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.googlecode.ipv6.IPv6Address;
import com.googlecode.ipv6.IPv6Network;

@WebServlet(name = "RDAP Bootstrap Server", urlPatterns = { "/help", "/domain/*", "/nameserver/*", "/ip/*", "/entity/*", "/autnum/*" } )
public class RedirectServlet extends HttpServlet
{
    private GcsResources gcsResources;
    Boolean matchSchemeOnRedirect = Boolean.FALSE;

    static final String MATCH_SCHEME_ON_REDIRECT = "rdap.match_scheme_on_redirect";

    public RedirectServlet()
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
            config.getServletContext().log( "Starting bootstrap server" );
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

    String getRedirectUrl( String scheme, String pathInfo, ServiceUrls urls )
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

    public ServiceUrls makeIpBase( String pathInfo )
    {
        return new MakeIpBase().makeBase( pathInfo );
    }

    public class MakeIpBase implements BaseMaker
    {
        public ServiceUrls makeBase( String pathInfo )
        {
            // strip leading "/ip/"
            pathInfo = pathInfo.substring( 4 );
            if ( pathInfo.indexOf( ":" ) == -1 ) // is not ipv6
            {
                // String firstOctet = pathInfo.split( "\\." )[ 0 ];
                return getIpv4Bootstrap().getServiceUrls( pathInfo );
            }
            // else
            IPv6Address addr = null;
            if ( pathInfo.indexOf( "/" ) == -1 )
            {
                addr = IPv6Address.fromString( pathInfo );
            }
            else
            {
                IPv6Network net = IPv6Network.fromString( pathInfo );
                addr = net.getFirst();
            }
            return getIpv6Bootstrap().getServiceUrls( addr );
        }
    }

    public ServiceUrls makeDomainBase( String pathInfo )
    {
        return new MakeDomainBase().makeBase( pathInfo );
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

    public void makeHelp( OutputStream outputStream ) throws IOException
    {
        Response response = new Response( null );
        ArrayList<Notice> notices = new ArrayList<Notice>();

        // Modified dates for various bootstrap files, done this way so that
        // Publication dates can be published as well.
        notices.add( createPublicationDateNotice( "Default",
            getDefaultBootstrap().getPublication() ) );
        notices.add( createPublicationDateNotice( "As",
            getAsBootstrap().getPublication() ) );
        notices.add( createPublicationDateNotice( "Domain",
            getDomainBootstrap().getPublication() ) );
        notices.add( createPublicationDateNotice( "Entity",
            getEntityBootstrap().getPublication() ) );
        notices.add( createPublicationDateNotice( "IpV4",
            getIpv4Bootstrap().getPublication() ) );
        notices.add( createPublicationDateNotice( "IpV6",
            getIpv6Bootstrap().getPublication() ) );

        response.setNotices( notices );

        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion( Include.NON_EMPTY );
        ObjectWriter writer = mapper.writer( new DefaultPrettyPrinter() );
        writer.writeValue( outputStream, response );
    }

    private Notice createPublicationDateNotice( String file,
                                                String publicationDate )
    {
        Notice bootFileModifiedNotice = new Notice();

        bootFileModifiedNotice
            .setTitle( String.format( "%s Bootstrap File and Publication Date", file ) );
        String[] bootFileModifiedDescription = new String[1];
        bootFileModifiedDescription[0] = publicationDate;
        bootFileModifiedNotice.setDescription( bootFileModifiedDescription );

        return bootFileModifiedNotice;
    }
}
