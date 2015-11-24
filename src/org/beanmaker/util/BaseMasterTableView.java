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

    public BaseMasterTableView(final String resourceBundleName, final String tableId) {
        super(resourceBundleName);
        this.tableId = tableId;
    }

    public String getMasterTable() {
        return getMasterTableTag().toString();
    }

    public TableTag getMasterTableTag() {
        return getTable().child(getHead()).child(getBody());
    }

    protected TableTag getTable() {
        return new TableTag().cssClass(tableCssClass).id(tableId);
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

    @Deprecated
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
        return new TrTag().id(tableId + "_row_" + id);
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
}
