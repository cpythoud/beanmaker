package org.beanmaker.util;

import org.dbbeans.util.Money;
import org.dbbeans.util.Strings;

import org.jcodegen.html.ATag;
import org.jcodegen.html.CData;
import org.jcodegen.html.HtmlCodeFragment;
import org.jcodegen.html.InputTag;
import org.jcodegen.html.OptionTag;
import org.jcodegen.html.PTag;
import org.jcodegen.html.SelectTag;
import org.jcodegen.html.SpanTag;
import org.jcodegen.html.TableTag;
import org.jcodegen.html.Tag;
import org.jcodegen.html.TbodyTag;
import org.jcodegen.html.TdTag;
import org.jcodegen.html.ThTag;
import org.jcodegen.html.TheadTag;
import org.jcodegen.html.TrTag;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

import java.text.DateFormat;

import java.util.List;
import java.util.Map;

public abstract class BaseMasterTableView extends BaseView {

    protected String tableId = "";

    protected String tableCssClass = "cctable";
    protected String thResetCssClass = null;
    protected String tdResetCssClass = null;
    protected String thFilterCssClass = null;
    protected String tdFilterCssClass = null;
    protected String thTitleCssClass = "tb-sort";
    protected String thSuperTitleCssClass = null;

    protected String trFilterCssClass = null;
    protected String trTitleCssClass = null;
    protected String trSuperTitleCssClass = null;

    protected String formElementFilterCssClass = "tb-filter";
    protected String removeFilteringLinkCssClass = "tb-nofilter";
    protected Tag removeFilteringHtmlTags =
            new SpanTag().cssClass("glyphicon glyphicon-remove").title("Remove Filtering");

    protected String yesName = "yes";
    protected String noName = "no";
    protected String yesValue = "A";
    protected String noValue = "Z";
    protected String yesDisplay = "✔";
    protected String noDisplay = "";

    protected DateFormat dateFormat = null;
    protected DateFormat timeFormat = null;
    protected DateFormat datetimeFormat = null;

    protected String booleanCenterValueCssClass = "center";

    protected int zeroFilledMaxDigits = 18;

    protected boolean displayId = false;

    protected int columnCount = 2;
    protected String noDataMessage = "NO DATA";

    protected String summaryTotalLabel = "items in table,";
    protected String summaryShownLabel = "items shown,";
    protected String summaryFilteredOutLabel = "items filtered out.";

    protected boolean showBeanIdInRowId = true;

    protected DbBeanLanguage dbBeanLanguage = null;
    protected boolean languageInfoRequired = false;
    protected boolean displayAllLanguages = true;

    protected String iconLibrary = "glyphicons glyphicons-";
    protected String removeFilteringIcon = "remove-circle";
    protected String editIcon = "edit";
    protected String deleteIcon = "bin";
    protected boolean showEditLinks = false;

    protected String moveUpLabel = "move up";
    protected String moveDownLabel = "move down";
    protected String moveUpIcon = "chevron-up";
    protected String moveDownIcon = "chevron-down";
    protected boolean showOrderingLinks = false;

    protected boolean doDataToggle = false;
    protected boolean showAllData = false;
    protected String showMoreLabel = "show more";
    protected String showLessLabel = "show less";
    protected String showMoreIcon = "eye-plus";
    protected String showLessIcon = "eye-minus";
    protected String showMoreCssClass = "tb-show-more";
    protected String showLessCssClass = "tb-show-less";
    protected String maskableCssClass = "tb-maskable";
    protected String maskingLinkCssClass = "tb-masking-link";
    protected String maskedCssClass = "tb-masked";
    protected String thShowDataToogleCssClass = null;

    public BaseMasterTableView(final String resourceBundleName, final String tableId) {
        super(resourceBundleName);
        this.tableId = tableId;
    }

    public String getMasterTable() {
        if (languageInfoRequired && dbBeanLanguage == null)
            throw new NullPointerException("Language is not defined.");

        return getMasterTableTag().toString();
    }

