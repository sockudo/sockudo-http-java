package io.sockudo.rest.util;

import java.io.ByteArrayOutputStream;
import java.util.Map;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import com.google.gson.Gson;

public class Matchers {

    public static <T> Matcher<HttpPost> field(final String fieldName, final T expected) {
        return new TypeSafeDiagnosingMatcher<HttpPost>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("HTTP request with field [" + fieldName + "], value [" + expected + "] in JSON body");
            }

            @Override
            public boolean matchesSafely(HttpPost item, Description mismatchDescription) {
                try {
                    @SuppressWarnings("unchecked")
                    T actual = (T)new Gson().fromJson(retrieveBody(item), Map.class).get(fieldName);
                    mismatchDescription.appendText("value was [" + actual + "]");
                    return expected.equals(actual);
                }
                catch (Exception e) {
                    return false;
                }
            }
        };
    }
    public static Matcher<HttpRequestBase> path(final String expected) {
        return new TypeSafeDiagnosingMatcher<HttpRequestBase>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("HTTP request with path [" + expected + "]");
            }

            @Override
            public boolean matchesSafely(HttpRequestBase item, Description mismatchDescription) {
                try {
                    String actual = item.getURI().getPath();
                    mismatchDescription.appendText("value was [" + actual + "]");
                    return expected.equals(actual);
                }
                catch (Exception e) {
                    return false;
                }
            }
        };
    }

    public static Matcher<HttpPost> stringBody(final String expected) {
        return new TypeSafeDiagnosingMatcher<HttpPost>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("HTTP request with body [" + expected + "]");
            }

            @Override
            public boolean matchesSafely(HttpPost item, Description mismatchDescription) {
                try {
                    String actual = retrieveBody(item);
                    mismatchDescription.appendText("Expected body [" + expected + "], but received [" + actual + "]");
                    return expected.equals(actual);
                }
                catch (Exception e) {
                    mismatchDescription.appendText("Encountered exception [" + e + "] attempting match");
                    return false;
                }
            }
        };
    }


    public static Matcher<HttpPost> header(final String headerName, final String expectedValue) {
        return new TypeSafeDiagnosingMatcher<HttpPost>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("HTTP request with header [" + headerName + "], value [" + expectedValue + "]");
            }

            @Override
            public boolean matchesSafely(HttpPost item, Description mismatchDescription) {
                org.apache.http.Header h = item.getFirstHeader(headerName);
                if (h == null) {
                    mismatchDescription.appendText("header [" + headerName + "] was not present");
                    return false;
                }
                String actual = h.getValue();
                mismatchDescription.appendText("value was [" + actual + "]");
                return expectedValue.equals(actual);
            }
        };
    }

    public static Matcher<HttpPost> fieldAndHeader(final String fieldName, final String expectedFieldValue, final String headerName, final String expectedHeaderValue) {
        return new TypeSafeDiagnosingMatcher<HttpPost>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("HTTP request with field [" + fieldName + "]=[" + expectedFieldValue + "] and header [" + headerName + "]=[" + expectedHeaderValue + "]");
            }

            @Override
            public boolean matchesSafely(HttpPost item, Description mismatchDescription) {
                try {
                    @SuppressWarnings("unchecked")
                    String actualField = (String) new Gson().fromJson(retrieveBody(item), Map.class).get(fieldName);
                    if (!expectedFieldValue.equals(actualField)) {
                        mismatchDescription.appendText("field value was [" + actualField + "]");
                        return false;
                    }
                } catch (Exception e) {
                    mismatchDescription.appendText("failed to read body");
                    return false;
                }
                org.apache.http.Header h = item.getFirstHeader(headerName);
                if (h == null) {
                    mismatchDescription.appendText("header [" + headerName + "] was not present");
                    return false;
                }
                String actualHeader = h.getValue();
                if (!expectedHeaderValue.equals(actualHeader)) {
                    mismatchDescription.appendText("header value was [" + actualHeader + "]");
                    return false;
                }
                return true;
            }
        };
    }

    private static String retrieveBody(HttpPost e) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        e.getEntity().writeTo(baos);
        return new String(baos.toByteArray(), "UTF-8");
    }

}
