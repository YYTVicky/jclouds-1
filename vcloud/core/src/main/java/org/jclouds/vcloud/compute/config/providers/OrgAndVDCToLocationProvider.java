/**
 *
 * Copyright (C) 2009 Cloud Conscious, LLC. <info@cloudconscious.com>
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ====================================================================
 */
package org.jclouds.vcloud.compute.config.providers;

import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.jclouds.domain.Location;
import org.jclouds.domain.LocationScope;
import org.jclouds.domain.internal.LocationImpl;
import org.jclouds.vcloud.domain.NamedResource;
import org.jclouds.vcloud.endpoints.Org;
import org.jclouds.vcloud.endpoints.VDC;

import com.google.common.base.Supplier;
import com.google.common.collect.Sets;

/**
 * @author Adrian Cole
 */
@Singleton
public class OrgAndVDCToLocationProvider implements Provider<Set<? extends Location>> {
   private final String providerName;
   private final Supplier<Map<String, NamedResource>> orgNameToResource;
   private final Supplier<Map<String, Map<String, NamedResource>>> orgNameToVDCResource;

   @Inject
   OrgAndVDCToLocationProvider(@org.jclouds.rest.annotations.Provider String providerName,
         @Org Supplier<Map<String, NamedResource>> orgNameToResource,
         @VDC Supplier<Map<String, Map<String, NamedResource>>> orgNameToVDCResource) {
      this.providerName = providerName;
      this.orgNameToResource = orgNameToResource;
      this.orgNameToVDCResource = orgNameToVDCResource;
   }

   @Override
   public Set<? extends Location> get() {
      Location provider = new LocationImpl(LocationScope.PROVIDER, providerName, providerName, null);
      Set<Location> locations = Sets.newLinkedHashSet();

      for (NamedResource org : orgNameToResource.get().values()) {
         Location orgL = new LocationImpl(LocationScope.REGION, org.getName(), org.getLocation().toASCIIString(),
               provider);
         for (NamedResource vdc : orgNameToVDCResource.get().get(org.getName()).values()) {
            locations.add(new LocationImpl(LocationScope.ZONE, vdc.getName(), vdc.getLocation().toASCIIString(), orgL));
         }
      }
      return locations;
   }
}