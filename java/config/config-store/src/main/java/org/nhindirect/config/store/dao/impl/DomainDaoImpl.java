/*
Copyright (c) 2010, NHIN Direct Project
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer
   in the documentation and/or other materials provided with the distribution.
3. Neither the name of the The NHIN Direct Project (nhindirect.org) nor the names of its contributors may be used to endorse or promote
   products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS
BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.nhindirect.config.store.dao.impl;

import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.connectopensource.config.store.ejb.DomainService;
import org.nhindirect.config.store.Address;
import org.nhindirect.config.store.ConfigurationStoreException;
import org.nhindirect.config.store.Domain;
import org.nhindirect.config.store.EntityStatus;
import org.nhindirect.config.store.dao.AddressDao;
import org.nhindirect.config.store.dao.DomainDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Default Spring/JPA implemenation
 *
 * @author ppyette
 */
@Repository
public class DomainDaoImpl implements DomainDao {

    @Autowired
    private DomainService domainService;

    @Autowired
    private AddressDao addressDao;

    private static final Log log = LogFactory.getLog(DomainDaoImpl.class);

    /*
     * (non-Javadoc)
     *
     * @see org.nhindirect.config.store.dao.DomainDao#count()
     */

    public int count() {
        return domainService.count();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.nhindirect.config.store.dao.DomainDao#add(org.nhindirect.config.store.Domain)
     */

    public void add(Domain item) {

            log.debug("Enter");

        if (item.getDomainName() == null || item.getDomainName().isEmpty())
            throw new ConfigurationStoreException("Domain name cannot be empty or null");

        // Save and clear Address information until the Domain is saved.
        // This is really something that JPA should be doing, but doesn't seem
        // to work.
        if (item != null) {
            String pm = item.getPostMasterEmail();
            Long pmId = item.getPostmasterAddressId();
            Collection<Address> addresses = item.getAddresses();
            if ((pmId != null) && (pmId.longValue() == 0)) {
                item.setPostmasterAddressId((Long) null);
            }
            item.setAddresses(null);

            item.setCreateTime(Calendar.getInstance());
            item.setUpdateTime(item.getCreateTime());


                log.debug("Calling JPA to persist the Domain");

            domainService.add(item);


                log.debug("Persisted the bare Domain");

            boolean needUpdate = false;
            if ((addresses != null) && (addresses.size() > 0)) {
                item.setAddresses(addresses);
                needUpdate = true;
            }
            if ((pm != null) && (pm.length() > 0)) {
                item.setPostMasterEmail(pm);
                needUpdate = true;
            }

            if (needUpdate) {

                    log.debug("Updating the domain with Address info");
                update(item);
            }


                log.debug("Returned from JPA: Domain ID=" + item.getId());
        }


            log.debug("Exit");
    }

    /*
     * (non-Javadoc)
     *
     * @see org.nhindirect.config.store.dao.DomainDao#update(org.nhindirect.config.store.Domain)
     */

    public void update(Domain item) {

            log.debug("Enter");

        if (item != null) {
            item.setUpdateTime(Calendar.getInstance());

            if ((item.getPostMasterEmail() != null) && (item.getPostMasterEmail().length() > 0)) {

                boolean found = false;
                Iterator<Address> addrs = item.getAddresses().iterator();
                while (addrs.hasNext()) {
                    if (addrs.next().getEmailAddress().equals(item.getPostMasterEmail())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {

                        log.debug("Adding new postmaster email address: " + item.getPostMasterEmail());
                    item.getAddresses().add(new Address(item, item.getPostMasterEmail(), "Postmaster"));
                }
            }

            for (Address address : item.getAddresses()) {
                if ((address.getId() == null) || (address.getId().longValue() == 0)) {

                        log.debug("Adding " + address.toString() + " to database");
                    addressDao.add(address);
                }
            }

            // Set the correct ID in the Domain.postmasterAddressId field, if
            // necessary.
            if ((item.getPostmasterAddressId() == null) || (item.getPostmasterAddressId().longValue() == 0L)) {
                Iterator<Address> addrs = item.getAddresses().iterator();
                while (addrs.hasNext()) {
                    Address address = addrs.next();
                    if (address.getDisplayName().equals("Postmaster")) {

                            log.debug("Linking domain's postmaster email address to " + address.toString());
                        item.setPostmasterAddressId(address.getId());
                        break;
                    }
                }
            }

                log.debug("Calling JPA to perform update...");

            domainService.update(item);
        }


            log.debug("Exit");
    }

    /*
     * (non-Javadoc)
     *
     * @see org.nhindirect.config.store.dao.DomainDao#save(org.nhindirect.config.store.Domain)
     */

    public void save(Domain item) {
        update(item);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.nhindirect.config.store.dao.DomainDao#delete(java.lang.String)
     */

    public void delete(String name) {

            log.debug("Enter");

        // delete addresses first if they exist
        final Domain domain = getDomainByName(name);

        if (domain != null) {
            disassociateTrustBundlesFromDomain(domain.getId());
            removePolicyGroupFromDomain(domain.getId());

            delete(domain);
        } else  {
            log.warn("No domain matching the name: " + name + " found.  Unable to delete.");
        }


            log.debug("Exit");
    }

    /*
     * (non-Javadoc)
     *
     * @see org.nhindirect.config.store.dao.DomainDao#delete(java.lang.String)
     */
    public void delete(Long anId) {

            log.debug("Enter");

        final Domain domain = getDomain(anId);

        if (domain != null)  {
            disassociateTrustBundlesFromDomain(domain.getId());
            removePolicyGroupFromDomain(domain.getId());

            delete(domain);
        } else {
           log.warn("No domain matching the id: " + anId + " found.  Unable to delete.");
        }


            log.debug("Exit");
    }

    public void delete(Domain domain) {
        domainService.delete(domain);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.nhindirect.config.store.dao.DomainDao#getDomainByName(java.lang.String)
     */

    public Domain getDomainByName(String name) {
        return domainService.getDomainByName(name);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.nhindirect.config.store.dao.DomainDao#getDomains(java.lang.String, org.nhindirect.config.store.EntityStatus)
     *
     * Convert the list of names into a String to be used in an IN clause (i.e.
     * {"One", "Two", "Three"} --> ('One', 'Two', 'Three'))
     */
    public List<Domain> getDomains(List<String> names, EntityStatus status) {
        return domainService.getDomains(names, status);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.nhindirect.config.store.dao.DomainDao#listDomains(java.lang.String, int)
     */
    public List<Domain> listDomains(String name, int count) {
        return domainService.listDomains(name, count);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.nhindirect.config.store.dao.DomainDao#searchDomain(java.lang.String, org.nhindirect.config.store.EntityStatus)
     */
    public List<Domain> searchDomain(String name, EntityStatus status) {
        return domainService.searchDomain(name, status);
    }

    /**
     * (non-Javadoc)
     *
     * @see org.nhindirect.config.store.dao.DomainDao#getDomain(java.lang.Long)
     */
    public Domain getDomain(Long id) {
        return domainService.getDomain(id);
    }

    /**
     * Set the value of addressDao.
     * @param aDao The value of addressDao.
     */
    public void setAddressDao(AddressDao aDao) {
        addressDao = aDao;
    }

    protected void disassociateTrustBundlesFromDomain(long domainId) throws ConfigurationStoreException {
        final TrustBundleDaoImpl dao = new TrustBundleDaoImpl();
        dao.setDomainDao(this);
        dao.disassociateTrustBundlesFromDomain(domainId);
    }

    protected void removePolicyGroupFromDomain(long domainId) {
        final CertPolicyDaoImpl dao = new CertPolicyDaoImpl();
        dao.setDomainDao(this);
        dao.disassociatePolicyGroupsFromDomain(domainId);
    }
}