    protected void setLanguage(DbBeanLanguage dbBeanLanguage, final Map<String, String> labels) {
        this.dbBeanLanguage = dbBeanLanguage;

        removeFilteringHtmlTags =
                new SpanTag()
                        .cssClass(iconLibrary + removeFilteringIcon)
                        .title(labels.get("cct_remove_filtering"));

        yesName = labels.get("yes");
        noName = labels.get("no");

        noDataMessage = labels.get("cct_no_data");

        summaryTotalLabel = labels.get("cct_total");
        summaryShownLabel = labels.get("cct_shown");
        summaryFilteredOutLabel = labels.get("cct_filtered");

        moveUpLabel = labels.get("cct_move_up");
        moveDownLabel = labels.get("cct_move_down");

        showMoreLabel = labels.get("cct_show_more");
        showLessLabel = labels.get("cct_show_less");

        setLocale(dbBeanLanguage.getLocale());
    }

    public void setShowAllData(final boolean showAllData) {
        this.showAllData = showAllData;
    }

    public TableTag getMasterTableTag() {
        return getTable().child(getHead()).child(getBody());
    }

    protected TableTag getTable() {
        final String dataToggleCssClass;
        if (doDataToggle) {
            if (showAllData)
                dataToggleCssClass = " " + showMoreCssClass;
            else
                dataToggleCssClass = " " + showLessCssClass;
        } else {
            dataToggleCssClass = "";
        }

        return new TableTag()
                .cssClass(tableCssClass + dataToggleCssClass)
                .id(tableId);
    }

    protected TheadTag getHead() {
        final TheadTag head = new TheadTag();

        head.child(getFilterRow());
        head.child(getTitleRow());

        return head;
    }

    protected TbodyTag getBody() {
        final TbodyTag body = new TbodyTag();

        int count = 0;
        for (TrTag tr: getData()) {
            body.child(tr);
            ++count;
        }
        if (count == 0)
            body.child(getNoDataAvailableLine());

        return body;
    }

    protected abstract List<TrTag> getData();

    protected abstract long getLineCount();

    protected TrTag getNoDataAvailableLine() {
        return getNoDataAvailableLine(noDataMessage);
    }

    protected TrTag getNoDataAvailableLine(final String message) {
        return getNoDataAvailableLine(message, columnCount);
    }

    protected TrTag getNoDataAvailableLine(final String message, final int columnCount) {
        return new TrTag().child(
                new TdTag().cssClass(tdResetCssClass)
        ).child(
                new TdTag(message).colspan(columnCount)
        );
    }

    protected TrTag getFilterRow() {
        return getDefaultStartOfFilterRow();
    }

    protected TrTag getTitleRow() {
        return getDefaultStartOfTitleRow();
    }

    protected TrTag getDefaultStartOfFilterRow() {
        final TrTag filterRow = new TrTag().child(getRemoveFilteringCellWithLink());

        if (trFilterCssClass != null)
            filterRow.cssClass(trFilterCssClass);

        return filterRow;
    }

    protected TrTag getDefaultStartOfTitleRow() {
        final TrTag titleRow = new TrTag().child(getRemoveFilteringCell());

        if (trTitleCssClass != null)
            titleRow.cssClass(trTitleCssClass);

        return titleRow;
    }

    protected ThTag getRemoveFilteringCellWithLink() {
        return getRemoveFilteringCell().child(
                new ATag().href("#").cssClass(removeFilteringLinkCssClass).child(removeFilteringHtmlTags)
        );
    }

    protected ThTag getRemoveFilteringCell() {
        final ThTag cell = new ThTag();

        if (thResetCssClass != null)
            cell.cssClass(thResetCssClass);

        return cell;
    }

    protected ThTag getTableFilterCell() {
        final ThTag cell = new ThTag();

        if (thFilterCssClass != null)
            cell.cssClass(thFilterCssClass);

        return cell;
    }

    protected ThTag getStringFilterCell(final String name) {
        return getTableFilterCell().child(
                new InputTag(InputTag.InputType.TEXT)
                        .name("tb-" + name)
                        .cssClass(formElementFilterCssClass)
                        .attribute("autocomplete", "off")
        );
    }

