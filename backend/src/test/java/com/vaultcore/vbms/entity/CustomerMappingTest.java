package com.vaultcore.vbms.entity;

import static org.junit.jupiter.api.Assertions.assertFalse;

import jakarta.persistence.JoinColumn;
import org.junit.jupiter.api.Test;

class CustomerMappingTest {
    @Test
    void branchAssociationIsMarkedNotNullable() throws NoSuchFieldException {
        JoinColumn joinColumn = Customer.class.getDeclaredField("branch").getAnnotation(JoinColumn.class);

        assertFalse(joinColumn.nullable());
    }
}
