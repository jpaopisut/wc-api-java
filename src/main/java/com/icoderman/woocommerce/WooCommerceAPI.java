package com.icoderman.woocommerce;

import com.icoderman.woocommerce.auth.AuthConfig;
import com.icoderman.woocommerce.auth.basic.BasicAuthConfig;
import com.icoderman.woocommerce.auth.oauth.OAuthSignature;
import com.icoderman.woocommerce.auth.oauth.OAuthConfig;
import java.util.HashMap;

import java.util.List;
import java.util.Map;

public class WooCommerceAPI implements WooCommerce {

    private static final String API_URL_FORMAT = "%s/wp-json/wc/%s/%s";
    private static final String API_URL_ONE_ENTITY_FORMAT = "%s/wp-json/wc/%s/%s/%d";
    private static final String URL_SECURED_FORMAT = "%s?%s";

    private final HttpClient client;
    private final AuthConfig config;
    private final String apiVersion;

    public WooCommerceAPI(AuthConfig config, ApiVersionType apiVersion) {
        this.config = config;
        this.apiVersion = apiVersion.getValue();
        if (config instanceof BasicAuthConfig) {
            this.client = new DefaultHttpClient((BasicAuthConfig) config);
        } else {
            this.client = new DefaultHttpClient();
        }
    }

    @Override
    public Map create(String endpointBase, Map<String, Object> object) {
        String url = String.format(API_URL_FORMAT, config.getUrl(), apiVersion, endpointBase);
        if (config instanceof OAuthConfig) {
            return client.post(url, OAuthSignature.getAsMap((OAuthConfig) config, url, HttpMethod.POST), object);
        } else {
            return client.post(url, new HashMap<>(), object);
        }
    }

    @Override
    public Map get(String endpointBase, int id) {
        String url = String.format(API_URL_ONE_ENTITY_FORMAT, config.getUrl(), apiVersion, endpointBase, id);
        String signature = "";
        if (config instanceof OAuthConfig) {
            signature = OAuthSignature.getAsQueryString((OAuthConfig) config, url, HttpMethod.GET);
        }
        String securedUrl = String.format(URL_SECURED_FORMAT, url, signature);
        return client.get(securedUrl);
    }

    @Override
    public List getAll(String endpointBase, Map<String, String> params) {
        String url = String.format(API_URL_FORMAT, config.getUrl(), apiVersion, endpointBase);
        String signature = "";
        if (config instanceof OAuthConfig) {
            signature = OAuthSignature.getAsQueryString((OAuthConfig) config, url, HttpMethod.GET, params);
        }
        String securedUrl = String.format(URL_SECURED_FORMAT, url, signature);
        return client.getAll(securedUrl);
    }

    @Override
    public Map update(String endpointBase, int id, Map<String, Object> object) {
        String url = String.format(API_URL_ONE_ENTITY_FORMAT, config.getUrl(), apiVersion, endpointBase, id);
        if (config instanceof OAuthConfig) {
            return client.put(url, OAuthSignature.getAsMap((OAuthConfig) config, url, HttpMethod.PUT), object);
        } else {
            return client.put(url, new HashMap<>(), object);
        }
    }

    @Override
    public Map delete(String endpointBase, int id) {
        String url = String.format(API_URL_ONE_ENTITY_FORMAT, config.getUrl(), apiVersion, endpointBase, id);
        Map<String, String> params = new HashMap<>();
        if (config instanceof OAuthConfig) {
            params = OAuthSignature.getAsMap((OAuthConfig) config, url, HttpMethod.DELETE);
        }
        return client.delete(url, params);
    }
}