    protected ThTag getBooleanFilterCell(final String name) {
        return getTableFilterCell().child(
                new SelectTag().name("tb-" + name).cssClass(formElementFilterCssClass).child(
                        new OptionTag("", "").selected()
                ).child(
                        new OptionTag(yesName, yesValue)
                ).child(
                        new OptionTag(noName, noValue)
                )
        );
    }

    protected ThTag getBasicSelectFilterCell(final String name, final List<String> values) {
        final SelectTag select = new SelectTag().name("tb-" + name).cssClass(formElementFilterCssClass);

        select.child(new OptionTag("", "").selected());
        for (String value: values)
            select.child(new OptionTag(value));

        return getTableFilterCell().child(select);
    }

    protected ThTag getAdvancedSelectFilterCell(final String name, final List<IdNamePair> pairs) {
        final SelectTag select = new SelectTag().name("tb-" + name).cssClass(formElementFilterCssClass);

        select.child(new OptionTag("", "").selected());
        for (IdNamePair pair: pairs)
            select.child(new OptionTag(
                    pair.getName(),
                    Strings.zeroFill(Long.valueOf(pair.getId()), zeroFilledMaxDigits)));

        return getTableFilterCell().child(select);
    }

    protected ThTag getTitleCell(final String name) {
        return getTitleCell(name, resourceBundle.getString(name));
    }

    protected ThTag getTitleCell(final String name,  final String adhocTitle) {
        return new ThTag(adhocTitle)
                .cssClass(getTitleCellCssClasses(name))
                .attribute("data-sort-class", "tb-" + name);
    }

    protected ThTag getTitleCell(final String name, final Tag adhocTitle) {
        return new ThTag()
                .cssClass(getTitleCellCssClasses(name))
                .attribute("data-sort-class", "tb-" + name)
                .child(adhocTitle);
    }

    protected String getTitleCellCssClasses(final String name) {
        return thTitleCssClass + " th-" + name;
    }

    protected TrTag getTableLine() {
        final TrTag line = new TrTag();

        line.child(getTableCellForRemoveFilteringPlaceholder());

        return line;
    }

    protected TrTag getTableLine(final long id) {
        final TrTag line = getTrTag(id);

        line.child(getTableCellForRemoveFilteringPlaceholder());

        return line;
    }

    protected TrTag getTrTag(final long id) {
        if (showBeanIdInRowId)
            return new TrTag().id(tableId + "_row_" + id);

        return new TrTag();
    }

    protected TrTag getTableLine(final String code) {
        final TrTag line = getTrTag(code);

        line.child(getTableCellForRemoveFilteringPlaceholder());

        return line;
    }

    protected TrTag getTrTag(final String code) {
        return new TrTag().id(tableId + "_row_" + code);
    }

    protected TdTag getTableCellForRemoveFilteringPlaceholder() {
        final TdTag cell = new TdTag();

        if (tdResetCssClass != null)
            cell.cssClass(tdResetCssClass);

        return cell;
    }

    protected TdTag getTableCell(final String name, final Tag content) {
        return getTableCell(name, content, null);
    }

    protected TdTag getTableCell(final String name, final Tag content, final String extraCssClasses) {
        return new TdTag().child(content).cssClass(getTableCellCssClasses(name, extraCssClasses));
    }

    protected TdTag getTableCell(final String name, final String value) {
        return getTableCell(name, value, null);
    }

    protected TdTag getTableCell(final String name, final String value, final String extraCssClasses) {
        return new TdTag(value).cssClass(getTableCellCssClasses(name, extraCssClasses));
    }

    private String getTableCellCssClasses(final String name, final String extraCssClasses) {
        return "tb-" + name + (extraCssClasses == null ? "" : " " + extraCssClasses);
    }

    protected TdTag getTableCell(final String name, final Date value) {
        return getTableCell(name, value, null);
    }

    protected TdTag getTableCell(final String name, final Date value, final String extraCssClasses) {
        if (value == null)
            return getTableCell(name, "");

        if (dateFormat == null)
            dateFormat = DateFormat.getDateInstance();

        return getTableCell(name, dateFormat.format(value), extraCssClasses)
                .attribute("data-sort-value", value.toString());
    }

