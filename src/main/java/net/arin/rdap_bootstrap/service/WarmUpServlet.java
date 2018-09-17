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

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import java.util.Timer;
import java.util.TimerTask;

@WebServlet(name = "_ah_warmup", value = "/_ah/warmup", loadOnStartup = 1 )
public class WarmUpServlet extends HttpServlet
{
    private GcsResources gcsResources;

    public WarmUpServlet()
    {
    }

    public GcsResources getGcsResources()
    {
        if( gcsResources == null )
        {
            gcsResources = new GcsResources();
        }
        return gcsResources;
    }

    @Override
    public void init( ServletConfig config ) throws ServletException
    {
        super.init( config );

        ServletContext context = config.getServletContext();
        context.log( "Loading resource files." );

        AsBootstrap asBootstrap = new AsBootstrap();
        IpV6Bootstrap ipV6Bootstrap = new IpV6Bootstrap();
        IpV4Bootstrap ipV4Bootstrap = new IpV4Bootstrap();
        DomainBootstrap domainBootstrap = new DomainBootstrap();
        DefaultBootstrap defaultBootstrap = new DefaultBootstrap();
        EntityBootstrap entityBootstrap = new EntityBootstrap();

        asBootstrap.loadData( getGcsResources() );
        ipV4Bootstrap.loadData( getGcsResources() );
        ipV6Bootstrap.loadData( getGcsResources() );
        domainBootstrap.loadData( getGcsResources() );
        entityBootstrap.loadData( getGcsResources() );
        defaultBootstrap.loadData( getGcsResources() );

        context.setAttribute( asBootstrap.getClass().getName(), asBootstrap );
        context.setAttribute( ipV4Bootstrap.getClass().getName(), ipV4Bootstrap );
        context.setAttribute( ipV6Bootstrap.getClass().getName(), ipV6Bootstrap );
        context.setAttribute( domainBootstrap.getClass().getName(), domainBootstrap );
        context.setAttribute( defaultBootstrap.getClass().getName(), defaultBootstrap );
        context.setAttribute( entityBootstrap.getClass().getName(), entityBootstrap );

        context.log( "Resource files loaded." );

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                context.log( "Timed task" );
            }
        };
        Timer timer = new Timer();
        timer.schedule( task, 10000L, 10000L );
    }
}
