package com.company.experiments.security;

import com.company.experiments.entity.InfrastructureHierarchy;
import com.company.experiments.entity.InfrastructureLevel;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.securityflowui.role.annotation.MenuPolicy;
import io.jmix.securityflowui.role.annotation.ViewPolicy;

@ResourceRole(name = "Infrastructure Read-Only", code = InfrastructureReadOnlyRole.CODE, description = "Grants read-only access to infrastructure entities")
public interface InfrastructureReadOnlyRole {

    String CODE = "infrastructure-read-only";

    @EntityPolicy(entityClass = InfrastructureLevel.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = InfrastructureLevel.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @ViewPolicy(viewIds = {"InfrastructureLevel.list", "InfrastructureLevel.detail"})
    void infrastructureLevel();

    @EntityPolicy(entityClass = InfrastructureHierarchy.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = InfrastructureHierarchy.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @ViewPolicy(viewIds = {"InfrastructureHierarchy.list", "InfrastructureHierarchy.detail"})
    void infrastructureHierarchy();

    @MenuPolicy(menuIds = {"InfrastructureLevel.list", "InfrastructureHierarchy.list"})
    void screens();
}
