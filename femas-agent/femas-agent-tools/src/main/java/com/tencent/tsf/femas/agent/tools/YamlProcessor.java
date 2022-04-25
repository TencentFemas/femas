package com.tencent.tsf.femas.agent.tools;


import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.parser.ParserException;
import org.yaml.snakeyaml.reader.UnicodeReader;

import java.io.IOException;
import java.io.Reader;
import java.util.*;

/**
 * Base class for YAML factories.
 *
 * <p>Requires SnakeYAML 1.18 or higher, as of Spring Framework 5.0.6.
 *
 * @author Dave Syer
 * @author Juergen Hoeller
 * @since 4.1
 */
public abstract class YamlProcessor {


    private ResolutionMethod resolutionMethod = ResolutionMethod.OVERRIDE;

    private Resource[] resources = new Resource[0];

    private List<DocumentMatcher> documentMatchers = Collections.emptyList();

    private boolean matchDefault = true;


    /**
     * A map of document matchers allowing callers to selectively use only
     * some of the documents in a YAML resource. In YAML documents are
     * separated by <code>---<code> lines, and each document is converted
     * to properties before the match is made. E.g.
     * <pre class="code">
     * environment: dev
     * url: http://dev.bar.com
     * name: Developer Setup
     * ---
     * environment: prod
     * url:http://foo.bar.com
     * name: My Cool App
     * </pre>
     * when mapped with
     * <pre class="code">
     * setDocumentMatchers(properties ->
     *     ("prod".equals(properties.getProperty("environment")) ? MatchStatus.FOUND : MatchStatus.NOT_FOUND));
     * </pre>
     * would end up as
     * <pre class="code">
     * environment=prod
     * url=http://foo.bar.com
     * name=My Cool App
     * </pre>
     */
    public void setDocumentMatchers(DocumentMatcher... matchers) {
        this.documentMatchers = Arrays.asList(matchers);
    }

    /**
     * Flag indicating that a document for which all the
     * {@link #setDocumentMatchers(DocumentMatcher...) document matchers} abstain will
     * nevertheless match. Default is {@code true}.
     */
    public void setMatchDefault(boolean matchDefault) {
        this.matchDefault = matchDefault;
    }

    /**
     * Method to use for resolving resources. Each resource will be converted to a Map,
     * so this property is used to decide which map entries to keep in the final output
     * from this factory. Default is {@link ResolutionMethod#OVERRIDE}.
     */
    public void setResolutionMethod(ResolutionMethod resolutionMethod) {
        this.resolutionMethod = resolutionMethod;
    }

    /**
     * Set locations of YAML {@link Resource resources} to be loaded.
     *
     * @see ResolutionMethod
     */
    public void setResources(Resource... resources) {
        this.resources = resources;
    }


    /**
     * Provide an opportunity for subclasses to process the Yaml parsed from the supplied
     * resources. Each resource is parsed in turn and the documents inside checked against
     * the {@link #setDocumentMatchers(DocumentMatcher...) matchers}. If a document
     * matches it is passed into the callback, along with its representation as Properties.
     * Depending on the {@link #setResolutionMethod(ResolutionMethod)} not all of the
     * documents will be parsed.
     *
     * @param callback a callback to delegate to once matching documents are found
     * @see #createYaml()
     */
    protected void process(MatchCallback callback) {
        Yaml yaml = createYaml();
        for (Resource resource : this.resources) {
            boolean found = process(callback, yaml, resource);
            if (this.resolutionMethod == ResolutionMethod.FIRST_FOUND && found) {
                return;
            }
        }
    }

    /**
     * Create the {@link Yaml} instance to use.
     * <p>The default implementation sets the "allowDuplicateKeys" flag to {@code false},
     * enabling built-in duplicate key handling in SnakeYAML 1.18+.
     *
     * @see LoaderOptions#setAllowDuplicateKeys(boolean)
     */
    protected Yaml createYaml() {
        LoaderOptions options = new LoaderOptions();
        options.setAllowDuplicateKeys(false);
        return new Yaml(options);
    }

    private boolean process(MatchCallback callback, Yaml yaml, Resource resource) {
        int count = 0;
        try {

            Reader reader = new UnicodeReader(resource.getInputStream());
            try {
                for (Object object : yaml.loadAll(reader)) {
                    if (object != null && process(asMap(object), callback)) {
                        count++;
                        if (this.resolutionMethod == ResolutionMethod.FIRST_FOUND) {
                            break;
                        }
                    }
                }

            } finally {
                if (reader != null) {
                    reader.close();
                }
            }
        } catch (IOException ex) {
            handleProcessError(resource, ex);
        }
        return (count > 0);
    }

