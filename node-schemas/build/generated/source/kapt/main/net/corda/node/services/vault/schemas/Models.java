// Generated file do not edit, generated by io.requery.processor.EntityProcessor
package net.corda.node.services.vault.schemas;

import io.requery.meta.EntityModel;
import io.requery.meta.EntityModelBuilder;
import javax.annotation.Generated;

@Generated("io.requery.processor.EntityProcessor")
public class Models {
    public static final EntityModel VAULT = new EntityModelBuilder("vault")
    .addType(VaultTxnNoteEntity.$TYPE)
    .addType(VaultCashBalancesEntity.$TYPE)
    .addType(VaultStatesEntity.$TYPE)
    .build();

    private Models() {
    }
}
