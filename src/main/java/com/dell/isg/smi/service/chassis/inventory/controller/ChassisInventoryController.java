/**
 * Copyright © 2017 DELL Inc. or its subsidiaries.  All Rights Reserved.
 */
package com.dell.isg.smi.service.chassis.inventory.controller;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.dell.isg.smi.common.protocol.command.cmc.entity.Chassis;
import com.dell.isg.smi.common.protocol.command.cmc.entity.ChassisCMCViewEntity;
import com.dell.isg.smi.common.protocol.command.cmc.entity.RacadmCredentials;
import com.dell.isg.smi.commons.elm.exception.RuntimeCoreException;
import com.dell.isg.smi.commons.elm.utilities.CustomRecursiveToStringStyle;
import com.dell.isg.smi.commons.model.common.Credential;
import com.dell.isg.smi.commons.model.common.DevicesIpsRequest;
import com.dell.isg.smi.commons.model.common.InventoryCallbackRequest;
import com.dell.isg.smi.commons.model.common.InventoryInformation;
import com.dell.isg.smi.commons.model.common.ResponseString;
import com.dell.isg.smi.commons.model.commons.chassis.inventory.ChassisDetail;
import com.dell.isg.smi.commons.model.commons.chassis.inventory.ChassisSummary;
import com.dell.isg.smi.service.chassis.exception.BadRequestException;
import com.dell.isg.smi.service.chassis.exception.EnumErrorCode;
import com.dell.isg.smi.service.chassis.inventory.manager.IInventoryManager;
import com.dell.isg.smi.service.chassis.inventory.utilities.TransformerUtil;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/api/1.0/chassis/inventory")
public class ChassisInventoryController {

    @Autowired
    IInventoryManager inventoryManagerImpl;

    private static final Logger logger = LoggerFactory.getLogger(ChassisInventoryController.class.getName());


    @RequestMapping(value = "/details", method = RequestMethod.POST, headers = "Accept=application/json", consumes = "application/json", produces = "application/json")
    @ApiOperation(value = "/details", nickname = "details", notes = "This operation allow user to get complete chassis hardware inventory throu Racadm.", response = ChassisDetail.class)
    // @ApiImplicitParams({
    // @ApiImplicitParam(name = "credential", value = "Credential", required = true, dataType = Credential.class, paramType = "Body", defaultValue = "no default") })
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Success", response = ChassisDetail.class), @ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 500, message = "Failure") })
    public ChassisDetail details(@RequestBody Credential credential) {
        ChassisDetail chassis = null;

        if (credential == null || StringUtils.isEmpty(credential.getAddress())) {
            BadRequestException badRequestException = new BadRequestException();
            badRequestException.setErrorCode(EnumErrorCode.IOIDENTITY_INVALID_INPUT);
            throw badRequestException;
        }

        try {
            RacadmCredentials racadmCredentials = new RacadmCredentials(credential.getAddress(), credential.getUserName(), credential.getPassword(), false);
            Chassis result = inventoryManagerImpl.collectChassisInventory(racadmCredentials);
            chassis = TransformerUtil.transformInventory(result);
        } catch (Exception e) {
            logger.error("Exception occured in inventory : ", e);
            RuntimeCoreException runtimeCoreException = new RuntimeCoreException(e);
            runtimeCoreException.setErrorCode(EnumErrorCode.ENUM_SERVER_ERROR);
            throw runtimeCoreException;
        }
        return chassis;
    }


    @RequestMapping(value = "/summary", method = RequestMethod.POST, headers = "Accept=application/json", consumes = "application/json", produces = "application/json")
    @ApiOperation(value = "/summary", nickname = "summary", notes = "This operation allow user to get partial chassis information throu Racadm.", response = ChassisSummary.class)
    // @ApiImplicitParams({
    // @ApiImplicitParam(name = "credential", value = "Credential", required = true, dataType = "Credential.class", paramType = "Body", defaultValue = "no default") })
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Success", response = ChassisSummary.class), @ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 500, message = "Failure") })
    public ChassisSummary summary(@RequestBody Credential credential) {
        ChassisSummary summary = null;
        if (credential == null || StringUtils.isEmpty(credential.getAddress())) {
            BadRequestException badRequestException = new BadRequestException();
            badRequestException.setErrorCode(EnumErrorCode.IOIDENTITY_INVALID_INPUT);
            throw badRequestException;
        }
        try {
            RacadmCredentials racadmCredentials = new RacadmCredentials(credential.getAddress(), credential.getUserName(), credential.getPassword(), false);
            ChassisCMCViewEntity chassis = inventoryManagerImpl.collectChassisSummary(racadmCredentials);
            summary = TransformerUtil.transformSummary(chassis);
        } catch (Exception e) {
            logger.error("Exception occured in inventory : ", e);
            RuntimeCoreException runtimeCoreException = new RuntimeCoreException(e);
            runtimeCoreException.setErrorCode(EnumErrorCode.ENUM_SERVER_ERROR);
            throw runtimeCoreException;
        }
        return summary;
    }


