/**
 * Copyright © 2017 DELL Inc. or its subsidiaries.  All Rights Reserved.
 */
package com.dell.isg.smi.service.chassis.inventory.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestTemplate;

import com.dell.isg.smi.adapter.chassis.IChassisAdapter;
import com.dell.isg.smi.common.protocol.command.cmc.entity.Chassis;
import com.dell.isg.smi.common.protocol.command.cmc.entity.ChassisCMCViewEntity;
import com.dell.isg.smi.common.protocol.command.cmc.entity.RacadmCredentials;
import com.dell.isg.smi.commons.elm.exception.RuntimeCoreException;
import com.dell.isg.smi.commons.elm.utilities.CustomRecursiveToStringStyle;
import com.dell.isg.smi.commons.model.common.Credential;
import com.dell.isg.smi.commons.model.common.Defaults;
import com.dell.isg.smi.commons.model.common.InventoryCallbackRequest;
import com.dell.isg.smi.commons.model.common.InventoryCallbackResponse;
import com.dell.isg.smi.commons.model.common.InventoryInformation;
import com.dell.isg.smi.commons.model.common.Options;
import com.dell.isg.smi.commons.model.commons.chassis.inventory.ChassisDetail;
import com.dell.isg.smi.commons.model.commons.chassis.inventory.ChassisSummary;
import com.dell.isg.smi.service.chassis.exception.EnumErrorCode;
import com.dell.isg.smi.service.chassis.inventory.manager.thread.InventoryCollectionThread;
import com.dell.isg.smi.service.chassis.inventory.utilities.TransformerUtil;
import com.fasterxml.jackson.core.JsonProcessingException;

@Component
public class InventoryManagerImpl implements IInventoryManager {

    @Autowired
    IChassisAdapter chassisAdapterImpl;

    private static final int CHASSIS_INVENTORY_THREAD_POOL = 1000;

    private static final Logger logger = LoggerFactory.getLogger(InventoryManagerImpl.class.getName());


    @Override
    public List<InventoryInformation> inventory(Set<String> ips) throws Exception {
        List<InventoryInformation> serverInfos = new ArrayList<InventoryInformation>();
        for (String validIp : ips) {
            InventoryInformation serverInfo = new InventoryInformation();
            serverInfo.setIpAddress(validIp);
            serverInfo.setCredential(new Credential());
            serverInfos.add(serverInfo);
        }
        return getInventory(serverInfos);
    }


    private List<InventoryInformation> getInventory(List<InventoryInformation> serverInfos) {
        logger.trace("Started inventory threads");
        System.out.println(" Chassis count for inventory = " + serverInfos.size());
        StopWatch watch = new StopWatch();
        watch.start();
        if (serverInfos.size() > 0) {
            ExecutorService executor = Executors.newFixedThreadPool(CHASSIS_INVENTORY_THREAD_POOL);
            for (InventoryInformation chassisInfo : serverInfos) {
                Runnable serverDiscoverTask = new InventoryCollectionThread(chassisInfo, chassisAdapterImpl);
                executor.execute(serverDiscoverTask);
            }
            executor.shutdown();
            try {
                executor.awaitTermination(60, TimeUnit.SECONDS);
                executor.shutdownNow();
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }
        watch.stop();
        logger.trace("Finished chassis inventory threads.");
        logger.trace("Time taken for inventory in seconds: " + watch.getTotalTimeSeconds());
        return serverInfos;
    }


    @Override
    @Async
    public void processInventoryCallback(InventoryCallbackRequest inventoryCallbackRequest) {
        String type = inventoryCallbackRequest.getType();
        try {
            Object dataObject = getInventoryObject(type, inventoryCallbackRequest.getCredential());
            InventoryCallbackResponse inventoryCallbackResponse = createCallbackResponse(dataObject, type, inventoryCallbackRequest.getCallbackGraph());
            logger.trace("Inventory callback response : ", ReflectionToStringBuilder.toString(dataObject, new CustomRecursiveToStringStyle(99)));
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<InventoryCallbackResponse> entity = new HttpEntity<InventoryCallbackResponse>(inventoryCallbackResponse, headers);
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(inventoryCallbackRequest.getCallbackUri(), entity, String.class);
            logger.debug("Response recived after posting to URI : " + responseEntity.getBody());
        } catch (Exception e) {
            logger.error("Exception occured in discovery : ", e);
            RuntimeCoreException runtimeCoreException = new RuntimeCoreException(e);
            runtimeCoreException.setErrorCode(EnumErrorCode.ENUM_SERVER_ERROR);
            throw runtimeCoreException;
        }
    }


    private InventoryCallbackResponse createCallbackResponse(Object dataObject, String type, String graphName) throws JsonProcessingException {
        InventoryCallbackResponse inventoryCallbackResponse = new InventoryCallbackResponse();
        inventoryCallbackResponse.setName(graphName);
        Defaults defaults = new Defaults();
        defaults.setType(type);
        defaults.setData(dataObject);
        Options options = new Options();
        options.setDefaults(defaults);
        inventoryCallbackResponse.setOptions(options);
        return inventoryCallbackResponse;
    }


    private Object getInventoryObject(String type, Credential cred) {
        Object dataObject = null;
        RacadmCredentials racadmCredentials = new RacadmCredentials(cred.getAddress(), cred.getUserName(), cred.getPassword(), false);
        try {
            if ("details".equals(type)) {
                Chassis result = chassisAdapterImpl.collectChassisInventory(racadmCredentials);
                ChassisDetail chassis = TransformerUtil.transformInventory(result);
                chassis.setId(cred.getAddress());
                dataObject = chassis;

            } else if ("summary".equals(type)) {
                ChassisCMCViewEntity chassis = chassisAdapterImpl.collectChassisSummary(racadmCredentials);
                ChassisSummary summary = TransformerUtil.transformSummary(chassis);
                summary.setId(cred.getAddress());
                dataObject = summary;
            }

        } catch (Exception e) {
            logger.error("Exception occured in discovery : ", e);
            RuntimeCoreException runtimeCoreException = new RuntimeCoreException(e);
            runtimeCoreException.setErrorCode(EnumErrorCode.ENUM_SERVER_ERROR);
            throw runtimeCoreException;
        }

        return dataObject;
    }


    @Override
    public InventoryCallbackResponse dummy(InventoryCallbackRequest inventoryCallbackRequest) {
        InventoryCallbackResponse inventoryCallbackResponse = null;
        try {
            Object dataObject = getInventoryObject(inventoryCallbackRequest.getType(), inventoryCallbackRequest.getCredential());
            inventoryCallbackResponse = createCallbackResponse(dataObject, inventoryCallbackRequest.getType(), inventoryCallbackRequest.getCallbackGraph());
        } catch (Exception e) {
            logger.error("Exception occured in discovery : ", e);
            RuntimeCoreException runtimeCoreException = new RuntimeCoreException(e);
            runtimeCoreException.setErrorCode(EnumErrorCode.ENUM_SERVER_ERROR);
            throw runtimeCoreException;
        }
        logger.trace("Hardware inventory Response : ", ReflectionToStringBuilder.toString(inventoryCallbackResponse, new CustomRecursiveToStringStyle(99)));
        return inventoryCallbackResponse;
    }


    @Override
    public Chassis collectChassisInventory(RacadmCredentials racadmCredentials) throws Exception {
        return chassisAdapterImpl.collectChassisInventory(racadmCredentials);
    }


    @Override
    public ChassisCMCViewEntity collectChassisSummary(RacadmCredentials racadmCredentials) throws Exception {
        return chassisAdapterImpl.collectChassisSummary(racadmCredentials);
    }

}
