/**
 * Copyright © 2017 DELL Inc. or its subsidiaries.  All Rights Reserved.
 */
package com.dell.isg.smi.service.chassis.inventory.utilities;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

import com.dell.isg.smi.common.protocol.command.chassis.entity.ModInfo;
import com.dell.isg.smi.common.protocol.command.cmc.entity.Chassis;
import com.dell.isg.smi.common.protocol.command.cmc.entity.ChassisCMCViewEntity;
import com.dell.isg.smi.common.protocol.command.cmc.entity.ChassisFanEntity;
import com.dell.isg.smi.common.protocol.command.cmc.entity.ChassisIkvm;
import com.dell.isg.smi.common.protocol.command.cmc.entity.ChassisPCIeEntity;
import com.dell.isg.smi.common.protocol.command.cmc.entity.ChassisPowerSupplyEntity;
import com.dell.isg.smi.common.protocol.command.cmc.entity.ChassisTemperatureSensorEntity;
import com.dell.isg.smi.common.protocol.command.cmc.entity.IOModuleEntity;
import com.dell.isg.smi.commons.model.commons.chassis.inventory.ChassisControllers;
import com.dell.isg.smi.commons.model.commons.chassis.inventory.ChassisDetail;
import com.dell.isg.smi.commons.model.commons.chassis.inventory.ChassisFan;
import com.dell.isg.smi.commons.model.commons.chassis.inventory.ChassisIkvmModel;
import com.dell.isg.smi.commons.model.commons.chassis.inventory.ChassisIomModel;
import com.dell.isg.smi.commons.model.commons.chassis.inventory.ChassisPci;
import com.dell.isg.smi.commons.model.commons.chassis.inventory.ChassisPowerSupply;
import com.dell.isg.smi.commons.model.commons.chassis.inventory.ChassisServerModel;
import com.dell.isg.smi.commons.model.commons.chassis.inventory.ChassisStashStorage;
import com.dell.isg.smi.commons.model.commons.chassis.inventory.ChassisSummary;
import com.dell.isg.smi.commons.model.commons.chassis.inventory.ChassisTemperatureSensor;

public class TransformerUtil {

    public static ChassisSummary transformSummary(ChassisCMCViewEntity chassisCMCViewEntity) {
        ChassisSummary chassis = new ChassisSummary();
        chassis.setServiceTag(chassisCMCViewEntity.getServiceTag());
        chassis.setLocation(chassisCMCViewEntity.getChassisLocation());
        chassis.setModel(chassisCMCViewEntity.getSystemModel());
        chassis.setName(chassisCMCViewEntity.getChassisName());
        chassis.setDnsName(chassisCMCViewEntity.getDnsCMCName());
        return chassis;

    }


    public static ChassisDetail transformInventory(Chassis chassisInventoryAdaptorDto) {
        if (chassisInventoryAdaptorDto == null)
            return null;

        ChassisDetail chassis = new ChassisDetail();

        List<ChassisControllers> newChassisCmcList = extractCmcEntity(chassisInventoryAdaptorDto.getChassisCmcList());
        chassis.getChassisControllers().addAll(newChassisCmcList);

        List<ChassisPowerSupply> newChassisPowerSupplyList = extractPowerSupplyEntity(chassisInventoryAdaptorDto.getChassisPowerSupplyList());
        chassis.getChassisPowerSupply().addAll(newChassisPowerSupplyList);

        List<ChassisIkvmModel> newChassisIkvmList = extractIkvmEntity(chassisInventoryAdaptorDto.getChassisIKvmList());
        chassis.getChassisIkvm().addAll(newChassisIkvmList);

        List<ChassisServerModel> newChassisServerList = extractChassisServerEntity(chassisInventoryAdaptorDto.getServerList());
        chassis.getServers().addAll(newChassisServerList);

        List<ChassisIomModel> newChassisIomList = extractChassisIomEntity(chassisInventoryAdaptorDto.getIoModuleEntityList());
        chassis.getChassisIoms().addAll(newChassisIomList);

        List<ChassisStashStorage> newChassisStashStorageList = extractChassisStashStorage(chassisInventoryAdaptorDto.getStashList());
        chassis.getChassisStashStorages().addAll(newChassisStashStorageList);

        List<ChassisFan> newChassisFanList = extractChassisFan(chassisInventoryAdaptorDto.getChassisFanList());
        chassis.getChassisFans().addAll(newChassisFanList);

        List<ChassisPci> newChassisPciList = extractChassisPci(chassisInventoryAdaptorDto.getChassisPciList());
        chassis.getChassisPcis().addAll(newChassisPciList);

        List<ChassisTemperatureSensor> newChassisTemperatureSensorList = extractChassisTemperatureSensor(chassisInventoryAdaptorDto.getChassisTemperatureSensorList());
        chassis.getChassisTemperatureSensors().addAll(newChassisTemperatureSensorList);

        chassis.setTotalSlots(chassisInventoryAdaptorDto.getNumberOfSlots());
        chassis.setFreeSlots(chassisInventoryAdaptorDto.getNumberOfFreeSlots());

        return chassis;
    }


