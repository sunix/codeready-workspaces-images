# Copyright (c) 2019 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#
# Contributors:
#   Red Hat, Inc. - initial API and implementation
#

# this container build continues from rhel.Dockerfile and rhel.Dockefile.extract.assets.sh
# assumes you have created asset-*.tar.gz files for all arches, but you'll only unpack the one for your arch

# https://access.redhat.com/containers/?tab=tags#/registry.access.redhat.com/ubi8-minimal
FROM ubi8-minimal:8.3-230
COPY asset-*.tar.gz /tmp/assets/
RUN microdnf -y install tar gzip shadow-utils && \
    adduser appuser && \
    tar xzf /tmp/assets/asset-configbump-$(uname -m).tar.gz -C / && \
    rm -fr /tmp/assets/ && \
    chmod 755 /usr/local/bin/configbump && \
    microdnf -y remove tar gzip shadow-utils && \
    echo "Installed Packages" && rpm -qa | sort -V && echo "End Of Installed Packages"
USER appuser
ENTRYPOINT ["configbump"]

# append Brew metadata here
ENV SUMMARY="Red Hat CodeReady Workspaces configbump container" \
DESCRIPTION="Red Hat CodeReady Workspaces configbump container" \
PRODNAME="codeready-workspaces" \
COMPNAME="configbump-rhel8" 
LABEL summary="$SUMMARY" \
description="$DESCRIPTION" \
io.k8s.description="$DESCRIPTION" \
io.k8s.display-name="$DESCRIPTION" \
io.openshift.tags="$PRODNAME,$COMPNAME" \
com.redhat.component="$PRODNAME-$COMPNAME-container" \
name="$PRODNAME/$COMPNAME" \
version="2.6" \
license="EPLv2" \
maintainer="Nick Boldt <nboldt@redhat.com>" \
io.openshift.expose-services="" \
usage="" 
