package io.dchq.sdk.core;

import com.dchq.schema.beans.base.ResponseEntity;
import com.dchq.schema.beans.one.plugin.Plugin;
import org.springframework.core.ParameterizedTypeReference;

import java.util.List;

/**
 * Encapsulates DCHQ Plugin endpoint calls.
 *
 * @author Atef Ahmed
 * @see <a href="https://dchq.readme.io/docs/plugins-3">Plugin endpoint</a>
 * @since 1.0
 */
public class PluginServiceImpl extends GenericServiceImpl<Plugin, ResponseEntity<List<Plugin>>, ResponseEntity<Plugin>>
        implements PluginService {

    public static final ParameterizedTypeReference<ResponseEntity<List<Plugin>>> listTypeReference = new ParameterizedTypeReference<ResponseEntity<List<Plugin>>>() {
    };
    public static final ParameterizedTypeReference<ResponseEntity<Plugin>> singleTypeReference = new ParameterizedTypeReference<ResponseEntity<Plugin>>() {
    };

    public static final String ENDPOINT = "plugins/";

    /**
     * @param baseURI  - e.g. https://dchq.io/api/1.0/
     * @param username - registered username with DCHQ.io
     * @param password - password used with the username
     */
    public PluginServiceImpl(String baseURI, String username, String password) {
        super(baseURI, ENDPOINT, username, password,
                new ParameterizedTypeReference<ResponseEntity<List<Plugin>>>() {
                },
                new ParameterizedTypeReference<ResponseEntity<Plugin>>() {
                }
        );
    }

    @Override
    public ResponseEntity<List<Plugin>> findByStarred() {
        return findAll();
    }
}
