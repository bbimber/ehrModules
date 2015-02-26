package org.labkey.test.tests;

import org.jetbrains.annotations.Nullable;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.labkey.test.Locator;
import org.labkey.test.TestFileUtils;
import org.labkey.test.categories.EHR;
import org.labkey.test.util.DataRegionTable;
import org.labkey.test.util.Ext4Helper;
import org.labkey.test.util.LogMethod;
import org.labkey.test.util.PortalHelper;
import org.labkey.test.util.RReportHelper;

import java.io.File;

/**
 * Created by RyanS on 1/21/2015.
 */
@Category ({EHR.class})
public class SNPRC_EHRTest extends AbstractGenericEHRTest
{
    private boolean _hasCreatedBirthRecords = false;

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
        click(Locator.linkWithText("Mature Female Exposed To Fertile Male"));
        //TODO: need test data with necessary criteria to be returned by this query
        assertTextPresent("No data to show.");
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
        //spot check a few of the data points
        assertTextPresent("Room1 / A1");
        assertTextPresent("There are no active medications");
        assertTextPresent("Rhesus");
        recallLocation();
        click(Locator.xpath("//label[.='Entire Database']/../input"));
        click(Locator.tagWithText("span", "Refresh"));
        //check count and links for one subject
        DataRegionTable tbl = new DataRegionTable("aqwp2", this);
        //DataRegionTable tbl = DataRegionTable.getTableNameByTitle("", this)
        Assert.assertEquals(tbl.getDataRowCount(), 49);
        assertElementPresent(Locator.linkWithText("test1020148"));
        assertElementPresent(Locator.linkWithText("male"));
        assertElementPresent(Locator.linkWithText("Alive"));
    }

}