    protected TdTag getTableCell(final String name, final Time value) {
        return getTableCell(name, value, null);
    }

    protected TdTag getTableCell(final String name, final Time value, final String extraCssClasses) {
        if (value == null)
            return getTableCell(name, "");

        if (timeFormat == null)
            timeFormat = DateFormat.getTimeInstance();

        return getTableCell(name, timeFormat.format(value), extraCssClasses)
                .attribute("data-sort-value", value.toString());
    }

    protected TdTag getTableCell(final String name, final Timestamp value) {
        return getTableCell(name, value, null);
    }

    protected TdTag getTableCell(final String name, final Timestamp value, final String extraCssClasses) {
        if (value == null)
            return getTableCell(name, "");

        if (datetimeFormat == null)
            datetimeFormat = DateFormat.getDateTimeInstance();

        return getTableCell(name, datetimeFormat.format(value), extraCssClasses)
                .attribute("data-sort-value", value.toString());
    }

    protected TdTag getTableCell(final String name, final boolean value) {
        return getTableCell(name, value, null);
    }

    protected TdTag getTableCell(final String name, final boolean value, final String extraCssClasses) {
        if (value)
            return getTableBooleanCell(name, yesDisplay, yesValue, extraCssClasses);

        return getTableBooleanCell(name, noDisplay, noValue, extraCssClasses);
    }

    protected TdTag getTableBooleanCell(final String name, final String value, final String sortnfilter) {
        return getTableBooleanCell(name, value, sortnfilter, null);
    }

    protected TdTag getTableBooleanCell(
            final String name,
            final String value,
            final String sortnfilter,
            final String extraCssClasses)
    {
        return decorateBooleanCell(new TdTag(value), name, sortnfilter, extraCssClasses);
    }

    protected TdTag getTableBooleanCell(final String name, final Tag value, final String sortnfilter) {
        return getTableBooleanCell(name, value, sortnfilter, null);
    }

    protected TdTag getTableBooleanCell(
            final String name,
            final Tag value,
            final String sortnfilter,
            final String extraCssClasses)
    {
        return decorateBooleanCell(new TdTag().child(value), name, sortnfilter, extraCssClasses);
    }

    protected TdTag getTableBooleanCell(final String name, final HtmlCodeFragment value, final String sortnfilter) {
        return getTableBooleanCell(name, value, sortnfilter, null);
    }

    protected TdTag getTableBooleanCell(
            final String name,
            final HtmlCodeFragment value,
            final String sortnfilter,
            final String extraCssClasses) {
        return decorateBooleanCell(new TdTag().addCodeFragment(value), name, sortnfilter, extraCssClasses);
    }

    private TdTag decorateBooleanCell(
            final TdTag cell,
            final String name,
            final String sortnfilter,
            final String extraCssClasses)
    {
        return cell
                .cssClass(booleanCenterValueCssClass + " " + getTableCellCssClasses(name, extraCssClasses))
                .attribute("data-filter-value", sortnfilter).attribute("data-sort-value", sortnfilter);
    }

    protected TdTag getTableCell(final String name, final long value) {
        return getTableCell(name, value, null);
    }

    protected TdTag getTableCell(final String name, final long value, final String extraCssClasses) {
        return getTableCell(name, Long.toString(value), extraCssClasses)
                .attribute("data-sort-value", Strings.zeroFill(value, zeroFilledMaxDigits));
    }

    protected TdTag getTableCell(final String name, final Money value) {
        return getTableCell(name, value, null);
    }

    protected TdTag getTableCell(final String name, final Money value, final String extraCssClasses) {
        return getTableCell(name, value.toString(), extraCssClasses)
                .attribute("data-sort-value", Strings.zeroFill(value.getVal(), zeroFilledMaxDigits));
    }

    protected TdTag getTableCell(final String name, final HtmlCodeFragment content) {
        return getTableCell(name, content, null);
    }

    protected TdTag getTableCell(final String name, final HtmlCodeFragment content, final String extraCssClasses) {
        return new TdTag().addCodeFragment(content).cssClass(getTableCellCssClasses(name, extraCssClasses));
    }

