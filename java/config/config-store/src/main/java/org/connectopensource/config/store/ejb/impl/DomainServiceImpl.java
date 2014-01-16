package org.connectopensource.config.store.ejb.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.connectopensource.config.store.ejb.DomainService;
import org.nhindirect.config.store.Domain;
import org.nhindirect.config.store.EntityStatus;

@Stateless
public class DomainServiceImpl implements DomainService {

    @PersistenceContext(unitName = "config-store")
    private EntityManager entityManager;

    private static final Log log = LogFactory.getLog(DomainServiceImpl.class);

    /*
     * (non-Javadoc)
     * 
     * @see org.nhindirect.config.store.dao.DomainDao#count()
     */
    
    public int count() {
        if (log.isDebugEnabled())
            log.debug("Enter");
        Long result = (Long) entityManager.createQuery("select count(d) from Domain d").getSingleResult();
        if (log.isDebugEnabled())
            log.debug("Exit: " + result.intValue());
        return result.intValue();
    }

    public void add(Domain item) {
        entityManager.persist(item);
    }

    public void update(Domain item) {
        entityManager.merge(item);
    }

    public void delete(Domain domain) {
        entityManager.remove(domain);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.nhindirect.config.store.dao.DomainDao#getDomainByName(java.lang.String)
     */
    
    public Domain getDomainByName(String name) {
        if (log.isDebugEnabled())
            log.debug("Enter");

        Domain result = null;

        if (name != null) {
            Query select = entityManager.createQuery("SELECT DISTINCT d from Domain d WHERE UPPER(d.domainName) = ?1");
            Query paramQuery = select.setParameter(1, name.toUpperCase(Locale.getDefault()));
            if (paramQuery.getResultList().size() > 0)
                result = (Domain) paramQuery.getSingleResult();
        }

        if (log.isDebugEnabled())
            log.debug("Exit");
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.nhindirect.config.store.dao.DomainDao#getDomains(java.lang.String, org.nhindirect.config.store.EntityStatus)
     * 
     * Convert the list of names into a String to be used in an IN clause (i.e.
     * {"One", "Two", "Three"} --> ('One', 'Two', 'Three'))
     */
    @SuppressWarnings("unchecked")
    
    public List<Domain> getDomains(List<String> names, EntityStatus status) {
        if (log.isDebugEnabled())
            log.debug("Enter");

        List<Domain> result = null;
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
            String query = "SELECT d from Domain d WHERE UPPER(d.domainName) IN " + nameList.toString();

            if (status != null) {
                select = entityManager.createQuery(query + " AND d.status = ?1");
                select.setParameter(1, status);
            } else {
                select = entityManager.createQuery(query);
            }
        } else {
            if (status != null) {
                select = entityManager.createQuery("SELECT d from Domain d WHERE d.status = ?1");
                select.setParameter(1, status);
            } else {
                select = entityManager.createQuery("SELECT d from Domain d");
            }

        }
        
        @SuppressWarnings("rawtypes")
        List rs = select.getResultList();
        if ((rs.size() != 0) && (rs.get(0) instanceof Domain)) {
            result = (List<Domain>) rs;
        } else {
            result = new ArrayList<Domain>();
        }

        if (log.isDebugEnabled())
            log.debug("Exit");
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.nhindirect.config.store.dao.DomainDao#listDomains(java.lang.String, int)
     */
    // TODO I'm not sure if this is doing the right thing. I suspect that the
    // real intent is to do some kind of db paging
    @SuppressWarnings("unchecked")
    
    public List<Domain> listDomains(String name, int count) {
        if (log.isDebugEnabled())
            log.debug("Enter");

        List<Domain> result = null;
        Query select = null;
        if (name != null) {
            select = entityManager.createQuery("SELECT d from Domain d WHERE UPPER(d.domainName) = ?1");
            select.setParameter(1, name.toUpperCase(Locale.getDefault()));
        } else {
            select = entityManager.createQuery("SELECT d from Domain d");
        }

        // assuming that a count of zero really means no limit
        if (count > 0) {
            select.setMaxResults(count);
        }

        @SuppressWarnings("rawtypes")
        List rs = select.getResultList();
        if ((rs.size() != 0) && (rs.get(0) instanceof Domain)) {
            result = (List<Domain>) rs;
        }

        if (log.isDebugEnabled())
            log.debug("Exit");
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.nhindirect.config.store.dao.DomainDao#searchDomain(java.lang.String, org.nhindirect.config.store.EntityStatus)
     */
    @SuppressWarnings("unchecked")
    
    public List<Domain> searchDomain(String name, EntityStatus status) {
        if (log.isDebugEnabled())
            log.debug("Enter");

        List<Domain> result = null;
        StringBuffer query = new StringBuffer("");
        Query select = null;
        if (name != null) {
            String search = name.replace('*', '%').toUpperCase(Locale.getDefault());
            search.replace('?', '_');
            query.append("SELECT d from Domain d WHERE UPPER(d.domainName) LIKE ?1 ");
            if (status != null) {
                query.append("AND d.status = ?2");
                select = entityManager.createQuery(query.toString());
                select.setParameter(1, search);
                select.setParameter(2, status);
            } else {
                select = entityManager.createQuery(query.toString());
                select.setParameter(1, search);
            }
        } else {
            if (status != null) {
                query.append("SELECT d from Domain d WHERE d.status LIKE ?1");
                select = entityManager.createQuery(query.toString());
                select.setParameter(1, status);
            } else {
                select = entityManager.createQuery("SELECT d from Domain d");
            }

        }

        result = (List<Domain>) select.getResultList();
        if (result == null) {
            result = new ArrayList<Domain>();
        }

        if (log.isDebugEnabled())
            log.debug("Exit");
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.nhindirect.config.store.dao.DomainDao#getDomain(java.lang.Long)
     */
        
    public Domain getDomain(Long id) {
        if (log.isDebugEnabled())
            log.debug("Enter");

        Domain result = null;
        if ((id != null) && (id.longValue() > 0)) {
            result = entityManager.find(Domain.class, id);
        }

        if (log.isDebugEnabled())
            log.debug("Exit");
        return result;
    }
}