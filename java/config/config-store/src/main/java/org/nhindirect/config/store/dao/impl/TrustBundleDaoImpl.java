/*
Copyright (c) 2010, NHIN Direct Project
All rights reserved.

Authors:
   Greg Meyer      gm2552@cerner.com

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer
in the documentation and/or other materials provided with the distribution.  Neither the name of the The NHIN Direct Project (nhindirect.org).
nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS
BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
THE POSSIBILITY OF SUCH DAMAGE.
*/

package org.nhindirect.config.store.dao.impl;

import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Collection;

import javax.persistence.NoResultException;

import org.connectopensource.config.store.ejb.TrustBundleService;
import org.nhindirect.config.store.BundleRefreshError;
import org.nhindirect.config.store.ConfigurationStoreException;
import org.nhindirect.config.store.Domain;
import org.nhindirect.config.store.TrustBundle;
import org.nhindirect.config.store.TrustBundleAnchor;
import org.nhindirect.config.store.TrustBundleDomainReltn;
import org.nhindirect.config.store.dao.DomainDao;
import org.nhindirect.config.store.dao.TrustBundleDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Implementation of the TrustBundleDao interface
 * @author Greg Meyer
 * @since 1.2
 */
@Repository
public class TrustBundleDaoImpl implements TrustBundleDao {

    @Autowired
    protected TrustBundleService trustBundleService;

    @Autowired
    protected DomainDao domainDao;

    /**
     * Empty constructor
     */
    public TrustBundleDaoImpl() {
    }

    /**
     * Sets the DomainDao used for validating the exists of domains for
     * domain to trust bundle association
     * @param domainDao The domain dao
     */
    @Autowired
    public void setDomainDao(DomainDao domainDao) {
        this.domainDao = domainDao;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<TrustBundle> getTrustBundles() throws ConfigurationStoreException  {
        return trustBundleService.getTrustBundles();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TrustBundle getTrustBundleByName(String bundleName) throws ConfigurationStoreException {
        return trustBundleService.getTrustBundleByName(bundleName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TrustBundle getTrustBundleById(long id) throws ConfigurationStoreException {
        return trustBundleService.getTrustBundleById(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addTrustBundle(TrustBundle bundle) throws ConfigurationStoreException {
        trustBundleService.addTrustBundle(bundle);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateTrustBundleAnchors(long trustBundleId, Calendar attemptTime, Collection<TrustBundleAnchor> newAnchorSet,
            String bundleCheckSum) throws ConfigurationStoreException {
        
    	trustBundleService.updateTrustBundleAnchors(trustBundleId, attemptTime, newAnchorSet, bundleCheckSum);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateLastUpdateError(long trustBundleId, Calendar attemptTime, BundleRefreshError error)
            throws ConfigurationStoreException {
    	
        trustBundleService.updateLastUpdateError(trustBundleId, attemptTime, error);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteTrustBundles(long[] trustBundleIds) throws ConfigurationStoreException {
        trustBundleService.deleteTrustBundles(trustBundleIds);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateTrustBundleSigningCertificate(long trustBundleId, X509Certificate signingCert)
            throws ConfigurationStoreException {

        trustBundleService.updateTrustBundleSigningCertificate(trustBundleId, signingCert);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateTrustBundleAttributes(long trustBundleId, String bundleName, String bundleUrl, X509Certificate signingCert,
            int refreshInterval) throws ConfigurationStoreException {

        trustBundleService.updateTrustBundleAttributes(trustBundleId, bundleName, bundleUrl, signingCert, refreshInterval);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void associateTrustBundleToDomain(long domainId, long trustBundleId, boolean incoming,
            boolean outgoing) throws ConfigurationStoreException {

        // make sure the domain exists
        final Domain domain = domainDao.getDomain(domainId);

        if (domain == null) {
            throw new ConfigurationStoreException("Domain with id " + domainId + " does not exist");
        }

        // make sure the trust bundle exists
        final TrustBundle trustBundle = this.getTrustBundleById(trustBundleId);

        if (trustBundle == null) {
            throw new ConfigurationStoreException("Trust bundle with id " + trustBundle + " does not exist");
        }

        try {
            final TrustBundleDomainReltn domainTrustBundleAssoc = new TrustBundleDomainReltn();

            domainTrustBundleAssoc.setDomain(domain);
            domainTrustBundleAssoc.setTrustBundle(trustBundle);
            domainTrustBundleAssoc.setIncoming(incoming);
            domainTrustBundleAssoc.setOutgoing(outgoing);

            trustBundleService.associateTrustBundleToDomain(domainTrustBundleAssoc);
        } catch (Exception e) {
            throw new ConfigurationStoreException("Failed to associate trust bundle to domain.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void disassociateTrustBundleFromDomain(long domainId, long trustBundleId) throws ConfigurationStoreException {
        // make sure the domain exists
        final Domain domain = domainDao.getDomain(domainId);

        if (domain == null) {
            throw new ConfigurationStoreException("Domain with id " + domainId + " does not exist");
        }

        // make sure the trust bundle exists
        final TrustBundle trustBundle = this.getTrustBundleById(trustBundleId);

        if (trustBundle == null) {
            throw new ConfigurationStoreException("Trust bundle with id " + trustBundle + " does not exist");
        }

        try {
            trustBundleService.disassociateTrustBundleFromDomain(domain, trustBundle);
        } catch (NoResultException e) {
            throw new ConfigurationStoreException("Association between domain id " + domainId + " and trust bundle id"
                     + trustBundleId + " does not exist", e);
        } catch (Exception e) {
            throw new ConfigurationStoreException("Failed to delete trust bundle to domain relation.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void disassociateTrustBundlesFromDomain(long domainId) throws ConfigurationStoreException {
        // make sure the domain exists
        final Domain domain = domainDao.getDomain(domainId);

        if (domain == null) {
            throw new ConfigurationStoreException("Domain with id " + domainId + " does not exist");
        }

        try {
            trustBundleService.disassociateTrustBundleFromDomain(domain);
        } catch (Exception e) {
            throw new ConfigurationStoreException("Failed to disassociate trust bundle from domain id ." + domainId, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void disassociateTrustBundleFromDomains(long trustBundleId) throws ConfigurationStoreException {
        trustBundleService.disassociateTrustBundleFromDomains(trustBundleId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<TrustBundleDomainReltn> getTrustBundlesByDomain(long domainId) throws ConfigurationStoreException {
        // make sure the domain exists
        final Domain domain = domainDao.getDomain(domainId);

        if (domain == null) {
            throw new ConfigurationStoreException("Domain with id " + domainId + " does not exist");
        }

        return trustBundleService.getTrustBundlesByDomain(domain);
    }
}
