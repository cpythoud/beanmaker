package org.beanmaker.util;

import org.dbbeans.util.MimeTypes;
import org.dbbeans.util.Pair;
import org.dbbeans.util.Strings;

import javax.lang.model.element.NestingKind;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.math.BigDecimal;
import java.util.Enumeration;
import java.util.List;

public abstract class BeanMakerBaseServlet extends HttpServlet {

    @Override
    public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    abstract protected void processRequest(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException;

    protected String getRootPath() {
        return getServletContext().getRealPath("/");
    }

    protected String getErrorsInJson(final List<ErrorMessage> errorMessages) {
        final StringBuilder buf = new StringBuilder();

        buf.append("{ \"ok\": false, \"errors\": [ ");

        for (ErrorMessage errorMessage: errorMessages)
            buf.append(errorMessage.toJson()).append(", ");

        buf.delete(buf.length() - 2, buf.length());
        buf.append(" ] }");

        return buf.toString();
    }

    protected long getBeanId(final HttpServletRequest request, final String parameterName) {
        return Strings.getLongVal(request.getParameter(parameterName));
    }

    protected String getJsonSimpleStatus(final String status) {
        return "{ \"status\": \"" + status + "\" }";
    }

    protected String getJsonOk() {
        return getJsonSimpleStatus("ok");
    }

    protected String getJsonNoSession() {
        return getJsonSimpleStatus("no session");
    }

    protected String getStartJsonErrors() {
        return "{ \"status\": \"errors\", ";
    }

    protected Pair<String, Long> getSubmittedFormAndId(final HttpServletRequest request) throws ServletException {
        String form = null;
        long id = 0;

        int count = 0;
        final Enumeration<String> params = request.getParameterNames();
        while (params.hasMoreElements()) {
            final String param = params.nextElement();
            if (param.startsWith("submitted")) {
                ++count;
                form = param.substring(9, param.length());
                id = Strings.getLongVal(request.getParameter(param));
            }
        }

        if (count > 1)
            throw new ServletException("More than one submittedXXX parameter.");

        return new Pair<String, Long>(form, id);
    }

    protected String processBean(final HttpServletRequest request, final DbBeanHTMLViewInterface htmlView) {
        htmlView.setAllFields(request);

        if (htmlView.isDataOK()) {
            htmlView.updateDB();
            return getJsonOk();
        }

        return getStartJsonErrors() + ErrorMessage.toJson(htmlView.getErrorMessages()) + " }";
    }

    protected Pair<String, Long> getBeanAndId(final HttpServletRequest request) throws ServletException {
        final String beanName = getBeanName(request);

        final long id = getBeanId(request, "id");
        if (id == 0)
            throw new ServletException("Missing id parameter or id == 0");

        return new Pair<String, Long>(beanName, id);
    }

    protected Pair<String, String> getBeanAndCode(final HttpServletRequest request) throws ServletException {
        final String beanName = getBeanName(request);

        final String code = request.getParameter("id");
        if (code == null)
            throw new ServletException("Missing id parameter");

        return new Pair<String, String>(beanName, code);
    }

    private String getBeanName(final HttpServletRequest request) throws ServletException {
        final String beanName = request.getParameter("bean");
        if (beanName == null)
            throw new ServletException("Missing bean parameter.");

        return beanName;
    }

    protected String deleteBean(final DbBeanInterface bean) {
        bean.delete();
        return getJsonOk();
    }

    protected void disableCaching(final HttpServletResponse response) {
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
    }

    protected int getItemOrderDirectionChangeParameter(HttpServletRequest request) throws ServletException {
        final String direction = request.getParameter("direction");

        if (direction == null)
            throw new ServletException("Missing direction parameter");

        return Strings.getIntVal(direction);
    }

    protected String singleStepItemOrderChangeFor(final DbBeanWithItemOrderInterface bean, final int direction) {
        if (direction == 0)
            throw new IllegalArgumentException(
                    "Illegal direction parameter value: must be a non zero positive or negative integer.");

        if (direction > 0)
            bean.itemOrderMoveUp();
        else
            bean.itemOrderMoveDown();

        return getJsonOk();
    }

    protected String getErrorMessageContainerHtml(final String idContainer) {
        return getErrorMessageContainerHtml(idContainer, "error-message-container");
    }

    protected String getErrorMessageContainerHtml(final String idContainer, final String cssClass) {
        return "<div id='" + idContainer + "' class='" + cssClass + "'></div>";
    }

    protected ChangeOrderDirection getChangeOrderDirection(final HttpServletRequest request) throws ServletException {
        return getChangeOrderDirection(request, "direction");
    }

    protected ChangeOrderDirection getChangeOrderDirection(
            final HttpServletRequest request,
            final String parameterName
    ) throws ServletException
    {
        final String direction = request.getParameter(parameterName);
        if (direction == null)
            throw new ServletException("Missing direction parameter");

        return ChangeOrderDirection.valueOf(direction.toUpperCase());
    }

    protected <B extends DbBeanWithItemOrderInterface<B>> String changeOrder(
            final B bean,
            final ChangeOrderDirection direction,
            final B companion)
    {
        switch (direction) {
            case UP:
                bean.itemOrderMoveUp();
                break;
            case DOWN:
                bean.itemOrderMoveDown();
                break;
            case AFTER:
                bean.itemOrderMoveAfter(companion);
                break;
            case BEFORE:
                bean.itemOrderMoveBefore(companion);
                break;
            default:
                throw new AssertionError("New/unchecked Direction ?");
        }

        return getJsonOk();
    }

    protected void changeLocalOrder(
            final long itemOrder,
            final ChangeOrderDirection direction,
            final long companionItemOrder,
            final TableLocalOrderContext context,
            final String orderingTable)
    {
        switch (direction) {
            case UP:
                TableLocalOrderUtil.itemOrderMoveUp(itemOrder, context, orderingTable);
                break;
            case DOWN:
                TableLocalOrderUtil.itemOrderMoveDown(itemOrder, context, orderingTable);
                break;
            case AFTER:
                TableLocalOrderUtil.itemOrderMoveAfter(itemOrder, companionItemOrder, context, orderingTable);
                break;
            case BEFORE:
                TableLocalOrderUtil.itemOrderMoveBefore(itemOrder, companionItemOrder, context, orderingTable);
                break;
            default:
                throw new AssertionError("New/unchecked Direction ?");
        }
    }

    protected void writeFileToServletOutputStream(
            final DbBeanFile dbBeanFile,
            final HttpServletResponse response
    ) throws IOException
    {
        writeFileToServletOutputStream(dbBeanFile.getFile(), dbBeanFile.getInternalFilename(), response);
    }

    protected void writeFileToServletOutputStream(final File file, final HttpServletResponse response) throws IOException {
        writeFileToServletOutputStream(file, file.getName(), response);
    }

    protected void writeFileToServletOutputStream(
            final File file,
            final String filename,
            final HttpServletResponse response
    ) throws IOException
    {
        response.setContentType(MimeTypes.getType(filename));
        response.setContentLength((int) file.length());
        response.setHeader(
                "Content-Disposition",
                String.format("attachment; filename=\"%s\"", filename));

        final FileInputStream inputStream = new FileInputStream(file);
        final OutputStream outputStream = response.getOutputStream();

        byte[] buffer = new byte[4096];
        int bytesRead;

        while ((bytesRead = inputStream.read(buffer)) != -1)
            outputStream.write(buffer, 0, bytesRead);

        inputStream.close();
        outputStream.close();
    }

    protected <B extends DbBeanInterface> B getBean(
            final B newBean,
            final HttpServletRequest request,
            final String idParameterName
    ) throws ServletException
    {
        final String idStr = request.getParameter(idParameterName);
        if (idStr == null)
            throw new ServletException("Missing parameter: " + idParameterName);
        final long id = Strings.getLongVal(idStr);
        if (id == 0)
            throw new ServletException("Invalid parameter: " + idParameterName + " = " + idStr);

        newBean.setId(id);
        return newBean;
    }
}
