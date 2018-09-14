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

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "RDAP Entity", urlPatterns = { "/entity/*" } )
public class EntityServlet extends BaseServlet
{

    public EntityServlet()
    {
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if( shouldService( req, resp ) )
        {
            String pathInfo = req.getPathInfo();
            serve( new MakeEntityBase(), DefaultBootstrap.Type.ENTITY, pathInfo, req, resp );

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
