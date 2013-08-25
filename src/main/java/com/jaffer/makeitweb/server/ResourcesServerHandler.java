/*
 * JBoss, Home of Professional Open Source
 *
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @author tags. See the COPYRIGHT.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.jaffer.makeitweb.server;

import java.io.InputStream;

import javax.activation.MimetypesFileTypeMap;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;

import com.jaffer.makeitweb.Configuration;
import com.jaffer.makeitweb.Main;

/**
 *
 * @author daijun
 */
public class ResourcesServerHandler extends SimpleChannelUpstreamHandler {

    private String urlMapping;
    private static final String WEB_RESOURCE_PREFIX="web/";
    private final StringBuilder responseContent = new StringBuilder();

    public ResourcesServerHandler(String urlMapping) {
        this.urlMapping = urlMapping;
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
    	// System.out.println("request URL :"+e.getRemoteAddress().toString());
        if (e.getMessage() instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) e.getMessage();
            System.out.println("URL : "+request.getUri());
            String requestedURI=request.getUri();
            if(requestedURI.contains("app_Id"))
            {
            	Main.initializeApp();
            	Configuration.mainToSelect=Integer.parseInt(requestedURI.split("=")[1].trim());
            	System.out.print("Main :"+Configuration.mainToSelect);
            	requestedURI="/";
            	
            }
            if (requestedURI.startsWith(urlMapping)) {
                String reqResource=WEB_RESOURCE_PREFIX+requestedURI.substring(urlMapping.length());
                if(reqResource.trim().equals(WEB_RESOURCE_PREFIX)){
                    reqResource=WEB_RESOURCE_PREFIX+"index.html";
                }
                InputStream input = this.getClass().getClassLoader().getResourceAsStream(reqResource);
                if (input != null) {
                    ChannelBuffer channel = ChannelBuffers.dynamicBuffer();
                    byte[] bs = new byte[1024];
                    int length=0;
                    while ((length=input.read(bs)) > 0) {
                         channel.writeBytes(bs,0,length);
                         bs= new byte[1024];
                    }
                    input.close();
                    String contenttype = new MimetypesFileTypeMap().getContentType(reqResource);
                    writeResponse(channel,contenttype, e);
                } else {
                    ctx.sendUpstream(e);
                }
            } else {
                ctx.sendUpstream(e);
            }
        }else{
            ctx.sendUpstream(e);
        }
    }

    private void writeResponse(ChannelBuffer buf, String contentType, MessageEvent e) {
        responseContent.setLength(0);
        // Build the response object.
        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        response.setContent(buf);
        response.setHeader(HttpHeaders.Names.CONTENT_TYPE, contentType);
        response.setHeader(HttpHeaders.Names.CONNECTION, "keep-alive");
        response.setHeader(HttpHeaders.Names.CONTENT_LENGTH, String.valueOf(buf.readableBytes()));
        e.getChannel().write(response);
    }

}
