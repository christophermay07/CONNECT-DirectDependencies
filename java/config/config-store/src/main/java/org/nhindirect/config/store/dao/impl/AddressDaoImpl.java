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

import java.util.List;

import org.connectopensource.config.store.ejb.AddressService;
import org.nhindirect.config.store.Address;
import org.nhindirect.config.store.Domain;
import org.nhindirect.config.store.EntityStatus;
import org.nhindirect.config.store.dao.AddressDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Implementing class for Address DAO methods.
 *
 * @author ppyette
 */
@Repository
public class AddressDaoImpl implements AddressDao {

    @Autowired
    private AddressService addressService;

    @Override
    public int count() {
        return addressService.count();
    }

    @Override
    public void add(Address item) {
        addressService.add(item);
    }

    @Override
    public void update(Address item) {
        addressService.update(item);
    }

    @Override
    public void save(Address item) {
        addressService.save(item);
    }

    @Override
    public void delete(String name) {
        addressService.delete(name);
    }

    @Override
    public Address get(String name) {
        return addressService.get(name);
    }

    @Override
    public List<Address> listAddresses(List<String> names, EntityStatus status) {
        return addressService.listAddresses(names, status);
    }

    @Override
    public List<Address> getByDomain(Domain domain, EntityStatus status) {
        return addressService.getByDomain(domain, status);
    }

    @Override
    public List<Address> listAddresses(String name, int count) {
        return addressService.listAddresses(name, count);
    }
}
