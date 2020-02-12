package org.eol.globi.service;

import org.globalbioticinteractions.dataset.Dataset;
import org.globalbioticinteractions.dataset.DatasetImpl;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class DatasetTest {

    @Test
    public void resourceURL() throws IOException {
        Dataset dataset = new DatasetImpl("some/namespace", URI.create("http://example.com"), inStream -> inStream);
        assertThat(URI.create("http://otherexample.com/bla"), is(dataset.getLocalURI(URI.create("http://otherexample.com/bla"))));
        assertThat(URI.create("http://example.com/someResource"), is(dataset.getLocalURI(URI.create("/someResource"))));
        assertThat(URI.create("http://example.com/someResource"), is(dataset.getLocalURI(URI.create("someResource"))));
    }


}