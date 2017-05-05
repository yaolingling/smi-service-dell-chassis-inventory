/**
 * Copyright © 2017 DELL Inc. or its subsidiaries.  All Rights Reserved.
 */
package com.dell.isg.smi.service.chassis.inventory.manager;

import java.util.List;
import java.util.Set;

import com.dell.isg.smi.common.protocol.command.cmc.entity.Chassis;
import com.dell.isg.smi.common.protocol.command.cmc.entity.ChassisCMCViewEntity;
import com.dell.isg.smi.common.protocol.command.cmc.entity.RacadmCredentials;
import com.dell.isg.smi.commons.model.common.InventoryCallbackRequest;
import com.dell.isg.smi.commons.model.common.InventoryCallbackResponse;
import com.dell.isg.smi.commons.model.common.InventoryInformation;

public interface IInventoryManager {

    public List<InventoryInformation> inventory(Set<String> ips) throws Exception;


    public void processInventoryCallback(InventoryCallbackRequest inventoryCallbackRequest);


    InventoryCallbackResponse dummy(InventoryCallbackRequest inventoryCallbackRequest);


    public Chassis collectChassisInventory(RacadmCredentials racadmCredentials) throws Exception;


    public ChassisCMCViewEntity collectChassisSummary(RacadmCredentials racadmCredentials) throws Exception;

}
