/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sparrow.mvc.ui;

import com.sparrow.cg.MethodAccessor;
import com.sparrow.constant.Config;
import com.sparrow.constant.Pager;
import com.sparrow.core.spi.ApplicationContext;
import com.sparrow.mvc.ui.grid.CellAttribute;
import com.sparrow.mvc.ui.grid.Head;
import com.sparrow.protocol.POJO;
import com.sparrow.protocol.constant.magic.Digit;
import com.sparrow.support.pager.SparrowPagerResult;
import com.sparrow.support.web.HttpContext;
import com.sparrow.utility.ConfigUtility;
import com.sparrow.utility.StringUtility;
import java.util.List;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 数据网格控件
 */
@SuppressWarnings({"serial", "unchecked"})
public class GridView extends WebControl {
    private static Logger logger = LoggerFactory.getLogger(GridView.class);
    private String emptyString = "没有符合条件的记录。";
    /**
     * tab缩进数
     */
    private int indent = Digit.THREE;
    /**
     * title1,2(colSpan)|title2|...
     */
    private String headTitles;
    /**
     * 表头HTML
     */
    private String headTitleHtml;
    /**
     * 字段信息..
     */
    private String fields;
    /**
     * 是否显示页码
     */
    private boolean showPage = true;
    /**
     * 是否显示列头
     */
    private boolean showHead = true;
    /**
     * 是否显示空行
     */
    private boolean showEmptyRow;

    /**
     * 分页链接字符格式
     */
    private String pageFormat = Pager.ACTION_PAGE_FORMAT;
    /**
     * 是否使用鼠标悬停事件
     */
    private boolean useMouseEvent = true;
    /**
     * 是否使用隔行不同样式
     */
    private boolean useAlternatingRowStyle = true;

    @Override
    public int doStartTag() throws JspException {
        if (Boolean.FALSE.toString().equalsIgnoreCase(this.getVisible())) {
            return TagSupport.SKIP_BODY;
        }
        SparrowPagerResult pagerResult = (SparrowPagerResult) super.pageContext.getRequest().getAttribute(this.getId());
        List<POJO> datasource = pagerResult.getList();
        if (datasource == null) {
            datasource = (List<POJO>) HttpContext.getContext().get(this.getId());
        }
        StringBuilder writeHTML = new StringBuilder();
        this.drawTable(writeHTML);

        if (this.isShowHead()) {
            writeHTML.append("<thead>");
            writeHTML.append(Head.parse(this.getHeadTitles(), this.getId(), this.indent()));
            writeHTML.append("</thead>");
        }
        writeHTML.append("<tbody>");
        try {
            if (datasource != null) {
                String indent1 = StringUtility.getIndent(this
                    .indent() + Digit.ONE);
                String indent2 = StringUtility.getIndent(this
                    .indent() + Digit.TOW);
                List<CellAttribute> cellList = CellAttribute.parse(this.getFields());

                if (datasource.size() > Digit.ZERO) {
                    MethodAccessor methodAccessor = ApplicationContext.getContainer().getProxyBean(datasource.get(Digit.ZERO).getClass());
                    for (int recordIndex = Digit.ZERO; recordIndex < datasource.size(); recordIndex++) {
                        POJO entity = datasource.get(recordIndex);
                        String alternating = this
                            .isUseAlternatingRowStyle() && (recordIndex % Digit.TOW == Digit.ZERO) ? " class='pure-table-odd'"
                            : "";
                        writeHTML.append(indent1);
                        writeHTML.append(String.format("<tr%s>", alternating));
                        for (CellAttribute cell : cellList) {
                            writeHTML.append(cell.toString(entity, indent2, methodAccessor));
                        }
                        writeHTML.append(indent1);
                        writeHTML.append("</tr>");
                    }
                }
                if (datasource.size() == Digit.ZERO && this.isShowEmptyRow()) {
                    writeHTML.append("<tr><td>");
                    writeHTML.append(this.getEmptyString());
                    writeHTML.append("<td></tr>");
                }
            }
            writeHTML.append(StringUtility.getIndent(this.indent()));
            writeHTML.append("</tbody></table>");
            if (this.isShowPage()) {
                SparrowPagerResult result = SparrowPagerResult.page(pagerResult.getPageSize(), this.getCurrentPageIndex(), pagerResult.getRecordCount());
                result.setPageFormat(this.pageFormat);
                result.setIndexPageFormat(this.pageFormat);
                result.setSimple(false);
                writeHTML.append(result.getHtml());
            }
            JspWriter out = this.pageContext.getOut();
            out.print(writeHTML.toString());
        } catch (Exception e1) {
            logger.error("may be contains '$' please replace '$' with 'Sparrow'!", e1);
        }
        return TagSupport.SKIP_BODY;
    }

