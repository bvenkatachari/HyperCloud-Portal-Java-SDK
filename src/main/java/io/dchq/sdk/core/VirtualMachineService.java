package io.dchq.sdk.core;

import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.quotapolicy.QuotaPolicy;

import java.util.List;

public interface VirtualMachineService
  extends GenericService<QuotaPolicy, ResponseEntity<List<QuotaPolicy>>, ResponseEntity<QuotaPolicy>> {
}