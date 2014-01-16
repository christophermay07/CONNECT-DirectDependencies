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

import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.connectopensource.config.store.ejb.AnchorService;
import org.connectopensource.config.store.ejb.DomainService;
import org.nhindirect.config.store.Anchor;
import org.nhindirect.config.store.CertificateException;
import org.nhindirect.config.store.EntityStatus;
import org.nhindirect.config.store.dao.AnchorDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementing class for Anchor DAO methods.
 * 
 * @author ppyette
 */
@Repository
public class AnchorDaoImpl implements AnchorDao {

    @Autowired
    private AnchorService anchorService;

    @Override
    public Anchor load(String owner) {
        return anchorService.load(owner);
    }

    @Override
    public List<Anchor> listAll() {
        return anchorService.listAll();
    }

    @Override
    public List<Anchor> list(List<String> owners) {
        return anchorService.list(owners);
    }

    @Override
    public void add(Anchor anchor) {
        anchorService.add(anchor);
    }

    @Override
    public void save(Anchor anchor) {
        anchorService.save(anchor);
    }

    @Override
    public void save(List<Anchor> anchorList) {
        anchorService.save(anchorList);
    }

    @Override
    public List<Anchor> listByIds(List<Long> anchorIds) {
        return anchorService.listByIds(anchorIds);
    }

    @Override
    public void setStatus(List<Long> anchorIDs, EntityStatus status) {
        anchorService.setStatus(anchorIDs, status);
    }

    @Override
    public void setStatus(String owner, EntityStatus status) {
        anchorService.setStatus(owner, status);
    }

    @Override
    public void delete(List<Long> idList) {
        anchorService.delete(idList);
    }

    @Override
    public void delete(String owner) {
        anchorService.delete(owner);
    }
}
