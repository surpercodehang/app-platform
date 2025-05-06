/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.jade.aipp.model;

import modelengine.fit.http.client.HttpClassicClient;
import modelengine.fit.http.client.HttpClassicClientFactory;
import modelengine.fit.http.client.HttpClassicClientRequest;
import modelengine.fit.http.client.HttpClassicClientResponse;
import modelengine.fit.http.protocol.HttpRequestMethod;
import modelengine.fit.http.protocol.HttpResponseStatus;
import modelengine.fit.jade.aipp.model.dto.ModelAccessInfo;
import modelengine.fit.jade.aipp.model.dto.ModelListDto;
import modelengine.fit.jade.aipp.model.service.AippModelCenterExtension;
import modelengine.fit.jane.common.entity.OperationContext;
import modelengine.fit.jober.aipp.common.exception.AippErrCode;
import modelengine.fit.jober.aipp.common.exception.AippNotFoundException;
import modelengine.fit.security.Decryptor;
import modelengine.fitframework.annotation.Component;
import modelengine.fitframework.annotation.Value;
import modelengine.fitframework.conf.Config;
import modelengine.fitframework.ioc.BeanContainer;
import modelengine.fitframework.ioc.BeanFactory;
import modelengine.fitframework.log.Logger;
import modelengine.fitframework.util.LazyLoader;
import modelengine.fitframework.util.MapBuilder;
import modelengine.fitframework.util.ObjectUtils;
import modelengine.fitframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 默认的模型服务中心实现。
 *
 * @author 方誉州
 * @since 2024-10-21
 */
@Component("defaultAippModelCenter")
public class DefaultAippModelCenter implements AippModelCenterExtension {
    private static final Logger log = Logger.get(DefaultAippModelCenter.class);
    private static final int HTTP_CLIENT_TIMEOUT = 60 * 1000;
    private static final Map<String, Boolean> HTTPS_CONFIG_KEY_MAPS = MapBuilder.<String, Boolean>get()
            .put("client.http.secure.ignore-trust", Boolean.FALSE)
            .put("client.http.secure.ignore-hostname", Boolean.FALSE)
            .put("client.http.secure.trust-store-file", Boolean.FALSE)
            .put("client.http.secure.trust-store-password", Boolean.TRUE)
            .put("client.http.secure.key-store-file", Boolean.FALSE)
            .put("client.http.secure.key-store-password", Boolean.TRUE)
            .build();
    private static final List<String> excludeModels = new ArrayList<>(Arrays.asList("DeepSeek-R1",
            "bge-large-zh",
            "DeepSeek-R1-Distill-Qwen-32B",
            "openai/whisper-large-v3",
            "Qwen-QwQ-32B"));

    private final HttpClassicClientFactory httpClientFactory;
    private final Config config;
    private final Decryptor decryptor;
    private final BeanContainer container;
    private final String url;
    private final Map<String, String> modelBaseUrls;
    private final String defaultModel;
    private LazyLoader<HttpClassicClient> httpClient;

    public DefaultAippModelCenter(HttpClassicClientFactory httpClientFactory, Config config, BeanContainer container,
            @Value("${model-fetch-url}") String url, @Value("${openai-urls}") Map<String, String> modelBaseUrls,
            @Value("${defaultModel.chatModel}") String defaultModel) {
        this.httpClientFactory = httpClientFactory;
        this.config = config;
        this.container = container;
        this.defaultModel = defaultModel;
        this.decryptor = this.container.lookup(Decryptor.class)
                .map(BeanFactory::<Decryptor>get)
                .orElseGet(() -> encrypted -> encrypted);
        this.url = url;
        this.modelBaseUrls = modelBaseUrls;
        this.httpClient = new LazyLoader<>(this::getHttpClient);
    }

