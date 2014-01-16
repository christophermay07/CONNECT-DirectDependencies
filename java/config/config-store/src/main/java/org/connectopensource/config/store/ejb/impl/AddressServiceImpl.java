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

package org.connectopensource.config.store.ejb.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.connectopensource.config.store.ejb.AddressService;
import org.nhindirect.config.store.Address;
import org.nhindirect.config.store.Domain;
import org.nhindirect.config.store.EntityStatus;

/**
 * Implementing class for Address DAO methods.
 *
 * @author ppyette
 */
@Stateless
public class AddressServiceImpl implements AddressService {

    @PersistenceContext(unitName = "config-store")
    private EntityManager entityManager;

    private static final Log log = LogFactory.getLog(AddressServiceImpl.class);

    /*
     * (non-Javadoc)
     *
     * @see org.nhindirect.config.store.dao.AddressDao#count()
     */

    public int count() {
        log.debug("Enter");

        Long result = (Long) entityManager.createQuery("select count(d) from Address a").getSingleResult();

        log.debug("Exit: " + result.intValue());

        return result.intValue();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.nhindirect.config.store.dao.AddressDao#add(org.nhindirect.config.store.Address)
     */

    public void add(Address item) {
        log.debug("Enter");

        if (item != null) {
            item.setCreateTime(Calendar.getInstance());
            item.setUpdateTime(item.getCreateTime());
            entityManager.persist(item);
        }

        log.debug("Exit");
    }

    /*
     * (non-Javadoc)
     *
     * @see org.nhindirect.config.store.dao.AddressDao#update(org.nhindirect.config.store.Address)
     */
    public void update(Address item) {
        log.debug("Enter");

        if (item != null) {
            Address inDb = entityManager.find(Address.class, item.getId());

            inDb.setDisplayName(item.getDisplayName());
            inDb.setEndpoint(item.getEndpoint());
            inDb.setDomain(item.getDomain());
            inDb.setEmailAddress(item.getEmailAddress());
            inDb.setType(item.getType());
            inDb.setStatus(item.getStatus());
            inDb.setUpdateTime(Calendar.getInstance());

            entityManager.merge(inDb);
        }

        log.debug("Exit");
    }

    /*
     * (non-Javadoc)
     *
     * @see org.nhindirect.config.store.dao.AddressDao#save(org.nhindirect.config.store.Address)
     */
    public void save(Address item) {
        update(item);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.nhindirect.config.store.dao.AddressDao#delete(java.lang.String)
     */
    // TODO Check to see if this address is a postmaster Address and remove it
    // from Domain prior to deletion
    public void delete(String name) {
        log.debug("Enter");

        int count = 0;

        if (name != null) {
            Query delete = entityManager.createQuery("DELETE FROM Address a WHERE UPPER(a.emailAddress) = ?1");
            delete.setParameter(1, name.toUpperCase(Locale.getDefault()));
            count = delete.executeUpdate();
        }

        log.debug("Exit: " + count + " records deleted");
    }

    /*
     * (non-Javadoc)
     *
     * @see org.nhindirect.config.store.dao.AddressDao#listAddresses(java.lang.String, int)
     *
     * The function below is verbatim from the RI 3.0.1 AddressDaoImpl; there is no implementation at the moment.
     */
    public List<Address> listAddresses(String name, int count) {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.nhindirect.config.store.dao.AddressDao#get(java.lang.String)
     */

    public Address get(String name) {
        log.debug("Enter");

        Address result = null;

        if (name != null) {
            Query select = entityManager.createQuery("SELECT DISTINCT a from Address a d WHERE UPPER(a.emailAddress) = ?1");
            result = (Address) select.setParameter(1, name.toUpperCase(Locale.getDefault())).getSingleResult();
        }

        log.debug("Exit");

        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.nhindirect.config.store.dao.AddressDao#listAddresses(java.util.List, org.nhindirect.config.store.EntityStatus)
     */
    @SuppressWarnings("unchecked")
    public List<Address> listAddresses(List<String> names, EntityStatus status) {
        log.debug("Enter");

        List<Address> result = null;
        Query select = null;

        if (names != null) {
            StringBuffer nameList = new StringBuffer("(");

            for (String aName : names) {
                if (nameList.length() > 1) {
                    nameList.append(", ");
                }

                nameList.append("'").append(aName.toUpperCase(Locale.getDefault())).append("'");
            }

            nameList.append(")");
            String query = "SELECT a from Address a WHERE UPPER(a.emailAddress) IN " + nameList.toString();

            if (status != null) {
                select = entityManager.createQuery(query + " AND a.status = ?1");
                select.setParameter(1, status);
            } else {
                select = entityManager.createQuery(query);
            }
        } else {
            if (status != null) {
                select = entityManager.createQuery("SELECT a from Address a WHERE a.status = ?1");
                select.setParameter(1, status);
            } else {
                select = entityManager.createQuery("SELECT a from Address a");
            }

        }

        @SuppressWarnings("rawtypes")
        List rs = select.getResultList();

        if ((rs.size() != 0) && (rs.get(0) instanceof Address)) {
            result = (List<Address>) rs;
        } else {
            result = new ArrayList<Address>();
        }

        log.debug("Exit");

        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.nhindirect.config.store.dao.AddressDao#getByDomain(org.nhindirect.config.store.Domain, org.nhindirect.config.store.EntityStatus)
     */
    @SuppressWarnings("unchecked")
    public List<Address> getByDomain(Domain domain, EntityStatus status) {
        log.debug("Enter");

        List<Address> result = null;
        Query select = null;

        if (domain != null) {
            String query = "SELECT a from Address a WHERE a.domain = ?1";

            if (status != null) {
                select = entityManager.createQuery(query + " AND a.status = ?2");
                select.setParameter(1, domain);
                select.setParameter(2, status);
            } else {
                select = entityManager.createQuery(query);
                select.setParameter(1, domain);
            }
        } else {
            if (status != null) {
                select = entityManager.createQuery("SELECT a from Address a WHERE a.status = ?1");
                select.setParameter(1, status);
            } else {
                select = entityManager.createQuery("SELECT a from Address a");
            }
        }

        @SuppressWarnings("rawtypes")
        List rs = select.getResultList();

        if ((rs.size() != 0) && (rs.get(0) instanceof Address)) {
            result = (List<Address>) rs;
        } else {
            result = new ArrayList<Address>();
        }

        log.debug("Exit");

        return result;
    }

    /**
     * Get the value of entityManager.
     *
     * @return the value of entityManager.
     */
    public EntityManager getEntityManager() {
        return entityManager;
    }

    /**
     * Set the value of entityManager.
     *
     * @param entityManager
     *            The value of entityManager.
     */
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
}
