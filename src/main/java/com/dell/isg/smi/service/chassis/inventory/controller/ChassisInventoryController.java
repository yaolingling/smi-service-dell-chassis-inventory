/**
 * Copyright © 2017 DELL Inc. or its subsidiaries.  All Rights Reserved.
 */
package com.dell.isg.smi.service.chassis.inventory.controller;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
import com.dell.isg.smi.commons.utilities.CustomRecursiveToStringStyle;
import com.dell.isg.smi.commons.model.common.Credential;
import com.dell.isg.smi.commons.model.common.DevicesIpsRequest;
import com.dell.isg.smi.commons.model.common.InventoryCallbackRequest;
import com.dell.isg.smi.commons.model.common.InventoryInformation;
import com.dell.isg.smi.commons.model.common.ResponseString;
import com.dell.isg.smi.commons.model.commons.chassis.inventory.ChassisDetail;
import com.dell.isg.smi.commons.model.commons.chassis.inventory.ChassisSummary;
import com.dell.isg.smi.service.chassis.inventory.manager.IInventoryManager;
import com.dell.isg.smi.service.chassis.inventory.utilities.TransformerUtil;
import com.dell.isg.smi.service.chassis.inventory.utilities.ValidationUtilities;
import com.dell.isg.smi.service.chassis.exception.BadRequestException;

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
    @ApiOperation(value = "/details", nickname = "details", notes = "This operation allows a user to retrieve complete chassis hardware inventory via the Racadm.", response = ChassisDetail.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Success", response = ChassisDetail.class), @ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 500, message = "Failure") })
    public ChassisDetail details(@RequestBody Credential credential) {
        ValidationUtilities.validateRequestPayload(credential);
        ChassisDetail chassis = null;
        try {
            RacadmCredentials racadmCredentials = new RacadmCredentials(credential.getAddress(), credential.getUserName(), credential.getPassword(), false);
            Chassis result = inventoryManagerImpl.collectChassisInventory(racadmCredentials);
            chassis = TransformerUtil.transformInventory(result);
        } catch (Exception e) {
            logger.error("Exception occured in inventory : {}", e.getMessage());
            BadRequestException badRequestException = new BadRequestException();
            badRequestException.setErrorCode(com.dell.isg.smi.commons.elm.model.EnumErrorCode.ENUM_GENERIC_MESSAGE);
            badRequestException.addAttribute(e.getMessage());
            throw badRequestException;
        }
        return chassis;
    }


    @RequestMapping(value = "/summary", method = RequestMethod.POST, headers = "Accept=application/json", consumes = "application/json", produces = "application/json")
    @ApiOperation(value = "/summary", nickname = "summary", notes = "This operation allows a user to retrieve chassis summary information via the Racadm.", response = ChassisSummary.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Success", response = ChassisSummary.class), @ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 500, message = "Failure") })
    public ChassisSummary summary(@RequestBody Credential credential) {
        ValidationUtilities.validateRequestPayload(credential);
        ChassisSummary summary = null;
        try {
            RacadmCredentials racadmCredentials = new RacadmCredentials(credential.getAddress(), credential.getUserName(), credential.getPassword(), false);
            ChassisCMCViewEntity chassis = inventoryManagerImpl.collectChassisSummary(racadmCredentials);
            summary = TransformerUtil.transformSummary(chassis);
        } catch (Exception e) {
            logger.error("Exception occured in inventory : {}", e.getMessage());
            BadRequestException badRequestException = new BadRequestException();
            badRequestException.setErrorCode(com.dell.isg.smi.commons.elm.model.EnumErrorCode.ENUM_GENERIC_MESSAGE);
            badRequestException.addAttribute(e.getMessage());
            throw badRequestException;
        }
        return summary;
    }


    public List<InventoryInformation> inventory(@RequestBody DevicesIpsRequest deviceIps) {
        ValidationUtilities.validateRequestPayload(deviceIps);
        logger.trace("Ips submitted for inventory : ", ReflectionToStringBuilder.toString(deviceIps, new CustomRecursiveToStringStyle(99)));
        List<InventoryInformation> response = null;
        try {
            response = inventoryManagerImpl.inventory(Arrays.stream(deviceIps.getIps()).collect(Collectors.toSet()));
        } catch (Exception e) {
            logger.error("Exception occured in discovery : {}", e.getMessage());
            BadRequestException badRequestException = new BadRequestException();
            badRequestException.setErrorCode(com.dell.isg.smi.commons.elm.model.EnumErrorCode.ENUM_GENERIC_MESSAGE);
            badRequestException.addAttribute(e.getMessage());
            throw badRequestException;
        }
        logger.trace("Inventory Response : ", ReflectionToStringBuilder.toString(response, new CustomRecursiveToStringStyle(99)));
        return response;
    }

    @RequestMapping(value = "/callback", method = RequestMethod.POST, headers = "Accept=application/json", consumes = "application/json", produces = "application/json")
    @ApiOperation(value = "/callback", nickname = "callback", notes = "This operation allows a user to retrieve all the chassis inventory via the Racadm. It uses callback uri to respond once the inventory is collected. Type value : summary : details", response = ResponseString.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Success", response = ResponseString.class), @ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 500, message = "Failure") })
    public ResponseString inventoryCallback(@RequestBody InventoryCallbackRequest inventoryCallbackRequest) {
        ValidationUtilities.validateRequestPayload(inventoryCallbackRequest);
        logger.trace("Inventory submitted for callback : {} : {}", inventoryCallbackRequest.getCredential().getAddress(), inventoryCallbackRequest.getCallbackUri());
        ResponseString response = new ResponseString();
        String result = "Failed to submit IP range for discovery..";
        response.setCallbackUri(inventoryCallbackRequest.getCallbackUri());
        result = "Request Submitted ... Result will  be posted to call uri : " + response.getCallbackUri();
        inventoryManagerImpl.processInventoryCallback(inventoryCallbackRequest);
        response.setResponse(result);
        return response;
    }

}