    @Override
    public ModelListDto fetchModelList(String type, String scene, OperationContext context) {
        if (this.url == null) {
            log.error("Failed to fetch model list. Model fetch url is null");
            return ModelListDto.builder().models(new ArrayList<>()).total(0).build();
        }
        String url = type == null ? this.url : this.url + "?type=" + type;
        HttpClassicClientRequest request = this.httpClient.get().createRequest(HttpRequestMethod.GET, url);
        try (HttpClassicClientResponse<?> response = request.exchange()) {
            if (response.statusCode() != HttpResponseStatus.OK.statusCode()) {
                throw new IOException(StringUtils.format("codeStatus: {0}, msg: {1}",
                        response.statusCode(),
                        response.reasonPhrase()));
            }
            Map<String, Object> modelListRsp = ObjectUtils.cast(response.objectEntity().get().object());
            if (modelListRsp.containsKey("code") && ObjectUtils.<Integer>cast(modelListRsp.get("code")) != 0) {
                throw new IOException(StringUtils.format("codeStatus: {0}, msg: {1}",
                        ObjectUtils.<Integer>cast(modelListRsp.get("code")),
                        ObjectUtils.<String>cast(modelListRsp.get("msg"))));
            }
            List<ModelAccessInfo> modelList =
                    ObjectUtils.<List<Map<String, Object>>>cast(modelListRsp.get("data")).stream().map(info -> {
                        // 兼容本地调试
                        String serviceName = ObjectUtils.cast(info.get("serviceName"));
                        String tag = ObjectUtils.cast(info.get("modelType"));
                        if (StringUtils.isBlank(serviceName)) {
                            serviceName = ObjectUtils.cast(info.get("id"));
                            tag = "INTERNAL";
                        }
                        return ModelAccessInfo.builder().serviceName(serviceName).tag(tag).build();
                    }).filter(info -> {
                        if (Objects.equals(scene, "fastTextProcess")) {
                            return !excludeModels.contains(info.getServiceName());
                        }
                        return true;
                    }).collect(Collectors.toList());
            return ModelListDto.builder().models(modelList).total(modelList.size()).build();
        } catch (IOException e) {
            log.error("Failed to fetch model list. cause: {}", e.getMessage());
            return ModelListDto.builder().models(new ArrayList<>()).total(0).build();
        }
    }

    @Override
    public ModelAccessInfo getModelAccessInfo(String tag, String modelName, OperationContext context) {
        String baseUrl = this.modelBaseUrls.get(tag.toLowerCase(Locale.ROOT));
        if (baseUrl == null) {
            log.warn("Unknown model tag: {}", tag);
            throw new AippNotFoundException(AippErrCode.NOT_FOUND, tag);
        }
        return ModelAccessInfo.builder().serviceName(modelName).tag(tag).baseUrl(baseUrl).build();
    }

    @Override
    public ModelAccessInfo getDefaultModel(String type, OperationContext context) {
        ModelAccessInfo firstModel = new ModelAccessInfo("", "", null, null);
        ModelListDto modelList = fetchModelList(type, "fastTextProcess", null);
        if (modelList != null && modelList.getModels() != null && !modelList.getModels().isEmpty()) {
            List<ModelAccessInfo> modelInfoList = modelList.getModels();
            for (ModelAccessInfo info : modelInfoList) {
                if (StringUtils.equals(info.getServiceName(), this.defaultModel)) {
                    return info;
                }
            }
            firstModel = modelList.getModels().get(0);
        }
        return firstModel;
    }

    private HttpClassicClient getHttpClient() {
        Map<String, Object> custom = HTTPS_CONFIG_KEY_MAPS.keySet()
                .stream()
                .filter(key -> config.keys().contains(Config.canonicalizeKey(key)))
                .collect(Collectors.toMap(key -> key, key -> {
                    Object value = this.config.get(key, Object.class);
                    if (HTTPS_CONFIG_KEY_MAPS.get(key).booleanValue()) {
                        value = this.decryptor.decrypt(ObjectUtils.cast(value));
                    }
                    return value;
                }));

        return this.httpClientFactory.create(HttpClassicClientFactory.Config.builder()
                .custom(custom)
                .connectTimeout(HTTP_CLIENT_TIMEOUT)
                .socketTimeout(HTTP_CLIENT_TIMEOUT)
                .build());
    }
}
