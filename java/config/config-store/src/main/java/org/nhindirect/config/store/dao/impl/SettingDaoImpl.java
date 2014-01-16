package org.nhindirect.config.store.dao.impl;


import java.util.Collection;

import org.connectopensource.config.store.ejb.SettingService;
import org.nhindirect.config.store.Setting;
import org.nhindirect.config.store.dao.SettingDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Implementing class for Setting DAO methods.
 *
 * @author Greg Meyer
 */
@Repository
public class SettingDaoImpl implements SettingDao {

    @Autowired
    private SettingService settingService;

    @Override
    public Collection<Setting> getAll() {
        return settingService.getAll();
    }

    @Override
    public Collection<Setting> getByNames(Collection<String> names) {
        return settingService.getByNames(names);
    }

    @Override
    public void add(String name, String value) {
        settingService.add(name, value);
    }

    @Override
    public void update(String name, String value) {
        settingService.update(name, value);
    }

    @Override
    public void delete(Collection<String> names) {
        settingService.delete(names);
    }
}