    private static List<ChassisControllers> extractCmcEntity(List<ChassisCMCViewEntity> chassisCMCViewEntityList) {
        List<ChassisControllers> chassisCmcList = new ArrayList<ChassisControllers>();
        if (CollectionUtils.isEmpty(chassisCMCViewEntityList))
            return chassisCmcList;

        for (ChassisCMCViewEntity chassisCMCViewEntity : chassisCMCViewEntityList) {
            ChassisControllers chassisCmc = new ChassisControllers();
            chassisCmc.setFirmwareVersion(chassisCMCViewEntity.getStandByCMCVersion());
            chassisCmc.setHardwareVersion(chassisCMCViewEntity.getHardwareVersion());
            chassisCmc.setLocation(chassisCMCViewEntity.getChassisLocation());
            chassisCmc.setMidplaneVersion(chassisCMCViewEntity.getChassisMidPlaneVersion());
            chassisCmc.setName(chassisCMCViewEntity.getChassisName());
            chassisCmc.setFirmwareVersion(chassisCMCViewEntity.getPrimaryCMCVersion());
            chassisCmcList.add(chassisCmc);
        }
        return chassisCmcList;
    }


    private static List<ChassisPowerSupply> extractPowerSupplyEntity(List<ChassisPowerSupplyEntity> powerSupplyEntityList) {
        List<ChassisPowerSupply> chassisPowerSupplyList = new ArrayList<ChassisPowerSupply>();
        if (CollectionUtils.isEmpty(powerSupplyEntityList))
            return chassisPowerSupplyList;

        for (ChassisPowerSupplyEntity chassisPowerSupplyEntity : powerSupplyEntityList) {
            ChassisPowerSupply chassisPowerSupply = new ChassisPowerSupply();
            chassisPowerSupply.setCapacity(chassisPowerSupplyEntity.getOutputRatedPower());
            chassisPowerSupply.setName(chassisPowerSupplyEntity.getName());
            // chassisPowerSupply.setPowerState(chassisPowerSupplyEntity.getPowerState());
            chassisPowerSupply.setPresent(chassisPowerSupplyEntity.getPresent());
            chassisPowerSupplyList.add(chassisPowerSupply);
        }

        return chassisPowerSupplyList;
    }


    private static List<ChassisIkvmModel> extractIkvmEntity(List<ChassisIkvm> chassisIkvmEntityList) {
        List<ChassisIkvmModel> chassisIkvmList = new ArrayList<ChassisIkvmModel>();
        if (CollectionUtils.isEmpty(chassisIkvmEntityList))
            return chassisIkvmList;
        for (ChassisIkvm chassisIkvmEntity : chassisIkvmEntityList) {
            ChassisIkvmModel chassisIkvm = new ChassisIkvmModel();
            chassisIkvm.setFirmwareVersion(chassisIkvmEntity.getFirmwareVersion());
            chassisIkvm.setHealth(chassisIkvmEntity.getHealth());
            chassisIkvm.setManufacturer(chassisIkvmEntity.getManufacturer());
            chassisIkvm.setName(chassisIkvmEntity.getName());
            chassisIkvm.setPartNumber(chassisIkvmEntity.getPartnumber());
            // chassisIkvm.setPowerStatus(chassisIkvmEntity.getPowerStatus());
            // chassisIkvm.setPresent(chassisIkvmEntity.isPresent());
            chassisIkvmList.add(chassisIkvm);
        }

        return chassisIkvmList;
    }