    public String getEmptyString() {
        return emptyString;
    }

    public void setEmptyString(String emptyString) {
        this.emptyString = emptyString;
    }

    public String getHeadTitles() {
        Object requestHeadTitles = this.pageContext.getRequest().getAttribute(
            this.getId() + ".headTitles");
        if (requestHeadTitles != null) {
            this.headTitles = requestHeadTitles.toString();
        } else if (this.headTitles == null) {
            logger.warn("请定义headTitles属性");
        }
        return this.headTitles;
    }

    public void setHeadTitles(String headTitles) {
        this.headTitles = headTitles;
    }

    public String getFields() {
        Object requestFields = this.pageContext.getRequest().getAttribute(
            this.getId() + ".fields");
        if (requestFields != null) {
            this.fields = requestFields.toString();
        }
        return this.fields;
    }

    public void setFields(String fields) {
        this.fields = fields;
    }

    /**
     * 获取当前页码
     *
     * @return
     */
    public int getCurrentPageIndex() {
        int pageIndex = Digit.ONE;
        Object currentPageIndex = this.pageContext.getRequest().getParameter(
            "currentPageIndex");
        if (!StringUtility.isNullOrEmpty(currentPageIndex)) {
            pageIndex = Integer.parseInt(currentPageIndex.toString().trim());
        }
        return pageIndex;
    }

    public Boolean isShowPage() {
        return showPage;
    }

    public void setShowPage(Boolean showPage) {
        this.showPage = showPage;
    }

    //必须保留该方法 set get 方法必须成对出现
    //否则报错Unable to find setter method for attribute: indent
    public int getIndent() {
        return this.indent;
    }

    public int indent() {
        return this.indent;
    }

    public void setIndent(int indent) {
        this.indent = indent;
    }

    public String getPageFormat() {
        String key = this.getId() + ".pageFormat";
        Object requestPageFormat = this.pageContext.getRequest().getAttribute(
            key);
        if (requestPageFormat == null) {
            requestPageFormat = HttpContext.getContext().get(key);
        }
        if (requestPageFormat != null) {
            this.pageFormat = requestPageFormat.toString();
        } else if (!StringUtility.isNullOrEmpty(this.pageFormat)) {
            this.pageFormat = this.pageFormat.replace("$root_path",
                ConfigUtility.getValue(Config.ROOT_PATH));
        }
        return this.pageFormat;
    }

    public void setPageFormat(String pageFormat) {
        this.pageFormat = pageFormat;
    }

    public boolean isUseMouseEvent() {
        return useMouseEvent;
    }

    public void setUseMouseEvent(boolean useMouseEvent) {
        this.useMouseEvent = useMouseEvent;
    }

    public boolean isUseAlternatingRowStyle() {
        return useAlternatingRowStyle;
    }

    public void setUseAlternatingRowStyle(boolean useAlternatingRowStyle) {
        this.useAlternatingRowStyle = useAlternatingRowStyle;
    }

    public boolean isShowHead() {
        return showHead;
    }

    public void setShowHead(boolean showHead) {
        this.showHead = showHead;
    }

    public boolean isShowEmptyRow() {
        return showEmptyRow;
    }

    public void setShowEmptyRow(boolean showEmptyRow) {
        this.showEmptyRow = showEmptyRow;
    }

    public String getHeadTitleHtml() {
        return headTitleHtml;
    }

    public void setHeadTitleHtml(String headTitleHtml) {
        this.headTitleHtml = headTitleHtml;
    }
}