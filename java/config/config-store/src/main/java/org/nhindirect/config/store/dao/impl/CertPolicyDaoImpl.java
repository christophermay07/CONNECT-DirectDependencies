package org.nhindirect.config.store.dao.impl;

import java.util.Collection;

import org.connectopensource.config.store.ejb.CertPolicyService;
import org.nhindirect.config.store.CertPolicy;
import org.nhindirect.config.store.CertPolicyGroup;
import org.nhindirect.config.store.CertPolicyUse;
import org.nhindirect.config.store.ConfigurationStoreException;
import org.nhindirect.config.store.CertPolicyGroupDomainReltn;
import org.nhindirect.config.store.Domain;
import org.nhindirect.config.store.dao.CertPolicyDao;
import org.nhindirect.config.store.dao.DomainDao;
import org.nhindirect.policy.PolicyLexicon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class CertPolicyDaoImpl implements CertPolicyDao {

    @Autowired
    private CertPolicyService certPolicyService;

    protected DomainDao domainDao;    
    
    @Autowired
    public void setDomainDao(DomainDao domainDao)
    {
        this.domainDao = domainDao;
    }
    
    @Override
    public Collection<CertPolicy> getPolicies() throws ConfigurationStoreException {
        return certPolicyService.getPolicies();
    }

    @Override
    public CertPolicy getPolicyByName(String policyName) throws ConfigurationStoreException  {
    	return certPolicyService.getPolicyByName(policyName);
    }

    @Override
    public CertPolicy getPolicyById(long id) throws ConfigurationStoreException {
    	return certPolicyService.getPolicyById(id);
    }

    @Override
    public void addPolicy(CertPolicy policy) throws ConfigurationStoreException {
    	certPolicyService.addPolicy(policy);
    }

    @Override
    public void deletePolicies(long[] policyIds) throws ConfigurationStoreException {        
    	certPolicyService.deletePolicies(policyIds);
    }

    /**
     * {@inheritDoc}
     */
    public void removePolicyUseFromGroups(long policyId) throws ConfigurationStoreException {
    	certPolicyService.removePolicyUseFromGroups(policyId);
    }
    
    @Override
    public void updatePolicyAttributes(long id, String policyName,
            PolicyLexicon lexicon, byte[] policyData) throws ConfigurationStoreException {        
    	
    	certPolicyService.updatePolicyAttributes(id, policyName, lexicon, policyData);
    }

    @Override
    public Collection<CertPolicyGroup> getPolicyGroups() throws ConfigurationStoreException {
    	return certPolicyService.getPolicyGroups();
    }

    @Override
    public CertPolicyGroup getPolicyGroupByName(String policyGroupName) throws ConfigurationStoreException  {
    	return certPolicyService.getPolicyGroupByName(policyGroupName);
    }

    @Override
    public CertPolicyGroup getPolicyGroupById(long id) throws ConfigurationStoreException  {
    	return certPolicyService.getPolicyGroupById(id);
    }

    @Override
    public void addPolicyGroup(CertPolicyGroup group) throws ConfigurationStoreException {        
    	certPolicyService.addPolicyGroup(group);
    }

    @Override
    public void deletePolicyGroups(long[] groupIds) throws ConfigurationStoreException {
    	certPolicyService.deletePolicyGroups(groupIds);
    }

    @Override
    public void updateGroupAttributes(long id, String groupName) throws ConfigurationStoreException  {
    	certPolicyService.updateGroupAttributes(id, groupName);
    }

    @Override
    public void addPolicyUseToGroup(long groupId, long policyId, 
            CertPolicyUse policyUse, boolean incoming, boolean outgoing) throws ConfigurationStoreException {
    	certPolicyService.addPolicyUseToGroup(groupId, policyId, policyUse, incoming, outgoing);
    }

    @Override
    public void removePolicyUseFromGroup(long policyGroupReltnId) throws ConfigurationStoreException {
    	certPolicyService.removePolicyUseFromGroup(policyGroupReltnId);
    }

    @Override
    public void associatePolicyGroupToDomain(long domainId, long policyGroupId)
            throws ConfigurationStoreException {
        // make sure the domain exists
        final Domain domain = domainDao.getDomain(domainId);
        if (domain == null)
            throw new ConfigurationStoreException("Domain with id " + domainId + " does not exist");
        
        // make sure the policy group exists
        final CertPolicyGroup policyGroup = this.getPolicyGroupById(policyGroupId);
        if (policyGroup == null)
            throw new ConfigurationStoreException("Policy group with id " + policyGroup + " does not exist");

        certPolicyService.associatePolicyGroupToDomain(domain, policyGroup);
    }

    @Override
    public void disassociatePolicyGroupFromDomain(long domainId, long policyGroupId) throws ConfigurationStoreException {
        // make sure the domain exists
        final Domain domain = domainDao.getDomain(domainId);
        if (domain == null)
            throw new ConfigurationStoreException("Domain with id " + domainId + " does not exist");
        
        // make sure the policy group exists
        final CertPolicyGroup policyGroup = this.getPolicyGroupById(policyGroupId);
        if (policyGroup == null) {
            throw new ConfigurationStoreException("Policy group with id " + policyGroup + " does not exist");
        }
        
        certPolicyService.disassociatePolicyGroupFromDomain(domain, policyGroup);
    }

    @Override
    public void disassociatePolicyGroupsFromDomain(long domainId) throws ConfigurationStoreException  {
        // make sure the domain exists
        final Domain domain = domainDao.getDomain(domainId);
        if (domain == null)
            throw new ConfigurationStoreException("Domain with id " + domainId + " does not exist");
        
        certPolicyService.disassociatePolicyGroupsFromDomain(domain);
    }

    @Override
    public void disassociatePolicyGroupFromDomains(long policyGroupId) throws ConfigurationStoreException {
    	certPolicyService.disassociatePolicyGroupFromDomains(policyGroupId);
    }

    @Override
    public Collection<CertPolicyGroupDomainReltn> getPolicyGroupDomainReltns() throws ConfigurationStoreException {
            return certPolicyService.getPolicyGroupDomainReltns();
    }
    
    @Override
    public Collection<CertPolicyGroupDomainReltn> getPolicyGroupsByDomain(long domainId) throws ConfigurationStoreException {
        // make sure the domain exists
        final Domain domain = domainDao.getDomain(domainId);

        if (domain == null) {
            throw new ConfigurationStoreException("Domain with id " + domainId + " does not exist");
        }
        
        return certPolicyService.getPolicyGroupsByDomain(domain);
    }    
}