    private static List<ChassisServerModel> extractChassisServerEntity(List<ModInfo> serverEntityList) {
        List<ChassisServerModel> chassisServerList = new ArrayList<ChassisServerModel>();
        if (CollectionUtils.isEmpty(serverEntityList))
            return chassisServerList;
        for (ModInfo modInfo : serverEntityList) {
            if (modInfo.getSvcTag() != null && !modInfo.getSvcTag().trim().equalsIgnoreCase("N/A")) {
                ChassisServerModel chassisServer = new ChassisServerModel();
                chassisServer.setServiceTag(modInfo.getSvcTag());
                chassisServer.setChassisSlotName(modInfo.getModule());
                chassisServer.setHealthStatus(modInfo.getHealth());
                chassisServer.setPowerStatus(modInfo.getPwrState());
                chassisServer.setPresence(modInfo.getPresence());
                chassisServerList.add(chassisServer);
            }
        }
        return chassisServerList;
    }


    private static List<ChassisIomModel> extractChassisIomEntity(List<IOModuleEntity> iomEntityList) {
        List<ChassisIomModel> chassisIomList = new ArrayList<ChassisIomModel>();
        if (CollectionUtils.isEmpty(iomEntityList))
            return chassisIomList;
        for (IOModuleEntity iomEntity : iomEntityList) {
            ChassisIomModel chassisIom = new ChassisIomModel();
            chassisIom.setDhcpEnabled(iomEntity.isDhcpEnbaled());
            chassisIom.setFabric(iomEntity.getFabric());
            chassisIom.setFirmwareVersion(iomEntity.getFirmwareVersion());
            chassisIom.setGateway(iomEntity.getGateway());
            chassisIom.setHardwareVersion(iomEntity.getHardwareVersion());
            chassisIom.setIpAddress(iomEntity.getIpAddress());
            chassisIom.setMacAddress(iomEntity.getMacAddress());
            chassisIom.setName(iomEntity.getName());
            chassisIom.setNumber(iomEntity.getNumber());
            // chassisIom.setPowerStatus(iomEntity.getPowerStatus());
            chassisIom.setPresent(iomEntity.isPresent());
            chassisIom.setRole(iomEntity.getRole());
            chassisIom.setServiceTag(iomEntity.getServiceTag());
            chassisIom.setSlot(iomEntity.getSlot());
            chassisIom.setSubnetMask(iomEntity.getSubnetMask());
            chassisIomList.add(chassisIom);
        }
        return chassisIomList;
    }


    private static List<ChassisStashStorage> extractChassisStashStorage(List<ModInfo> stashList) {

        List<ChassisStashStorage> chassisStashStorageList = new ArrayList<ChassisStashStorage>();
        if (CollectionUtils.isEmpty(stashList))
            return chassisStashStorageList;

        for (ModInfo modInfo : stashList) {
            if (modInfo.getSvcTag() != null && !modInfo.getSvcTag().trim().equalsIgnoreCase("N/A")) {
                ChassisStashStorage chassisStashStorage = new ChassisStashStorage();
                chassisStashStorage.setSlotNumber(new Integer(modInfo.getModule().trim().split("-")[1]));
                chassisStashStorage.setServiceTag(modInfo.getSvcTag());
                chassisStashStorage.setSlotName(modInfo.getModule());
                chassisStashStorage.setPresence(modInfo.getPresence());
                // chassisStashStorage.setPowerState(AssemblerUtil.getComponentPowerStatusValue(modInfo.getPwrState());
                chassisStashStorage.setHealth(modInfo.getHealth());
                chassisStashStorageList.add(chassisStashStorage);
            }
        }
        return chassisStashStorageList;
    }


