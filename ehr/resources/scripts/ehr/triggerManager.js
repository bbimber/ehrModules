/*
 * Copyright (c) 2012-2019 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
var console = require("console");
var LABKEY = require("labkey");

var EHR = {};
exports.EHR = EHR;

EHR.Server = {};
EHR.Server.TriggerManager = {};

/**
 * This class is used to manage the set of trigger event handlers registered with EHR.
 * Other modules can register handlers that will be called at specific points in the validation scripts
 */
EHR.Server.TriggerManager = new function(){
    var events = {};
    var queryEvents = {};
    var properties = {

    };

    // Build a map of study dataset labels to names, in case callers are still using the old approach of registering by label
    var labelsToNames = {};
    var triggerHelper = org.labkey.ehr.utils.TriggerScriptHelper.create(LABKEY.Security.currentUser.id, LABKEY.Security.currentContainer.id);
    var study = triggerHelper.getEHRStudy();
    if (study) {
        var datasets = study.getDatasets();
        for (var i = 0; i < datasets.length; i++) {
            var label = datasets[i].getLabel();
            if (label) {
                labelsToNames[label.toLowerCase()] = datasets[i].getName().toLowerCase();
            }
        }
    }

    return {
        /**
         * Constants representing the hooks available for triggers
         * @type {Object}
         */
        Events: {
            /** equivalent to standard init() function */
            INIT: 'init',
            /** equivalent to standard beforeInsert() function */
            BEFORE_INSERT: 'beforeInsert',
            /** equivalent to standard afterInsert() function */
            AFTER_INSERT: 'afterInsert',
            /** equivalent to standard beforeDelete() function */
            BEFORE_DELETE: 'beforeDelete',
            /** equivalent to standard afterDelete() function */
            AFTER_DELETE: 'afterDelete',
            /** equivalent to standard beforeUpdate() function */
            BEFORE_UPDATE: 'beforeUpdate',
            /** equivalent to standard afterUpdate() function */
            AFTER_UPDATE: 'afterUpdate',
            /** invoked before both insert and update operations */
            BEFORE_UPSERT: 'beforeUpsert',
            /** invoked after both insert and update operations */
            AFTER_UPSERT: 'afterUpsert',
            /** invoked when a record is being saved into the 'Completed' QC state, regardless of if it's an insert or update */
            ON_BECOME_PUBLIC: 'onBecomePublic',
            /** invoked after a record was being saved into the 'Completed' QC state, regardless of if it's an insert or update */
            AFTER_BECOME_PUBLIC: 'afterBecomePublic',
            /** equivalent to standard complete() function */
            COMPLETE: 'complete',
            /** generates a single-string description, returned from the function, to capture the important details of the row */
            DESCRIPTION: 'description'
        },

        /**
         * Can be used to register a handler that will be included on all tables
         * @param event One of the events in EHR.Server.TriggerManager.Events
         * @param handler A function that will be called.  The arguments vary by event
         */
        registerHandler: function(event, handler){
            if (!events[event]){
                events[event] = [];
            }

            events[event].push(handler);
        },

        toLower: function(s) {
            if (s && s.toLowerCase)
                return s.toLowerCase();
            return s;
        },

        /**
         * Can be used to register a handler that will be included on a specific query
         * @param event One of the events in EHR.Server.TriggerManager.Events
         * @param handler A function that will be called.  The arguments vary by event
         */
        registerHandlerForQuery: function(event, schemaName, queryName, handler){
            schemaName = this.toLower(schemaName);
            queryName = this.toLower(queryName);

            if (schemaName === 'study') {
                var realName = labelsToNames[queryName];
                if (realName && realName !== queryName) {
                    console.log('Study dataset registered by label "' + queryName + '" instead of name "' + realName + '". The reference should be updated.')
                    queryName = realName;
                }
            }

            if (!queryEvents[schemaName]){
                queryEvents[schemaName] = {};
            }

            if (!queryEvents[schemaName][queryName]){
                queryEvents[schemaName][queryName] = {};
            }

            if (!queryEvents[schemaName][queryName][event]){
                queryEvents[schemaName][queryName][event] = [];
            }

            queryEvents[schemaName][queryName][event].push(handler);
        },
        /**
         * Unregister EHR registered handlers
         * @param schemaName
         * @param queryName
         * @param event
         */
        unregisterAllHandlersForQueryNameAndEvent: function (schemaName, queryName, event) {
            schemaName = this.toLower(schemaName);
            queryName = this.toLower(queryName);

            if (queryEvents[schemaName] && queryEvents[schemaName][queryName] && queryEvents[schemaName][queryName][event]) {
                queryEvents[schemaName][queryName][event] = [];
            }

        },

        getHandlers: function(event){
            return events[event] || [];
        },

        /**
         * This will return an array of all handlers registers for the requested event for the
         * requested table.
         * @param event One of the events in EHR.Server.TriggerManager.Events
         * @param schemaName
         * @param queryName
         * @param includeAll If true, any handlers registered for all tables will be appended first.  This is usually the desired behavior
         * @return {Array}
         */
        getHandlersForQuery: function(event, schemaName, queryName, includeAll){
            schemaName = this.toLower(schemaName);
            queryName = this.toLower(queryName);

            var handlers = [];

            //optionally append handlers for all tables
            if (includeAll && events[event])
                handlers = handlers.concat(events[event]);

            if (!queryEvents[schemaName])
                return handlers;

            if (!queryEvents[schemaName][queryName])
                return handlers;

            if (!queryEvents[schemaName][queryName][event])
                return handlers;

            handlers = handlers.concat(queryEvents[schemaName][queryName][event]);

            return handlers;
        },

        setProperty: function(name, value){
            properties[name] = value;
        },

        getProperty: function(name){
            return properties[name];
        }
    }
};