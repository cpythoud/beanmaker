package org.beanmaker.util;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import org.dbbeans.util.Pair;
import org.dbbeans.util.Strings;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import java.io.File;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpRequestParameters {

    private final Map<String, String> parameters = new HashMap<String, String>();
    private final Map<String, List<String>> multiValueParameters = new HashMap<String, List<String>>();
    private final Map<String, FileItem> files = new HashMap<String, FileItem>();

    private final HttpServletRequest request;
    private final boolean multipartRequest;

    public HttpRequestParameters(final HttpServletRequest request) {
        this.request = request;
        multipartRequest = ServletFileUpload.isMultipartContent(request);

        if (multipartRequest) {
            for (FileItem item: parseRequest(request)) {
                if (item.isFormField())
                    recordParameter(item);
                else
                    recordFile(item);
            }
        } else {
            for (Map.Entry<String, String[]> entry: request.getParameterMap().entrySet()) {
                final String[] vals = entry.getValue();
                if (vals.length == 1)
                    parameters.put(entry.getKey(), vals[0]);
                else
                    multiValueParameters.put(entry.getKey(), Arrays.asList(vals));
            }
        }

        for (Map.Entry<String, String> entry: parameters.entrySet())
            System.out.println(entry.getKey() + " = " + entry.getValue());
        for (Map.Entry<String, FileItem> entry: files.entrySet())
            System.out.println(entry.getKey() + " = " + entry.getValue().getName());
    }

    private List<FileItem> parseRequest(final HttpServletRequest request) {
        final DiskFileItemFactory factory = new DiskFileItemFactory();

        final File repository = (File) request.getServletContext().getAttribute("javax.servlet.context.tempdir");
        factory.setRepository(repository);

        final ServletFileUpload upload = new ServletFileUpload(factory);

        final List<FileItem> items;
        try {
            items = upload.parseRequest(request);
        } catch (final FileUploadException fuex) {
            throw new RuntimeException(fuex);
        }

        return items;
    }

    private void recordParameter(final FileItem item) {
        final String value = item.getString();
        if (Strings.isEmpty(value))
            return;

        final String name  = item.getFieldName();

        if (multiValueParameters.containsKey(name)) {
            multiValueParameters.get(name).add(value);
            return;
        }

        if (parameters.containsKey(name)) {
            final List<String> values = new ArrayList<String>();
            values.add(parameters.get(name));
            values.add(value);

            parameters.remove(name);
            multiValueParameters.put(name, values);

            return;
        }

        parameters.put(name, value);
    }

    private void recordFile(final FileItem item) {
        if (Strings.isEmpty(item.getName()))
            return;

        files.put(item.getFieldName(), item);
    }

    public boolean isMultipartRequest() {
        return multipartRequest;
    }

    public HttpServletRequest getRequest() {
        if (multipartRequest)
            throw new IllegalArgumentException("Cannot return multipart request. Request is consumed when processed");

        return request;
    }

    public HttpSession getSession() {
        return request.getSession();
    }

    public boolean hasParameter(final String name) {
        return parameters.containsKey(name) || multiValueParameters.containsKey(name);
    }

    public String getValue(final String name) {
        if (multiValueParameters.containsKey(name))
            throw new IllegalArgumentException(
                    "Attempted to access a multivalue parameter (" + name + ") as a single value");

        return parameters.get(name);
    }

    public List<String> getValues(final String name) {
        if (parameters.containsKey(name))
            return Collections.singletonList(parameters.get(name));

        return multiValueParameters.get(name);
    }

    public boolean hasFiles() {
        return !files.isEmpty();
    }

    public boolean hasFileItem(final String parameterName) {
        return files.containsKey(parameterName);
    }

    public FileItem getFileItem(final String parameterName) {
        return files.get(parameterName);
    }

    public String getDebugInfo() {
        final StringBuilder buf = new StringBuilder();

        if (multipartRequest)
            buf.append("MULTIPART REQUEST\n-----------------\n\n");
        else
            buf.append("STANDARD REQUEST\n----------------\n\n");

        if (!parameters.isEmpty()) {
            buf.append("SINGLE VALUE PARAMETERS:\n\n");
            for (Map.Entry<String, String> entry: parameters.entrySet())
                buf.append(entry.getKey())
                        .append(" = ")
                        .append(entry.getValue())
                        .append("\n");
            buf.append("\n");
        }

        if (!multiValueParameters.isEmpty()) {
            buf.append("MULTI VALUE PARAMETERS:\n\n");
            for (Map.Entry<String, List<String>> entry: multiValueParameters.entrySet())
                buf.append(entry.getKey())
                        .append(" = ")
                        .append(Strings.concatWithSeparator(", ", entry.getValue()))
                        .append("\n");
            buf.append("\n");
        }

        if (!files.isEmpty()) {
            buf.append("FILES:\n\n");
            for (Map.Entry<String, FileItem> entry: files.entrySet())
                buf.append(entry.getKey())
                        .append(" = ")
                        .append(entry.getValue().getName())
                        .append("\n");
            buf.append("\n");
        }

        return buf.toString();
    }

    public Pair<String, Long> getSubmittedFormAndId() {
        String form = null;
        long id = 0;

        boolean foundFormName = false;

        for (String param: parameters.keySet())
            if (param.startsWith("submitted")) {
                if (foundFormName)
                    throw new IllegalArgumentException("More than one submittedXXX parameter in request");

                form = param.substring(9, param.length());
                id = Strings.getLongVal(parameters.get(param));
                foundFormName = true;
            }

        return new Pair<String, Long>(form, id);
    }
}
