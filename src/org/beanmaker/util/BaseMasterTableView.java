package org.beanmaker.util;

import org.dbbeans.util.Strings;
import org.jcodegen.html.ATag;
import org.jcodegen.html.InputTag;
import org.jcodegen.html.OptionTag;
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

    protected final String tableId;

    protected String tableCssClass = "cctable";
    protected String thResetCssClass = null;
    protected String tdResetCssClass = null;
    protected String thFilterCssClass = null;
    protected String tdFilterCssClass = null;
    protected String thTitleCssClass = "tb-sort";

    protected String formElementFilterCssClass = "tb-filter";
    protected String removeFilteringLinkCssClass = "tb-nofilter";
    protected Tag removeFilteringHtmlTags = new SpanTag().cssClass("glyphicon glyphicon-remove").title("Remove Filtering");

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

    protected int zeroFilledMaxDigits = 20;

    protected boolean displayId = false;

    public BaseMasterTableView(final String resourceBundleName, final String tableId) {
        super(resourceBundleName);
        this.tableId = tableId;
    }

    public String getMasterTable() {
        return getTable().child(getHead()).child(getBody()).toString();
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

        for (TrTag tr: getData())
            body.child(tr);

        return body;
    }

    protected abstract List<TrTag> getData();

    protected TrTag getFilterRow() {
        return new TrTag().child(getRemoveFilteringCellWithLink());
    }

    protected TrTag getTitleRow() {
        return new TrTag().child(getRemoveFilteringCell());
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
                new InputTag(InputTag.InputType.TEXT).name("tb-" + name).cssClass(formElementFilterCssClass).attribute("autocomplete", "off")
        );
    }

    protected ThTag getBooleanFilterCell(final String name) {
        return getTableFilterCell().child(
                new SelectTag().name(name).cssClass(formElementFilterCssClass).child(
                        new OptionTag("", "").selected()
                ).child(
                        new OptionTag(yesName, yesValue)
                ).child(
                        new OptionTag(noName, noValue)
                )
        );
    }

    protected ThTag getTitleCell(final String name) {
        return getTitleCell(name, resourceBundle.getString(name));
    }

    protected ThTag getTitleCell(final String name,  final String adhocTitle) {
        return new ThTag(adhocTitle).cssClass(thTitleCssClass).attribute("data-sort-class", "tb-" + name);
    }

    protected TrTag getTableLine() {
        final TrTag line = new TrTag();

        line.child(getTableCellForRemoveFilteringPlaceholder());

        return line;
    }

    protected TdTag getTableCellForRemoveFilteringPlaceholder() {
        final TdTag cell = new TdTag();

        if (tdResetCssClass != null)
            cell.cssClass(tdResetCssClass);

        return cell;
    }

    protected TdTag getTableCell(final String name, final String value) {
        return new TdTag(value).cssClass("tb-" + name);
    }

    protected TdTag getTableCell(final String name, final Date value) {
        if (dateFormat == null)
            dateFormat = DateFormat.getDateInstance();

        return getTableCell(name, dateFormat.format(value)).attribute("data-sort-value", value.toString());
    }

    protected TdTag getTableCell(final String name, final Time value) {
        if (timeFormat == null)
            timeFormat = DateFormat.getTimeInstance();

        return getTableCell(name, timeFormat.format(value)).attribute("data-sort-value", value.toString());
    }

    protected TdTag getTableCell(final String name, final Timestamp value) {
        if (datetimeFormat == null)
            datetimeFormat = DateFormat.getDateTimeInstance();

        return getTableCell(name, datetimeFormat.format(value)).attribute("data-sort-value", value.toString());
    }

    protected TdTag getTableCell(final String name, final boolean value) {
        if (value)
            return getTableBooleanCell(name, yesDisplay, yesValue);

        return getTableBooleanCell(name, noDisplay, noValue);
    }

    protected TdTag getTableBooleanCell(final String name, final String value, final String sortnfilter) {
        return new TdTag(value).cssClass(booleanCenterValueCssClass + " tb-" + name)
                .attribute("data-filter-value", sortnfilter).attribute("data-sort-value", sortnfilter);
    }

    protected TdTag getTableCell(final String name, final long value) {
        return getTableCell(name, Long.toString(value)).attribute("data-sort-value", Strings.zeroFill(value, zeroFilledMaxDigits));
    }
}
