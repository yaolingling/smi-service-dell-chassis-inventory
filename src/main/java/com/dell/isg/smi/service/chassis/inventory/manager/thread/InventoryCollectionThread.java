/**
 * Copyright © 2017 DELL Inc. or its subsidiaries.  All Rights Reserved.
 */
package com.dell.isg.smi.service.chassis.inventory.manager.thread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dell.isg.smi.adapter.chassis.IChassisAdapter;
import com.dell.isg.smi.common.protocol.command.cmc.entity.Chassis;
import com.dell.isg.smi.common.protocol.command.cmc.entity.RacadmCredentials;
import com.dell.isg.smi.commons.model.common.InventoryInformation;
import com.dell.isg.smi.commons.model.common.InventoryStatus;
import com.dell.isg.smi.commons.model.commons.chassis.inventory.ChassisDetail;
import com.dell.isg.smi.service.chassis.inventory.utilities.TransformerUtil;

public class InventoryCollectionThread implements Runnable {
    private InventoryInformation chassisInventory;

    IChassisAdapter chassisAdapterImpl;

    private static final Logger logger = LoggerFactory.getLogger(InventoryCollectionThread.class.getName());


    public InventoryCollectionThread(InventoryInformation info, IChassisAdapter chassisAdapterImpl) {
        this.chassisInventory = info;
        this.chassisAdapterImpl = chassisAdapterImpl;
    }


    @Override
    public void run() {
        try {
            processCommand();
        } catch (Exception e) {
            logger.error(" Discovery Failed Reason for :" + e.getMessage());
        }
    }


    private void processCommand() throws Exception {
        chassisInventory.setStatus(InventoryStatus.INPROGRESS.name());
        logger.trace("Started inventory for IP : ", chassisInventory.getIpAddress());
        try {
            RacadmCredentials racadmCredentials = new RacadmCredentials(chassisInventory.getIpAddress(), chassisInventory.getCredential().getUserName(), chassisInventory.getCredential().getPassword(), false);
            Chassis result = chassisAdapterImpl.collectChassisInventory(racadmCredentials);
            ChassisDetail chassis = TransformerUtil.transformInventory(result);
            chassis.setId(chassisInventory.getIpAddress());
            chassisInventory.setInventory(chassis);
            chassisInventory.setStatus(InventoryStatus.COMPLETED.name());
            logger.trace("Completed inventory for IP : ", chassisInventory.getIpAddress());
        } catch (Exception e) {
            chassisInventory.setStatus(InventoryStatus.FAILED.name());
            logger.trace("Failed inventory for IP : ", chassisInventory.getIpAddress(), e);
        }
    }

}