    protected TdTag getTableCell(final String name, final IdNamePair pair) {
        return getTableCell(name, pair, null);
    }

    protected TdTag getTableCell(final String name, final IdNamePair pair, final String extraCssClasses) {
        return getTableCell(name, pair.getName(), extraCssClasses)
                .attribute("data-filter-value", Strings.zeroFill(Long.valueOf(pair.getId()), zeroFilledMaxDigits));
    }

    protected TdTag getTableCell(
            final String name,
            final List<IdNamePair> pairs,
            final long idRow,
            final long idSelected,
            final boolean emptyChoice)
    {
        return getTableCell(name, pairs, idRow, idSelected, emptyChoice, null);
    }

    protected TdTag getTableCell(
            final String name,
            final List<IdNamePair> pairs,
            final long idRow,
            final long idSelected,
            final boolean emptyChoice,
            final String extraCssClasses)
    {
        final SelectTag select =
                new SelectTag().id("tb-cell-select-" + name + "_" + idRow).cssClass("tb-cell-select-" + name);

        if (emptyChoice) {
            final OptionTag emptyOption = new OptionTag("", "");
            if (idSelected == 0)
                emptyOption.selected();
            select.child(emptyOption);
        }

        String sortValue = null;
        for (IdNamePair pair: pairs) {
            final OptionTag option =
                    new OptionTag(pair.getName(), Strings.zeroFill(Long.valueOf(pair.getId()), zeroFilledMaxDigits));
            if (pair.getId().equals(Long.toString(idSelected))) {
                option.selected();
                sortValue = pair.getName();
            }
            select.child(option);
        }

        return getTableCell(name, select, extraCssClasses)
                .attribute("data-filter-value", Strings.zeroFill(idSelected, zeroFilledMaxDigits))
                .attribute("data-sort-value", sortValue == null ? "" : sortValue);
    }

    protected TheadTag getThreeLineHead() {
        final TheadTag head = new TheadTag();

        head.child(getFilterRow());
        head.child(getSuperTitleRow());
        head.child(getTitleRow());

        return head;
    }

    protected TrTag getSuperTitleRow() {
        return getDefautStartSuperTitleRow();
    }

    protected TrTag getDefautStartSuperTitleRow() {
        final TrTag row = new TrTag().child(new ThTag().cssClass(thResetCssClass));

        if (trSuperTitleCssClass != null)
            row.cssClass(trSuperTitleCssClass);

        if (displayId)
            row.child(new ThTag());

        return row;
    }

    protected ThTag getMultiColTitle(final String text, final int colspan) {
        final ThTag multiColTitle = new ThTag(text).colspan(colspan);

        if (thSuperTitleCssClass != null)
            multiColTitle.cssClass(thSuperTitleCssClass);

        return multiColTitle;
    }

    public String getSummaryInfo() {
        return getSummaryInfoCode().toString();
    }

    public Tag getSummaryInfoCode() {
        final long count = getLineCount();

        return new PTag().cssClass("cctable-summary")
                .child(getSummarySpan(count, "_total"))
                .child(new CData(summaryTotalLabel))
                .child(getSummarySpan(count, "_shown"))
                .child(new CData(summaryShownLabel))
                .child(getSummarySpan(0, "_filtered_out"))
                .child(new CData(summaryFilteredOutLabel));
    }

    protected SpanTag getSummarySpan(final long count, final String idPostfix) {
        return new SpanTag(Long.toString(count))
                .id(tableId + idPostfix);
    }

    protected ATag getEditLineLink(final long id, final String idPrefix, final String cssClass, final String tooltip) {
        return getOperationLink(id, idPrefix, cssClass, editIcon, tooltip);
    }

    protected ATag getDeleteLineLink(final long id, final String idPrefix, final String cssClass, final String tooltip) {
        return getOperationLink(id, idPrefix, cssClass, deleteIcon, tooltip);
    }

    protected ATag getMoveUpLink(final long id, final String idPrefix, final String cssClass) {
        return getOperationLink(id, idPrefix, cssClass, moveUpIcon, moveUpLabel);
    }

