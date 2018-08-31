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
 */
package net.arin.rdap_bootstrap.service;

import com.google.cloud.ReadChannel;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import java.io.InputStream;
import java.nio.channels.Channels;

import static net.arin.rdap_bootstrap.Constants.BUCKET_NAME_PROPERTY;

/**
 * Manages getting InputStreams from Google Cloud Storage.
 */
public class GcsResources
{
    public enum BootFile
    {
        DEFAULT( "default.json" ),
        AS( "asn.json" ),
        DOMAIN( "dns.json" ),
        V4( "ipv4.json" ),
        V6( "ipv6.json" ),
        ENTITY( "entity.json" );

        private String blobName;

        public String getBlobName()
        {
            return blobName;
        }

        BootFile( String blobName)
        {
            this.blobName = blobName;
        }
    }

    public InputStream getInputStream( BootFile bootFile )
    {
        Storage storage = StorageOptions.getDefaultInstance().getService();

        String bucketName = System.getProperty( BUCKET_NAME_PROPERTY );
        Bucket bucket = storage.get( bucketName );

        Blob blob = bucket.get( bootFile.getBlobName() );
        ReadChannel readChannel = blob.reader();
        InputStream inputStream = Channels.newInputStream( readChannel );
        return inputStream;
    }
}
