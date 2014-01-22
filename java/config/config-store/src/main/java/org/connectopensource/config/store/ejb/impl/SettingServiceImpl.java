package org.connectopensource.config.store.ejb.impl;


import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.connectopensource.config.store.ejb.SettingService;
import org.nhindirect.config.store.ConfigurationStoreException;
import org.nhindirect.config.store.Setting;

/**
 * Implementing class for Setting DAO methods.
 *
 * @author Greg Meyer
 */
@Stateless
public class SettingServiceImpl implements SettingService {
    
	@PersistenceContext(unitName = "config-store")
    private EntityManager entityManager;

    private static final Log log = LogFactory.getLog(SettingServiceImpl.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(String name, String value) {
        log.debug("Enter");

        if (name == null || name.isEmpty() || value == null) {
            return;
        }

        // make sure this setting doesn't already exist
        if (this.getByNames(Arrays.asList(name)).size() > 0) {
            throw new ConfigurationStoreException("Setting " + name + " already exists.");
        }

        Setting setting = new Setting();

        setting.setName(name);
        setting.setValue(value);
        setting.setCreateTime(Calendar.getInstance());
        setting.setUpdateTime(setting.getCreateTime());

        log.debug("Calling JPA to persist the setting");

        entityManager.persist(setting);

        log.debug("Returned from JPA: Setting ID=" + setting.getId());
        log.debug("Exit");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(Collection<String> names) {
        log.debug("Enter");

        if (names != null && names.size() > 0) {
            StringBuffer queryNames = new StringBuffer("(");

            for (String name : names) {
                if (queryNames.length() > 1) {
                    queryNames.append(", ");
                }

                queryNames.append("'").append(name.toUpperCase(Locale.getDefault())).append("'");
            }

            queryNames.append(")");
            String query = "DELETE FROM Setting s WHERE UPPER(s.name) IN " + queryNames.toString();

            Query delete = entityManager.createQuery(query);
            int count = delete.executeUpdate();

            log.debug("Exit: " + count + " setting records deleted");
        }

        log.debug("Exit");
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public Collection<Setting> getAll() {
        log.debug("Enter");

        List<Setting> result = Collections.emptyList();

        Query select = entityManager.createQuery("SELECT s from Setting s");

        @SuppressWarnings("rawtypes")
        List rs = select.getResultList();

        if (rs != null && (rs.size() != 0) && (rs.get(0) instanceof Setting)) {
            result = (List<Setting>) rs;
        }

        log.debug("Exit");

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public Collection<Setting> getByNames(Collection<String> names) {
        log.debug("Enter");

        if (names == null || names.size() == 0) {
            return getAll();
        }

        List<Setting> result = Collections.emptyList();

        Query select = null;
        StringBuffer nameList = new StringBuffer("(");

        for (String name : names)  {
            if (nameList.length() > 1) {
                nameList.append(", ");
            }

            nameList.append("'").append(name.toUpperCase(Locale.getDefault())).append("'");
        }
        
        nameList.append(")");
        String query = "SELECT s from Setting s WHERE UPPER(s.name) IN " + nameList.toString();

        select = entityManager.createQuery(query);
        
        @SuppressWarnings("rawtypes")
        List rs = select.getResultList();
        
        if (rs != null && (rs.size() != 0) && (rs.get(0) instanceof Setting)) {
            result = (List<Setting>) rs;
        }

        log.debug("Exit");

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(String name, String value) {
        log.debug("Enter");

        if (name == null || name.isEmpty()) {
            return;
        }

        Collection<Setting> settings = getByNames(Arrays.asList(name));

        for (Setting setting : settings) {
            setting.setValue(value);
            setting.setUpdateTime(Calendar.getInstance());

            entityManager.merge(setting);
        }

        log.debug("Exit");
    }
}
