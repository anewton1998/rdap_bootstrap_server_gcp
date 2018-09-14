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

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.googlecode.ipv6.IPv6Address;
import com.googlecode.ipv6.IPv6Network;
import net.arin.rdap_bootstrap.json.Notice;
import net.arin.rdap_bootstrap.json.Response;
import net.arin.rdap_bootstrap.service.DefaultBootstrap.Type;
import net.arin.rdap_bootstrap.service.JsonBootstrapFile.ServiceUrls;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;

@WebServlet(name = "RDAP Help", urlPatterns = { "/help" } )
public class HelpServlet extends BaseServlet
{
    public HelpServlet()
    {
    }

    @Override
    protected void service( HttpServletRequest req, HttpServletResponse resp )
        throws IOException
    {
        if( shouldService( req, resp ) )
        {
            resp.setContentType( "application/rdap+json" );
            makeHelp( resp.getOutputStream() );
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
