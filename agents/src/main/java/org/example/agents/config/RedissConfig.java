//package org.example.agents.config;
//
//import org.redisson.Redisson;
//import org.redisson.api.RedissonClient;
//import org.redisson.codec.JsonJacksonCodec;
//import org.redisson.config.ClusterServersConfig;
//import org.redisson.config.Config;
//import org.redisson.config.SentinelServersConfig;
//import org.redisson.config.SingleServerConfig;
//import org.redisson.spring.starter.RedissonAutoConfigurationCustomizer;
//import org.redisson.spring.starter.RedissonProperties;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.util.ReflectionUtils;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.lang.reflect.Method;
//import java.time.Duration;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//
//@Configuration
//public class RedissConfig {
//    @Autowired
//    private RedisProperties redisProperties;
//    @Autowired
//    private RedissonProperties redissonProperties;
//
//    @Autowired(required = false)
//    private List<RedissonAutoConfigurationCustomizer> redissonAutoConfigurationCustomizers;
//
//    private static final String REDIS_PROTOCOL_PREFIX = "redis://";
//    private static final String REDISS_PROTOCOL_PREFIX = "rediss://";
//
//    @Bean
//    public RedissonClient redisson() throws IOException {
//        Config config;
//        Method clusterMethod = ReflectionUtils.findMethod(RedisProperties.class, "getCluster");
//        Method usernameMethod = ReflectionUtils.findMethod(RedisProperties.class, "getUsername");
//        Method timeoutMethod = ReflectionUtils.findMethod(RedisProperties.class, "getTimeout");
//        Method connectTimeoutMethod = ReflectionUtils.findMethod(RedisProperties.class, "getConnectTimeout");
//        Method clientNameMethod = ReflectionUtils.findMethod(RedisProperties.class, "getClientName");
//        Object timeoutValue = ReflectionUtils.invokeMethod(timeoutMethod, redisProperties);
//
//        Integer timeout = null;
//        if (timeoutValue instanceof Duration) {
//            timeout = (int) ((Duration) timeoutValue).toMillis();
//        } else if (timeoutValue != null){
//            timeout = (Integer)timeoutValue;
//        }
//
//        Integer connectTimeout = null;
//        if (connectTimeoutMethod != null) {
//            Object connectTimeoutValue = ReflectionUtils.invokeMethod(connectTimeoutMethod, redisProperties);
//            if (connectTimeoutValue != null) {
//                connectTimeout = (int) ((Duration) connectTimeoutValue).toMillis();
//            }
//        } else {
//            connectTimeout = timeout;
//        }
//
//        String clientName = null;
//        if (clientNameMethod != null) {
//            clientName = (String) ReflectionUtils.invokeMethod(clientNameMethod, redisProperties);
//        }
//
//        String username = null;
//        if (usernameMethod != null) {
//            username = (String) ReflectionUtils.invokeMethod(usernameMethod, redisProperties);
//        }
//
//        if (redissonProperties.getConfig() != null) {
//            try {
//                config = Config.fromYAML(redissonProperties.getConfig());
//            } catch (IOException e) {
//                try {
//                    config = Config.fromJSON(redissonProperties.getConfig());
//                } catch (IOException e1) {
//                    e1.addSuppressed(e);
//                    throw new IllegalArgumentException("Can't parse config", e1);
//                }
//            }
//        } else if (redisProperties.getSentinel() != null) {
//            Method nodesMethod = ReflectionUtils.findMethod(RedisProperties.Sentinel.class, "getNodes");
//            Object nodesValue = ReflectionUtils.invokeMethod(nodesMethod, redisProperties.getSentinel());
//
//            String[] nodes;
//            if (nodesValue instanceof String) {
//                nodes = convert(Arrays.asList(((String)nodesValue).split(",")));
//            } else {
//                nodes = convert((List<String>)nodesValue);
//            }
//
//            config = new Config();
//            SentinelServersConfig c = config.useSentinelServers()
//                    .setMasterName(redisProperties.getSentinel().getMaster())
//                    .addSentinelAddress(nodes)
//                    .setDatabase(redisProperties.getDatabase())
//                    .setUsername(username)
//                    .setPassword(redisProperties.getPassword())
//                    .setClientName(clientName);
//            if (connectTimeout != null) {
//                c.setConnectTimeout(connectTimeout);
//            }
//            if (connectTimeoutMethod != null && timeout != null) {
//                c.setTimeout(timeout);
//            }
//        } else if (clusterMethod != null && ReflectionUtils.invokeMethod(clusterMethod, redisProperties) != null) {
//            Object clusterObject = ReflectionUtils.invokeMethod(clusterMethod, redisProperties);
//            Method nodesMethod = ReflectionUtils.findMethod(clusterObject.getClass(), "getNodes");
//            List<String> nodesObject = (List) ReflectionUtils.invokeMethod(nodesMethod, clusterObject);
//
//            String[] nodes = convert(nodesObject);
//
//            config = new Config();
//            ClusterServersConfig c = config.useClusterServers()
//                    .addNodeAddress(nodes)
//                    .setUsername(username)
//                    .setPassword(redisProperties.getPassword())
//                    .setClientName(clientName);
//            if (connectTimeout != null) {
//                c.setConnectTimeout(connectTimeout);
//            }
//            if (connectTimeoutMethod != null && timeout != null) {
//                c.setTimeout(timeout);
//            }
//        } else {
//            config = new Config();
//            config.setCodec(new JsonJacksonCodec());
//            String prefix = REDIS_PROTOCOL_PREFIX;
//            Method method = ReflectionUtils.findMethod(RedisProperties.class, "isSsl");
//            if (method != null && (Boolean)ReflectionUtils.invokeMethod(method, redisProperties)) {
//                prefix = REDISS_PROTOCOL_PREFIX;
//            }
//
//            SingleServerConfig c = config.useSingleServer()
//                    .setAddress(prefix + redisProperties.getHost() + ":" + redisProperties.getPort())
//                    .setDatabase(redisProperties.getDatabase())
//                    .setUsername(username)
//                    .setPassword(redisProperties.getPassword())
//                    .setClientName(clientName);
//            if (connectTimeout != null) {
//                c.setConnectTimeout(connectTimeout);
//            }
//            if (connectTimeoutMethod != null && timeout != null) {
//                c.setTimeout(timeout);
//            }
//        }
//        if (redissonAutoConfigurationCustomizers != null) {
//            for (RedissonAutoConfigurationCustomizer customizer : redissonAutoConfigurationCustomizers) {
//                customizer.customize(config);
//            }
//        }
//        return Redisson.create(config);
//    }
//
//    private String[] convert(List<String> nodesObject) {
//        List<String> nodes = new ArrayList<String>(nodesObject.size());
//        for (String node : nodesObject) {
//            if (!node.startsWith(REDIS_PROTOCOL_PREFIX) && !node.startsWith(REDISS_PROTOCOL_PREFIX)) {
//                nodes.add(REDIS_PROTOCOL_PREFIX + node);
//            } else {
//                nodes.add(node);
//            }
//        }
//        return nodes.toArray(new String[0]);
//    }
//}
