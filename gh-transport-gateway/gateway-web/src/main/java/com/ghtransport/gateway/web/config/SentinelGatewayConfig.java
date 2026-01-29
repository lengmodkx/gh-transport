package com.ghtransport.gateway.web.config;

import com.alibaba.csp.sentinel.adapter.gateway.common.SentinelGatewayConstants;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiDefinition;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPathPredicateItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPredicateItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.adapter.gateway.sc.SentinelGatewayFilter;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.BlockRequestHandler;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import com.alibaba.csp.sentinel.adapter.gateway.sc.exception.SentinelGatewayBlockExceptionHandler;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.view.ViewResolver;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.*;

/**
 * Sentinel网关限流配置
 */
@Slf4j
@Configuration
public class SentinelGatewayConfig {

    private final List<ViewResolver> viewResolvers;
    private final ServerCodecConfigurer serverCodecConfigurer;

    public SentinelGatewayConfig(ObjectProvider<List<ViewResolver>> viewResolversProvider,
                                  ServerCodecConfigurer serverCodecConfigurer) {
        this.viewResolvers = viewResolversProvider.getIfAvailable(Collections::emptyList);
        this.serverCodecConfigurer = serverCodecConfigurer;
    }

    /**
     * 配置Sentinel网关过滤器
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public GlobalFilter sentinelGatewayFilter() {
        return new SentinelGatewayFilter();
    }

    /**
     * 配置Sentinel限流异常处理器
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SentinelGatewayBlockExceptionHandler sentinelGatewayBlockExceptionHandler() {
        return new SentinelGatewayBlockExceptionHandler(viewResolvers, serverCodecConfigurer) {
            @Override
            public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
                if (ex instanceof FlowException) {
                    log.warn("请求被限流: {} - {}", exchange.getRequest().getPath(), ex.getMessage());
                    return buildResponse(exchange, "RATE_LIMITED", "请求过于频繁，请稍后重试");
                } else if (ex instanceof ParamFlowException) {
                    log.warn("参数限流: {} - {}", exchange.getRequest().getPath(), ex.getMessage());
                    return buildResponse(exchange, "PARAM_RATE_LIMITED", "访问频率受限");
                }
                return Mono.error(ex);
            }
        };
    }

    /**
     * 配置限流回调处理器
     */
    @Bean
    public BlockRequestHandler blockRequestHandler() {
        return (serverWebExchange, throwable) -> {
            Map<String, Object> result = new HashMap<>();
            result.put("code", "429");
            result.put("message", "请求过于频繁，请稍后重试");
            result.put("timestamp", System.currentTimeMillis());
            return ServerResponse.status(HttpStatus.TOO_MANY_REQUESTS)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(result));
        };
    }

    /**
     * 构建限流响应
     */
    private Mono<Void> buildResponse(ServerWebExchange exchange, String code, String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", code);
        result.put("message", message);
        result.put("path", exchange.getRequest().getPath().value());
        result.put("timestamp", System.currentTimeMillis());
        return ServerResponse.status(HttpStatus.TOO_MANY_REQUESTS)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(result));
    }

    /**
     * 配置API定义
     */
    private Set<ApiDefinition> apiDefinitions() {
        Set<ApiDefinition> definitions = new HashSet<>();

        // 认证API - 允许较高频率
        ApiDefinition authApi = new ApiDefinition("auth-api")
                .setPredicateItems(Set.of(
                        new ApiPathPredicateItem().setPattern("/api/v1/auth/**")
                                .setMatchStrategy(SentinelGatewayConstants.URL_MATCH_STRATEGY_PREFIX)
                ));
        definitions.add(authApi);

        // 订单API
        ApiDefinition orderApi = new ApiDefinition("order-api")
                .setPredicateItems(Set.of(
                        new ApiPathPredicateItem().setPattern("/api/v1/orders/**")
                                .setMatchStrategy(SentinelGatewayConstants.URL_MATCH_STRATEGY_PREFIX)
                ));
        definitions.add(orderApi);

        // 客户API
        ApiDefinition customerApi = new ApiDefinition("customer-api")
                .setPredicateItems(Set.of(
                        new ApiPathPredicateItem().setPattern("/api/v1/customers/**")
                                .setMatchStrategy(SentinelGatewayConstants.URL_MATCH_STRATEGY_PREFIX)
                ));
        definitions.add(customerApi);

        // 调度API
        ApiDefinition dispatchApi = new ApiDefinition("dispatch-api")
                .setPredicateItems(Set.of(
                        new ApiPathPredicateItem().setPattern("/api/v1/dispatches/**")
                                .setMatchStrategy(SentinelGatewayConstants.URL_MATCH_STRATEGY_PREFIX)
                ));
        definitions.add(dispatchApi);

        // 运输API
        ApiDefinition transportApi = new ApiDefinition("transport-api")
                .setPredicateItems(Set.of(
                        new ApiPathPredicateItem().setPattern("/api/v1/transports/**")
                                .setMatchStrategy(SentinelGatewayConstants.URL_MATCH_STRATEGY_PREFIX)
                ));
        definitions.add(transportApi);

        // 库存API
        ApiDefinition inventoryApi = new ApiDefinition("inventory-api")
                .setPredicateItems(Set.of(
                        new ApiPathPredicateItem().setPattern("/api/v1/inventory/**")
                                .setMatchStrategy(SentinelGatewayConstants.URL_MATCH_STRATEGY_PREFIX)
                ));
        definitions.add(inventoryApi);

        return definitions;
    }

    /**
     * 配置限流规则
     */
    private List<GatewayFlowRule> gatewayFlowRules() {
        List<GatewayFlowRule> rules = new ArrayList<>();

        // 全局限流规则
        rules.add(new GatewayFlowRule("auth-api")
                .setResourceMode(SentinelGatewayConstants.RESOURCE_MODE_CUSTOM_API_NAME)
                .setCount(100)  // 每秒最多100个请求
                .setIntervalSec(1)  // 统计间隔1秒
                .setGrade(RuleConstant.FLOW_GRADE_QPS)
        );

        rules.add(new GatewayFlowRule("order-api")
                .setResourceMode(SentinelGatewayConstants.RESOURCE_MODE_CUSTOM_API_NAME)
                .setCount(50)
                .setIntervalSec(1)
                .setGrade(RuleConstant.FLOW_GRADE_QPS)
        );

        rules.add(new GatewayFlowRule("customer-api")
                .setResourceMode(SentinelGatewayConstants.RESOURCE_MODE_CUSTOM_API_NAME)
                .setCount(50)
                .setIntervalSec(1)
                .setGrade(RuleConstant.FLOW_GRADE_QPS)
        );

        rules.add(new GatewayFlowRule("dispatch-api")
                .setResourceMode(SentinelGatewayConstants.RESOURCE_MODE_CUSTOM_API_NAME)
                .setCount(50)
                .setIntervalSec(1)
                .setGrade(RuleConstant.FLOW_GRADE_QPS)
        );

        rules.add(new GatewayFlowRule("transport-api")
                .setResourceMode(SentinelGatewayConstants.RESOURCE_MODE_CUSTOM_API_NAME)
                .setCount(50)
                .setIntervalSec(1)
                .setGrade(RuleConstant.FLOW_GRADE_QPS)
        );

        rules.add(new GatewayFlowRule("inventory-api")
                .setResourceMode(SentinelGatewayConstants.RESOURCE_MODE_CUSTOM_API_NAME)
                .setCount(50)
                .setIntervalSec(1)
                .setGrade(RuleConstant.FLOW_GRADE_QPS)
        );

        // 基于IP的限流
        rules.add(new GatewayFlowRule("order-api")
                .setResourceMode(SentinelGatewayConstants.RESOURCE_MODE_CUSTOM_API_NAME)
                .setCount(10)
                .setIntervalSec(1)
                .setGrade(RuleConstant.FLOW_GRADE_QPS)
                .setParamFlowItemList(Collections.singletonList(
                        new com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowItem()
                                .setObject("remoteAddr")
                                .setClassType(String.class.getName())
                                .setCount(10)
                                .setDurationInSec(1)
                ))
        );

        return rules;
    }
}
