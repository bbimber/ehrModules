/*
 * Copyright (c) 2016 LabKey Corporation
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
package org.labkey.test.pages.ehr;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualTreeBidiMap;
import org.apache.commons.lang3.StringUtils;
import org.labkey.test.Locator;
import org.labkey.test.WebDriverWrapper;
import org.labkey.test.WebTestHelper;
import org.labkey.test.pages.LabKeyPage;
import org.labkey.test.util.DataRegionTable;
import org.labkey.test.util.LogMethod;
import org.labkey.test.util.LoggedParam;
import org.labkey.test.util.Maps;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ParticipantViewPage<EC extends ParticipantViewPage.ElementCache> extends LabKeyPage<EC>
{
    public static final String REPORT_TAB_SIGNAL = "LDK_reportTabLoaded";
    public static final String REPORT_PANEL_SIGNAL = "LDK_reportPanelLoaded";

    public ParticipantViewPage(WebDriver driver)
    {
        super(driver);
    }

    public static ParticipantViewPage beginAt(WebDriverWrapper driver, String participantId)
    {
        return beginAt(driver, driver.getCurrentContainerPath(), participantId);
    }

    public static ParticipantViewPage beginAt(WebDriverWrapper driver, String containerPath, String participantId)
    {
        driver.beginAt(WebTestHelper.buildURL("ehr", containerPath, "participantView", Maps.of("participantId", participantId)));
        return new ParticipantViewPage(driver.getDriver());
    }

    @Override
    protected void waitForPage()
    {
        waitForElement(org.labkey.test.Locators.pageSignal(REPORT_PANEL_SIGNAL));
    }

    @LogMethod(quiet = true)
    public ParticipantViewPage clickCategoryTab(@LoggedParam String categoryLabel)
    {
        elementCache().findCategoryTab(categoryLabel).select();
        return this;
    }

    @LogMethod(quiet = true)
    public ParticipantViewPage clickReportTab(@LoggedParam String reportLabel)
    {
        elementCache().findReportTab(reportLabel).select();
        return this;
    }

    public DataRegionTable getActiveReportDataRegion()
    {
        WebElement el = DataRegionTable.Locators.dataRegion().notHidden().waitForElement(getDriver(), 30000);
        return new DataRegionTable(el, getDriver());
    }

    public List<DataRegionTable> getActiveReportDataRegions()
    {
        final List<WebElement> els = DataRegionTable.Locators.dataRegion().notHidden().waitForElements(getDriver(), 30000);
        List<DataRegionTable> drts = new ArrayList<>();
        els.forEach(el -> drts.add(new DataRegionTable(el, getDriver())));
        return drts;
    }

    @LogMethod(quiet = true)
    public CategoryTab findCategoryTab(String category)
    {
        return elementCache().findCategoryTab(category);
    }

    @LogMethod(quiet = true)
    public Map<Integer, CategoryTab> findCategoryTabs()
    {
        return elementCache().findCategoryTabs();
    }

    @LogMethod(quiet = true)
    public ReportTab findReportTab(String reportLabel)
    {
        return elementCache().findReportTab(reportLabel);
    }

    @LogMethod(quiet = true)
    public Map<Integer, ReportTab> getReportTabsForSelectedCategory()
    {
        return elementCache().getReportTabsForSelectedCategory();
    }

    @Override
    protected EC newElementCache()
    {
        return (EC) new ElementCache();
    }

    protected class ElementCache extends LabKeyPage.ElementCache
    {
        protected final BidiMap<String, Integer> categoryLabels = new DualTreeBidiMap<>();
        protected final Map<Integer, CategoryTab> categoryTabs = new TreeMap<>();
        protected final Map<String, BidiMap<String, Integer>> reportLabels = new TreeMap<>();
        protected final Map<String, Map<Integer, ReportTab>> reportTabs = new TreeMap<>();
        protected CategoryTab selectedCategory;

        public CategoryTab findCategoryTab(String category)
        {
            if (!categoryLabels.containsKey(category))
            {
                WebElement tabEl = Locators.categoryTab.withText(category).findElement(this);
                CategoryTab tab = new CategoryTab(tabEl, category);
                categoryLabels.put(category, tab.getIndex());
                categoryTabs.put(tab.getIndex(), tab);
            }
            return findCategoryTabs().get(categoryLabels.get(category));
        }

        public ReportTab findReportTab(String reportLabel)
        {
            String selectedCategory = findSelectedCategory().getLabel();
            reportLabels.putIfAbsent(selectedCategory, new DualTreeBidiMap<>());

            if (!reportLabels.get(selectedCategory).containsKey(reportLabel))
            {
                reportTabs.put(selectedCategory, new TreeMap<>());

                WebElement tabEl = Locators.reportTab.withText(reportLabel).findElement(this);
                ReportTab tab = new ReportTab(tabEl, reportLabel);
                reportLabels.get(selectedCategory).put(reportLabel, tab.getIndex());
                reportTabs.get(selectedCategory).put(tab.getIndex(), tab);
            }
            return getReportTabsForSelectedCategory().get(reportLabels.get(selectedCategory).get(reportLabel));
        }

        // null for category tabs, else found all report tabs for each listed category
        private final List<String> foundAllTabs = new ArrayList<>();
        public Map<Integer, CategoryTab> findCategoryTabs()
        {
            if (!foundAllTabs.contains(null))
            {
                foundAllTabs.add(null);
                List<WebElement> tabs = Locators.categoryTab.findElements(this);
                for (int i = 0; i < tabs.size(); i++)
                {
                    categoryTabs.putIfAbsent(i, new CategoryTab(tabs.get(i), i));
                }
            }
            return categoryTabs;
        }

        public Map<Integer, ReportTab> getReportTabsForSelectedCategory()
        {
            String selectedCategory = findSelectedCategory().getLabel();

            if (!foundAllTabs.contains(selectedCategory))
            {
                foundAllTabs.add(selectedCategory);
                reportTabs.putIfAbsent(selectedCategory, new TreeMap<>());
                reportLabels.putIfAbsent(selectedCategory, new DualTreeBidiMap<>());
                List<WebElement> tabs = Locators.reportTab.findElements(this);

                for (int i = 0; i < tabs.size(); i++)
                {
                    reportTabs.get(selectedCategory).putIfAbsent(i, new ReportTab(tabs.get(i), i));
                }
            }
            return reportTabs.get(selectedCategory);
        }

        CategoryTab findSelectedCategory()
        {
            if (selectedCategory != null && selectedCategory.isActive())
                return selectedCategory;

            for (CategoryTab categoryTab : findCategoryTabs().values())
            {
                if (categoryTab.isActive())
                {
                    selectedCategory = categoryTab;
                    return categoryTab;
                }
            }
            return null;
        }
    }

    public abstract class Tab
    {
        WebElement _el;
        String _label;
        Integer _index;

        public Tab(WebElement el)
        {
            _el = el;
        }

        public Tab(WebElement el, String label)
        {
            this(el);
            _label = label;
        }

        public Tab(WebElement el, Integer index)
        {
            this(el);
            _index = index;
        }

        public String getLabel()
        {
            if (_label == null)
                _label = _el.getText();
            return _label;
        }

        public Integer getIndex()
        {
            if (_index == null)
                _index = Locator.xpath("preceding-sibling::a").findElements(_el).size();
            return _index;
        }

        public boolean isActive()
        {
            return _el.getAttribute("class").contains("x4-tab-active");
        }

        @Override
        public String toString()
        {
            return getLabel();
        }

        public abstract void select();
    }

    public class CategoryTab extends Tab
    {
        public CategoryTab(WebElement el)
        {
            super(el);
        }

        public CategoryTab(WebElement el, String label)
        {
            super(el, label);
        }

        public CategoryTab(WebElement el, Integer index)
        {
            super(el, index);
        }

        @Override
        public void select()
        {
            if (!StringUtils.trimToEmpty(_el.getAttribute("class")).contains("active"))
            {
                WebElement activeReportPanel = Locators.activeReportPanel.findElement(getDriver());
                scrollIntoView(_el);
                _el.click();
                shortWait().until(ExpectedConditions.invisibilityOfAllElements(Collections.singletonList(activeReportPanel)));
                elementCache().selectedCategory = this;
                Locators.activeReportPanel.waitForElement(getDriver(), 1000);
            }
        }
    }

    public class ReportTab extends Tab
    {
        public ReportTab(WebElement el)
        {
            super(el);
        }

        public ReportTab(WebElement el, String label)
        {
            super(el, label);
        }

        public ReportTab(WebElement el, Integer index)
        {
            super(el, index);
        }

        @Override
        public void select()
        {
            if (!StringUtils.trimToEmpty(_el.getAttribute("class")).contains("active"))
            {
                scrollIntoView(_el);
                try
                {
                    doAndWaitForPageSignal(_el::click, REPORT_TAB_SIGNAL, longWait());
                }
                catch (StaleElementReferenceException ignore) // Tab signal might fire more than once
                {
                    _el.isDisplayed(); // Make sure it was actually the signal that was stale
                }
                _ext4Helper.waitForMaskToDisappear(30000);
            }
        }
    }

    static class Locators extends LabKeyPage.Locators
    {
        static final Locator.XPathLocator categoryTab = Locator.tagWithClass("div", "category-tab-bar").append(Locator.tagWithClass("a", "x4-tab"));
        static final Locator.XPathLocator activeReportPanel = Locator.tagWithClass("div", "x4-tabpanel-child").withAttributeContaining("id", "tabpanel").notHidden();
        static final Locator.XPathLocator reportTab = activeReportPanel.append(Locator.tagWithClass("div", "report-tab-bar")).append(Locator.tagWithClass("a", "x4-tab"));
    }
}