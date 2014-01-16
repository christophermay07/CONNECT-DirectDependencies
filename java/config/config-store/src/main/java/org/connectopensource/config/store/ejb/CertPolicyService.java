package org.connectopensource.config.store.ejb;

import java.util.Collection;

import org.nhindirect.config.store.CertPolicy;
import org.nhindirect.config.store.CertPolicyGroup;
import org.nhindirect.config.store.CertPolicyUse;
import org.nhindirect.config.store.ConfigurationStoreException;
import org.nhindirect.config.store.CertPolicyGroupDomainReltn;
import org.nhindirect.config.store.Domain;
import org.nhindirect.policy.PolicyLexicon;

public interface CertPolicyService {
    public Collection<CertPolicy> getPolicies() throws ConfigurationStoreException;

    public CertPolicy getPolicyByName(String policyName) throws ConfigurationStoreException;

    public CertPolicy getPolicyById(long id) throws ConfigurationStoreException;

    public void addPolicy(CertPolicy policy) throws ConfigurationStoreException;

    public void deletePolicies(long[] policyIds) throws ConfigurationStoreException;

    public void updatePolicyAttributes(long id, String policyName, PolicyLexicon lexicon,
            byte[] policyData) throws ConfigurationStoreException;

    public Collection<CertPolicyGroup> getPolicyGroups() throws ConfigurationStoreException;

    public CertPolicyGroup getPolicyGroupByName(String policyGroupName) throws ConfigurationStoreException;

    public CertPolicyGroup getPolicyGroupById(long id) throws ConfigurationStoreException;

    public void addPolicyGroup(CertPolicyGroup group) throws ConfigurationStoreException;

    public void deletePolicyGroups(long[] groupIds) throws ConfigurationStoreException;

    public void updateGroupAttributes(long id, String groupName) throws ConfigurationStoreException;

    public void addPolicyUseToGroup(long groupId, long policyId, CertPolicyUse policyUse,
            boolean incoming, boolean outgoing) throws ConfigurationStoreException;

    public void removePolicyUseFromGroup(long policyGroupReltnId) throws ConfigurationStoreException;

    public void disassociatePolicyGroupFromDomains(long policyGroupId) throws ConfigurationStoreException;

    public Collection<CertPolicyGroupDomainReltn> getPolicyGroupDomainReltns() throws ConfigurationStoreException;

    public void removePolicyUseFromGroups(long policyId) throws ConfigurationStoreException;

    public void associatePolicyGroupToDomain(Domain domain, CertPolicyGroup policyGroup) throws ConfigurationStoreException;

    public void disassociatePolicyGroupFromDomain(Domain domain,
			CertPolicyGroup policyGroup) throws ConfigurationStoreException;

    public void disassociatePolicyGroupsFromDomain(Domain domain) throws ConfigurationStoreException;

    public Collection<CertPolicyGroupDomainReltn> getPolicyGroupsByDomain(Domain domain) throws ConfigurationStoreException;
}
