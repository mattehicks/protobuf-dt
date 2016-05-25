/*
 * Copyright (c) 2016 Google Inc.
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.google.eclipse.protobuf.scoping;

import com.google.eclipse.protobuf.model.util.Imports;
import com.google.eclipse.protobuf.model.util.Protobufs;
import com.google.eclipse.protobuf.model.util.Resources;
import com.google.eclipse.protobuf.protobuf.Import;
import com.google.eclipse.protobuf.protobuf.Protobuf;
import com.google.inject.Inject;
import com.google.inject.Provider;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.scoping.impl.ImportUriGlobalScopeProvider;
import org.eclipse.xtext.util.IResourceScopeCache;

import java.util.LinkedHashSet;

/**
 * A global scope provider that reads each {@link Import} in the protobuf file, resolves it, and
 * caches the result.
 */
public class ProtobufImportUriGlobalScopeProvider extends ImportUriGlobalScopeProvider {
  @Inject private Protobufs protobufs;
  @Inject private Resources resources;
  @Inject private Imports imports;
  @Inject private IResourceScopeCache cache;

  @Override
  protected LinkedHashSet<URI> getImportedUris(final Resource resource) {
    return cache.get(
        ProtobufImportUriGlobalScopeProvider.class.getName(),
        resource,
        new Provider<LinkedHashSet<URI>>() {
          @Override
          public LinkedHashSet<URI> get() {
            LinkedHashSet<URI> importedUris = new LinkedHashSet<>();
            Protobuf protobuf = resources.rootOf(resource);
            if (protobuf == null) {
              return importedUris;
            }
            for (Import singleImport : protobufs.importsIn(protobuf)) {
              resolveImport(importedUris, singleImport);
            }
            return importedUris;
          }

          private void addPublicImportedUris(Protobuf protobuf, LinkedHashSet<URI> importedUris) {
            for (Import singleImport : protobufs.publicImportsIn(protobuf)) {
              resolveImport(importedUris, singleImport);
            }
          }

          private void resolveImport(LinkedHashSet<URI> importedUris, Import singleImport) {
            if (imports.isResolved(singleImport)) {
              importedUris.add(imports.resolvedUriOf(singleImport));
              Protobuf root = resources.rootOf(imports.importedResource(singleImport));
              if (root != null) {
                addPublicImportedUris(root, importedUris);
              }
            }
          }
        });
  }
}