    private void handleProcessError(Resource resource, IOException ex) {
        if (this.resolutionMethod != ResolutionMethod.FIRST_FOUND &&
                this.resolutionMethod != ResolutionMethod.OVERRIDE_AND_IGNORE) {
            throw new IllegalStateException(ex);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object object) {
        // YAML can have numbers as keys
        Map<String, Object> result = new LinkedHashMap<>();
        if (!(object instanceof Map)) {
            // A document can be a text literal
            result.put("document", object);
            return result;
        }

        Map<Object, Object> map = (Map<Object, Object>) object;
        map.forEach((key, value) -> {
            if (value instanceof Map) {
                value = asMap(value);
            }
            if (key instanceof CharSequence) {
                result.put(key.toString(), value);
            } else {
                // It has to be a map key in this case
                result.put("[" + key.toString() + "]", value);
            }
        });
        return result;
    }

    private Properties createStringAdaptingProperties() {
        return new Properties() {
            @Override
            public String getProperty(String key) {
                Object value = get(key);
                return (value != null ? value.toString() : null);
            }
        };
    }

    private boolean process(Map<String, Object> map, MatchCallback callback) {
        Properties properties = createStringAdaptingProperties();
        properties.putAll(getFlattenedMap(map));

        if (this.documentMatchers.isEmpty()) {
            callback.process(properties, map);
            return true;
        }

        MatchStatus result = MatchStatus.ABSTAIN;
        for (DocumentMatcher matcher : this.documentMatchers) {
            MatchStatus match = matcher.matches(properties);
            result = MatchStatus.getMostSpecific(match, result);
            if (match == MatchStatus.FOUND) {

                callback.process(properties, map);
                return true;
            }
        }
        if (result == MatchStatus.ABSTAIN && this.matchDefault) {

            callback.process(properties, map);
            return true;
        }
        return false;
    }

    /**
     * Return a flattened version of the given map, recursively following any nested Map
     * or Collection values. Entries from the resulting map retain the same order as the
     * source. When called with the Map from a {@link MatchCallback} the result will
     * contain the same values as the {@link MatchCallback} Properties.
     *
     * @param source the source map
     * @return a flattened map
     * @since 4.1.3
     */
    protected final Map<String, Object> getFlattenedMap(Map<String, Object> source) {
        Map<String, Object> result = new LinkedHashMap<>();
        buildFlattenedMap(result, source, null);
        return result;
    }

    private void buildFlattenedMap(Map<String, Object> result, Map<String, Object> source, String path) {
        source.forEach((key, value) -> {
            if (StringUtils.hasText(path)) {
                if (key.startsWith("[")) {
                    key = path + key;
                } else {
                    key = path + '.' + key;
                }
            }
            if (value instanceof String) {
                result.put(key, value);
            } else if (value instanceof Map) {
                // Need a compound key
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) value;
                buildFlattenedMap(result, map, key);
            } else if (value instanceof Collection) {
                // Need a compound key
                @SuppressWarnings("unchecked")
                Collection<Object> collection = (Collection<Object>) value;
                if (collection.isEmpty()) {
                    result.put(key, "");
                } else {
                    int count = 0;
                    for (Object object : collection) {
                        buildFlattenedMap(result, Collections.singletonMap(
                                "[" + (count++) + "]", object), key);
                    }
                }
            } else {
                result.put(key, (value != null ? value : ""));
            }
        });
    }


    /**
     * Status returned from {@link DocumentMatcher#matches(Properties)}
     */
    public enum MatchStatus {

        /**
         * A match was found.
         */
        FOUND,

        /**
         * No match was found.
         */
        NOT_FOUND,

        /**
         * The matcher should not be considered.
         */
        ABSTAIN;

        /**
         * Compare two {@link MatchStatus} items, returning the most specific status.
         */
        public static MatchStatus getMostSpecific(MatchStatus a, MatchStatus b) {
            return (a.ordinal() < b.ordinal() ? a : b);
        }
    }


    /**
     * Method to use for resolving resources.
     */
    public enum ResolutionMethod {

        /**
         * Replace values from earlier in the list.
         */
        OVERRIDE,

        /**
         * Replace values from earlier in the list, ignoring any failures.
         */
        OVERRIDE_AND_IGNORE,

        /**
         * Take the first resource in the list that exists and use just that.
         */
        FIRST_FOUND
    }


    /**
     * Callback interface used to process the YAML parsing results.
     */
    public interface MatchCallback {

        /**
         * Process the given representation of the parsing results.
         *
         * @param properties the properties to process (as a flattened
         *                   representation with indexed keys in case of a collection or map)
         * @param map        the result map (preserving the original value structure
         *                   in the YAML document)
         */
        void process(Properties properties, Map<String, Object> map);
    }


    /**
     * Strategy interface used to test if properties match.
     */
    public interface DocumentMatcher {

        /**
         * Test if the given properties match.
         *
         * @param properties the properties to test
         * @return the status of the match
         */
        MatchStatus matches(Properties properties);
    }

    /**
     * A specialized {@link Constructor} that checks for duplicate keys.
     *
     * @deprecated as of Spring Framework 5.0.6 (not used anymore here),
     * superseded by SnakeYAML's own duplicate key handling
     */
    @Deprecated
    protected static class StrictMapAppenderConstructor extends Constructor {

        // Declared as public for use in subclasses
        public StrictMapAppenderConstructor() {
            super();
        }

        @Override
        protected Map<Object, Object> constructMapping(MappingNode node) {
            try {
                return super.constructMapping(node);
            } catch (IllegalStateException ex) {
                throw new ParserException("while parsing MappingNode",
                        node.getStartMark(), ex.getMessage(), node.getEndMark());
            }
        }

//        @Override
//        protected Map<Object, Object> createDefaultMap() {
//            final Map<Object, Object> delegate = super.createDefaultMap();
//            return new AbstractMap<Object, Object>() {
//                @Override
//                public Object put(Object key, Object value) {
//                    if (delegate.containsKey(key)) {
//                        throw new IllegalStateException("Duplicate key: " + key);
//                    }
//                    return delegate.put(key, value);
//                }
//                @Override
//                public Set<Entry<Object, Object>> entrySet() {
//                    return delegate.entrySet();
//                }
//            };
//        }
    }

}
