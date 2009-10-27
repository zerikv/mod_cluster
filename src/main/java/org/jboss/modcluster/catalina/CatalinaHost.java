/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.modcluster.catalina;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.catalina.Container;
import org.jboss.modcluster.Context;
import org.jboss.modcluster.Engine;
import org.jboss.modcluster.Host;

public class CatalinaHost implements Host
{
   private final org.apache.catalina.Host host;
   private final Engine engine;
   
   public CatalinaHost(org.apache.catalina.Host host, Engine engine)
   {
      this.host = host;
      this.engine = engine;
   }
   
   public CatalinaHost(org.apache.catalina.Host host)
   {
      this.host = host;
      this.engine = new CatalinaEngine((org.apache.catalina.Engine) host.getParent());
   }
   
   public Set<String> getAliases()
   {
      String name = this.host.getName();
      String[] aliases = this.host.findAliases();

      if (aliases.length == 0) 
      {
         return Collections.singleton(name);
      }
      
      Set<String> hosts = new LinkedHashSet<String>();
      
      hosts.add(name);
      
      for (String alias: aliases)
      {
         hosts.add(alias);
      }
      
      return hosts;
   }

   public Iterable<Context> getContexts()
   {
      final Iterator<Container> children = Arrays.asList(this.host.findChildren()).iterator();
      
      final Iterator<Context> contexts = new Iterator<Context>()
      {
         public boolean hasNext()
         {
            return children.hasNext();
         }

         public Context next()
         {
            return new CatalinaContext((org.apache.catalina.Context) children.next(), CatalinaHost.this);
         }

         public void remove()
         {
            children.remove();
         }
      };
      
      return new Iterable<Context>()
      {
         public Iterator<Context> iterator()
         {
            return contexts;
         }
      };
   }

   public Engine getEngine()
   {
      return this.engine;
   }

   public String getName()
   {
      return this.host.getName();
   }

   public Context findContext(String path)
   {
      org.apache.catalina.Context context = (org.apache.catalina.Context) this.host.findChild(path);
      
      return (context != null) ? new CatalinaContext(context, this) : null;
   }
   
   public String toString()
   {
      return this.host.getName();
   }
}