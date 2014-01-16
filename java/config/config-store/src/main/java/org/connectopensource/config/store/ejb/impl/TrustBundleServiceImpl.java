package org.connectopensource.config.store.ejb.impl;

import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.connectopensource.config.store.ejb.TrustBundleService;
import org.nhindirect.config.store.BundleRefreshError;
import org.nhindirect.config.store.ConfigurationStoreException;
import org.nhindirect.config.store.Domain;
import org.nhindirect.config.store.TrustBundle;
import org.nhindirect.config.store.TrustBundleAnchor;
import org.nhindirect.config.store.TrustBundleDomainReltn;

@Stateless
public class TrustBundleServiceImpl implements TrustBundleService {

    @PersistenceContext(unitName = "config-store")
    private EntityManager entityManager;

    private static final Log log = LogFactory.getLog(TrustBundleServiceImpl.class);

    /**
     * Sets the entity manager for access to the underlying data store medium.
     * @param entityManager The entity manager.
     */
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public Collection<TrustBundle> getTrustBundles() throws ConfigurationStoreException  {
        Collection<TrustBundle> rs;

        try {
            Query select = entityManager.createQuery("SELECT tb from TrustBundle tb");

            rs = select.getResultList();
            if (rs.size() == 0)
                return Collections.emptyList();
        } catch (Exception e) {
            throw new ConfigurationStoreException("Failed to execute trust bundle DAO query.", e);
        }

        // make sure the anchors are loaded
        for (TrustBundle bundle : rs) {
            if (!bundle.getTrustBundleAnchors().isEmpty()) {
                for (TrustBundleAnchor anchor : bundle.getTrustBundleAnchors()) {
                    anchor.getData();
                }
            }
        }

        return rs;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TrustBundle getTrustBundleByName(String bundleName) throws ConfigurationStoreException {
        try {
            Query select = entityManager.createQuery("SELECT tb from TrustBundle tb WHERE UPPER(tb.bundleName) = ?1");
            
            select.setParameter(1, bundleName.toUpperCase(Locale.getDefault()));

            TrustBundle rs = (TrustBundle)select.getSingleResult();

            // make sure the anchors are loaded
            if (!rs.getTrustBundleAnchors().isEmpty()) {
                for (TrustBundleAnchor anchor : rs.getTrustBundleAnchors()) {
                    anchor.getData();
                }
            }

            return rs;
        } catch (NoResultException e) {
            return null;
        } catch (Exception e) {
            throw new ConfigurationStoreException("Failed to execute trust bundle DAO query.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TrustBundle getTrustBundleById(long id) throws ConfigurationStoreException {
        try {
            Query select = entityManager.createQuery("SELECT tb from TrustBundle tb WHERE tb.id = ?1");
            select.setParameter(1, id);

            TrustBundle rs = (TrustBundle)select.getSingleResult();

            // make sure the anchors are loaded
            if (!rs.getTrustBundleAnchors().isEmpty()) {
                for (TrustBundleAnchor anchor : rs.getTrustBundleAnchors()) {
                    anchor.getData();
                }
            }

            return rs;
        } catch (NoResultException e) {
            return null;
        } catch (Exception e) {
            throw new ConfigurationStoreException("Failed to execute trust bundle DAO query.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addTrustBundle(TrustBundle bundle) throws ConfigurationStoreException  {
        try {
            final TrustBundle existingBundle = this.getTrustBundleByName(bundle.getBundleName());

            if (existingBundle != null) {
                throw new ConfigurationStoreException("Trust bundle " + bundle.getBundleName() + " already exists");
            }

            bundle.setCreateTime(Calendar.getInstance(Locale.getDefault()));

            entityManager.persist(bundle);
        } catch (ConfigurationStoreException cse) {
            throw cse;
        } catch (Exception e) {
            throw new ConfigurationStoreException("Failed to add trust bundle " + bundle.getBundleName(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateTrustBundleAnchors(long trustBundleId, Calendar attemptTime, Collection<TrustBundleAnchor> newAnchorSet,
            String bundleCheckSum) throws ConfigurationStoreException {

        try {
            final TrustBundle existingBundle = this.getTrustBundleById(trustBundleId);

            if (existingBundle == null) {
                throw new ConfigurationStoreException("Trust bundle does not exist");
            }

            // blow away all the existing bundles
            final Query delete = entityManager.createQuery("DELETE from TrustBundleAnchor tba where tba.trustBundle = ?1");
            delete.setParameter(1, existingBundle);
            delete.executeUpdate();

            // now update the bundle
            existingBundle.setCheckSum(bundleCheckSum);
            existingBundle.setTrustBundleAnchors(newAnchorSet);
            existingBundle.setLastRefreshAttempt(attemptTime);
            existingBundle.setLastSuccessfulRefresh(Calendar.getInstance(Locale.getDefault()));

            entityManager.persist(existingBundle);
        } catch (ConfigurationStoreException cse) {
            throw cse;
        } catch (Exception e) {
            throw new ConfigurationStoreException("Failed to update anchors in trust bundle.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateLastUpdateError(long trustBundleId, Calendar attemptTime, BundleRefreshError error)
            throws ConfigurationStoreException {

        try {
            final TrustBundle existingBundle = this.getTrustBundleById(trustBundleId);

            if (existingBundle == null) {
                throw new ConfigurationStoreException("Trust bundle does not exist");
            }

            existingBundle.setLastRefreshAttempt(attemptTime);
            existingBundle.setLastRefreshError(error);

            entityManager.persist(existingBundle);
        } catch (ConfigurationStoreException cse) {
            throw cse;
        } catch (Exception e) {
            throw new ConfigurationStoreException("Failed to update bundle last refresh error.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteTrustBundles(long[] trustBundleIds) throws ConfigurationStoreException  {
        if (trustBundleIds == null || trustBundleIds.length == 0) {
            return;
        }

        for (long id : trustBundleIds) {
            try {
                final TrustBundle bundle = this.getTrustBundleById(id);

                this.disassociateTrustBundleFromDomains(id);

                entityManager.remove(bundle);
            } catch (ConfigurationStoreException e) {
                log.warn(e.getMessage(), e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateTrustBundleSigningCertificate(long trustBundleId, X509Certificate signingCert)
            throws ConfigurationStoreException {

        try {
            final TrustBundle existingBundle = this.getTrustBundleById(trustBundleId);

            if (existingBundle == null) {
                throw new ConfigurationStoreException("Trust bundle does not exist");
            }

            if (signingCert == null) {
                existingBundle.setSigningCertificateData(null);
            } else {
                existingBundle.setSigningCertificateData(signingCert.getEncoded());
            }

            entityManager.persist(existingBundle);
        } catch (ConfigurationStoreException cse) {
            throw cse;
        } catch (Exception e) {
            throw new ConfigurationStoreException("Failed to update bundle last refresh error.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateTrustBundleAttributes(long trustBundleId, String bundleName, String bundleUrl, X509Certificate signingCert,
            int refreshInterval) throws ConfigurationStoreException {

        try {
            final TrustBundle existingBundle = this.getTrustBundleById(trustBundleId);

            if (existingBundle == null) {
                throw new ConfigurationStoreException("Trust bundle does not exist");
            }

            if (signingCert == null) {
                existingBundle.setSigningCertificateData(null);
            } else {
                existingBundle.setSigningCertificateData(signingCert.getEncoded());
            }

            existingBundle.setRefreshInterval(refreshInterval);

            if (bundleName != null && !bundleName.isEmpty()) {
                existingBundle.setBundleName(bundleName);
            }

            if (bundleUrl != null && !bundleUrl.isEmpty()) {
                existingBundle.setBundleURL(bundleUrl);
            }

            entityManager.persist(existingBundle);
        } catch (ConfigurationStoreException cse) {
            throw cse;
        } catch (Exception e) {
            throw new ConfigurationStoreException("Failed to update bundle last refresh error.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void disassociateTrustBundleFromDomains(long trustBundleId) throws ConfigurationStoreException {
        // make sure the trust bundle exists
        final TrustBundle trustBundle = this.getTrustBundleById(trustBundleId);

        if (trustBundle == null) {
            throw new ConfigurationStoreException("Trust bundle with id " + trustBundle + " does not exist");
        }
        
        try {
            final Query delete = entityManager.createQuery("DELETE from TrustBundleDomainReltn tbd where tbd.trustBundle  = ?1");

            delete.setParameter(1, trustBundle);
            delete.executeUpdate();
        } catch (Exception e) {
            throw new ConfigurationStoreException("Failed to disassociate domains from trust bundle id ." + trustBundleId, e);
        }
    }

    @Override
    public void associateTrustBundleToDomain(TrustBundleDomainReltn reltn) {
        entityManager.persist(reltn);
    }

    @Override
    public void disassociateTrustBundleFromDomain(Domain domain) {
        final Query delete = entityManager.createQuery("DELETE from TrustBundleDomainReltn tbd where tbd.domain  = ?1");

        delete.setParameter(1, domain);
        delete.executeUpdate();
    }

    @Override
    public void disassociateTrustBundleFromDomain(Domain domain, TrustBundle trustBundle)  {
        final Query select = entityManager.createQuery("SELECT tbd from TrustBundleDomainReltn tbd where tbd.domain  = ?1 " +
                " and tbd.trustBundle = ?2 ");

        select.setParameter(1, domain);
        select.setParameter(2, trustBundle);

        final TrustBundleDomainReltn reltn = (TrustBundleDomainReltn)select.getSingleResult();

        entityManager.remove(reltn);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<TrustBundleDomainReltn> getTrustBundlesByDomain(Domain domain) throws ConfigurationStoreException {
        final Query select = entityManager.createQuery("SELECT tbd from TrustBundleDomainReltn tbd where tbd.domain = ?1");

        Collection<TrustBundleDomainReltn> retVal = null;

        try {
            select.setParameter(1, domain);

            retVal = (Collection<TrustBundleDomainReltn>)select.getResultList();
            if (retVal.size() == 0) {
                return Collections.emptyList();
            }

            for (TrustBundleDomainReltn reltn : retVal) {
                if (!reltn.getTrustBundle().getTrustBundleAnchors().isEmpty()) {
                    for (TrustBundleAnchor anchor : reltn.getTrustBundle().getTrustBundleAnchors()) {
                        anchor.getData();
                    }
                }
            }
        } catch (Exception e) {
            throw new ConfigurationStoreException("Failed to execute trust bundle relation DAO query.", e);
        }

        return retVal;
    }
}