    @RequestMapping(value = "/ips", method = RequestMethod.POST, headers = "Accept=application/json", consumes = "application/json", produces = "application/json")
    @ApiOperation(value = "/ips", nickname = "ips", notes = "This operation allow user to collect server software identity throu wsman.", response = InventoryInformation.class, responseContainer = "List")
    // @ApiImplicitParams({
    // @ApiImplicitParam(name = "deviceIps", value = "DevicesIpsRequest", required = true, dataType = "DevicesIpsRequest.class", paramType = "Body", defaultValue = "no default") })
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Success", response = InventoryInformation.class), @ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 500, message = "Failure") })
    public List<InventoryInformation> inventory(@RequestBody DevicesIpsRequest deviceIps) {
        logger.trace("Ips submitted for inventory : ", ReflectionToStringBuilder.toString(deviceIps, new CustomRecursiveToStringStyle(99)));
        List<InventoryInformation> response = null;
        if (deviceIps == null) {
            BadRequestException badRequestException = new BadRequestException();
            badRequestException.setErrorCode(EnumErrorCode.IOIDENTITY_INVALID_INPUT);
            throw badRequestException;
        }
        try {
            response = inventoryManagerImpl.inventory(Arrays.stream(deviceIps.getIps()).collect(Collectors.toSet()));
        } catch (Exception e) {
            logger.error("Exception occured in discovery : ", e);
            RuntimeCoreException runtimeCoreException = new RuntimeCoreException(e);
            runtimeCoreException.setErrorCode(EnumErrorCode.ENUM_SERVER_ERROR);
            throw runtimeCoreException;
        }
        logger.trace("Inventory Response : ", ReflectionToStringBuilder.toString(response, new CustomRecursiveToStringStyle(99)));
        return response;
    }


    @RequestMapping(value = "/callback", method = RequestMethod.POST, headers = "Accept=application/json", consumes = "application/json", produces = "application/json")
    @ApiOperation(value = "/callback", nickname = "callback", notes = "This operation allow user to collect all the chassis inventory throu wsman. It uses callback uri to respond once the inventory is done.", response = ResponseString.class)
    // @ApiImplicitParams({
    // @ApiImplicitParam(name = "inventoryCallbackRequest", value = "InventoryCallbackRequest", required = true, dataType = "InventoryCallbackRequest.class", paramType = "Body",
    // defaultValue = "no default") })
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Success", response = ResponseString.class), @ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 500, message = "Failure") })
    public ResponseString inventoryCallback(@RequestBody InventoryCallbackRequest inventoryCallbackRequest) {
        logger.trace("Inventory submitted for callback : {} : {}", inventoryCallbackRequest.getCredential().getAddress(), inventoryCallbackRequest.getCallbackUri());
        ResponseString response = new ResponseString();
        String result = "Failed to submit IP range for discovery..";
        if (inventoryCallbackRequest.getCredential() == null || StringUtils.isEmpty(inventoryCallbackRequest.getCredential().getAddress())) {
            BadRequestException badRequestException = new BadRequestException();
            badRequestException.setErrorCode(EnumErrorCode.IOIDENTITY_INVALID_INPUT);
            throw badRequestException;
        }
        response.setCallbackUri(inventoryCallbackRequest.getCallbackUri());
        result = "Request Submitted ... Result will  be posted to call uri : " + response.getCallbackUri();
        inventoryManagerImpl.processInventoryCallback(inventoryCallbackRequest);
        response.setResponse(result);
        return response;
    }

    // @RequestMapping(value = "/dummyCallback", method = RequestMethod.POST, headers = "Accept=application/json", consumes = "application/json", produces = "application/json")
    // @ApiOperation(value = "/dummyCallback", nickname = "dummyCallback", notes = "This operation allow user to collect server software identity throu wsman.", response =
    // ResponseString.class)
    // public String dummyCallback(@RequestBody InventoryCallbackResponse inventoryCallbackResponse) {
    // if (inventoryCallbackResponse == null) {
    // BadRequestException badRequestException = new BadRequestException();
    // badRequestException.setErrorCode(EnumErrorCode.IOIDENTITY_INVALID_INPUT);
    // throw badRequestException;
    // }
    // logger.debug("Inventory for callback : {} ", ReflectionToStringBuilder.toString(inventoryCallbackResponse, new CustomRecursiveToStringStyle(99)));
    // return "Request Submitted";
    // }

    // @RequestMapping(value = "/dummy", method = RequestMethod.POST, headers = "Accept=application/json", consumes = "application/json", produces = "application/json")
    // public InventoryCallbackResponse dummy(@RequestBody InventoryCallbackRequest inventoryCallbackRequest) {
    // logger.trace("Inventory submitted for callback : {} : {}", inventoryCallbackRequest.getCredential().getAddress(),inventoryCallbackRequest.getCallbackUri());
    // if (inventoryCallbackRequest.getCredential() == null || StringUtils.isEmpty(inventoryCallbackRequest.getCredential().getAddress())) {
    // BadRequestException badRequestException = new BadRequestException();
    // badRequestException.setErrorCode(EnumErrorCode.IOIDENTITY_INVALID_INPUT);
    // throw badRequestException;
    // }
    // return inventoryManagerImpl.dummy(inventoryCallbackRequest);
    // }

}
