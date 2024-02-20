# Camel 4.1.0 Invalid Content-Encoding Reproducer

This project was initialized from the camel Archetype as follow:

    $ mvn archetype:generate -B \
        -DarchetypeGroupId=org.apache.camel.archetypes \
        -DarchetypeArtifactId=camel-archetype-java \
        -DarchetypeVersion=4.1.0 \
        -Dpackage=org.apache.camel \
        -DgroupId=org.apache.camel.reproducer \
        -DartifactId=reproducer-for-my-issue \
        -Dversion=1.0.0-SNAPSHOT


# Issue Description

When a character set (aka charset) is defined on a call to an HTTP endpoint,
that endpoint can incorrectly set the `Content-Encoding` header to that charset.

Here are the key conditions under which this happens:

* The body of the message processed by the HTTP component is a String type
* The following Java expression, evaludated by HttpProducer, is false: `GZIPHelper.isGzip(exchange.getIn())`


# Building and Running the example

**NOTE** that port 9000 is used by this test, and must be unused on the system.

    $ mvn clean install
    $ mvn camel:run


# Reading the results

The following log line is written when the invalid Content-Encoding header is detected.

    INFO  *** INVALID CONTENT ENCODING DETECTED ***


# Additional Tests

Here is a 1-liner that will run an additional test while the application is running:

    curl --data-ascii 'hello' 'Content-Type: text/plain;charset=us-ascii' http://localhost:9000


# Root Cause

The root cause of this bug is the following call made by `HttpProducer`:

    answer = new StringEntity(content, contentType, charset, false);

Note the 3rd argument passed in, charset, is the character set (e.g. "UTF-8").
Here is the signature of that constructor; note that the 3rd argument is `contentEncoding`:

    public StringEntity(
            final String string, final ContentType contentType, final String contentEncoding, final boolean chunked) {