    protected ATag getMoveDownLink(final long id, final String idPrefix, final String cssClass) {
        return getOperationLink(id, idPrefix, cssClass, moveDownIcon, moveDownLabel);
    }

    protected ATag getOperationLink(
            final long id,
            final String idPrefix,
            final String cssClass,
            final String icon,
            final String tooltip)
    {
        return getOperFileLink(id, idPrefix, cssClass, iconLibrary + icon, tooltip);
    }

    private ATag getOperFileLink(
            final long id,
            final String idPrefix,
            final String cssClass,
            final String icon,
            final String tooltip)
    {
        return new ATag()
                .id(idPrefix + "_" + id)
                .cssClass("tb-operation " + cssClass)
                .child(
                        new SpanTag()
                                .cssClass(icon)
                                .title(tooltip));
    }

    protected TdTag getOperationCell(
            final DbBeanWithItemOrderInterface bean,
            final String beanName,
            final String editTooltip)
    {
        final TdTag cell = new TdTag();

        if (showEditLinks)
            cell.child(getEditLineLink(
                    bean.getId(),
                    beanName,
                    "edit_" + beanName,
                    editTooltip));

        if (showOrderingLinks) {
            if (!bean.isFirstItemOrder())
                cell.child(getMoveUpLink(
                        bean.getId(),
                        beanName + "Up",
                        "move_up_" + beanName));

            if (!bean.isLastItemOrder())
                cell.child(getMoveDownLink(
                        bean.getId(),
                        beanName + "Down",
                        "move_down_" + beanName));
        }

        return cell;
    }

    protected TdTag getEditCell(
            final DbBeanInterface bean,
            final String beanName,
            final String tooltip)
    {
        return new TdTag()
                .cssClass(tdResetCssClass)
                .child(getEditLineLink(
                        bean.getId(),
                        beanName,
                        "edit_" + beanName,
                        tooltip));
    }

    protected TdTag getDeleteCell(
            final DbBeanInterface bean,
            final String beanName,
            final String tooltip)
    {
        return new TdTag()
                .cssClass(tdResetCssClass)
                .child(getDeleteLineLink(
                        bean.getId(),
                        beanName + "Del",
                        "delete_" + beanName,
                        tooltip));
    }

    protected ThTag showMoreLessCell() {
        final ThTag cell = new ThTag().child(showMoreLink()).child(showLessLink());
        if (thShowDataToogleCssClass != null)
            cell.cssClass(thShowDataToogleCssClass);

        return cell;
    }

    protected ATag showMoreLink() {
        final SpanTag icon =
                new SpanTag()
                        .cssClass(iconLibrary + showMoreIcon)
                        .title(showMoreLabel);

        final ATag link = new ATag().href("#").id(tableId + "-masking-link-show").child(icon);
        if (showAllData)
            link.cssClass(maskingLinkCssClass + " " + maskedCssClass);
        else
            link.cssClass(maskingLinkCssClass);

        return link;
    }

    protected ATag showLessLink() {
        final SpanTag icon =
                new SpanTag()
                        .cssClass(iconLibrary + showLessIcon)
                        .title(showLessLabel);

        final ATag link = new ATag().href("#").id(tableId + "-masking-link-hide").child(icon);
        if (showAllData)
            link.cssClass(maskingLinkCssClass);
        else
            link.cssClass(maskingLinkCssClass + " " + maskedCssClass);

        return link;
    }

    protected ThTag getMaskableHeader(final ThTag header) {
        return header.changeCssClasses(addShowMoreOrLessCssClasses(header));
    }

    protected TdTag getMaskableCell(final TdTag cell) {
        return cell.changeCssClasses(addShowMoreOrLessCssClasses(cell));
    }

    private String addShowMoreOrLessCssClasses(final Tag cell) {
        final StringBuilder cssClasses = new StringBuilder();

        cssClasses.append(cell.getCssClasses());
        if (cssClasses.length() > 0)
            cssClasses.append(" ");

        cssClasses.append(maskableCssClass);

        return cssClasses.toString();
    }
}
