/*
public class HttpRequestCore {

    public ResponseContentSupplier performHttpRequest(Run<?,?> run, TaskListener listener)
    throws InterruptedException, IOException
    {
        final PrintStream logger = listener.getLogger();
        this.listener = listener;
        logger.println("HttpMode: " + httpMode);


        final EnvVars envVars = run.getEnvironment(listener);
        final List<NameValuePair> params = createParameters(run, logger, envVars);
        String evaluatedUrl;
        if (run instanceof AbstractBuild<?, ?>) {
            final AbstractBuild<?, ?> build = (AbstractBuild<?, ?>) run;
            evaluatedUrl = evaluate(url, build.getBuildVariableResolver(), envVars);
        } else {
            evaluatedUrl = url;
        }
        logger.println(String.format("URL: %s", evaluatedUrl));


        DefaultHttpClient httpclient = new SystemDefaultHttpClient();
        RequestAction requestAction = new RequestAction(new URL(evaluatedUrl), httpMode, params);
        HttpClientUtil clientUtil = new HttpClientUtil();
        HttpRequestBase httpRequestBase = getHttpRequestBase(logger, requestAction, clientUtil);
        HttpContext context = new BasicHttpContext();

        if (authentication != null && !authentication.isEmpty()) {
            final Authenticator auth = getDescriptor().getAuthentication(authentication);
            if (auth == null) {
                throw new IllegalStateException("Authentication '" + authentication + "' doesn't exist anymore");
            }

            logger.println("Using authentication: " + auth.getKeyName());
            auth.authenticate(httpclient, context, httpRequestBase, logger, timeout);
        }
        final HttpResponse response = clientUtil.execute(httpclient, context, httpRequestBase, logger, timeout);

        // The HttpEntity is consumed by the ResponseContentSupplier
        ResponseContentSupplier responseContentSupplier = new ResponseContentSupplier(response);
        if (consoleLogResponseBody) {
            logger.println("Response: \n" + responseContentSupplier.getContent());
        }

        responseCodeIsValid(responseContentSupplier, logger);
        contentIsValid(responseContentSupplier, logger);

        return responseContentSupplier;
    }

    private void contentIsValid(ResponseContentSupplier responseContentSupplier, PrintStream logger)
    throws AbortException
    {
        if (Strings.isNullOrEmpty(validResponseContent)) {
            return;
        }

        String response = responseContentSupplier.getContent();
        if (!response.contains(validResponseContent)) {
            throw new AbortException("Fail: Response with length " + response.length() + " doesn't contain '" + validResponseContent + "'");
        }
        return;
    }

    private void responseCodeIsValid(ResponseContentSupplier response, PrintStream logger)
    throws AbortException
    {
        List<Range<Integer>> ranges = getDescriptor().parseToRange(validResponseCodes);
        for (Range<Integer> range : ranges) {
            if (range.contains(response.getStatus())) {
                logger.println("Success code from " + range);
                return;
            }
        }
        throw new AbortException("Fail: the returned code " + response.getStatus()+" is not in the accepted range: "+ranges);
    }

    private void logResponseToFile(FilePath workspace, PrintStream logger, ResponseContentSupplier responseContentSupplier) throws IOException, InterruptedException {

        FilePath outputFilePath = getOutputFilePath(workspace, logger);

        if (outputFilePath != null) {
            if (outputFilePath != null && responseContentSupplier.getContent() != null) {
                OutputStream write = null;
                try {
                    write = outputFilePath.write();
                    write.write(responseContentSupplier.getContent().getBytes());
                } finally {
                    if (write != null) {
                        write.close();
                    }
                }
            }
        }
    }

    private HttpRequestBase getHttpRequestBase(PrintStream logger, RequestAction requestAction, HttpClientUtil clientUtil) throws IOException {
        HttpRequestBase httpRequestBase = clientUtil.createRequestBase(requestAction);

        if (contentType != MimeType.NOT_SET) {
            httpRequestBase.setHeader("Content-type", contentType.getValue());
            logger.println("Content-type: " + contentType);
        }

        if (acceptType != MimeType.NOT_SET) {
            httpRequestBase.setHeader("Accept", acceptType.getValue());
            logger.println("Accept: " + acceptType);
        }

        for (NameValuePair header : customHeaders) {
            httpRequestBase.addHeader(header.getName(), header.getValue());
        }
        return httpRequestBase;
    }

    private String evaluate(String value, VariableResolver<String> vars, Map<String, String> env) {
        return Util.replaceMacro(Util.replaceMacro(value, vars), env);
    }

}
*/
