/*
 * Copyright (c) 2015 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.labkey.test.tests;

import org.jetbrains.annotations.Nullable;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.labkey.test.Locator;
import org.labkey.test.TestFileUtils;
import org.labkey.test.categories.CustomModules;
import org.labkey.test.categories.EHR;
import org.labkey.test.util.DataRegionTable;
import org.labkey.test.util.Ext4Helper;
import org.labkey.test.util.LogMethod;
import org.labkey.test.util.PortalHelper;
import org.labkey.test.util.RReportHelper;

import java.io.File;
import java.util.List;

@Category ({CustomModules.class, EHR.class})
public class SNPRC_EHRTest extends AbstractGenericEHRTest
{
    private boolean _hasCreatedBirthRecords = false;
    private static final int POPULATE_TIMEOUT_MS = 300000;

    public String getModuleDirectory()
    {
        return "snprc_ehr";
    }

    @Nullable
    @Override
    protected String getProjectName()
    {
        return "SNPRC_EHRTestProject";
    }

    @Override
    public BrowserType bestBrowser()
    {
        return BrowserType.CHROME;
    }

    @BeforeClass
    @LogMethod
    public static void doSetup() throws Exception
    {
        SNPRC_EHRTest initTest = (SNPRC_EHRTest)getCurrentTest();

        initTest.initProject("SNPRC EHR");
        initTest.createTestSubjects();
        new RReportHelper(initTest).ensureRConfig();
        initTest.goToProjectHome();
        initTest.clickFolder(FOLDER_NAME);
        new PortalHelper(initTest).addWebPart("EHR Front Page");
    }

    @Override
    protected boolean doSetUserPasswords()
    {
        return true;
    }

    protected void importStudy()
    {
        File path = new File(TestFileUtils.getLabKeyRoot(), getModulePath() + "/resources/referenceStudy");
        setPipelineRoot(path.getPath());

        beginAt(getBaseURL() + "/pipeline-status/" + getContainerPath() + "/begin.view");
        clickButton("Process and Import Data", defaultWaitForPage);

        _fileBrowserHelper.expandFileBrowserRootNode();
        _fileBrowserHelper.checkFileBrowserFileCheckbox("study.xml");

        if (isTextPresent("Reload Study"))
            _fileBrowserHelper.selectImportDataAction("Reload Study");
        else
            _fileBrowserHelper.selectImportDataAction("Import Study");

        if (skipStudyImportQueryValidation())
        {
            Locator cb = Locator.checkboxByName("validateQueries");
            waitForElement(cb);
            uncheckCheckbox(cb);
        }

        clickButton("Start Import"); // Validate queries page
        waitForPipelineJobsToComplete(1, "Study import", false, MAX_WAIT_SECONDS * 2500);
    }

    @Override
    protected void populateInitialData()
    {
        beginAt(getBaseURL() + "/" + getModuleDirectory() + "/" + getContainerPath() + "/populateData.view");

        log("Repopulate Lookup Sets");
        clickButton("Delete Data From Lookup Sets", 0);
        waitForElement(Locator.tagContainingText("div", "Delete Complete"), POPULATE_TIMEOUT_MS);
        Assert.assertFalse(elementContains(Locator.id("msgbox"), "ERROR"));

        clickButton("Populate Lookup Sets", 0);
        waitForElement(Locator.tagContainingText("div", "Populate Complete"), POPULATE_TIMEOUT_MS);
        Assert.assertFalse(elementContains(Locator.id("msgbox"), "ERROR"));
        sleep(2000);

        log("Repopulate Procedures");
        clickButton("Delete Data From Procedures", 0);
        waitForElement(Locator.tagContainingText("div", "Delete Complete"), POPULATE_TIMEOUT_MS);
        Assert.assertFalse(elementContains(Locator.id("msgbox"), "ERROR"));

        clickButton("Populate Procedures", 0);
        waitForElement(Locator.tagContainingText("div", "Populate Complete"), POPULATE_TIMEOUT_MS);
        Assert.assertFalse(elementContains(Locator.id("msgbox"), "ERROR"));
        sleep(2000);

        log("Repopulate Everything");
        clickButton("Delete All", 0);
        waitForElement(Locator.tagContainingText("div", "Delete Complete"), POPULATE_TIMEOUT_MS);
        Assert.assertFalse(elementContains(Locator.id("msgbox"), "ERROR"));

        clickButton("Populate All", 0);
        waitForElement(Locator.tagContainingText("div", "Populate Complete"), POPULATE_TIMEOUT_MS);
        Assert.assertFalse(elementContains(Locator.id("msgbox"), "ERROR"));

        log("Repopulate SNOMED Codes");
        //NOTE: this is excluded from populate all since it changes rarely
        clickButton("Delete Data From SNOMED Codes", 0);
        waitForElement(Locator.tagContainingText("div", "Delete Complete"), POPULATE_TIMEOUT_MS);
        Assert.assertFalse(elementContains(Locator.id("msgbox"), "ERROR"));

        clickButton("Populate SNOMED Codes", 0);
        waitForElement(Locator.tagContainingText("div", "Populate Complete"), POPULATE_TIMEOUT_MS);
        Assert.assertFalse(elementContains(Locator.id("msgbox"), "ERROR"));

        //also populate templates
        beginAt(getBaseURL() + "/onprc_ehr/" + getContainerPath() + "/populateTemplates.view");

        log("Repopulate Templates");
        clickButton("Delete Data From Form Templates", 0);
        waitForElement(Locator.tagContainingText("div", "Delete Complete"), POPULATE_TIMEOUT_MS);
        clickButton("Populate Form Templates", 0);
        waitForElement(Locator.tagContainingText("div", "Populate Complete"), POPULATE_TIMEOUT_MS);

        log("Repopulate Formulary");
        clickButton("Delete Data From Formulary", 0);
        waitForElement(Locator.tagContainingText("div", "Delete Complete"), POPULATE_TIMEOUT_MS);
        clickButton("Populate Formulary", 0);
        waitForElement(Locator.tagContainingText("div", "Populate Complete"), POPULATE_TIMEOUT_MS);
    }

    @Test
    public void testAnimalSearch()
    {
        goToProjectHome();
        clickFolder("EHR");
        click(Locator.linkWithText("Animal Search"));
        waitForElement(Locator.inputByNameContaining("Id"));
        //pushLocation();
        saveLocation();
        setFormElement(Locator.inputByNameContaining("Id"), "1");
        click(Ext4Helper.Locators.ext4Button("Submit"));
        waitForElement(Locator.linkWithText("test1020148"));
        recallLocation();
        waitForElement(Locator.inputByNameContaining("Id/curLocation/cage"));
        setFormElement(Locator.inputByNameContaining("Id/curLocation/cage"), "5426");
        click(Ext4Helper.Locators.ext4Button("Submit"));
        assertElementPresent(Locator.linkWithText("test499022"));
        assertElementPresent(Locator.linkWithText("test6390238"));

    }

    @Test
    public void testHousingSearch()
    {
        goToProjectHome();
        clickFolder("EHR");
        click(Locator.linkWithText("Housing Queries"));
        waitForElement(Locator.inputByNameContaining("Id"));
        pushLocation();
        setFormElement(Locator.inputByNameContaining("Id"), "1");
        click(Ext4Helper.Locators.ext4Button("Submit"));
        waitForElement(Locator.linkWithText("test1112911"));
        popLocation();
        waitForElement(Locator.inputByNameContaining("cage"));
        setFormElement(Locator.inputByNameContaining("cage"), "100172");
        click(Ext4Helper.Locators.ext4Button("Submit"));
        waitForElement(Locator.linkWithText("100172"));
    }

    @Test
    public void testProtocolSearch()
    {
      //TODO: add protocol search
    }

    @Test
    public void testCustomQueries()
    {
        goToProjectHome();
        clickFolder("EHR");
        clickAndWait(Locator.linkWithText("Mature Female Exposed To Fertile Male"));
        assertTextPresent("test3844307", "test5598475");
    }

    @Test
    public void testLookups()
    {
        goToProjectHome();
        clickFolder("EHR");
        navigateToQuery("ehr", "animalExposure");

        DataRegionTable query = new DataRegionTable("query", this);
        List<String> row = query.getRowDataAsText(0);

        for (int i = 0; i < row.size(); i++)
        {
            String cell = row.get(i);
            if (cell != null && cell.startsWith("<"))
            {
                List<String> header = query.getColumnHeaders();
                Assert.fail("Broken lookup '" + cell + "' for column '" + header.get(i) + "'");
            }
        }
    }

    @Test
    public void testAnimalHistory()
    {
        goToProjectHome();
        clickFolder("EHR");
        click(Locator.linkWithText("Animal History"));
        saveLocation();
        waitForElement(Locator.inputByNameContaining("textfield"));
        setFormElement(Locator.inputByNameContaining("textfield"), "12345");
        click(Locator.tagWithText("span", "Refresh"));
        waitForText("Overview: 12345");
        waitForTextToDisappear("Loading...");
        //spot check a few of the data points
        assertTextPresent("Room1 / A1", "There are no active medications", "Rhesus");
        recallLocation();
        click(Locator.xpath("//label[.='Entire Database']/../input"));
        click(Locator.tagWithText("span", "Refresh"));
        //check count and links for one subject
        DataRegionTable tbl = new DataRegionTable("aqwp2", this);
        Assert.assertEquals(tbl.getDataRowCount(), 49);
        assertElementPresent(Locator.linkWithText("test1020148"));
        assertElementPresent(Locator.linkWithText("Male"));
        assertElementPresent(Locator.linkWithText("Alive"));
    }

}