    private static List<ChassisTemperatureSensor> extractChassisTemperatureSensor(List<ChassisTemperatureSensorEntity> chassisTemperatureSensorList) {
        List<ChassisTemperatureSensor> chassisTemperatureList = new ArrayList<ChassisTemperatureSensor>();
        if (CollectionUtils.isEmpty(chassisTemperatureSensorList))
            return chassisTemperatureList;
        for (ChassisTemperatureSensorEntity entity : chassisTemperatureSensorList) {
            ChassisTemperatureSensor chassisTemperatureSensorDbEntity = new ChassisTemperatureSensor();
            chassisTemperatureSensorDbEntity.setSensorName(entity.getSensorName());
            chassisTemperatureSensorDbEntity.setSensorUnits(entity.getSensorUnits());
            chassisTemperatureSensorDbEntity.setStatus(entity.getStatus());
            chassisTemperatureSensorDbEntity.setReading(entity.getReading());
            chassisTemperatureSensorDbEntity.setLc(entity.getLc());
            chassisTemperatureSensorDbEntity.setLw(entity.getLw());
            chassisTemperatureSensorDbEntity.setUc(entity.getUc());
            chassisTemperatureSensorDbEntity.setUw(entity.getUw());
            chassisTemperatureSensorDbEntity.setNumber(entity.getNum());
            chassisTemperatureList.add(chassisTemperatureSensorDbEntity);
        }
        return chassisTemperatureList;
    }


    private static List<ChassisPci> extractChassisPci(List<ChassisPCIeEntity> chassisPciList) {
        List<ChassisPci> chassisPciDbList = new ArrayList<ChassisPci>();
        if (CollectionUtils.isEmpty(chassisPciList))
            return chassisPciDbList;
        for (ChassisPCIeEntity entity : chassisPciList) {
            ChassisPci chassisPciDbEntity = new ChassisPci();
            chassisPciDbEntity.setSlotNumber(entity.getSlotNumber());
            chassisPciDbEntity.setSlotName(entity.getSlotName());
            chassisPciDbEntity.setFabric(entity.getFabric());
            chassisPciDbEntity.setServerMapping(entity.getServerMapping());
            chassisPciDbEntity.setServerSlot(entity.getServerSlot());
            chassisPciDbEntity.setAdapterPresent(entity.getAdapterPresent());
            chassisPciDbEntity.setAssignmentStatus(entity.getAssignmentStatus());
            chassisPciDbEntity.setAllocatedSlotPower(entity.getAllocatedSlotPower());
            chassisPciDbEntity.setSlotType(entity.getSlotType());
            chassisPciDbEntity.setPciDeviceId(entity.getPciDeviceId());
            chassisPciDbEntity.setPciVendorId(entity.getPciVendorId());
            // chassisPciDbEntity.setPowerState(AssemblerUtil.getComponentPowerStatusValue(entity.getPowerState());
            chassisPciDbList.add(chassisPciDbEntity);
        }
        return chassisPciDbList;
    }


    private static List<ChassisFan> extractChassisFan(List<ChassisFanEntity> chassisFanList) {
        List<ChassisFan> chassisFanDbList = new ArrayList<ChassisFan>();
        if (CollectionUtils.isEmpty(chassisFanList))
            return chassisFanDbList;
        for (ChassisFanEntity entity : chassisFanList) {
            ChassisFan chassisFanDbEntity = new ChassisFan();
            chassisFanDbEntity.setName(entity.getName());
            chassisFanDbEntity.setUnits(entity.getUnits());
            chassisFanDbEntity.setPresent(Boolean.toString(entity.isPresent()));
            chassisFanDbEntity.setReading(entity.getReading());
            chassisFanDbEntity.setLc(entity.getLc());
            chassisFanDbEntity.setLw(entity.getLw());
            chassisFanDbEntity.setUc(entity.getUc());
            chassisFanDbEntity.setUw(entity.getUw());
            chassisFanDbEntity.setHealth(entity.getHealth());
            chassisFanDbEntity.setStatus(entity.getStatus());
            // chassisFanDbEntity.setPowerState(AssemblerUtil.getComponentPowerStatusValue(entity.getPowerState());
            chassisFanDbList.add(chassisFanDbEntity);
        }
        return chassisFanDbList;
    }

}
